/*
 * Copyright 2023 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.graal.gcn.feature.replaced;

import cloud.graal.gcn.feature.replaced.template.maindocs;
import cloud.graal.gcn.feature.replaced.template.readme;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.feature.other.Readme;
import io.micronaut.starter.template.DefaultTemplate;
import io.micronaut.starter.template.RockerWritable;
import io.micronaut.starter.template.Writable;
import jakarta.inject.Singleton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.micronaut.starter.template.Template.ROOT;

/**
 * Replaces the default feature to eliminate duplicates and do other processing.
 *
 * @since 1.0.0
 */
@Replaces(Readme.class)
@Singleton
public class GcnReadme extends Readme {

    private static final byte[] LINE_SEPARATOR = System.lineSeparator().getBytes(Charset.defaultCharset());

    @Override
    public void apply(GeneratorContext generatorContext) {

        List<Feature> featuresWithDocumentationLinks = generatorContext.getFeatures().getFeatures().stream()
                .filter(feature -> feature.getMicronautDocumentation() != null || feature.getThirdPartyDocumentation() != null)
                .toList();
        List<Writable> helpTemplates = generatorContext.getHelpTemplates();
        if (helpTemplates.isEmpty() && featuresWithDocumentationLinks.isEmpty()) {
            return;
        }

        // Remove the default micronaut readme template
        generatorContext.removeTemplate("readme");
        generatorContext.addTemplate("readme", new DefaultTemplate(ROOT, "README.md") {
            @Override
            public void write(OutputStream outputStream) throws IOException {
                new RockerWritable(maindocs.template()).write(outputStream);

                Set<String> renderedHelp = new HashSet<>();
                for (Writable writable : generatorContext.getHelpTemplates()) {
                    renderedHelp.add(render(writable));
                }
                renderSorted(renderedHelp, outputStream);

                Set<String> renderedLinks = new HashSet<>();
                for (Feature feature : featuresWithDocumentationLinks) {
                    renderedLinks.add(render(new RockerWritable(readme.template(feature))));
                }
                renderSorted(renderedLinks, outputStream);
            }
        });
    }

    private String render(Writable writable) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writable.write(baos);
        return baos.toString();
    }

    private void renderSorted(Set<String> rendered,
                              OutputStream outputStream) throws IOException {
        List<String> sorted = new ArrayList<>(rendered);
        Collections.sort(sorted);
        for (String help : sorted) {
            outputStream.write(help.getBytes());
            outputStream.write(LINE_SEPARATOR);
        }
    }
}
