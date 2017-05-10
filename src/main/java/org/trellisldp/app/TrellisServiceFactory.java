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

import java.io.IOException;
import java.util.Properties;

import org.trellisldp.datastream.DefaultDatastreamService;
import org.trellisldp.datastream.FileResolver;
import org.trellisldp.io.JenaSerializationService;
import org.trellisldp.namespaces.NamespacesJsonContext;
import org.trellisldp.rosid.file.FileResourceService;
import org.trellisldp.spi.DatastreamService;
import org.trellisldp.spi.NamespaceService;
import org.trellisldp.spi.ResourceService;
import org.trellisldp.spi.SerializationService;

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
            final Properties config = new Properties();
            config.setProperty("kafka.bootstrap.servers", configuration.getBootstrapServers());
            config.setProperty("zk.connectString", configuration.getEnsemble());

            configuration.getStorage().forEach(partition ->
                config.setProperty("trellis.storage." + partition.getName() + ".resources", partition.getLdprs()));

            resourceService = new FileResourceService(null, config);
        }
        return resourceService;
    }

    /**
     * Create a SerializationService
     * @return the serialization service
     */
    public synchronized SerializationService createSerializationService() {
        if (isNull(serializationService)) {
            serializationService = new JenaSerializationService(createNamespaceService());
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
            datastreamService = new DefaultDatastreamService(asList(new FileResolver()));
        }
        return datastreamService;
    }
}
