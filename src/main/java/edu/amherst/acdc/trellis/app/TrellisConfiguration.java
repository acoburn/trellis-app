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
    private String dataPath;

    @NotEmpty
    private String defaultName = "Trellis";

    @NotEmpty
    private String ensemble;

    @NotEmpty
    private String bootstrapServers;

    /**
     * Get the data path
     * @return the data path
     */
    @JsonProperty
    public String getDataPath() {
        return dataPath;
    }

    /**
     * Set the data path
     * @param dataPath the data path
     */
    @JsonProperty
    public void setDataPath(final String dataPath) {
        this.dataPath = dataPath;
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
