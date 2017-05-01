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
package edu.amherst.acdc.trellis.http;

import static edu.amherst.acdc.trellis.http.Constants.ACCEPT_DATETIME;
import static edu.amherst.acdc.trellis.http.Constants.ACCEPT_PATCH;
import static edu.amherst.acdc.trellis.http.Constants.ACCEPT_POST;
import static edu.amherst.acdc.trellis.http.Constants.MEMENTO_DATETIME;
import static edu.amherst.acdc.trellis.http.Constants.TRELLIS_PREFIX;
import static edu.amherst.acdc.trellis.http.Constants.VARY;
import static edu.amherst.acdc.trellis.http.Prefer.PREFER;
import static edu.amherst.acdc.trellis.http.Prefer.PREFERENCE_APPLIED;
import static edu.amherst.acdc.trellis.http.RdfMediaType.APPLICATION_LD_JSON;
import static edu.amherst.acdc.trellis.http.RdfMediaType.APPLICATION_N_TRIPLES;
import static edu.amherst.acdc.trellis.http.RdfMediaType.APPLICATION_SPARQL_UPDATE;
import static edu.amherst.acdc.trellis.http.RdfMediaType.TEXT_TURTLE;
import static edu.amherst.acdc.trellis.http.RdfMediaType.VARIANTS;
import static edu.amherst.acdc.trellis.spi.ConstraintService.ldpResourceTypes;
import static java.time.ZonedDateTime.parse;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Arrays.asList;
import static java.util.Date.from;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.empty;
import static java.util.stream.Stream.of;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.Response.Status.GONE;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.UriBuilder.fromUri;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.apache.commons.rdf.api.RDFSyntax.RDFA_HTML;
import static org.slf4j.LoggerFactory.getLogger;

import com.codahale.metrics.annotation.Timed;

import edu.amherst.acdc.trellis.api.Datastream;
import edu.amherst.acdc.trellis.api.Resource;
import edu.amherst.acdc.trellis.spi.DatastreamService;
import edu.amherst.acdc.trellis.spi.NamespaceService;
import edu.amherst.acdc.trellis.spi.ResourceService;
import edu.amherst.acdc.trellis.spi.SerializationService;
import edu.amherst.acdc.trellis.vocabulary.LDP;
import edu.amherst.acdc.trellis.vocabulary.OA;
import edu.amherst.acdc.trellis.vocabulary.Trellis;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.api.RDFSyntax;
import org.slf4j.Logger;

/**
 * @author acoburn
 */
@Path("{path: .+}")
@Produces({TEXT_TURTLE, APPLICATION_LD_JSON, APPLICATION_N_TRIPLES, TEXT_HTML})
public class LdpResource {

    private static final Logger LOGGER = getLogger(LdpResource.class);

    private static final RDF rdf = ServiceLoader.load(RDF.class).iterator().next();

    private final String baseUrl;
    private final ResourceService resourceService;
    private final SerializationService serializationService;
    private final DatastreamService datastreamService;
    private final NamespaceService namespaceService;

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpHeaders headers;

    /**
     * Create a LdpResource
     * @param baseUrl the baseUrl
     * @param resourceService the resource service
     * @param serializationService the serialization service
     * @param datastreamService the datastream service
     * @param namespaceService the namespace service
     */
    public LdpResource(final String baseUrl, final ResourceService resourceService,
            final SerializationService serializationService,
            final DatastreamService datastreamService,
            final NamespaceService namespaceService) {
        this.baseUrl = baseUrl;
        this.resourceService = resourceService;
        this.serializationService = serializationService;
        this.datastreamService = datastreamService;
        this.namespaceService = namespaceService;
    }

    /**
     * Perform a GET operation on an LDP Resource
     * @param path the path
     * @return the response
     */
    @GET
    @Timed
    public Response getResource(@PathParam("path") final String path) {
        // can this go somewhere more central?
        if (path.endsWith("/")) {
            return Response.seeOther(fromUri(stripSlash(path)).build()).build();
        }

        final String urlPrefix = ofNullable(baseUrl).orElseGet(() -> uriInfo.getBaseUri().toString());
        final String identifier = urlPrefix + path;
        final Optional<RDFSyntax> syntax = getRdfSyntax(headers.getAcceptableMediaTypes());
        final Optional<Instant> datetime = ofNullable(headers.getRequestHeader(ACCEPT_DATETIME))
                .orElse(emptyList()).stream().map(String::trim).filter(x -> x.length() > 1)
                .map(x -> parse(x, RFC_1123_DATE_TIME)).map(ZonedDateTime::toInstant).findFirst();

        LOGGER.info("RDF Syntax: {}", syntax.map(RDFSyntax::toString).orElse("Nothing"));

        final Optional<Resource> resource;
        if (datetime.isPresent()) {
            resource = resourceService.get(rdf.createIRI(TRELLIS_PREFIX + path), datetime.get());
        } else {
            resource = resourceService.get(rdf.createIRI(TRELLIS_PREFIX + path));
        }

        return resource.map(res -> {
            if (res.getTypes().anyMatch(Trellis.DeletedResource::equals)) {
                // TODO add Mementos
                return Response.status(GONE);
            }

            final Response.ResponseBuilder builder = Response.ok();

            // Standard HTTP Headers
            builder.lastModified(from(res.getModified()));
            builder.variants(VARIANTS);
            builder.header(VARY, asList(PREFER, ACCEPT_DATETIME).stream().collect(joining(",")));
            syntax.map(s -> s.mediaType).ifPresent(builder::type);

            // Add LDP-required headers
            final IRI model = res.getDatastream().isPresent() && syntax.isPresent() ?
                    LDP.RDFSource : res.getInteractionModel();
            ldpResourceTypes(model).forEach(type -> {
                builder.link(type.getIRIString(), "type");
                if (LDP.Container.equals(type)) {
                    builder.header(ACCEPT_POST, VARIANTS.stream().map(Variant::getMediaType)
                            .map(mt -> mt.getType() + "/" + mt.getSubtype()).collect(joining(",")));
                } else if (LDP.RDFSource.equals(type)) {
                    builder.header(ACCEPT_PATCH, APPLICATION_SPARQL_UPDATE);
                }
            });

            // Add NonRDFSource-related "describe*" link headers
            res.getDatastream().ifPresent(ds -> {
                if (syntax.isPresent()) {
                    // TODO make this identifier opaque
                    builder.link(identifier + "#description", "canonical");
                    builder.link(identifier, "describes");
                } else {
                    builder.link(identifier, "canonical");
                    builder.link(identifier + "#description", "describedby");
                    builder.type(ds.getMimeType().orElse(APPLICATION_OCTET_STREAM));
                }
            });

            // Link headers from User data
            res.getTypes().map(IRI::getIRIString).forEach(type -> builder.link(type, "type"));
            res.getInbox().map(IRI::getIRIString).ifPresent(inbox -> builder.link(inbox, "inbox"));
            res.getAnnotationService().map(IRI::getIRIString).ifPresent(svc ->
                    builder.link(svc, OA.annotationService.getIRIString()));

            // TODO add link for timemap
            if (res.isMemento()) {
                builder.header(MEMENTO_DATETIME, from(res.getModified()));
                builder.link(identifier, "memento");
            } else {
                builder.link(identifier, "original timegate");
            }

            // NonRDFSources get strong ETags
            if (res.getDatastream().isPresent() && !syntax.isPresent()) {
                builder.tag(md5Hex(res.getDatastream().map(Datastream::getModified)
                            .map(Instant::toString).get() + identifier));
                final IRI dsid = res.getDatastream().map(Datastream::getIdentifier).get();
                final InputStream datastream = datastreamService.getResolver(dsid).flatMap(svc -> svc.getContent(dsid))
                    .orElseThrow(() ->
                        new WebApplicationException("Could not load datastream resolver for " + dsid.getIRIString()));
                builder.entity(datastream);
            } else {
                final Prefer prefer = new Prefer(ofNullable(headers.getRequestHeader(PREFER))
                        .orElse(emptyList()).stream().findFirst().orElse(""));
                builder.header(PREFERENCE_APPLIED, "return=" + prefer.getPreference().orElse("representation"));
                builder.tag(new EntityTag(md5Hex(res.getModified().toString() + identifier + syntax
                            .map(RDFSyntax::toString).orElse("")), true));

                if (prefer.getPreference().filter("minimal"::equals).isPresent()) {
                    builder.status(NO_CONTENT);
                } else if (syntax.get().equals(RDFA_HTML)) {
                    builder.entity(
                            new ResourceView(res.getIdentifier(), res.stream().filter(filterWithPrefer(prefer))
                                .map(unskolemize(resourceService, urlPrefix)).collect(toList()), namespaceService));
                } else {
                    // TODO add support for json-ld profile data (4th param)
                    builder.entity(new ResourceStreamer(serializationService,
                                res.stream().filter(filterWithPrefer(prefer))
                                .map(unskolemize(resourceService, urlPrefix)),
                                syntax.get()));
                }
            }

            // TODO add acl header, if in effect
            // add Memento headers
            // res.getMementos().forEach(range -> {
            //     builder.header(...);
            // });
            // TODO check cache control headers
            // TODO add support for instance digests
            // TODO add support for range requests

            return builder;
        }).orElse(Response.status(NOT_FOUND)).build();
    }

    private static RDFTerm toExternalIri(final RDFTerm term, final String baseUrl) {
        if (term instanceof IRI) {
            final String iri = ((IRI) term).getIRIString();
            if (iri.startsWith(TRELLIS_PREFIX)) {
                return rdf.createIRI(baseUrl + iri.substring(TRELLIS_PREFIX.length()));
            }
        }
        return term;
    }

    private static Function<Quad, Quad> unskolemize(final ResourceService svc, final String baseUrl) {
        return quad -> rdf.createQuad(quad.getGraphName().orElse(Trellis.PreferUserManaged),
                    (BlankNodeOrIRI) toExternalIri(svc.unskolemize(quad.getSubject()), baseUrl),
                    quad.getPredicate(), toExternalIri(svc.unskolemize(quad.getObject()), baseUrl));
    }

    private static Set<String> getDefaultRepresentation() {
        final Set<String> include = new HashSet<>();
        include.add(LDP.PreferContainment.getIRIString());
        include.add(LDP.PreferMembership.getIRIString());
        include.add(Trellis.PreferUserManaged.getIRIString());
        return include;
    }

    private static Predicate<Quad> filterWithPrefer(final Prefer prefer) {
        final Set<String> include = getDefaultRepresentation();
        prefer.getOmit().forEach(include::remove);
        prefer.getInclude().forEach(include::add);
        return quad -> quad.getGraphName().filter(x -> x instanceof IRI).map(x -> (IRI) x)
            .map(IRI::getIRIString).filter(include::contains).isPresent();
    }

    private static final Function<MediaType, Stream<RDFSyntax>> getSyntax = type -> {
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
