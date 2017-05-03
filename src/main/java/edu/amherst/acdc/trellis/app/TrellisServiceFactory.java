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

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;

import edu.amherst.acdc.trellis.datastream.DefaultDatastreamService;
import edu.amherst.acdc.trellis.datastream.FileResolver;
import edu.amherst.acdc.trellis.io.JenaSerializationService;
import edu.amherst.acdc.trellis.namespaces.NamespacesJsonContext;
import edu.amherst.acdc.trellis.rosid.file.FileResourceService;
import edu.amherst.acdc.trellis.spi.DatastreamService;
import edu.amherst.acdc.trellis.spi.NamespaceService;
import edu.amherst.acdc.trellis.spi.ResourceService;
import edu.amherst.acdc.trellis.spi.SerializationService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author acoburn
 */
class TrellisServiceFactory {

    private SerializationService serializationService = null;
    private NamespaceService namespaceService = null;
    private DatastreamService datastreamService = null;
    private ResourceService resourceService = null;

    private final TrellisConfiguration configuration;

    /**
     * Create a factory for creating Services
     * @param configuration the configuration
     */
    public TrellisServiceFactory(final TrellisConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Create a ResourceService
     * @return the resource service
     * @throws IOException if there was an error starting up
     */
    public synchronized ResourceService createResourceService() throws IOException {
        if (isNull(resourceService)) {
            final Properties kafkaProps = new Properties();
            final Properties zkProps = new Properties();
            kafkaProps.setProperty("bootstrap.servers", configuration.getBootstrapServers());
            zkProps.setProperty("connectString", configuration.getEnsemble());

            final Map<String, String> repositories = new HashMap<>();
            configuration.getPartitions()
                .forEach(partition -> repositories.put(partition.getName(), partition.getLdprs()));
            resourceService = new FileResourceService(kafkaProps, zkProps, repositories);
        }
        return resourceService;
    }

    /**
     * Create a SerializationService
     * @return the serialization service
     */
    public synchronized SerializationService createSerializationService() {
        if (isNull(serializationService)) {
            serializationService = new JenaSerializationService();
            serializationService.bind(createNamespaceService());
        }
        return serializationService;
    }

    /**
     * Create a NamespaceService
     * @return the namespace service
     */
    public synchronized NamespaceService createNamespaceService() {
        if (isNull(namespaceService)) {
            namespaceService = new NamespacesJsonContext(configuration.getNamespaceFile());
        }
        return namespaceService;
    }

    /**
     * Create a DatastreamService
     * @return the datastream service
     */
    public synchronized DatastreamService createDatastreamService() {
        if (isNull(datastreamService)) {
            datastreamService = new DefaultDatastreamService();
            datastreamService.setResolvers(asList(new FileResolver(configuration.getPartitions().get(0).getLdpnr())));
        }
        return datastreamService;
    }
}
