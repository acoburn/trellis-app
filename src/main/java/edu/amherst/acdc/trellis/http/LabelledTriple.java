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

import static java.util.Objects.nonNull;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.Triple;

/**
 * @author acoburn
 */
class LabelledTriple {

    private final Triple triple;
    private final String predLabel;
    private final String objLabel;

    public LabelledTriple(final Triple triple, final String predicate, final String object) {
        this.triple = triple;
        this.predLabel = predicate;
        this.objLabel = object;
    }

    public String getSubject() {
        if (triple.getObject() instanceof IRI) {
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
