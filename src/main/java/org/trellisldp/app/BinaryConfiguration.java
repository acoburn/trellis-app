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
class BinaryConfiguration {

    @NotEmpty
    private String prefix;

    private String path = "";

    private Integer hierarchy = 0;

    private Integer size = 0;

    /**
     * Get the prefix value for new binary identifiers
     * @return the prefix
     */
    @JsonProperty
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set a prefix value for new binary identifiers
     * @param prefix the prefix
     */
    @JsonProperty
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    /**
     * Get the levels of hierarchy desired for new identifiers
     * @return the hierarchy level
     */
    @JsonProperty
    public Integer getHierarchy() {
        return hierarchy;
    }

    /**
     * Set the levels of hierarchy desired for new identifiers
     * @param hierarchy the hierarchy level
     */
    @JsonProperty
    public void setHierarchy(final Integer hierarchy) {
        this.hierarchy = hierarchy;
    }

    /**
     * Get the size of hierarchy levels
     * @return the size of hierarchy levels
     */
    @JsonProperty
    public Integer getSize() {
        return size;
    }

    /**
     * Set the size of hierarchy levels
     * @param size the size or length of hierarchy segments
     */
    @JsonProperty
    public void setSize(final Integer size) {
        this.size = size;
    }

    /**
     * Get the underlying path for file-based binary resources
     * @return the path
     */
    @JsonProperty
    public String getPath() {
        return path;
    }

    /**
     * Set the underlying path for file-based binary resources
     * @param path the path
     */
    @JsonProperty
    public void setPath(final String path) {
        this.path = path;
    }
}
