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
package cloud.graal.gdk.feature.misc;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.misc.template.ApacheLicense;
import cloud.graal.gdk.feature.misc.template.ApacheNotice;
import cloud.graal.gdk.template.TemplatePostProcessor;
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

import static cloud.graal.gdk.feature.misc.License.PomLicensePostProcessor.PATTERN_POM;
import static cloud.graal.gdk.feature.misc.License.SrcLicensePostProcessor.PATTERN_GRADLE;
import static cloud.graal.gdk.feature.misc.License.SrcLicensePostProcessor.PATTERN_SRC;
import static io.micronaut.starter.template.Template.ROOT;

/**
 * Adds LICENSE and NOTICE files, and post-processes Java/Groovy/Kotlin source
 * and build.gradle, settings.gradle, pom.xml files to add a license header.
 *
 * @since 1.0.0
 */
@Singleton
public class License implements DefaultFeature {

    private static final List<String> HEADER_LINES = List.of(
            "Copyright 2025 Oracle and/or its affiliates",
            "",
            "Licensed under the Apache License, Version 2.0 (the \"License\");",
            "you may not use this file except in compliance with the License.",
            "You may obtain a copy of the License at",
            "",
            "    https://www.apache.org/licenses/LICENSE-2.0",
            "",
            "Unless required by applicable law or agreed to in writing, software",
            "distributed under the License is distributed on an \"AS IS\" BASIS,",
            "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.",
            "See the License for the specific language governing permissions and",
            "limitations under the License."
    );

    @Override
    public void apply(GeneratorContext gc) {

        GdkGeneratorContext generatorContext = (GdkGeneratorContext) gc;

        generatorContext.addTemplate("license",
                new RockerTemplate(ROOT, "LICENSE", ApacheLicense.template()));
        generatorContext.addTemplate("notice",
                new RockerTemplate(ROOT, "NOTICE", ApacheNotice.template()));

        generatorContext.addPostProcessor(PATTERN_GRADLE, SrcLicensePostProcessor.INSTANCE);
        generatorContext.addPostProcessor(PATTERN_POM, PomLicensePostProcessor.INSTANCE);
        generatorContext.addPostProcessor(PATTERN_SRC, SrcLicensePostProcessor.INSTANCE);
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-license";
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

    /**
     * Post-processor for pom.xml files.
     */
    public static class PomLicensePostProcessor implements TemplatePostProcessor {

        /**
         * The singleton instance.
         */
        public static final PomLicensePostProcessor INSTANCE = new PomLicensePostProcessor();

        /**
         * Regex for pom.xml files.
         */
        public static final Pattern PATTERN_POM = Pattern.compile(".*pom\\.xml");

        private static final String PROJECT_START = "<project ";
        private static final String HEADER = "<!--\n" + String.join("\n", HEADER_LINES) + "\n-->\n";

        @NonNull
        @Override
        public String process(@NonNull String pom) {
            return pom.replace(PROJECT_START, HEADER + PROJECT_START);
        }
    }

    /**
     * Post-processor for source files and Gradle build files.
     */
    public static class SrcLicensePostProcessor implements TemplatePostProcessor {

        /**
         * The singleton instance.
         */
        public static final SrcLicensePostProcessor INSTANCE = new SrcLicensePostProcessor();

        /**
         * Regex for Java/Groovy/Kotlin source files.
         */
        public static final Pattern PATTERN_SRC = Pattern.compile(".*src/(main|test)/(java|groovy|kotlin)/.+\\.(java|groovy|kt)");

        /**
         * Regex for Gradle build files.
         */
        public static final Pattern PATTERN_GRADLE = Pattern.compile(".+\\.gradle");

        private static final String HEADER = "/*\n" + HEADER_LINES
                .stream()
                .map(line -> " * " + line)
                .collect(Collectors.joining("\n")) +
                "\n */\n";

        @NonNull
        @Override
        public String process(@NonNull String source) {
            return HEADER + "\n" + source;
        }
    }
}
