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

import cloud.graal.gcn.feature.create.GcnRepository;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.Repository;
import io.micronaut.starter.build.RequiresRepository;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.build.gradle.GradlePlugin;
import io.micronaut.starter.feature.DefaultFeature;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.options.Options;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Set;

/**
 * Adds the GCN BOM to Gradle and Maven builds.
 *
 * @since 1.0.0
 */
@Singleton
public class GcnBom implements DefaultFeature, RequiresRepository {

    private static final String BOM_VERSION = "4.2.1.3";
    private static final Dependency BOM = Dependency.builder()
            .groupId("cloud.graal.gcn")
            .artifactId("gcn-bom")
            .version(BOM_VERSION)
            .pom()
            .compile()
            .build();

    private static final GradlePlugin BOM_PLUGIN = GradlePlugin.builder().id("cloud.graal.gcn.gcn-bom").build();

    @Override
    public void apply(GeneratorContext generatorContext) {
        generatorContext.addDependency(BOM);

        if (generatorContext.getBuildTool().isGradle()) {
            generatorContext.addBuildPlugin(BOM_PLUGIN);
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-bom";
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

    @Override
    public @NonNull List<Repository> getRepositories() {
        return List.of(new GcnRepository());
    }
}
