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
import static org.trellisldp.rosid.common.RosidConstants.ZNODE_COORDINATION;

import com.codahale.metrics.health.HealthCheck;

import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.RetryNTimes;

/**
 * @author acoburn
 */
public class ZookeeperHealthCheck extends HealthCheck {

    protected static final int RETRIES = 10;

    protected final int timeout;
    protected final String connectString;

    /**
     * Create an object that checks the health of a zk ensemble
     * @param connectString the connection string
     * @param timeout the timeout
     */
    public ZookeeperHealthCheck(final String connectString, final int timeout) {
        super();
        this.connectString = connectString;
        this.timeout = timeout;
    }

    @Override
    protected HealthCheck.Result check() throws InterruptedException {
        try (final CuratorFramework zk = newClient(connectString, new RetryNTimes(RETRIES, timeout))) {
            zk.start();
            zk.blockUntilConnected();
            if (!zk.getZookeeperClient().isConnected()) {
                return unhealthy("Could not connect to zookeeper: " + connectString);
            } else if (!zk.getZookeeperClient().getZooKeeper().getState().isAlive()) {
                return unhealthy("Zookeeper ensemble is not alive.");
            } else if (zk.checkExists().forPath(ZNODE_COORDINATION) == null) {
                return unhealthy("Zookeeper not properly initialized");
            }
            return healthy("Zookeeper appears to be healthy.");
        } catch (final IOException ex) {
            return unhealthy("Error connecting to Zookeeper: " + ex.getMessage());
        } catch (final Exception ex) {
            return unhealthy("Error checking on Zookeeper: " + ex.getMessage());
        }
    }
}
