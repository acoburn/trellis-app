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

import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author acoburn
 */
public class KafkaConfiguration extends Configuration {

    private static final String DEFAULT_ACKS = "all";
    private static final Integer DEFAULT_BATCH_SIZE = 16384;
    private static final Integer DEFAULT_RETRIES = 0;
    private static final Integer DEFAULT_LINGER_MS = 1;
    private static final Integer DEFAULT_BUFFER_MEMORY = 33554432;

    @NotEmpty
    private String bootstrapServers;

    @NotEmpty
    private String acks = DEFAULT_ACKS;

    @NotEmpty
    private Integer retries = DEFAULT_RETRIES;

    @NotEmpty
    private Integer batchSize = DEFAULT_BATCH_SIZE;

    @NotEmpty
    private Integer lingerMs = DEFAULT_LINGER_MS;

    @NotEmpty
    private Integer bufferMemory = DEFAULT_BUFFER_MEMORY;

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
     * Set the Acks value (e.g. "all")
     * @param acks the acks value
     */
    @JsonProperty
    public void setAcks(final String acks) {
        this.acks = acks;
    }

    /**
     * Get the Acks value
     * @return the acks value
     */
    @JsonProperty
    public String getAcks() {
        return acks;
    }

    /**
     * Set the retries value
     * @param retries the number of retries
     */
    @JsonProperty
    public void setRetries(final Integer retries) {
        this.retries = retries;
    }

    /**
     * Get the retries value
     * @return the number of retries
     */
    @JsonProperty
    public Integer getRetries() {
        return retries;
    }

    /**
     * Set the batch.size value
     * @param batchSize the size of a batch
     */
    @JsonProperty
    public void setBatchSize(final Integer batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Get the batch.size value
     * @return the batch size
     */
    @JsonProperty
    public Integer getBatchSize() {
        return batchSize;
    }

    /**
     * Set the linger.ms value, in milliseconds
     * @param lingerMs the number of milliseconds to linger
     */
    @JsonProperty
    public void setLingerMs(final Integer lingerMs) {
        this.lingerMs = lingerMs;
    }

    /**
     * Get the linger.ms value, in milliseconds
     * @return the number of milliseconds to linger
     */
    @JsonProperty
    public Integer getLingerMs() {
        return lingerMs;
    }

    /**
     * Set the buffer.memory value
     * @param bufferMemory the buffer.memory value
     */
    @JsonProperty
    public void setBufferMemory(final Integer bufferMemory) {
        this.bufferMemory = bufferMemory;
    }

    /**
     * Get the buffer.memory value
     * @return the buffer.memory value
     */
    @JsonProperty
    public Integer getBufferMemory() {
        return bufferMemory;
    }
}
