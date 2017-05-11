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

import java.io.IOException;

import com.codahale.metrics.health.HealthCheck;
import org.apache.zookeeper.ZooKeeper;

/**
 * @author acoburn
 */
public class ZookeeperHealthCheck extends HealthCheck {

    private final String connectString;
    private final int timeout;

    /**
     * Create an object that checks the health of a zk ensemble
     * @param connectString the connection string
     * @param timeout the timeout
     */
    public ZookeeperHealthCheck(final String connectString, final int timeout) {
        this.connectString = connectString;
        this.timeout = timeout;
    }

    @Override
    protected HealthCheck.Result check() throws InterruptedException {
        ZooKeeper zk = null;
        HealthCheck.Result result = healthy("Zookeeper is healthy");
        try {
            zk = new ZooKeeper(connectString, timeout, evt -> { });
            if (!zk.getState().isConnected() || !zk.getState().isAlive()) {
                result = unhealthy("Could not connect to zookeeper");
            }
        } catch (final IOException ex) {
            result = unhealthy("Error connecting to Zookeeper: " + ex.getMessage());
        } finally {
            if (zk != null) {
                zk.close();
            }
        }
        return result;
    }
}
