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
package cloud.graal.gdk;

import cloud.graal.gdk.feature.create.AbstractGdkCreateFeature;
import cloud.graal.gdk.feature.create.PlatformIndependent;
import cloud.graal.gdk.feature.service.AbstractGdkServiceFeature;
import cloud.graal.gdk.model.GdkCloud;
import cloud.graal.gdk.model.GdkProjectType;
import cloud.graal.gdk.model.GdkService;
import io.micronaut.starter.application.OperatingSystem;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.io.ConsoleOutput;
import io.micronaut.starter.io.OutputHandler;
import io.micronaut.starter.options.BuildTool;
import io.micronaut.starter.options.JdkVersion;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.options.Options;
import io.micronaut.starter.options.TestFramework;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cloud.graal.gdk.model.GdkCloud.NONE;
import static io.micronaut.starter.options.BuildTool.GRADLE;
import static io.micronaut.starter.options.BuildTool.GRADLE_KOTLIN;
import static io.micronaut.starter.options.Language.KOTLIN;

/**
 * Creates GDK projects. Auto-adds "create" and "service" features based on the
 * selected clouds and services.
 */
@Singleton
public class GdkProjectCreator {

    private final GdkProjectGenerator projectGenerator;
    private final Collection<AbstractGdkCreateFeature> createFeatures;
    private final Collection<AbstractGdkServiceFeature> serviceFeatures;

    GdkProjectCreator(GdkProjectGenerator projectGenerator,
                      Collection<AbstractGdkCreateFeature> createFeatures,
                      Collection<AbstractGdkServiceFeature> serviceFeatures) {
        this.projectGenerator = projectGenerator;
        this.createFeatures = createFeatures;
        this.serviceFeatures = serviceFeatures;
    }

    /**
     * @param projectType       project type
     * @param project           the project
     * @param lang              selected (or default) language
     * @param test              selected (or default) test framework
     * @param build             selected (or default) build tool
     * @param clouds            selected clouds
     * @param services          selected services
     * @param features          selected features
     * @param jdkMajorVersion   selected (or default) JDK version
     * @param operatingSystem   inferred OS
     * @param additionalOptions currently only the "example code" boolean
     * @param outputHandler     destination for rendered templates
     * @param consoleOutput     destination for messages and errors
     * @throws Exception if something goes wrong
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    public void create(GdkProjectType projectType,
                       Project project,
                       Language lang,
                       TestFramework test,
                       BuildTool build,
                       List<GdkCloud> clouds,
                       List<GdkService> services,
                       List<String> features,
                       Integer jdkMajorVersion,
                       OperatingSystem operatingSystem,
                       Map<String, Object> additionalOptions,
                       OutputHandler outputHandler,
                       ConsoleOutput consoleOutput) throws Exception {
        if (clouds.isEmpty()) {
            // platform independent, will generate a single-module application
            clouds = List.of(NONE);
        }

        if (features.size() == 1 && features.get(0).isEmpty()) {
            // CLI specified "--features=", which sets the value to an empty string
            features.remove(0);
        }

        if (clouds.size() == 1 && clouds.get(0) == NONE) {
            features = new ArrayList<>(features);
            features.add(PlatformIndependent.NAME);
        }

        if (lang == null) {
            lang = Language.DEFAULT_OPTION;
        }

        if (test == null) {
            test = lang.getDefaults().getTest();
        }

        if (build == null) {
            build = lang == KOTLIN ? GRADLE_KOTLIN : GRADLE; // Micronaut switched to Kotlin DSL, sticking with Groovy for now
        }

        Options options = new Options(lang, test, build, jdkVersion(jdkMajorVersion), additionalOptions);
        projectGenerator.generate(projectType.applicationType(), project, options, operatingSystem,
                derivedFeatures(clouds, services, features, projectType), outputHandler, consoleOutput);
    }

    private JdkVersion jdkVersion(Integer majorVersion) {

        if (majorVersion == null) {
            String property = System.getProperty("java.version");
            if (property.startsWith("1.")) {
                property = property.substring(2);
            }
            // Allow these formats:
            // 1.8.0_72-ea
            // 9-ea
            // 9
            // 9.0.1
            int dotPos = property.indexOf('.');
            int dashPos = property.indexOf('-');
            majorVersion = Integer.parseInt(property.substring(0, dotPos > -1 ? dotPos : dashPos > -1 ? dashPos : property.length()));
            return JdkVersion.valueOf(majorVersion);
        }
        return JdkVersion.valueOf(majorVersion);
    }

    private List<String> derivedFeatures(List<GdkCloud> clouds,
                                         List<GdkService> services,
                                         List<String> features,
                                         GdkProjectType projectType) {

        List<String> derived = new ArrayList<>(features);

        derived.addAll(createFeatures.stream()
                .filter(f -> f.getProjectType() == projectType)
                .filter(f -> clouds.contains(f.getCloud()))
                .map(Feature::getName)
                .toList());

        derived.addAll(serviceFeatures.stream()
                .filter(f -> clouds.contains(f.getCloud()))
                .filter(f -> services.contains(f.getService()))
                .map(Feature::getName)
                .toList());

        return derived;
    }
}
