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
package cloud.graal.gcn.feature;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.template.TemplatePostProcessor;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.DefaultFeature;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.options.Options;
import io.micronaut.starter.template.RockerTemplate;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.micronaut.starter.template.Template.ROOT;

/**
 * Adds a LICENSE file and post-processes Java/Groovy/Kotlin source and
 * build.gradle, settings.gradle, pom.xml files to add a license header.
 *
 * @since 1.0.0
 */
@Singleton
public class License implements DefaultFeature {

    private static final List<String> HEADER_LINES = List.of(
            "Copyright (c) 2023, Oracle.",
            "Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl."
    );

    @Override
    public void apply(GeneratorContext gc) {

        GcnGeneratorContext generatorContext = (GcnGeneratorContext) gc;

        generatorContext.addTemplate("license",
                new RockerTemplate(ROOT, "LICENSE", UplLicense.template()));

        generatorContext.addPostProcessor(PomLicensePostProcessor.PATTERN_GRADLE, SrcLicensePostProcessor.INSTANCE);
        generatorContext.addPostProcessor(PomLicensePostProcessor.PATTERN_POM, PomLicensePostProcessor.INSTANCE);
        generatorContext.addPostProcessor(SrcLicensePostProcessor.PATTERN_SRC, SrcLicensePostProcessor.INSTANCE);
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-license";
    }

    @Override
    public boolean shouldApply(ApplicationType applicationType, Options options, Set<Feature> selectedFeatures) {
        return true;
    }

    @Override
    public boolean supports(ApplicationType applicationType) {
        return true;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    public static class PomLicensePostProcessor implements TemplatePostProcessor {

        public static final PomLicensePostProcessor INSTANCE = new PomLicensePostProcessor();
        public static final Pattern PATTERN_GRADLE = Pattern.compile(".+\\.gradle");
        public static final Pattern PATTERN_POM = Pattern.compile(".*pom\\.xml");

        private static final String PROJECT_START = "<project ";
        private static final String HEADER = "<!--\n" + String.join("\n", HEADER_LINES) + "\n-->\n";

        @NonNull
        @Override
        public String process(@NonNull String pom) {
            return pom.replace(PROJECT_START, HEADER + PROJECT_START);
        }
    }

    public static class SrcLicensePostProcessor implements TemplatePostProcessor {

        public static final SrcLicensePostProcessor INSTANCE = new SrcLicensePostProcessor();
        public static final Pattern PATTERN_SRC = Pattern.compile(".*src/(main|test)/(java|groovy|kotlin)/.+\\.(java|groovy|kt)");

        private static final String HEADER = HEADER_LINES
                .stream()
                .map(line -> "// " + line)
                .collect(Collectors.joining("\n"));

        @NonNull
        @Override
        public String process(@NonNull String source) {
            return HEADER + "\n" + source;
        }
    }
}
