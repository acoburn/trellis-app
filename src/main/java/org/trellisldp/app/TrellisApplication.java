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
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;
import static org.apache.curator.framework.CuratorFrameworkFactory.newClient;
import static org.trellisldp.rosid.common.RosidConstants.TOPIC_EVENT;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharingFilter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import org.trellisldp.agent.JsonAgent;
import org.trellisldp.app.health.KafkaHealthCheck;
import org.trellisldp.app.health.ZookeeperHealthCheck;
import org.trellisldp.binary.DefaultBinaryService;
import org.trellisldp.binary.FileResolver;
import org.trellisldp.constraint.LdpConstraints;
import org.trellisldp.http.AgentAuthorizationFilter;
import org.trellisldp.http.CacheControlFilter;
import org.trellisldp.http.LdpResource;
import org.trellisldp.http.MultipartUploader;
import org.trellisldp.http.RootResource;
import org.trellisldp.http.TrailingSlashFilter;
import org.trellisldp.http.WebAcFilter;
import org.trellisldp.http.WebAcHeaderFilter;
import org.trellisldp.id.UUIDGenerator;
import org.trellisldp.io.JenaIOService;
import org.trellisldp.kafka.KafkaPublisher;
import org.trellisldp.namespaces.NamespacesJsonContext;
import org.trellisldp.rosid.file.FileResourceService;
import org.trellisldp.spi.AccessControlService;
import org.trellisldp.spi.AgentService;
import org.trellisldp.spi.BinaryService;
import org.trellisldp.spi.ConstraintService;
import org.trellisldp.spi.EventService;
import org.trellisldp.spi.IOService;
import org.trellisldp.spi.IdentifierService;
import org.trellisldp.spi.NamespaceService;
import org.trellisldp.spi.ResourceService;
import org.trellisldp.spi.RuntimeRepositoryException;
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

    // TODO make this configurable
    private static final Integer CACHE_MAX_AGE = 86400;

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
        serverProperties.setProperty("title", "Trellis Repository");

        // Kafka producer configuration
        final Properties producerProps = config.getKafka().asProperties();
        producerProps.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // Other configurations
        final Map<String, Properties> partitions = config.getPartitions().stream()
            .collect(toMap(PartitionConfiguration::getId, p -> {
                final Properties props = p.getBinaries().asProperties();
                props.setProperty(BASE_URL, p.getBaseUrl());
                props.setProperty(RESOURCE_PATH, p.getResources().getPath());
                if (props.getProperty(PREFIX).startsWith(FILE_PREFIX)) {
                    if (isNull(props.getProperty(BINARY_PATH))) {
                        throw new RuntimeRepositoryException("No path value defined for file-based binary storage");
                    }
                    if (!props.getProperty(PREFIX).startsWith(FILE_PREFIX + p.getId() + "/")) {
                        throw new RuntimeRepositoryException("Prefix value does not include partition!");
                    }
                }
                return props;
            }));

        final CuratorFramework curator = newClient(config.getZookeeper().getEnsembleServers(),
                new BoundedExponentialBackoffRetry(config.getZookeeper().getRetryMs(),
                    config.getZookeeper().getRetryMaxMs(), config.getZookeeper().getRetryMax()));
        curator.start();

        final Producer<String, String> producer = new KafkaProducer<>(producerProps);

        final EventService notifications = new KafkaPublisher(producer, TOPIC_EVENT);

        final IdentifierService idService = new UUIDGenerator();

        final ResourceService resourceService = new FileResourceService(partitions.entrySet().stream()
                    .collect(toMap(Map.Entry::getKey, e -> e.getValue().getProperty(RESOURCE_PATH))),
                curator, producer, notifications, idService.getSupplier(), config.getAsync());

        final NamespaceService namespaceService = new NamespacesJsonContext(config.getNamespaceFile());

        final IOService ioService = new JenaIOService(namespaceService, config.getAssets().asMap());

        final ConstraintService constraintService = new LdpConstraints();

        final BinaryService binaryService = new DefaultBinaryService(idService, partitions,
                asList(new FileResolver(partitions.entrySet().stream()
                        .filter(e -> e.getValue().getProperty(PREFIX).startsWith(FILE_PREFIX + e.getKey()))
                        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getProperty(BINARY_PATH))))));

        final Map<String, String> partitionUrls = partitions.entrySet().stream().collect(toMap(Map.Entry::getKey,
                                e -> e.getValue().getProperty(BASE_URL)));

        // TODO -- make this configurable
        final AgentService agentService = new JsonAgent("/Users/acoburn/trellisData/agents.json", "user:");
        final AccessControlService accessControlService = new WebACService(resourceService, agentService);

        // CORS configuration
        final CrossOriginResourceSharingFilter corsFilter = new CrossOriginResourceSharingFilter();
        corsFilter.setExposeHeaders(asList("Link"));
        corsFilter.setAllowOrigins(asList("*"));

        final WebAcFilter webacFilter = new WebAcFilter(partitionUrls, asList("Authorization"), accessControlService);
        final AgentAuthorizationFilter agentFilter = new AgentAuthorizationFilter(agentService, "admin");

        // Health checks
        environment.healthChecks()
            .register("zookeeper", new ZookeeperHealthCheck(config.getZookeeper().getEnsembleServers(),
                        config.getZookeeper().getTimeout()));
        environment.healthChecks()
            .register("kafka", new KafkaHealthCheck(config.getZookeeper().getEnsembleServers(),
                        config.getZookeeper().getTimeout()));

        // Resource matchers
        environment.jersey().register(new RootResource(ioService, partitionUrls, serverProperties));
        environment.jersey().register(new LdpResource(resourceService, ioService, constraintService, binaryService,
                    partitionUrls));
        environment.jersey().register(new MultipartUploader(resourceService, binaryService));

        // Filters
        environment.jersey().register(new TrailingSlashFilter());
        //environment.jersey().register(agentFilter);
        //environment.jersey().register(webacFilter);
        environment.jersey().register(new CacheControlFilter(CACHE_MAX_AGE));
        environment.jersey().register(new WebAcHeaderFilter());
        //environment.jersey().register(corsFilter);
    }
}
