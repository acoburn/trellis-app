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

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.IOException;
import java.util.Properties;

import org.trellisldp.app.health.KafkaHealthCheck;
import org.trellisldp.app.health.ZookeeperHealthCheck;
import org.trellisldp.binary.DefaultBinaryService;
import org.trellisldp.binary.FileResolver;
import org.trellisldp.constraint.LdpConstraints;
import org.trellisldp.http.AdminResource;
import org.trellisldp.http.LdpResource;
import org.trellisldp.io.JenaIOService;
import org.trellisldp.kafka.KafkaPublisher;
import org.trellisldp.namespaces.NamespacesJsonContext;
import org.trellisldp.rosid.file.FileResourceService;
import org.trellisldp.spi.BinaryService;
import org.trellisldp.spi.ConstraintService;
import org.trellisldp.spi.EventService;
import org.trellisldp.spi.IOService;
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

        final int timeout = 1000;

        final Properties props = new Properties();
        props.setProperty("kafka.bootstrap.servers", config.getBootstrapServers());
        props.setProperty("zk.connectString", config.getEnsemble());

        config.getStorage().forEach(partition ->
            props.setProperty("trellis.storage." + partition.getName() + ".resources", partition.getLdprs()));

        final EventService eventService = new KafkaPublisher(config.getBootstrapServers(), config.getTopic());
        final ResourceService resourceService = new FileResourceService(eventService, props);
        final NamespaceService namespaceService = new NamespacesJsonContext(config.getNamespaceFile());
        final IOService ioService = new JenaIOService(namespaceService);
        final ConstraintService constraintService = new LdpConstraints(config.getBaseUrl());
        final BinaryService binaryService = new DefaultBinaryService(asList(new FileResolver()));

        environment.healthChecks().register("zookeeper", new ZookeeperHealthCheck(config.getEnsemble(), timeout));
        environment.healthChecks().register("kafka", new KafkaHealthCheck(config.getEnsemble(), timeout));
        environment.jersey().register(new AdminResource());
        environment.jersey().register(new LdpResource(config.getBaseUrl(),
                    resourceService, ioService, constraintService, binaryService));
    }
}
