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

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.PrincipalImpl;
import io.dropwizard.auth.basic.BasicCredentials;
import org.slf4j.Logger;

/**
 * Class Trellis Authenticator
 * @author acoburn
 */
class TrellisAuthenticator implements Authenticator<BasicCredentials, PrincipalImpl> {

    private final static Logger LOGGER = getLogger(TrellisAuthenticator.class);

    @Override
    public Optional<PrincipalImpl> authenticate(final BasicCredentials credentials) throws AuthenticationException {
        LOGGER.info("Checking: {}", credentials);
        if ("admin".equals(credentials.getPassword())) {
            return of(new PrincipalImpl(credentials.getUsername()));
        }
        return empty();
    }
}
