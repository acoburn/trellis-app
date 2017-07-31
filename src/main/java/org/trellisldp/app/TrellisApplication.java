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
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import org.trellisldp.app.health.KafkaHealthCheck;
import org.trellisldp.app.health.ZookeeperHealthCheck;
import org.trellisldp.binary.DefaultBinaryService;
import org.trellisldp.binary.FileResolver;
import org.trellisldp.constraint.LdpConstraints;
import org.trellisldp.http.AdminResource;
import org.trellisldp.http.LdpResource;
import org.trellisldp.id.UUIDGenerator;
import org.trellisldp.io.JenaIOService;
import org.trellisldp.kafka.KafkaPublisher;
import org.trellisldp.namespaces.NamespacesJsonContext;
import org.trellisldp.rosid.file.FileResourceService;
import org.trellisldp.spi.BinaryService;
import org.trellisldp.spi.ConstraintService;
import org.trellisldp.spi.EventService;
import org.trellisldp.spi.IOService;
import org.trellisldp.spi.IdentifierService;
import org.trellisldp.spi.NamespaceService;
import org.trellisldp.spi.ResourceService;


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
        return "Trellis";
    }

    @Override
    public void initialize(final Bootstrap<TrellisConfiguration> bootstrap) {
    }

    @Override
    public void run(final TrellisConfiguration config,
                    final Environment environment) throws IOException {

        // Kafka producer configuration
        final Properties producerProps = config.getKafka().asProperties();
        producerProps.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // Other configurations
        final Map<String, Properties> partitions = config.getPartitions().stream()
            .collect(toMap(PartitionConfiguration::getId, p -> {
                final Properties props = p.getBinaries().asProperties();
                props.setProperty("baseUrl", p.getBaseUrl());
                props.setProperty("resourcePath", p.getResources().getPath());
                return props;
            }));

        final CuratorFramework curator = newClient(config.getZookeeper().getEnsembleServers(),
                new BoundedExponentialBackoffRetry(config.getZookeeper().getRetryMs(),
                    config.getZookeeper().getRetryMaxMs(), config.getZookeeper().getRetryMax()));
        curator.start();

        final Producer<String, String> producer = new KafkaProducer<>(producerProps);

        final EventService notifications = new KafkaPublisher(producer, TOPIC_EVENT);

        final IdentifierService idService = new UUIDGenerator();

        final Properties props = new Properties();
        partitions.forEach((k, v) -> props.setProperty("trellis.storage." + k + ".resources",
                    v.getProperty("resourcePath")));
        final ResourceService resourceService = new FileResourceService(props, curator, producer,
                notifications, idService.getSupplier(), config.getAsync());

        final NamespaceService namespaceService = new NamespacesJsonContext(config.getNamespaceFile());

        final IOService ioService = new JenaIOService(namespaceService, config.getAssets().asMap());

        final ConstraintService constraintService = new LdpConstraints();

        // TODO file resolver needs a method for accessing `path` values
        final BinaryService binaryService = new DefaultBinaryService(idService, partitions, asList(new FileResolver()));

        environment.healthChecks()
            .register("zookeeper", new ZookeeperHealthCheck(config.getZookeeper().getEnsembleServers(),
                        config.getZookeeper().getTimeout()));
        environment.healthChecks()
            .register("kafka", new KafkaHealthCheck(config.getZookeeper().getEnsembleServers(),
                        config.getZookeeper().getTimeout()));
        environment.jersey().register(new AdminResource());
        environment.jersey()
            .register(new LdpResource(resourceService, ioService, constraintService, binaryService,
                        partitions.entrySet().stream().collect(toMap(Map.Entry::getKey,
                                e -> e.getValue().getProperty("baseUrl"))),
                        config.getUnsupportedTypes()));
    }
}
