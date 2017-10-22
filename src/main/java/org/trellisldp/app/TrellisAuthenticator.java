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

import static java.nio.file.Files.lines;
import static java.util.Optional.empty;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import java.util.stream.Stream;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.PrincipalImpl;
import io.dropwizard.auth.basic.BasicCredentials;
import org.slf4j.Logger;

/**
 * Class Trellis Authenticator
 * @author acoburn
 */
class TrellisAuthenticator implements Authenticator<BasicCredentials, Principal> {

    private final static Logger LOGGER = getLogger(TrellisAuthenticator.class);

    private final String credentialsFile;

    public TrellisAuthenticator(final String credentialsFile) {
        this.credentialsFile = credentialsFile;
    }

    @Override
    public Optional<Principal> authenticate(final BasicCredentials credentials) throws AuthenticationException {
        return lookup(credentials).map(PrincipalImpl::new);
    }

    private Optional<String> lookup(final BasicCredentials creds) {
        final File file = new File(credentialsFile);
        if (!file.exists()) {
            return empty();
        }

        try (final Stream<String> fileLines = lines(file.toPath())) {
            return fileLines.map(String::trim).filter(line -> !line.startsWith("#"))
                .map(line -> line.split(":", 3)).filter(x -> x.length == 3)
                .filter(d -> d[0].trim().equals(creds.getUsername()) && d[1].trim().equals(creds.getPassword()))
                .map(d -> d[2]).findFirst();
        } catch (final IOException ex) {
            LOGGER.error("Error processing credentials file: {}", ex.getMessage());
        }
        return empty();
    }
}
