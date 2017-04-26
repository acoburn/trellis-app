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

import io.dropwizard.views.View;

import java.util.List;

/**
 * @author acoburn
 */
class ResourceView extends View {

    private final List<LabelledTriple> triples;
    private final String title;
    private final String subject;

    public ResourceView(final String title, final String subject, final List<LabelledTriple> triples) {
        super("resource.mustache");
        this.triples = triples;
        this.title = title;
        this.subject = subject;
    }

    public List<LabelledTriple> getTriples() {
        return triples;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }
}
