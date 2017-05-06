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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author acoburn
 */
public class PartitionConfiguration {

    @NotEmpty
    private String ldprs;

    @NotEmpty
    private String ldpnr;

    @NotEmpty
    private String name;

    @NotEmpty
    private String url;

    /**
     * Get the name of the partition
     * @return the partition name
     */
    @JsonProperty
    public String getName() {
        return name;
    }

    /**
     * Set the name of the partition
     * @param name the partition name
     */
    @JsonProperty
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the base URL for the partition
     * @return the partition baseURL
     */
    @JsonProperty
    public String getUrl() {
        return url;
    }

    /**
     * Set the base URL for the partition
     * @param url the partition baseURL
     */
    @JsonProperty
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * Get the LDP-RS directory
     * @return the LDP-RS directory
     */
    @JsonProperty
    public String getLdprs() {
        return ldprs;
    }

    /**
     * Set the LDP-RS directory
     * @param directory the LDP-RS directory
     */
    @JsonProperty
    public void setLdprs(final String directory) {
        this.ldprs = directory;
    }

    /**
     * Get the LDP-NR directory
     * @return the LDP-NR directory
     */
    @JsonProperty
    public String getLdpnr() {
        return ldpnr;
    }

    /**
     * Set the LDP-NR directory
     * @param directory the LDP-NR directory
     */
    @JsonProperty
    public void setLdpnr(final String directory) {
        this.ldpnr = directory;
    }
}
