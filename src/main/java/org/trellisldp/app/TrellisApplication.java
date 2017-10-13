/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trellisldp.app;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.joining;
import static org.apache.curator.framework.CuratorFrameworkFactory.newClient;
import static org.trellisldp.rosid.common.RosidConstants.TOPIC_EVENT;
import static org.trellisldp.rosid.common.RosidConstants.ZNODE_NAMESPACES;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import org.trellisldp.agent.SimpleAgent;
import org.trellisldp.api.AccessControlService;
import org.trellisldp.api.AgentService;
import org.trellisldp.api.BinaryService;
import org.trellisldp.api.EventService;
import org.trellisldp.api.IOService;
import org.trellisldp.api.IdentifierService;
import org.trellisldp.api.NamespaceService;
import org.trellisldp.api.ResourceService;
import org.trellisldp.app.config.AuthConfiguration;
import org.trellisldp.app.config.PartitionConfiguration;
import org.trellisldp.app.health.KafkaHealthCheck;
import org.trellisldp.app.health.ZookeeperHealthCheck;
import org.trellisldp.binary.DefaultBinaryService;
import org.trellisldp.binary.FileResolver;
import org.trellisldp.http.AgentAuthorizationFilter;
import org.trellisldp.http.CacheControlFilter;
import org.trellisldp.http.CrossOriginResourceSharingFilter;
import org.trellisldp.http.LdpResource;
import org.trellisldp.http.MultipartUploader;
import org.trellisldp.http.RootResource;
import org.trellisldp.http.WebAcFilter;
import org.trellisldp.id.UUIDGenerator;
import org.trellisldp.io.JenaIOService;
import org.trellisldp.kafka.KafkaPublisher;
import org.trellisldp.rosid.common.Namespaces;
import org.trellisldp.rosid.file.FileResourceService;
import org.trellisldp.webac.WebACService;


/**
 * @author acoburn
 */
public class TrellisApplication extends Application<TrellisConfiguration> {

    private static final String RESOURCE_PATH = "resourcePath";
    private static final String BASE_URL = "baseUrl";
    private static final String BINARY_PATH = "path";
    private static final String FILE_PREFIX = "file:";
    private static final String PREFIX = "prefix";
    private static final String BINARY_LEVELS = "levels";
    private static final String BINARY_LENGTH = "length";

    /**
     * The main entry point
     * @param args the argument list
     * @throws Exception if something goes horribly awry
     */
    public static void main(final String[] args) throws Exception {
        new TrellisApplication().run(args);
    }

    @Override
    public String getName() {
        return "Trellis";
    }

    @Override
    public void initialize(final Bootstrap<TrellisConfiguration> bootstrap) {
        // Not currently used
    }

    @Override
    public void run(final TrellisConfiguration config,
                    final Environment environment) throws IOException {

        // Static configurations
        final Properties serverProperties = new Properties();
        serverProperties.setProperty("title", "Trellis LDP");

        // Kafka producer configuration
        final Properties producerProps = config.getKafka().asProperties();
        producerProps.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // Other configurations
        final Map<String, Properties> partitions = config.getPartitions().stream()
            .collect(toMap(PartitionConfiguration::getId, p -> {
                final Properties props = new Properties();
                props.setProperty(PREFIX, "file:" + p.getId());
                props.setProperty(BASE_URL, p.getBaseUrl());
                props.setProperty(RESOURCE_PATH, p.getResources().getPath());
                props.setProperty(BINARY_PATH, p.getBinaries().getPath());
                props.setProperty(BINARY_LEVELS, p.getBinaries().getLevels().toString());
                props.setProperty(BINARY_LENGTH, p.getBinaries().getLength().toString());
                return props;
            }));

        // Partition data configuration
        final Map<String, String> partitionData = partitions.entrySet().stream().collect(toMap(Map.Entry::getKey,
                                e -> e.getValue().getProperty(RESOURCE_PATH)));

        // Partition BaseURL configuration
        final Map<String, String> partitionUrls = partitions.entrySet().stream().collect(toMap(Map.Entry::getKey,
                                e -> e.getValue().getProperty(BASE_URL)));

        final CuratorFramework curator = newClient(config.getZookeeper().getEnsembleServers(),
                new BoundedExponentialBackoffRetry(config.getZookeeper().getRetryMs(),
                    config.getZookeeper().getRetryMaxMs(), config.getZookeeper().getRetryMax()));
        curator.start();

        final Producer<String, String> producer = new KafkaProducer<>(producerProps);

        final EventService notifications = new KafkaPublisher(producer, TOPIC_EVENT);

        final IdentifierService idService = new UUIDGenerator();

        final ResourceService resourceService = new FileResourceService(partitionData, partitionUrls,
                curator, producer, notifications, idService.getSupplier(), config.getAsync());

        final TreeCache cache = new TreeCache(curator, ZNODE_NAMESPACES);
        final NamespaceService namespaceService = new Namespaces(curator, cache, config.getNamespaces().getFile());

        // TODO -- JDK9 initializer
        final Map<String, String> assetMap = new HashMap<>();
        assetMap.put("icon", config.getAssets().getIcon());
        assetMap.put("css", config.getAssets().getCss().stream().map(String::trim).collect(joining(",")));
        assetMap.put("js", config.getAssets().getJs().stream().map(String::trim).collect(joining(",")));
        final IOService ioService = new JenaIOService(namespaceService, assetMap);

        final BinaryService binaryService = new DefaultBinaryService(idService, partitions,
                asList(new FileResolver(partitions.entrySet().stream()
                        .filter(e -> e.getValue().getProperty(PREFIX).startsWith(FILE_PREFIX + e.getKey()))
                        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getProperty(BINARY_PATH))))));

        final AgentService agentService = new SimpleAgent();
        final AccessControlService accessControlService = new WebACService(resourceService);

        // Health checks
        environment.healthChecks()
            .register("zookeeper", new ZookeeperHealthCheck(config.getZookeeper().getEnsembleServers(),
                        config.getZookeeper().getTimeout()));
        environment.healthChecks()
            .register("kafka", new KafkaHealthCheck(config.getZookeeper().getEnsembleServers(),
                        config.getZookeeper().getTimeout()));

        // Authentication
        final List<AuthFilter> filters = new ArrayList<>();
        final AuthConfiguration auth = config.getAuth();

        if (auth.getBasic().getEnabled()) {
            filters.add(new BasicCredentialAuthFilter.Builder<Principal>()
                    .setAuthenticator(new TrellisAuthenticator(auth.getBasic().getUsersFile()))
                    .setRealm("Trellis Basic Authentication")
                    .buildAuthFilter());
        }

        if (auth.getJwt().getEnabled()) {
            filters.add(new OAuthCredentialAuthFilter.Builder<Principal>()
                    .setAuthenticator(new JwtAuthenticator(auth.getJwt().getKey()))
                    .setPrefix("Bearer")
                    .buildAuthFilter());
        }

        if (auth.getAnon().getEnabled()) {
            filters.add(new AnonymousAuthFilter.Builder()
                .setAuthenticator(new AnonymousAuthenticator())
                .buildAuthFilter());
        }

        if (!filters.isEmpty()) {
            environment.jersey().register(new ChainedAuthFilter<>(filters));
        }

        // Resource matchers
        environment.jersey().register(new RootResource(ioService, partitionUrls, serverProperties));
        environment.jersey().register(new LdpResource(resourceService, ioService, binaryService, partitionUrls));
        environment.jersey().register(new MultipartUploader(resourceService, binaryService, partitionUrls));

        // Filters
        environment.jersey().register(new AgentAuthorizationFilter(agentService, asList("admin")));
        environment.jersey().register(new CacheControlFilter(config.getCacheMaxAge()));

        // Authorization
        if (auth.getWebac().getEnabled()) {
            environment.jersey().register(new WebAcFilter(partitionUrls, asList("Authorization"),
                        accessControlService));
        }

        // CORS
        if (config.getCors().getEnabled()) {
            environment.jersey().register(new CrossOriginResourceSharingFilter(
                        config.getCors().getAllowOrigin(), config.getCors().getAllowMethods(),
                        config.getCors().getAllowHeaders(), config.getCors().getExposeHeaders(),
                        config.getCors().getAllowCredentials(), config.getCors().getMaxAge()));
        }
    }
}
