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

import io.dropwizard.Configuration;

import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * @author acoburn
 */
class TrellisConfiguration extends Configuration {

    @NotNull
    private String namespaceFile;

    @NotNull
    private String defaultName = "Trellis";

    @NotNull
    private AssetConfiguration assets = new AssetConfiguration();

    @NotNull
    private List<PartitionConfiguration> partitions;

    @NotNull
    private KafkaConfiguration kafka;

    @NotNull
    private ZookeeperConfiguration zookeeper;

    @NotNull
    private String topic = "trellis";

    @NotNull
    private String userPrefix = "user:";

    @NotNull
    private String basicAuthFile = "users.auth";

    private Boolean async = false;

    private Boolean enableCORS = true;

    private Boolean enableWebAc = true;

    private Integer cacheMaxAge = 86400;

    private Boolean enableAnonAuth = false;

    private Boolean enableBasicAuth = true;

    private Boolean enableOAuth = true;

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
     * Set the asset configuration
     * @param assets the asset config
     */
    @JsonProperty
    public void setAssets(final AssetConfiguration assets) {
        this.assets = assets;
    }

    /**
     * Get the asset configuration
     * @return the asset config
     */
    @JsonProperty
    public AssetConfiguration getAssets() {
        return assets;
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

    @JsonProperty
    public void setEnableCORS(final Boolean enableCORS) {
        this.enableCORS = enableCORS;
    }

    @JsonProperty
    public Boolean getEnableCORS() {
        return enableCORS;
    }

    @JsonProperty
    public void setEnableWebAc(final Boolean enableWebAc) {
        this.enableWebAc = enableWebAc;
    }

    @JsonProperty
    public Boolean getEnableWebAc() {
        return enableWebAc;
    }

    @JsonProperty
    public void setUserPrefix(final String userPrefix) {
        this.userPrefix = userPrefix;
    }

    @JsonProperty
    public String getUserPrefix() {
        return userPrefix;
    }

    @JsonProperty
    public void setCacheMaxAge(final Integer cacheMaxAge) {
        this.cacheMaxAge = cacheMaxAge;
    }

    @JsonProperty
    public Integer getCacheMaxAge() {
        return cacheMaxAge;
    }

    @JsonProperty
    public void setEnableBasicAuth(final Boolean enableBasicAuth) {
        this.enableBasicAuth = enableBasicAuth;
    }

    @JsonProperty
    public Boolean getEnableBasicAuth() {
        return enableBasicAuth;
    }

    @JsonProperty
    public void setEnableOAuth(final Boolean enableOAuth) {
        this.enableOAuth = enableOAuth;
    }

    @JsonProperty
    public Boolean getEnableOAuth() {
        return enableOAuth;
    }

    @JsonProperty
    public void setEnableAnonAuth(final Boolean enableAnonAuth) {
        this.enableAnonAuth = enableAnonAuth;
    }

    @JsonProperty
    public Boolean getEnableAnonAuth() {
        return enableAnonAuth;
    }

    @JsonProperty
    public void setBasicAuthFile(final String basicAuthFile) {
        this.basicAuthFile = basicAuthFile;
    }

    @JsonProperty
    public String getBasicAuthFile() {
        return basicAuthFile;
    }
}
