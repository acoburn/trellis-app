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

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author acoburn
 */
public class TrellisConfiguration extends Configuration {

    @NotEmpty
    private String namespaceFile;

    @NotEmpty
    private String defaultName = "Trellis";

    @NotEmpty
    private List<PartitionConfiguration> partitions;

    @NotEmpty
    private KafkaConfiguration kafka;

    @NotEmpty
    private ZookeeperConfiguration zookeeper;

    @NotEmpty
    private String topic = "trellis";

    @NotEmpty
    private Boolean async = false;

    private List<String> unsupportedTypes = asList("message/external-body");

    /**
     * Set async mode
     * @param async true if async mode is enabled; false otherwise
     */
    @JsonProperty
    public void setAsync(final Boolean async) {
        this.async = async;
    }

    /**
     * Get async mode
     * @return true if async mode is enabled; false otherwise
     */
    @JsonProperty
    public Boolean getAsync() {
        return async;
    }

    /**
     * Get the storage partitions for this repository
     * @return the storage partitions
     */
    @JsonProperty
    public List<PartitionConfiguration> getPartitions() {
        return partitions;
    }

    /**
     * Set the partitions for this repository
     * @param partitions the partitions
     */
    @JsonProperty
    public void setPartitions(final List<PartitionConfiguration> partitions) {
        this.partitions = partitions;
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
     * Set the kafka configuration
     * @param config the Kafka configuration
     */
    @JsonProperty
    public void setKafka(final KafkaConfiguration config) {
        this.kafka = config;
    }

    /**
     * Get the kafka configuration
     * @return the kafka configuration
     */
    @JsonProperty
    public KafkaConfiguration getKafka() {
        return kafka;
    }

    /**
     * Set the zookeeper configuration
     * @param zookeeper the zookeeper configuration
     */
    @JsonProperty
    public void setZookeeper(final ZookeeperConfiguration zookeeper) {
        this.zookeeper = zookeeper;
    }

    /**
     * Get the zookeeper configuration
     * @return the zookeeper configuration
     */
    @JsonProperty
    public ZookeeperConfiguration getZookeeper() {
        return zookeeper;
    }

    /**
     * Set the event topic name
     * @param topic the name of the event topic
     */
    @JsonProperty
    public void setTopic(final String topic) {
        this.topic = topic;
    }

    /**
     * Get the event topic name
     * @return the name of the event topic
     */
    @JsonProperty
    public String getTopic() {
        return topic;
    }

    /**
     * Get any unsupported types
     * @return unsupported mimeTypes
     */
    @JsonProperty
    public List<String> getUnsupportedTypes() {
        return unsupportedTypes;
    }

    /**
     * Set any unsupported types
     * @param unsupportedTypes any unsupported mimetypes
     */
    @JsonProperty
    public void setUnsupportedTypes(final List<String> unsupportedTypes) {
        this.unsupportedTypes = unsupportedTypes;
    }
}
