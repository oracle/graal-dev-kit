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
package cloud.graal.gcn.feature.create;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.buildtool.GcnGradleBuild;
import cloud.graal.gcn.buildtool.GradleProjectDependency;
import cloud.graal.gcn.buildtool.MavenProjectDependency;
import cloud.graal.gcn.feature.AbstractGcnFeature;
import cloud.graal.gcn.feature.create.gatewayfunction.AbstractGcnCloudGatewayFunction;
import cloud.graal.gcn.model.GcnCloud;
import cloud.graal.gcn.model.GcnProjectType;
import cloud.graal.gcn.template.BuildGradlePostProcessor;
import cloud.graal.gcn.template.GcnYamlTemplate;
import cloud.graal.gcn.template.MavenPomPostProcessor;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.build.BuildPlugin;
import io.micronaut.starter.build.Repository;
import io.micronaut.starter.build.gradle.GradleBuild;
import io.micronaut.starter.build.gradle.GradleDependency;
import io.micronaut.starter.build.gradle.GradleDsl;
import io.micronaut.starter.build.gradle.GradlePlugin;
import io.micronaut.starter.build.gradle.GradleRepository;
import io.micronaut.starter.build.maven.MavenBuild;
import io.micronaut.starter.feature.Features;
import io.micronaut.starter.feature.build.gradle.MicronautApplicationGradlePlugin;
import io.micronaut.starter.feature.build.gradle.templates.buildGradle;
import io.micronaut.starter.feature.build.gradle.templates.micronautGradle;
import io.micronaut.starter.feature.build.maven.templates.multimodule;
import io.micronaut.starter.feature.build.maven.templates.pom;
import io.micronaut.starter.feature.config.Configuration;
import io.micronaut.starter.feature.database.TransactionalNotSupported;
import io.micronaut.starter.feature.test.template.groovyJunit;
import io.micronaut.starter.feature.test.template.javaJunit;
import io.micronaut.starter.feature.test.template.koTest;
import io.micronaut.starter.feature.test.template.kotlinJunit;
import io.micronaut.starter.feature.test.template.spock;
import io.micronaut.starter.template.RockerTemplate;
import io.micronaut.starter.template.RockerWritable;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cloud.graal.gcn.GcnUtils.LIB_MODULE;
import static cloud.graal.gcn.GcnUtils.USE_GRADLE_VERSION_CATALOG;
import static io.micronaut.starter.application.ApplicationType.FUNCTION;
import static io.micronaut.starter.feature.FeaturePhase.HIGHEST;
import static io.micronaut.starter.options.Language.GROOVY;
import static io.micronaut.starter.options.Language.JAVA;
import static io.micronaut.starter.options.Language.KOTLIN;
import static io.micronaut.starter.template.Template.ROOT;

/**
 * Abstract base class for "create" features. For each user-selected cloud, the
 * feature for the specified project type will be selected, e.g.,
 * GcnAwsCloudApp for create-app + AWS, GcnAzureCloudFunction for create-function + Azure,
 * GcnOciCloudGatewayFunction for create-gateway-function + OCI, etc.
 *
 * @since 1.0.0
 */
public abstract class AbstractGcnCreateFeature extends AbstractGcnFeature {

    /**
     * The plugin id for the Micronaut application plugin.
     */
    protected static final String APPLICATION_PLUGIN_ID = MicronautApplicationGradlePlugin.Builder.APPLICATION;
    /**
     * The plugin id for the Micronaut library plugin.
     */
    protected static final String LIBRARY_PLUGIN_ID = MicronautApplicationGradlePlugin.Builder.LIBRARY;

    private static final GradlePlugin GROOVY_PLUGIN = GradlePlugin.builder().id("groovy").build();

    // TODO convert these 2 to use constructor injection - currently failing in tests with
    //
    // io.micronaut.context.exceptions.BeanInstantiationException: Error instantiating bean of type  [gcn.GcnProjectCreator]
    // Message: gcn.feature.create.function.AbstractGcnCloudFunction: method 'void <init>()' not found
    // Path Taken: FooSpec.setProjectCreator([GcnProjectCreator projectCreator]) --> new GcnProjectCreator(ProjectGenerator projectGenerator,[Collection createFeatures],Collection serviceFeatures)
    //    at app//io.micronaut.context.DefaultBeanContext.resolveByBeanFactory(DefaultBeanContext.java:2367)
    private GcnGradleBuildCreator gradleBuildCreator;
    private GcnMavenBuildCreator mavenBuildCreator;

    /**
     * Temporary dependency injection method for GradleBuildCreator.
     *
     * @param gradleBuildCreator GradleBuildCreator
     */
    @Inject
    public void setGradleBuildCreator(GcnGradleBuildCreator gradleBuildCreator) {
        this.gradleBuildCreator = gradleBuildCreator;
    }

    /**
     * Temporary dependency injection method for MavenBuildCreator.
     *
     * @param mavenBuildCreator MavenBuildCreator
     */
    @Inject
    public void setMavenBuildCreator(GcnMavenBuildCreator mavenBuildCreator) {
        this.mavenBuildCreator = mavenBuildCreator;
    }

    @Override
    public void apply(GcnGeneratorContext generatorContext) {

        if (generatorContext.getApplicationType() != FUNCTION) {
            createApplicationClass(generatorContext);
        }
        createApplicationCloudYml(generatorContext);
        createExtraCloudConfigYml(generatorContext);

        if (generatorContext.getBuildTool().isGradle()) {
            addGradleBuild(generatorContext);
        } else {
            addMavenBuild(generatorContext);
        }
    }

    private void createApplicationClass(GcnGeneratorContext generatorContext) {

        Project project = generatorContext.getProject();
        String path = generatorContext.getLanguage().getSourcePath("/" + project.getPackagePath() + "/Application");
        Features features = generatorContext.getFeatures(getCloud());

        generatorContext.addTemplate(getModuleName(), "application-" + getModuleName(), path,
                io.micronaut.starter.feature.lang.java.application.template(project, features, generatorContext.getApplicationRenderingContext(JAVA)),
                io.micronaut.starter.feature.lang.kotlin.application.template(project, features, generatorContext.getApplicationRenderingContext(KOTLIN)),
                io.micronaut.starter.feature.lang.groovy.application.template(project, features, generatorContext.getApplicationRenderingContext(GROOVY)));

        String testSourcePath = generatorContext.getTestSourcePath("/{packagePath}/{className}");
        boolean transactional = !generatorContext.getFeatures().hasFeature(TransactionalNotSupported.class);
        generatorContext.addTestTemplate(getModuleName(), "applicationTest-" + getModuleName(), testSourcePath,
                spock.template(project, transactional),
                javaJunit.template(project, transactional),
                groovyJunit.template(project, transactional),
                kotlinJunit.template(project, transactional),
                koTest.template(project, transactional));
    }

    private void createApplicationCloudYml(GcnGeneratorContext generatorContext) {

        generatorContext.addTemplate("application-yml-" + getModuleName(),
                new GcnYamlTemplate(
                        getModuleName(),
                        "src/main/resources/application" + getCloud().getEnvironmentNameSuffix() + ".yml",
                        generatorContext.getConfiguration(getCloud())));

        generatorContext.addTemplate("bootstrap-yml-" + getModuleName(),
                new GcnYamlTemplate(
                        getModuleName(),
                        "src/main/resources/bootstrap" + getCloud().getEnvironmentNameSuffix() + ".yml",
                        generatorContext.getBootstrapConfiguration(getCloud())));
    }

    private void addGradleBuild(GcnGeneratorContext generatorContext) {

        List<GradleDependency> dependencies = GradleDependency.listOf(generatorContext, USE_GRADLE_VERSION_CATALOG);
        dependencies.add(new GradleProjectDependency(LIB_MODULE, generatorContext));

        if (generatorContext.getFeatures().language().isGroovy() || generatorContext.getFeatures().testFramework().isSpock()) {
            generatorContext.addBuildPlugin(GROOVY_PLUGIN);
        }

        List<Repository> repositories = Repository.micronautRepositories();
        GradleBuild original = gradleBuildCreator.create(generatorContext, repositories, USE_GRADLE_VERSION_CATALOG);

        List<GradlePlugin> copiedPlugins = new ArrayList<>();
        for (BuildPlugin p : generatorContext.getBuildPlugins()) {
            GradlePlugin plugin = (GradlePlugin) p;
            if (plugin.getExtension() == null && plugin.getSettingsExtension() == null) {
                // ok to reuse since it has no Rocker templates
                copiedPlugins.add(plugin);
            } else {
                String id = plugin.getId();
                if (APPLICATION_PLUGIN_ID.equals(id) || LIBRARY_PLUGIN_ID.equals(id)) {
                    copiedPlugins.add(cloneMicronautPlugin(plugin, generatorContext));
                } else {
                    throw new IllegalStateException("Unknown build plugin '" + id + "'");
                }
            }
        }

        GradleDsl dsl = generatorContext.getBuildTool().getGradleDsl().orElse(GradleDsl.GROOVY);

        GradleBuild build = new GcnGradleBuild(
                original.getDsl(), dependencies, copiedPlugins,
                GradleRepository.listOf(dsl, repositories));

        String templateKey = "build-" + getModuleName();
        generatorContext.addTemplate(templateKey, new RockerTemplate(getModuleName(), generatorContext.getBuildTool().getBuildFileName(),
                buildGradle.template(
                        generatorContext.getApplicationType(),
                        generatorContext.getProject(),
                        generatorContext.getFeatures(getCloud()),
                        build)
        ));
        generatorContext.addPostProcessor(templateKey, new BuildGradlePostProcessor(dsl, true));

        // for lib/build.gradle
        generatorContext.addPostProcessor("build", new BuildGradlePostProcessor(dsl, false));
    }

    private GradlePlugin cloneMicronautPlugin(GradlePlugin plugin,
                                              GcnGeneratorContext generatorContext) {

        micronautGradle extensionModel = (micronautGradle) ((RockerWritable) plugin.getExtension()).getModel();

        return new GradlePlugin(
                plugin.getGradleFile(),
                plugin.getId(),
                plugin.getVersion(),
                null,
                new RockerWritable(micronautGradle.template(
                        extensionModel.dsl(),
                        extensionModel.build(),
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
                        extensionModel.sharedTestResources())),
                null,
                false,
                false,
                plugin.getOrder(),
                plugin.getBuildImports());
    }

    private void addMavenBuild(GcnGeneratorContext generatorContext) {

        MavenBuild mavenBuild = mavenBuildCreator.create(generatorContext);

        mavenBuild.getDependencies().add(new MavenProjectDependency(
                generatorContext.getProject().getPackageName(), LIB_MODULE));

        String templateKey = "mavenPom-" + getModuleName();
        generatorContext.addTemplate(templateKey, new RockerTemplate(getModuleName(), "pom.xml",
                pom.template(
                        generatorContext.getApplicationType(),
                        generatorContext.getProject(),
                        generatorContext.getFeatures(),
                        mavenBuild)
        ));
        generatorContext.addPostProcessor(templateKey, new MavenPomPostProcessor(
                generatorContext.getLibProject().getName(),
                generatorContext.getLibProject().getPackageName(),
                generatorContext.getApplicationType(),
                generatorContext.getFeature(AbstractGcnCloudGatewayFunction.class).orElse(null) != null,
                false));

        // this is called for each cloud, but the templates are stored in a Map, so it's idempotent
        generatorContext.addTemplate("multi-module-pom", new RockerTemplate(ROOT, generatorContext.getBuildTool().getBuildFileName(),
                multimodule.template(null,
                        generatorContext.getLibProject(),
                        generatorContext.getModuleNames())
        ));

        // this is called for each cloud, but the post processors are stored in a Set, so it's idempotent
        generatorContext.addPostProcessor("mavenPom", new MavenPomPostProcessor(
                generatorContext.getLibProject().getName(),
                generatorContext.getLibProject().getPackageName(),
                generatorContext.getApplicationType(),
                generatorContext.getFeature(AbstractGcnCloudGatewayFunction.class).orElse(null) != null,
                true));
    }

    private void createExtraCloudConfigYml(GcnGeneratorContext generatorContext) {
        for (Map.Entry<GcnCloud, Collection<Configuration>> e : generatorContext.getExtraConfigurations().entrySet()) {
            GcnCloud cloud = e.getKey();
            for (Configuration c : e.getValue()) {
                generatorContext.addTemplate(c.getTemplateKey() + '-' + cloud.getModuleName(),
                        new GcnYamlTemplate(cloud.getModuleName(), c.getFullPath("yml"), c));
            }
        }
    }

    /**
     * @return the project type enum
     */
    @NonNull
    public abstract GcnProjectType getProjectType();

    @Override
    public int getOrder() {
        return HIGHEST.getOrder();
    }
}
