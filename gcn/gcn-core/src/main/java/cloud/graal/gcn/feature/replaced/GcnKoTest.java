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

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.model.GcnCloud;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.feature.test.KoTest;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.NONE;
import static io.micronaut.starter.options.BuildTool.MAVEN;

/**
 * Replaces the default KoTest feature.
 *
 * @since 1.0.0
 */
@Replaces(KoTest.class)
@Singleton
public class GcnKoTest extends KoTest {

    // mirrored from base class for the UI
    protected static final String ARTIFACT_ID_KOTEST_RUNNER_JUNIT_5_JVM = KoTest.ARTIFACT_ID_KOTEST_RUNNER_JUNIT_5_JVM;
    protected static final String ARTIFACT_ID_KOTEST_ASSERTIONS_CORE_JVM = KoTest.ARTIFACT_ID_KOTEST_ASSERTIONS_CORE_JVM;
    protected static final Dependency DEPENDENCY_MICRONAUT_TEST_KOTEST = KoTest.DEPENDENCY_MICRONAUT_TEST_KOTEST;

    @Override
    public void doApply(GeneratorContext generatorContext) {

        GcnGeneratorContext context = (GcnGeneratorContext) generatorContext;
        GcnCloud cloud = context.getCloud();

        if (cloud != NONE || context.isPlatformIndependent()) {
            // Configure Kotest for everything but the LIB_MODULE

            // Note: GcnCloud.NONE here would be if a user selected a feature or a service but no cloud
            context.addUrlTemplate(cloud.getModuleName(),
                "koTestConfig" + cloud.getEnvironmentNameSuffix() + cloud.getModuleName(),
                "src/test/kotlin/io/kotest/provided/ProjectConfig.kt",
                "kotest/ProjectConfig.kt");

            // Gradle Plugin applies the KoTest dependencies
            if (context.getBuildTool() == MAVEN) {
                context.addDependency(DEPENDENCY_MICRONAUT_TEST_KOTEST);
                context.addDependency(Dependency.builder()
                        .lookupArtifactId(ARTIFACT_ID_KOTEST_RUNNER_JUNIT_5_JVM)
                        .test());
                context.addDependency(Dependency.builder()
                        .lookupArtifactId(ARTIFACT_ID_KOTEST_ASSERTIONS_CORE_JVM)
                        .test());
            }
        } else if (context.getBuildTool() == MAVEN) {
            // Micronaut Starter auto adds the 'Mockk' dependency if Maven, Kotlin and KoTest are selected
            // so remove it from the lib/pom.xml
            context.getLibDependencies().removeIf(it -> "mockk".equals(it.getArtifactId()));
        }
    }
}
