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

import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.feature.DefaultFeature;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.options.Options;
import jakarta.inject.Singleton;

import java.util.Set;

/**
 * Adds the GCN BOM to Gradle and Maven builds.
 *
 * @since 1.0.0
 */
@Singleton
public class GcnBom implements DefaultFeature {

    private static final Dependency.Builder DEPENDENCY_BUILDER = Dependency.builder()
            .groupId("cloud.graal.gcn")
            .artifactId("gcn-bom")
            .version("1.0")
            .pom();
    private static final Dependency COMPILE_DEPENDENCY = DEPENDENCY_BUILDER.compile().build();

    @Override
    public void apply(GeneratorContext generatorContext) {
        generatorContext.addDependency(COMPILE_DEPENDENCY);
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
}
