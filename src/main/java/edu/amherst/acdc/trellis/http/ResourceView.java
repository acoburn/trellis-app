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

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import edu.amherst.acdc.trellis.spi.NamespaceService;
import edu.amherst.acdc.trellis.vocabulary.DC;
import edu.amherst.acdc.trellis.vocabulary.RDFS;
import edu.amherst.acdc.trellis.vocabulary.SKOS;
import edu.amherst.acdc.trellis.vocabulary.Trellis;

import io.dropwizard.views.View;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.Triple;

/**
 * @author acoburn
 */
class ResourceView extends View {

    private static final List<IRI> titleCandidates = asList(SKOS.prefLabel, RDFS.label, DC.title);

    public class LabelledTriple {

        private final Triple triple;
        private final String predLabel;
        private final String objLabel;

        public LabelledTriple(final Triple triple, final String predicate, final String object) {
            this.triple = triple;
            this.predLabel = predicate;
            this.objLabel = object;
        }

        public String getSubject() {
            if (triple.getSubject() instanceof IRI) {
                return ((IRI) triple.getSubject()).getIRIString();
            }
            return triple.getSubject().ntriplesString();
        }

        public String getPredicate() {
            return triple.getPredicate().getIRIString();
        }

        public String getObject() {
            if (triple.getObject() instanceof Literal) {
                return ((Literal) triple.getObject()).getLexicalForm();
            } else if (triple.getObject() instanceof IRI) {
                return ((IRI) triple.getObject()).getIRIString();
            }
            return triple.getObject().ntriplesString();
        }

        public String getPredicateLabel() {
            if (nonNull(predLabel)) {
                return predLabel;
            }
            return getPredicate();
        }

        public String getObjectLabel() {
            if (nonNull(objLabel)) {
                return objLabel;
            }
            return getObject();
        }
    }

    private final IRI subject;
    private final List<Quad> quads;
    private final NamespaceService namespaceService;

    public ResourceView(final IRI subject, final List<Quad> quads, final NamespaceService namespaceService) {
        super("resource.mustache");
        this.subject = subject;
        this.quads = quads;
        this.namespaceService = namespaceService;
    }

    public List<LabelledTriple> getTriples() {
        return quads.stream().map(Quad::asTriple).map(labelTriple).collect(toList());
    }

    public String getTitle() {
        final Map<IRI, List<String>> titles = quads.stream()
            .filter(quad -> quad.getGraphName().equals(of(Trellis.PreferUserManaged)))
            .filter(quad -> titleCandidates.contains(quad.getPredicate()))
            .filter(quad -> quad.getObject() instanceof Literal)
            .collect(groupingBy(Quad::getPredicate, mapping(quad ->
                            ((Literal) quad.getObject()).getLexicalForm(), toList())));
        return titleCandidates.stream().filter(titles::containsKey)
            .map(titles::get).flatMap(List::stream).findFirst()
            .orElseGet(this::getSubject);
    }

    public String getSubject() {
        return subject.getIRIString();
    }

    private Function<Triple, LabelledTriple> labelTriple = triple -> {
        final String pred = triple.getPredicate().getIRIString();
        if (triple.getObject() instanceof IRI) {
            return new LabelledTriple(triple, getLabel(pred), getLabel(((IRI) triple.getObject()).getIRIString()));
        } else if (triple.getObject() instanceof Literal) {
            return new LabelledTriple(triple, getLabel(pred), ((Literal) triple.getObject()).getLexicalForm());
        }
        return new LabelledTriple(triple, getLabel(pred), triple.getObject().ntriplesString());
    };

    private String getLabel(final String iri) {
        final int lastHash = iri.lastIndexOf('#');
        String namespace = null;
        final String qname;
        if (lastHash != -1) {
            namespace = iri.substring(0, lastHash + 1);
            qname = iri.substring(lastHash + 1);
        } else {
            final int lastSlash = iri.lastIndexOf('/');
            if (lastSlash != -1) {
                namespace = iri.substring(0, lastSlash + 1);
                qname = iri.substring(lastSlash + 1);
            } else {
                qname = "";
            }
        }
        return ofNullable(namespace).flatMap(namespaceService::getPrefix).map(pre -> pre + ":" + qname)
            .orElse(iri);
    }

}
