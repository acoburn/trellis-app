/*
 * Copyright Amherst College
 *
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
package edu.amherst.acdc.trellis.app;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

import edu.amherst.acdc.trellis.datastream.DefaultDatastreamService;
import edu.amherst.acdc.trellis.datastream.FileResolver;
import edu.amherst.acdc.trellis.http.LdpResource;
import edu.amherst.acdc.trellis.io.JenaSerializationService;
import edu.amherst.acdc.trellis.namespaces.NamespacesJsonContext;
import edu.amherst.acdc.trellis.rosid.file.FileResourceService;
import edu.amherst.acdc.trellis.spi.DatastreamService;
import edu.amherst.acdc.trellis.spi.NamespaceService;
import edu.amherst.acdc.trellis.spi.ResourceService;
import edu.amherst.acdc.trellis.spi.SerializationService;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.IOException;
import java.util.Properties;

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

        final Properties kafkaProps = new Properties();
        final Properties zkProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", configuration.getBootstrapServers());
        zkProps.setProperty("connectString", configuration.getEnsemble());

        final ResourceService resSvc = new FileResourceService(kafkaProps, zkProps,
                singletonMap("repository", configuration.getLdprsDirectory()));
        // TODO add namespace support here
        final SerializationService ioSvc = new JenaSerializationService();
        final NamespaceService nsSvc = new NamespacesJsonContext(configuration.getNamespaceFile());
        final DatastreamService dsSvc = new DefaultDatastreamService();

        dsSvc.setResolvers(singletonList(new FileResolver(configuration.getLdpnrDirectory())));
        ioSvc.bind(nsSvc);

        final LdpResource resource = new LdpResource(resSvc, ioSvc, dsSvc);
        environment.jersey().register(resource);
    }
}
