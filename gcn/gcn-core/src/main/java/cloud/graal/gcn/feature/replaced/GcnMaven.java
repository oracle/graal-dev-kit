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
import cloud.graal.gcn.feature.create.GcnMavenBuildCreator;
import cloud.graal.gcn.feature.create.gatewayfunction.AbstractGcnCloudGatewayFunction;
import cloud.graal.gcn.feature.replaced.template.LibPom;
import cloud.graal.gcn.model.GcnCloud;
import cloud.graal.gcn.template.MavenPlatformPostProcessor;
import cloud.graal.gcn.template.MavenPomPostProcessor;
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

import static cloud.graal.gcn.GcnUtils.LIB_MODULE;
import static io.micronaut.starter.template.Template.ROOT;

/**
 * Replaces the default feature to render lib/pom.xml without application-related parts.
 *
 * @since 1.0.0
 */
@Replaces(Maven.class)
@Singleton
public class GcnMaven extends Maven {

    private static final String PROPERTY_MAINCLASS = "exec.mainClass";
    private static final String PROPERTY_RUNTIME = "micronaut.runtime";
    private static final String PROPERTY_TEST_RESOURCES = "micronaut.test.resources.enabled";

    private final GcnMavenBuildCreator mavenBuildCreator;

    public GcnMaven(GcnMavenBuildCreator mavenBuildCreator, RepositoryResolver repositoryResolver) {
        super(mavenBuildCreator, repositoryResolver);
        this.mavenBuildCreator = mavenBuildCreator;
    }

    @Override
    protected MavenBuild createBuild(GeneratorContext generatorContext) {

        if (((GcnGeneratorContext) generatorContext).isPlatformIndependent()) {
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

        if (((GcnGeneratorContext) generatorContext).isPlatformIndependent()) {
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
        addMavenBuild((GcnGeneratorContext) generatorContext);
    }

    private void addMavenBuild(GcnGeneratorContext generatorContext) {

        GcnCloud reset = generatorContext.getCloud();

        generatorContext.getClouds().forEach(x -> cloudPom(generatorContext, x));

        generatorContext.setCloud(reset);

        // this is called for each cloud, but the templates are stored in a Map, so it's idempotent
        generatorContext.addTemplate("multi-module-pom", new RockerTemplate(ROOT, generatorContext.getBuildTool().getBuildFileName(),
                multimodule.template(MavenRepository.listOf(repositoryResolver.resolveRepositories(generatorContext)),
                        generatorContext.getLibProject(),
                        generatorContext.getModuleNames())
        ));

        generatorContext.addPostProcessor("multi-module-pom", new MavenPlatformPostProcessor());

        if (generatorContext.getClouds().size() > 1) {

            // this is called for each cloud, but the post processors are stored in a Set, so it's idempotent
            generatorContext.addPostProcessor("mavenPom", new MavenPomPostProcessor(
                    generatorContext.getLibProject().getName(),
                    generatorContext.getLibProject().getPackageName(),
                    generatorContext.getApplicationType(),
                    generatorContext.getFeature(AbstractGcnCloudGatewayFunction.class).orElse(null) != null,
                    generatorContext.getCloud(),
                    true));
        }
    }

    private void cloudPom(GcnGeneratorContext generatorContext, GcnCloud gcnCloud) {
        generatorContext.setCloud(gcnCloud);

        generatorContext.addDependency(Dependency.builder()
                .groupId(generatorContext.getProject().getPackageName())
                .artifactId(LIB_MODULE)
                .version("1.0-SNAPSHOT")
                .compile()
                .build());

        MavenBuild mavenBuild = mavenBuildCreator.create(generatorContext, repositoryResolver.resolveRepositories(generatorContext));

        String templateKey = "mavenPom-" + gcnCloud.getModuleName();

        if (templateKey.equals("mavenPom-")) {
            templateKey = "mavenPom";
        }

        generatorContext.addTemplate(templateKey, new RockerTemplate(gcnCloud.getModuleName(), "pom.xml",
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
                generatorContext.getFeature(AbstractGcnCloudGatewayFunction.class).orElse(null) != null,
                generatorContext.getCloud(),
                false));
    }
}
