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
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.apache.commons.rdf.api.RDFSyntax.TURTLE;
import static org.slf4j.LoggerFactory.getLogger;

import com.codahale.metrics.annotation.Timed;

import edu.amherst.acdc.trellis.api.Resource;
import edu.amherst.acdc.trellis.spi.ResourceService;
import edu.amherst.acdc.trellis.spi.SerializationService;
import edu.amherst.acdc.trellis.vocabulary.LDP;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
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

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFSyntax;
import org.slf4j.Logger;

/**
 * @author acoburn
 */
@Path("{path: .+}")
@Produces({TEXT_TURTLE, APPLICATION_LD_JSON, APPLICATION_N_TRIPLES})
public class LdpResource {

    private static final Logger LOGGER = getLogger(LdpResource.class);

    private static final RDF rdf = ServiceLoader.load(RDF.class).iterator().next();

    private final ResourceService resourceService;
    private final SerializationService serializationService;

    /**
     * Create a LdpResource
     * @param resourceService the resource service
     * @param serializationService the serialization service
     */
    public LdpResource(final ResourceService resourceService, final SerializationService serializationService) {
        this.resourceService = resourceService;
        this.serializationService = serializationService;
    }

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

        final IRI identifier = rdf.createIRI("trellis:" + path);
        // should also support timegates...
        final Optional<Resource> resource = resourceService.get(identifier);

        if (!resource.isPresent()) {
            return Response.status(NOT_FOUND).build();
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
            // add Accept-Post for container resources
            // add Accept-Patch for LDP-RS
            // add ETag values
            // add Memento headers
            // add inbox, acl, web-annotation headers, if applicable
            // add Link rel="{canonical, describes, describedby}", for LDP-NR
            // add Link rel="memento" for each Memento resource
            // add Content-Length for LDP-NR
            // add Content-Type for LDP-NR
            // add Last-Modified
            // add Created
            // add CreatedBy ??
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
