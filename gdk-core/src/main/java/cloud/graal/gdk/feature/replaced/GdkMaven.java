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
import cloud.graal.gdk.feature.create.GdkMavenBuildCreator;
import cloud.graal.gdk.feature.create.gatewayfunction.AbstractGdkCloudGatewayFunction;
import cloud.graal.gdk.feature.replaced.template.LibPom;
import cloud.graal.gdk.model.GdkCloud;
import cloud.graal.gdk.template.MavenPlatformPostProcessor;
import cloud.graal.gdk.template.MavenPomPostProcessor;
import com.fizzed.rocker.RockerModel;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.Property;
import io.micronaut.starter.build.RepositoryResolver;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.build.maven.JvmArgumentsFeature;
import io.micronaut.starter.build.maven.MavenBuild;
import io.micronaut.starter.build.maven.MavenRepository;
import io.micronaut.starter.feature.build.maven.Maven;
import io.micronaut.starter.feature.build.maven.templates.multimodule;
import io.micronaut.starter.feature.build.maven.templates.pom;
import io.micronaut.starter.template.RockerTemplate;
import jakarta.inject.Singleton;

import java.util.ListIterator;

import static cloud.graal.gdk.GdkUtils.LIB_MODULE;
import static io.micronaut.starter.template.Template.ROOT;

/**
 * Replaces the default feature to render lib/pom.xml without application-related parts.
 *
 * @since 1.0.0
 */
@Replaces(Maven.class)
@Singleton
public class GdkMaven extends Maven {

    private static final String PROPERTY_MAINCLASS = "exec.mainClass";
    private static final String PROPERTY_RUNTIME = "micronaut.runtime";
    private static final String PROPERTY_TEST_RESOURCES = "micronaut.test.resources.enabled";

    private final GdkMavenBuildCreator mavenBuildCreator;

    public GdkMaven(GdkMavenBuildCreator mavenBuildCreator,
                    RepositoryResolver repositoryResolver) {
        super(mavenBuildCreator, repositoryResolver);
        this.mavenBuildCreator = mavenBuildCreator;
    }

    @Override
    protected MavenBuild createBuild(GeneratorContext generatorContext) {

        if (((GdkGeneratorContext) generatorContext).isPlatformIndependent()) {
            return super.createBuild(generatorContext);
        }

        MavenBuild build = super.createBuild(generatorContext);
        for (ListIterator<Property> iter = build.getProperties().listIterator(); iter.hasNext(); ) {
            Property property = iter.next();
            String key = property.getKey();
            if (PROPERTY_MAINCLASS.equals(key) || PROPERTY_RUNTIME.equals(key) || PROPERTY_TEST_RESOURCES.equals(key)) {
                iter.remove();
            }
        }
        return build;
    }

    @Override
    protected RockerModel pom(GeneratorContext generatorContext, MavenBuild mavenBuild) {

        if (((GdkGeneratorContext) generatorContext).isPlatformIndependent()) {
            return super.pom(generatorContext, mavenBuild);
        }

        return LibPom.template(
                generatorContext.getProject(),
                generatorContext.getFeatures(),
                mavenBuild);
    }

    @Override
    public void apply(GeneratorContext generatorContext) {
        super.apply(generatorContext);
        addMavenBuild((GdkGeneratorContext) generatorContext);
    }

    private void addMavenBuild(GdkGeneratorContext generatorContext) {

        GdkCloud currentCloud = generatorContext.getCloud();

        for (GdkCloud cloud : generatorContext.getClouds()) {
            cloudPom(generatorContext, cloud);
        }

        generatorContext.setCloud(currentCloud);

        String templateName = generatorContext.getModuleNames().size() > 1 ? "multi-module-pom" : "mavenPom";

        // this is called for each cloud, but the templates are stored in a Map, so it's idempotent
        generatorContext.addTemplate(templateName, new RockerTemplate(ROOT, generatorContext.getBuildTool().getBuildFileName(),
                multimodule.template(MavenRepository.listOf(repositoryResolver.resolveRepositories(generatorContext)),
                        generatorContext.getLibProject(),
                        generatorContext.getModuleNames())
        ));

        generatorContext.addPostProcessor(templateName, new MavenPlatformPostProcessor());

        if (generatorContext.getClouds().size() > 1) {
            // this is called for each cloud, but the post processors are stored in a Set, so it's idempotent
            generatorContext.addPostProcessor("mavenPom", new MavenPomPostProcessor(
                    generatorContext.getLibProject().getName(),
                    generatorContext.getLibProject().getPackageName(),
                    generatorContext.getApplicationType(),
                    generatorContext.getFeature(AbstractGdkCloudGatewayFunction.class).isPresent(),
                    generatorContext.getCloud(),
                    true));
        }
    }

    private void cloudPom(GdkGeneratorContext generatorContext, GdkCloud cloud) {
        generatorContext.setCloud(cloud);

        generatorContext.addDependency(Dependency.builder()
                .groupId(generatorContext.getProject().getPackageName())
                .artifactId(LIB_MODULE)
                .version("1.0-SNAPSHOT")
                .compile()
                .build());

        MavenBuild mavenBuild = mavenBuildCreator.create(generatorContext,
                repositoryResolver.resolveRepositories(generatorContext));

        String templateKey = "mavenPom-" + cloud.getModuleName();
        if (templateKey.equals("mavenPom-")) {
            templateKey = "mavenPom";
        }

        generatorContext.addTemplate(templateKey, new RockerTemplate(cloud.getModuleName(), "pom.xml",
                pom.template(
                        generatorContext.getApplicationType(),
                        generatorContext.getProject(),
                        generatorContext.getFeatures(),
                        mavenBuild,
                        JvmArgumentsFeature.getJvmArguments(generatorContext.getFeatures().getFeatures()))
        ));
        generatorContext.addPostProcessor(templateKey, new MavenPomPostProcessor(
                generatorContext.getLibProject().getName(),
                generatorContext.getLibProject().getPackageName(),
                generatorContext.getApplicationType(),
                generatorContext.getFeature(AbstractGdkCloudGatewayFunction.class).isPresent(),
                generatorContext.getCloud(),
                false));
    }
}
