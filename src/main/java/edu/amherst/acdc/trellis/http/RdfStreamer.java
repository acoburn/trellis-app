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

import edu.amherst.acdc.trellis.api.Resource;
import edu.amherst.acdc.trellis.spi.SerializationService;
import edu.amherst.acdc.trellis.vocabulary.JSONLD;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.RDFSyntax;

/**
 * @author acoburn
 */
class RdfStreamer implements StreamingOutput {

    private final SerializationService service;
    private final Resource resource;
    private final RDFSyntax syntax;
    private final String profile;

    public RdfStreamer(final SerializationService service, final Resource resource, final RDFSyntax syntax,
            final String profile) {
        this.service = service;
        this.resource = resource;
        this.syntax = syntax;
        this.profile = profile;
    }

    public RdfStreamer(final SerializationService service, final Resource resource, final RDFSyntax syntax) {
        this(service, resource, syntax, "");
    }

    @Override
    public void write(final OutputStream os) throws IOException, WebApplicationException {
        service.write(resource.stream().map(Quad::asTriple), os, syntax, profileToIRI(profile));
    }

    private static IRI profileToIRI(final String profile) {
        if (profile.contains(JSONLD.compacted.getIRIString())) {
            return JSONLD.compacted;
        } else if (profile.contains(JSONLD.flattened.getIRIString())) {
            return JSONLD.flattened;
        }
        return JSONLD.expanded;
    }
}
