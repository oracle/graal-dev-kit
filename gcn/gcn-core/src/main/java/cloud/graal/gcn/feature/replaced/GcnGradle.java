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
import cloud.graal.gcn.buildtool.GcnGradleBuild;
import cloud.graal.gcn.feature.create.GcnGradleBuildCreator;
import cloud.graal.gcn.feature.create.GcnRepository;
import cloud.graal.gcn.feature.replaced.template.LibBuildGradle;
import cloud.graal.gcn.feature.replaced.template.LibMicronautGradle;
import com.fizzed.rocker.RockerModel;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.Repository;
import io.micronaut.starter.build.dependencies.CoordinateResolver;
import io.micronaut.starter.build.gradle.GradleBuild;
import io.micronaut.starter.build.gradle.GradlePlugin;
import io.micronaut.starter.build.gradle.GradleRepository;
import io.micronaut.starter.feature.build.KotlinBuildPlugins;
import io.micronaut.starter.feature.build.MicronautBuildPlugin;
import io.micronaut.starter.feature.build.gradle.Gradle;
import io.micronaut.starter.template.BinaryTemplate;
import io.micronaut.starter.template.RockerWritable;
import io.micronaut.starter.template.URLTemplate;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static cloud.graal.gcn.GcnGeneratorContext.PLUGIN_SHADOW;
import static io.micronaut.starter.build.Repository.micronautRepositories;
import static io.micronaut.starter.build.gradle.GradleDsl.GROOVY;
import static io.micronaut.starter.feature.build.gradle.MicronautApplicationGradlePlugin.Builder.APPLICATION;
import static io.micronaut.starter.feature.build.gradle.MicronautApplicationGradlePlugin.Builder.LIBRARY;
import static io.micronaut.starter.template.Template.ROOT;

/**
 * Replaces the default feature to render lib/build.gradle(.kts) without application-related parts.
 *
 * @since 1.0.0
 */
@Replaces(Gradle.class)
@Singleton
public class GcnGradle extends Gradle {

    private static final String PLUGIN_TEST_RESOURCES = "io.micronaut.test-resources";
    private static final String ARTIFACT_ID = "micronaut-gradle-plugin";
    private static final String GCN_WRAPPER_JAR = WRAPPER_JAR.replaceFirst("gradle/", "gcn_gradle/");
    private static final String GCN_WRAPPER_PROPS = WRAPPER_PROPS.replaceFirst("gradle/", "gcn_gradle/");

    private final CoordinateResolver coordinateResolver;

    /**
     * @param gradleBuildCreator   GradleBuildCreator bean
     * @param micronautBuildPlugin MicronautBuildPlugin feature
     * @param kotlinBuildPlugins   KotlinBuildPlugins feature
     * @param coordinateResolver   CoordinateResolver bean
     */
    public GcnGradle(GcnGradleBuildCreator gradleBuildCreator,
                     MicronautBuildPlugin micronautBuildPlugin,
                     KotlinBuildPlugins kotlinBuildPlugins,
                     CoordinateResolver coordinateResolver) {
        super(gradleBuildCreator, micronautBuildPlugin, kotlinBuildPlugins);
        this.coordinateResolver = coordinateResolver;
    }

    @Override
    protected RockerModel buildFile(GeneratorContext generatorContext,
                                    GradleBuild build) {

        if (((GcnGeneratorContext) generatorContext).isPlatformIndependent()) {
            return super.buildFile(generatorContext, build);
        }

        return LibBuildGradle.template(
                generatorContext.getApplicationType(),
                generatorContext.getProject(),
                generatorContext.getFeatures(),
                build);
    }

    @Override
    protected GradleBuild createBuild(GeneratorContext generatorContext) {

        List<Repository> repositories = micronautRepositories();
        repositories.add(0, new GcnRepository());

        if (((GcnGeneratorContext) generatorContext).isPlatformIndependent()) {
            return dependencyResolver.create(generatorContext, repositories, Gradle.DEFAULT_USER_VERSION_CATALOGUE);
        }

        GradleBuild original = dependencyResolver.create(generatorContext, repositories, Gradle.DEFAULT_USER_VERSION_CATALOGUE);

        List<GradleRepository> gradleRepositories = GradleRepository.listOf(
                generatorContext.getBuildTool().getGradleDsl().orElse(GROOVY),
                repositories);

        List<GradlePlugin> plugins = new ArrayList<>();

        for (GradlePlugin p : original.getPlugins()) {

            if (PLUGIN_SHADOW.equals(p.getId()) || PLUGIN_TEST_RESOURCES.equals(p.getId())) {
                continue;
            }

            if (p.getId().equals(APPLICATION)) {
                p = (GradlePlugin) GradlePlugin.builder()
                        .id(LIBRARY)
                        .lookupArtifactId(ARTIFACT_ID)
                        .extension(new RockerWritable(LibMicronautGradle.template(generatorContext.getProject().getPackageName(), resolveTestRuntime(generatorContext).orElse(null))))
                        .build()
                        .resolved(coordinateResolver);
            }

            plugins.add(p);
        }

        return new GcnGradleBuild(original.getDsl(),
                original.getDependencies(), plugins, gradleRepositories);
    }

    // TODO this override is here to upgrade Gradle to v7.6 to add support for JDK 19;
    //      remove this and the resource files when we've upgrade to a version of Micronaut
    //      that uses Gradle 7.6+
    @Override
    protected void addGradleInitFiles(GeneratorContext generatorContext) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        generatorContext.addTemplate("gradleWrapperJar", new BinaryTemplate(ROOT, WRAPPER_JAR, classLoader.getResource(GCN_WRAPPER_JAR)));
        generatorContext.addTemplate("gradleWrapperProperties", new URLTemplate(ROOT, WRAPPER_PROPS, classLoader.getResource(GCN_WRAPPER_PROPS)));
        generatorContext.addTemplate("gradleWrapper", new URLTemplate(ROOT, "gradlew", classLoader.getResource("gcn_gradle/gradlew"), true));
        generatorContext.addTemplate("gradleWrapperBat", new URLTemplate(ROOT, "gradlew.bat", classLoader.getResource("gcn_gradle/gradlew.bat"), false));
    }

    private Optional<String> resolveTestRuntime(GeneratorContext generatorContext) {
        if (generatorContext.getFeatures().testFramework().isJunit()) {
            return Optional.of("junit5");
        }
        if (generatorContext.getFeatures().testFramework().isKotlinTestFramework()) {
            return Optional.of("kotest");
        }
        if (generatorContext.getFeatures().testFramework().isSpock()) {
            return Optional.of("spock2");
        }

        return Optional.empty();
    }
}
