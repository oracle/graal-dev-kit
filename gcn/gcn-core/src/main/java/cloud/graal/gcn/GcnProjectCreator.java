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
package cloud.graal.gcn;

import cloud.graal.gcn.feature.create.AbstractGcnCreateFeature;
import cloud.graal.gcn.feature.service.AbstractGcnServiceFeature;
import cloud.graal.gcn.model.GcnCloud;
import cloud.graal.gcn.model.GcnProjectType;
import cloud.graal.gcn.model.GcnService;
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
import io.micronaut.starter.util.VersionInfo;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cloud.graal.gcn.model.GcnCloud.NONE;

/**
 * Creates GCN projects. Auto-adds "create" and "service" features based on the
 * selected clouds and services.
 */
@Singleton
public class GcnProjectCreator {

    private final GcnProjectGenerator projectGenerator;
    private final Collection<AbstractGcnCreateFeature> createFeatures;
    private final Collection<AbstractGcnServiceFeature> serviceFeatures;

    GcnProjectCreator(GcnProjectGenerator projectGenerator,
                      Collection<AbstractGcnCreateFeature> createFeatures,
                      Collection<AbstractGcnServiceFeature> serviceFeatures) {
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
     * @param javaVersion       selected (or default) JDK version
     * @param operatingSystem   inferred OS
     * @param additionalOptions currently only the "example code" boolean
     * @param outputHandler     destination for rendered templates
     * @param consoleOutput     destination for messages and errors
     * @throws Exception if something goes wrong
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    public void create(GcnProjectType projectType,
                       Project project,
                       Language lang,
                       TestFramework test,
                       BuildTool build,
                       List<GcnCloud> clouds,
                       List<GcnService> services,
                       List<String> features,
                       Integer javaVersion,
                       OperatingSystem operatingSystem,
                       Map<String, Object> additionalOptions,
                       OutputHandler outputHandler,
                       ConsoleOutput consoleOutput) throws Exception {

        if (clouds.isEmpty()) {
            // platform independent, will generate a single-module application
            clouds = List.of(NONE);
        }

        if (lang == null) {
            lang = Language.DEFAULT_OPTION;
        }

        if (test == null) {
            test = lang.getDefaults().getTest();
        }

        if (features.size() == 1 && features.get(0).length() == 0) {
            // CLI specified "--features=", which sets the value to an empty string
            features.remove(0);
        }

        JdkVersion jdkVersion = javaVersion == null ? VersionInfo.getJavaVersion() : JdkVersion.valueOf(javaVersion);
        Options options = new Options(lang, test, build, jdkVersion, additionalOptions);
        projectGenerator.generate(projectType.applicationType(), project, options, operatingSystem,
                derivedFeatures(clouds, services, features, projectType), outputHandler, consoleOutput);
    }

    private List<String> derivedFeatures(List<GcnCloud> clouds,
                                         List<GcnService> services,
                                         List<String> features,
                                         GcnProjectType projectType) {

        List<String> derived = new ArrayList<>(features);

        derived.addAll(createFeatures.stream()
                .filter(f -> f.getProjectType() == projectType)
                .filter(f -> clouds.contains(f.getCloud()))
                .map(Feature::getName)
                .collect(Collectors.toList()));

        derived.addAll(serviceFeatures.stream()
                .filter(f -> clouds.contains(f.getCloud()))
                .filter(f -> services.contains(f.getService()))
                .map(Feature::getName)
                .collect(Collectors.toList()));

        return derived;
    }
}
