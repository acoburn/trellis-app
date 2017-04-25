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

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author acoburn
 */
public class TrellisConfiguration extends Configuration {

    @NotEmpty
    private String ldprsDirectory;

    @NotEmpty
    private String ldpnrDirectory;

    @NotEmpty
    private String namespaceFile;

    @NotEmpty
    private String defaultName = "Trellis";

    @NotEmpty
    private String ensemble;

    @NotEmpty
    private String bootstrapServers;

    /**
     * Get the LDP-RS directory
     * @return the LDP-RS directory
     */
    @JsonProperty
    public String getLdprsDirectory() {
        return ldprsDirectory;
    }

    /**
     * Set the LDP-RS directory
     * @param directory the LDP-RS directory
     */
    @JsonProperty
    public void setLdprsDirectory(final String directory) {
        this.ldprsDirectory = directory;
    }

    /**
     * Get the LDP-NR directory
     * @return the LDP-NR directory
     */
    @JsonProperty
    public String getLdpnrDirectory() {
        return ldpnrDirectory;
    }

    /**
     * Set the LDP-NR directory
     * @param directory the LDP-NR directory
     */
    @JsonProperty
    public void setLdpnrDirectory(final String directory) {
        this.ldpnrDirectory = directory;
    }

    /**
     * Get the namespace file location
     * @return the namespace file location
     */
    @JsonProperty
    public String getNamespaceFile() {
        return namespaceFile;
    }

    /**
     * Set the namespace file location
     * @param file the namespace file location
     */
    @JsonProperty
    public void setNamespaceFile(final String file) {
        this.namespaceFile = file;
    }

    /**
     * Get the application name
     * @return the name
     */
    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    /**
     * Set the application name
     * @param name the name
     */
    @JsonProperty
    public void setDefaultName(final String name) {
        this.defaultName = name;
    }

    /**
     * Set the kafka bootstrap server locations
     * @param bootstrapServers a comma-delimited list of kafka servers
     */
    @JsonProperty
    public void setBootstrapServers(final String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    /**
     * Get the kafka bootstrap server locations
     * @return the kafka bootstrap server locations
     */
    @JsonProperty
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    /**
     * Set the zookeeper ensemble locations
     * @param ensemble a comma-delimited list of zookeeper servers
     */
    @JsonProperty
    public void setEnsemble(final String ensemble) {
        this.ensemble = ensemble;
    }

    /**
     * Get the zookeeper ensemble locations
     * @return the zookeeper ensemble locations
     */
    @JsonProperty
    public String getEnsemble() {
        return ensemble;
    }
}
