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

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

/**
 * @author acoburn
 */
final class RdfMediaType {

    public static final String APPLICATION_LD_JSON = "application/ld+json";

    public static final MediaType APPLICATION_LD_JSON_TYPE = new MediaType("application", "ld+json");

    public static final String APPLICATION_N_TRIPLES = "application/n-triples";

    public static final MediaType APPLICATION_N_TRIPLES_TYPE = new MediaType("application", "n-triples");

    public static final String TEXT_TURTLE = "text/turtle;charset=utf-8";

    public static final MediaType TEXT_TURTLE_TYPE = new MediaType("text", "turtle", "utf-8");

    public static final List<Variant> VARIANTS = Variant.mediaTypes(TEXT_TURTLE_TYPE, APPLICATION_LD_JSON_TYPE,
                APPLICATION_N_TRIPLES_TYPE).build();

    private RdfMediaType() {
        // prevent instantiation
    }
}
