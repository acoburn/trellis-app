/*
 * Copyright Amherst College
 *
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
package edu.amherst.acdc.trellis.app.resources;

import static edu.amherst.acdc.trellis.app.core.RdfMediaType.APPLICATION_LD_JSON;
import static edu.amherst.acdc.trellis.app.core.RdfMediaType.APPLICATION_N_TRIPLES;
import static edu.amherst.acdc.trellis.app.core.RdfMediaType.TEXT_TURTLE;
import static edu.amherst.acdc.trellis.app.core.RdfMediaType.VARIANTS;
import static java.util.stream.Stream.empty;
import static java.util.stream.Stream.of;
import static org.apache.commons.rdf.api.RDFSyntax.TURTLE;
import static org.slf4j.LoggerFactory.getLogger;

import com.codahale.metrics.annotation.Timed;

import edu.amherst.acdc.trellis.vocabulary.LDP;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;

import org.apache.commons.rdf.api.RDFSyntax;
import org.slf4j.Logger;

/**
 * @author acoburn
 */
@Path("{path: .+}")
@Produces({TEXT_TURTLE, APPLICATION_LD_JSON, APPLICATION_N_TRIPLES})
public class LdpResource {

    private static final Logger LOGGER = getLogger(LdpResource.class);

    /**
     * Perform a GET operation on an LDP Resource
     * @param path the path
     * @param headers the headers
     * @return the response
     */
    @GET
    @Timed
    public Response getResource(@PathParam("path") final String path, @Context final HttpHeaders headers) {
        // can this go somewhere more central?
        if (path.endsWith("/")) {
            final URI uri = UriBuilder.fromUri(stripSlash(path)).build();
            return Response.seeOther(uri).build();
        }

        // Try to load the resource using the SPI
        // This may be a LDP-NR, so check the accept headers...
        final Optional<RDFSyntax> syntax = getRdfSyntax(headers.getAcceptableMediaTypes());

        // If it's RDF or the syntax is set, respond w/ RDF
        // Configure prefer headers
        // Add header values (LastModified, Created, Link, etc)

        return Response.ok()
            .link(LDP.Resource.getIRIString(), "type")
            // add other LDP type(s)
            .variants(VARIANTS)
            .header("Vary", "Prefer")
            .entity("trellis:" + path + " " + "format:" + syntax.orElse(TURTLE).toString())
            .build();
    }

    private static Function<MediaType, Stream<RDFSyntax>> getSyntax = type -> {
        final Optional<RDFSyntax> syntax = VARIANTS.stream().map(Variant::getMediaType).filter(type::isCompatible)
            .findFirst().map(MediaType::toString).flatMap(RDFSyntax::byMediaType);
        return syntax.isPresent() ? of(syntax.get()) : empty();
    };

    private static Optional<RDFSyntax> getRdfSyntax(final List<MediaType> types) {
        return types.stream().flatMap(getSyntax).findFirst();
    }

    private static String stripSlash(final String path) {
        return path.endsWith("/") ? stripSlash(path.substring(0, path.length() - 1)) : path;
    }
}
