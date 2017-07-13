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

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.IOException;

import org.trellisldp.app.health.KafkaHealthCheck;
import org.trellisldp.app.health.ZookeeperHealthCheck;
import org.trellisldp.http.AdminResource;
import org.trellisldp.http.LdpResource;

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
    public void run(final TrellisConfiguration configuration,
                    final Environment environment) throws IOException {

        final int timeout = 1000;
        final TrellisServiceFactory factory = new TrellisServiceFactory(configuration);

        environment.healthChecks().register("zookeeper",
                new ZookeeperHealthCheck(configuration.getEnsemble(), timeout));
        environment.healthChecks().register("kafka",
                new KafkaHealthCheck(configuration.getEnsemble(), timeout));
        environment.jersey().register(new AdminResource());
        environment.jersey().register(new LdpResource(configuration.getBaseUrl(),
                    factory.createResourceService(),
                    factory.createIOService(),
                    factory.createConstraintService(),
                    factory.createBinaryService()));
    }
}
