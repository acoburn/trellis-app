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

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author acoburn
 */
class BinaryConfiguration {

    @NotEmpty
    private String prefix;

    private Map<String, String> other = new HashMap<String, String>();

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
     * Set configuration values dynamically
     * @param name the configuration name
     * @param value the value
     */
    @JsonAnySetter
    public void set(final String name, final String value) {
        other.put(name, value);
    }

    /**
     * Get a dynamically set property
     * @param name the property name
     * @param defaultValue a default value
     * @return the corresponding value
     */
    public String getOrDefault(final String name, final String defaultValue) {
        return other.getOrDefault(name, defaultValue);
    }
}
