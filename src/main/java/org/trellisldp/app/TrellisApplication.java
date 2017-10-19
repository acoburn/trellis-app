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
import static java.util.Collections.emptyList;
import static org.trellisldp.app.TrellisUtils.getAuthFilters;
import static org.trellisldp.app.TrellisUtils.getCorsConfiguration;
import static org.trellisldp.app.TrellisUtils.getKafkaProperties;
import static org.trellisldp.app.TrellisUtils.getServerProperties;
import static org.trellisldp.app.TrellisUtils.getWebacConfiguration;
import static org.trellisldp.rosid.common.RosidConstants.TOPIC_EVENT;
import static org.trellisldp.rosid.common.RosidConstants.ZNODE_NAMESPACES;

import io.dropwizard.Application;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import org.trellisldp.agent.SimpleAgent;
import org.trellisldp.api.BinaryService;
import org.trellisldp.api.IOService;
import org.trellisldp.api.IdentifierService;
import org.trellisldp.api.NamespaceService;
import org.trellisldp.api.ResourceService;
import org.trellisldp.app.config.TrellisConfiguration;
import org.trellisldp.app.health.KafkaHealthCheck;
import org.trellisldp.app.health.ZookeeperHealthCheck;
import org.trellisldp.binary.DefaultBinaryService;
import org.trellisldp.binary.FileResolver;
import org.trellisldp.http.AgentAuthorizationFilter;
import org.trellisldp.http.CacheControlFilter;
import org.trellisldp.http.CrossOriginResourceSharingFilter;
import org.trellisldp.http.LdpResource;
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
        return "Trellis LDP";
    }

    @Override
    public void initialize(final Bootstrap<TrellisConfiguration> bootstrap) {
        // Not currently used
    }

    @Override
    public void run(final TrellisConfiguration config,
                    final Environment environment) throws IOException {

        // Other configurations
        final Map<String, Properties> partitions = TrellisUtils.getPartitionConfigurations(config);

        // Partition data configuration
        final Map<String, String> partitionData = TrellisUtils.getResourceDataPaths(partitions);

        // Partition BaseURL configuration
        final Map<String, String> partitionUrls = TrellisUtils.getPartitionBaseUrls(partitions);

        final CuratorFramework curator = TrellisUtils.getCuratorClient(config);

        final Producer<String, String> producer = new KafkaProducer<>(getKafkaProperties(config));

        final IdentifierService idService = new UUIDGenerator();

        final ResourceService resourceService = new FileResourceService(partitionData, partitionUrls, curator,
                producer, new KafkaPublisher(producer, TOPIC_EVENT), idService.getSupplier(), config.getAsync());

        final NamespaceService namespaceService = new Namespaces(curator, new TreeCache(curator, ZNODE_NAMESPACES),
                config.getNamespaces().getFile());

        final IOService ioService = new JenaIOService(namespaceService, TrellisUtils.getAssetConfiguration(config));

        final BinaryService binaryService = new DefaultBinaryService(idService, partitions,
                asList(new FileResolver(TrellisUtils.getBinaryDataPaths(partitions))));

        // Health checks
        environment.healthChecks().register("zookeeper", new ZookeeperHealthCheck(curator));
        environment.healthChecks().register("kafka", new KafkaHealthCheck(curator));

        getAuthFilters(config).ifPresent(filters -> environment.jersey().register(new ChainedAuthFilter<>(filters)));

        // Resource matchers
        environment.jersey().register(new RootResource(ioService, partitionUrls, getServerProperties(config)));
        environment.jersey().register(new LdpResource(resourceService, ioService, binaryService, partitionUrls));

        // Filters
        environment.jersey().register(new AgentAuthorizationFilter(new SimpleAgent(), emptyList()));
        environment.jersey().register(new CacheControlFilter(config.getCacheMaxAge()));

        // Authorization
        getWebacConfiguration(config).ifPresent(webac -> environment.jersey().register(new WebAcFilter(
                        partitionUrls, asList("Authorization"), new WebACService(resourceService))));

        // CORS
        getCorsConfiguration(config).ifPresent(cors -> environment.jersey().register(
                new CrossOriginResourceSharingFilter(cors.getAllowOrigin(), cors.getAllowMethods(),
                    cors.getAllowHeaders(), cors.getExposeHeaders(), cors.getAllowCredentials(), cors.getMaxAge())));
    }
}
