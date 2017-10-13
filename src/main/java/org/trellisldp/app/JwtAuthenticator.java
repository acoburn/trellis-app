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

import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

import java.security.Principal;
import java.util.Optional;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.PrincipalImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import org.slf4j.Logger;

/**
 * @author acoburn
 */
class JwtAuthenticator implements Authenticator<String, Principal> {

    private final static Logger LOGGER = getLogger(JwtAuthenticator.class);

    public final static String WEBID = "webid";

    private final String key;

    public JwtAuthenticator(final String key) {
        this.key = key;
    }

    @Override
    public Optional<Principal> authenticate(final String credentials) throws AuthenticationException {
        try {
            final Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(credentials).getBody();
            // Use a webid claim, if one exists
            if (claims.containsKey(WEBID)) {
                return ofNullable(claims.get(WEBID, String.class)).map(PrincipalImpl::new);
            }
            final String sub = claims.getSubject();
            if (nonNull(sub)) {
                // use the sub claim if it looks like a webid
                if (isUrl(sub)) {
                    return of(new PrincipalImpl(sub));
                }
                final String iss = claims.getIssuer();
                // combine the iss and sub fields if that appears possible
                if (nonNull(iss) && isUrl(iss)) {
                    final String webid = iss.endsWith("/") ? iss + sub : iss + "/" + sub;
                    return of(new PrincipalImpl(webid));
                }
            }
        } catch (final JwtException ex) {
            LOGGER.warn("Problem reading JWT value: {}", ex.getMessage());
        }
        return empty();
    }

    private Boolean isUrl(final String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }
}
