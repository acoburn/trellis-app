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
package org.trellisldp.app.health;

import static com.codahale.metrics.health.HealthCheck.Result.healthy;
import static com.codahale.metrics.health.HealthCheck.Result.unhealthy;
import static org.apache.curator.framework.CuratorFrameworkFactory.newClient;

import com.codahale.metrics.health.HealthCheck;

import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.KeeperException;

/**
 * @author acoburn
 */
public class KafkaHealthCheck extends ZookeeperHealthCheck {

    /**
     * Create an object that checks the health of a zk ensemble
     * @param connectString the connection string
     * @param timeout the timeout
     */
    public KafkaHealthCheck(final String connectString, final int timeout) {
        super(connectString, timeout);
    }

    @Override
    protected HealthCheck.Result check() throws InterruptedException {
        try (final CuratorFramework zk = newClient(connectString, new RetryNTimes(RETRIES, timeout))) {
            zk.start();
            zk.blockUntilConnected();
            if (!zk.getZookeeperClient().isConnected()) {
                return unhealthy("Could not connect to zookeeper: " + connectString);
            } else if (zk.getZookeeperClient().getZooKeeper().getChildren("/brokers/ids", false).isEmpty()) {
                return unhealthy("No Kafka brokers are connected.");
            }
            return healthy("Kafka appears to be in fine health.");
        } catch (final IOException ex) {
            return unhealthy("Error connecting to Zookeeper: " + ex.getMessage());
        } catch (final KeeperException ex) {
            return unhealthy("Error fetching kafka broker list: " + ex.getMessage());
        } catch (final Exception ex) {
            return unhealthy("Error checking on Kafka: " + ex.getMessage());
        }
    }
}
