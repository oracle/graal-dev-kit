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
package cloud.graal.gdk.feature.replaced;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.buildtool.GdkGradleBuild;
import cloud.graal.gdk.feature.create.GdkGradleBuildCreator;
import cloud.graal.gdk.feature.create.gatewayfunction.AbstractGdkCloudGatewayFunction;
import cloud.graal.gdk.feature.create.template.BuildSrcBuildGradle;
import cloud.graal.gdk.feature.create.template.EnforceVersionsGroovy;
import cloud.graal.gdk.feature.create.template.EnforceVersionsKotlin;
import cloud.graal.gdk.feature.replaced.template.LibBuildGradle;
import cloud.graal.gdk.feature.replaced.template.LibMicronautGradle;
import cloud.graal.gdk.feature.replaced.template.gdkGradlePluginJTE;
import cloud.graal.gdk.model.GdkCloud;
import cloud.graal.gdk.template.BuildGradlePostProcessor;
import com.fizzed.rocker.RockerModel;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.BuildPlugin;
import io.micronaut.starter.build.Repository;
import io.micronaut.starter.build.RepositoryResolver;
import io.micronaut.starter.build.dependencies.CoordinateResolver;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.build.gradle.GradleBuild;
import io.micronaut.starter.build.gradle.GradleDependency;
import io.micronaut.starter.build.gradle.GradleDsl;
import io.micronaut.starter.build.gradle.GradlePlugin;
import io.micronaut.starter.build.gradle.GradleRepository;
import io.micronaut.starter.feature.build.gradle.Gradle;
import io.micronaut.starter.feature.build.gradle.MicronautApplicationGradlePlugin;
import io.micronaut.starter.feature.build.gradle.templates.buildGradle;
import io.micronaut.starter.feature.build.gradle.templates.micronautGradle;
import io.micronaut.starter.feature.function.azure.template.azurefunctions;
import io.micronaut.starter.template.RockerTemplate;
import io.micronaut.starter.template.RockerWritable;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static cloud.graal.gdk.GdkGeneratorContext.PLUGIN_SHADOW;
import static cloud.graal.gdk.GdkUtils.LIB_MODULE;
import static cloud.graal.gdk.GdkUtils.USE_GRADLE_VERSION_CATALOG;
import static cloud.graal.gdk.model.GdkCloud.NONE;
import static io.micronaut.starter.build.gradle.GradleDsl.GROOVY;
import static io.micronaut.starter.build.gradle.GradleDsl.KOTLIN;
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
public class GdkGradle extends Gradle {

    private static final String ARTIFACT_ID = "micronaut-gradle-plugin";
    private static final String PLUGIN_TEST_RESOURCES = "io.micronaut.test-resources";

    /**
     * The plugin id for the Micronaut application plugin.
     */
    private static final String APPLICATION_PLUGIN_ID = MicronautApplicationGradlePlugin.Builder.APPLICATION;
    /**
     * The plugin id for the Micronaut library plugin.
     */
    private static final String LIBRARY_PLUGIN_ID = MicronautApplicationGradlePlugin.Builder.LIBRARY;
    /**
     * The plugin id for the JTE plugin.
     */
    private static final String JTE_PLUGIN_ID = "gg.jte.gradle";

    /**
     * The plugin id for the AZURE functions plugin.
     */
    private static final String AZURE_FUNCTIONS_PLUGIN = "com.microsoft.azure.azurefunctions";

    private static final GradlePlugin GROOVY_PLUGIN = GradlePlugin.builder().id("groovy").build();

    private final CoordinateResolver coordinateResolver;
    private final RepositoryResolver repositoryResolver;
    private final GdkGradleBuildCreator gradleBuildCreator;

    /**
     * @param gradleBuildCreator GradleBuildCreator bean
     * @param coordinateResolver CoordinateResolver bean
     * @param repositoryResolver RepositoryResolver bean
     */
    public GdkGradle(GdkGradleBuildCreator gradleBuildCreator,
                     CoordinateResolver coordinateResolver,
                     RepositoryResolver repositoryResolver) {
        super(gradleBuildCreator, repositoryResolver);
        this.coordinateResolver = coordinateResolver;
        this.repositoryResolver = repositoryResolver;
        this.gradleBuildCreator = gradleBuildCreator;
    }

    @Override
    protected RockerModel buildFile(GeneratorContext generatorContext,
                                    GradleBuild build) {

        if (((GdkGeneratorContext) generatorContext).isPlatformIndependent()) {
            return super.buildFile(generatorContext, build);
        }

        return LibBuildGradle.template(
                generatorContext.getApplicationType(),
                generatorContext.getProject(),
                generatorContext.getFeatures(),
                build);
    }

    @Override
    protected GradleBuild createBuild(GeneratorContext gc) {

        GdkGeneratorContext generatorContext = (GdkGeneratorContext) gc;

        List<Repository> repositories = repositoryResolver.resolveRepositories(gc);

        addBuildSrc(generatorContext);

        if (generatorContext.isPlatformIndependent()) {
            return dependencyResolver.create(generatorContext, repositories, DEFAULT_USER_VERSION_CATALOGUE);
        }

        GradleBuild original = dependencyResolver.create(generatorContext, repositories, DEFAULT_USER_VERSION_CATALOGUE);

        List<GradleRepository> gradleRepositories = GradleRepository.listOf(original.getDsl(), repositories);

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

        return new GdkGradleBuild(original.getDsl(),
                original.getDependencies(), plugins, gradleRepositories);
    }

    // don't delete - this is needed for web image generation
    @Override
    protected void addGradleInitFiles(GeneratorContext generatorContext) {
        super.addGradleInitFiles(generatorContext);
    }

    private void addBuildSrc(GdkGeneratorContext generatorContext) {

        GradleDsl dsl = generatorContext
                .getBuildTool()
                .getGradleDsl()
                .orElse(GROOVY);
        boolean kotlin = dsl == KOTLIN;
        String ext = kotlin ? ".kts" : "";

        String path = "buildSrc/build.gradle";
        generatorContext.addTemplate(path + ext, new RockerTemplate(ROOT, path + ext,
                BuildSrcBuildGradle.template(generatorContext, kotlin)));

        path = "buildSrc/src/main/" + (kotlin ? "kotlin" : "groovy") + "/cloud.graal.gdk.gdk-bom.gradle" + ext;
        generatorContext.addTemplate("gdk-bom-plugin", new RockerTemplate(ROOT, path,
                kotlin ? EnforceVersionsKotlin.template() : EnforceVersionsGroovy.template()));
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

    @Override
    public void apply(GeneratorContext generatorContext) {
        super.apply(generatorContext);
        addGradleBuild((GdkGeneratorContext) generatorContext);
    }

    private void addGradleBuild(GdkGeneratorContext generatorContext) {

        GradleDsl dsl = generatorContext.getBuildTool().getGradleDsl().orElse(GradleDsl.GROOVY);

        GdkCloud reset = generatorContext.getCloud();

        generatorContext.getClouds().forEach(x -> addCloudGradleBuild(generatorContext, dsl, x));

        generatorContext.setCloud(reset);

        // for lib/build.gradle
        generatorContext.addPostProcessor("build", new BuildGradlePostProcessor(dsl, false, generatorContext.getFeature(AbstractGdkCloudGatewayFunction.class).orElse(null) != null, generatorContext.getApplicationType()));
    }

    private void addCloudGradleBuild(GdkGeneratorContext generatorContext, GradleDsl dsl,
                                     GdkCloud cloud) {

        if (cloud == NONE) {
            return;
        }

        generatorContext.setCloud(cloud);

        generatorContext.addDependency(Dependency.builder()
                .artifactId(LIB_MODULE + "-reference")
                .groupId("")
                .compile()
                .build());

        if (generatorContext.getFeatures().language().isGroovy() || generatorContext.getFeatures().testFramework().isSpock()) {
            generatorContext.addBuildPlugin(GROOVY_PLUGIN);
        }
        List<GradlePlugin> copiedPlugins = new ArrayList<>();

        List<GradlePlugin> plugins = generatorContext.getBuildPlugins()
                .stream()
                .filter(GradlePlugin.class::isInstance)
                .map(GradlePlugin.class::cast)
                .sorted(OrderUtil.COMPARATOR)
                .toList();

        for (BuildPlugin p : plugins) {
            GradlePlugin plugin = (GradlePlugin) p;
            if (plugin.getExtension() == null && plugin.getSettingsExtension() == null) {
                // ok to reuse since it has no Rocker templates
                copiedPlugins.add(plugin);
            } else {
                String id = plugin.getId();
                if (APPLICATION_PLUGIN_ID.equals(id) || LIBRARY_PLUGIN_ID.equals(id)) {
                    copiedPlugins.add(cloneMicronautPlugin(plugin, generatorContext));
                } else if (JTE_PLUGIN_ID.equals(id)) {
                    copiedPlugins.add(cloneJtePlugin(plugin));
                } else if (AZURE_FUNCTIONS_PLUGIN.equals(id)) {
                    copiedPlugins.add(cloneAzureFunctionsPlugin(plugin, generatorContext));
                } else {
                    throw new IllegalStateException("Unknown build plugin '" + id + "'");
                }
            }
        }

        List<Repository> repositories = repositoryResolver.resolveRepositories(generatorContext);

        GradleBuild original = gradleBuildCreator.create(generatorContext, repositories, USE_GRADLE_VERSION_CATALOG);

        List<GradleDependency> dependencies = GradleDependency.listOf(generatorContext, USE_GRADLE_VERSION_CATALOG);

        GradleBuild build = new GdkGradleBuild(
                original.getDsl(), dependencies, copiedPlugins,
                GradleRepository.listOf(dsl, repositories));

        String templateKey = "build-" + cloud.getModuleName();
        generatorContext.addTemplate(templateKey, new RockerTemplate(cloud.getModuleName(), generatorContext.getBuildTool().getBuildFileName(),
                buildGradle.template(
                        generatorContext.getApplicationType(),
                        generatorContext.getProject(),
                        generatorContext.getFeatures(cloud),
                        build)
        ));

        generatorContext.addPostProcessor(templateKey, new BuildGradlePostProcessor(dsl, true, generatorContext.getFeature(AbstractGdkCloudGatewayFunction.class).orElse(null) != null, generatorContext.getApplicationType()));

    }

    private GradlePlugin cloneJtePlugin(GradlePlugin plugin) {
        gdkGradlePluginJTE extensionModel = (gdkGradlePluginJTE) ((RockerWritable) plugin.getExtension()).getModel();

        return new GradlePlugin(
                plugin.getGradleFile(),
                plugin.getId(),
                plugin.getVersion(),
                null,
                new RockerWritable(gdkGradlePluginJTE.template(
                        true,
                        extensionModel.dsl(),
                        extensionModel.path())),
                null,
                plugin.getPluginsManagementRepositories(),
                false,
                plugin.getOrder(),
                plugin.getBuildImports(),
                plugin.getSettingsImports());
    }

    private GradlePlugin cloneMicronautPlugin(GradlePlugin plugin,
                                              GdkGeneratorContext generatorContext) {

        micronautGradle extensionModel = (micronautGradle) ((RockerWritable) plugin.getExtension()).getModel();
        return new GradlePlugin(
                plugin.getGradleFile(),
                plugin.getId(),
                plugin.getVersion(),
                null,
                new RockerWritable(micronautGradle.template(
                        extensionModel.dsl(),
                        extensionModel.build(),
                        extensionModel.javaVersion(),
                        extensionModel.dockerfile(),
                        extensionModel.dockerfileNative(),
                        extensionModel.dockerBuilderImages(),
                        extensionModel.dockerBuilderNativeImages(),
                        extensionModel.runtime(),
                        extensionModel.testRuntime(),
                        extensionModel.aotVersion(),
                        extensionModel.incremental(),
                        generatorContext.getProject().getPackageName(),
                        extensionModel.additionalTestResourceModules(),
                        extensionModel.sharedTestResources(),
                        extensionModel.aotKeys(),
                        extensionModel.lambdaRuntimeMainClass(),
                        extensionModel.ignoredAutomaticDependencies())),
                null,
                plugin.getPluginsManagementRepositories(),
                false,
                plugin.getOrder(),
                plugin.getBuildImports(),
                plugin.getSettingsImports());
    }

    private GradlePlugin cloneAzureFunctionsPlugin(GradlePlugin plugin, GeneratorContext generatorContext) {


        azurefunctions extensionModel = (azurefunctions) ((RockerWritable) plugin.getExtension()).getModel();

        return new GradlePlugin(
                plugin.getGradleFile(),
                plugin.getId(),
                plugin.getVersion(),
                null,
                new RockerWritable(azurefunctions.template(
                        generatorContext.getProject(),
                        extensionModel.dsl(),
                        generatorContext.getJdkVersion().asString())
                ),
                null,
                plugin.getPluginsManagementRepositories(),
                false,
                plugin.getOrder(),
                plugin.getBuildImports(),
                plugin.getSettingsImports());
    }
}
