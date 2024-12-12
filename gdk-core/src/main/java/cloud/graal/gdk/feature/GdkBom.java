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
package cloud.graal.gdk.feature;

import cloud.graal.gdk.GdkUtils;
import cloud.graal.gdk.feature.create.GdkRepository;
import cloud.graal.gdk.feature.create.GdkStageRepository;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.MavenLocal;
import io.micronaut.starter.build.Repository;
import io.micronaut.starter.build.RequiresRepository;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.build.gradle.GradlePlugin;
import io.micronaut.starter.feature.DefaultFeature;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.options.Options;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Adds the GDK BOM to Gradle and Maven builds.
 *
 * @since 1.0.0
 */
@Singleton
public class GdkBom implements DefaultFeature, RequiresRepository {

    private static final Dependency BOM = Dependency.builder()
            .groupId("cloud.graal.gdk")
            .artifactId("gdk-bom")
            .version(GdkUtils.getGdkBomVersion())
            .pom()
            .compile()
            .build();
    private static final GradlePlugin BOM_PLUGIN = GradlePlugin.builder().id("cloud.graal.gdk.gdk-bom").build();

    @Override
    public void apply(GeneratorContext generatorContext) {
        if (generatorContext.getBuildTool().isGradle()) {
            generatorContext.addDependency(BOM);
            generatorContext.addBuildPlugin(BOM_PLUGIN);
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-bom";
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

    @NonNull
    @Override
    public List<Repository> getRepositories() {
        List<Repository> result = new ArrayList<>();

        if (!StringUtils.isEmpty(GdkUtils.getenv("USE_MAVEN_LOCAL"))) {
            result.add(new MavenLocal());
        }
        if (!StringUtils.isEmpty(GdkUtils.getenv("STAGE_URL"))) {
            result.add(new GdkStageRepository());
        } else {
            result.add(new GdkRepository());
        }
        return result;
    }
}
