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
package cloud.graal.gcn.feature.service.objectstore;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.GcnFeatureContext;
import cloud.graal.gcn.feature.service.AbstractGcnServiceFeature;
import cloud.graal.gcn.feature.service.objectstore.template.ProfilePicturesApiGroovy;
import cloud.graal.gcn.feature.service.objectstore.template.ProfilePicturesApiJava;
import cloud.graal.gcn.feature.service.objectstore.template.ProfilePicturesApiKotlin;
import cloud.graal.gcn.feature.service.objectstore.template.ProfilePicturesControllerGroovy;
import cloud.graal.gcn.feature.service.objectstore.template.ProfilePicturesControllerJava;
import cloud.graal.gcn.feature.service.objectstore.template.ProfilePicturesControllerKotlin;
import cloud.graal.gcn.feature.service.objectstore.template.ProfilePicturesControllerSpec;
import cloud.graal.gcn.feature.service.objectstore.template.ProfilePicturesControllerTestGroovyJUnit;
import cloud.graal.gcn.feature.service.objectstore.template.ProfilePicturesControllerTestJava;
import cloud.graal.gcn.feature.service.objectstore.template.ProfilePicturesControllerTestKotest;
import cloud.graal.gcn.feature.service.objectstore.template.ProfilePicturesControllerTestKotlinJUnit;
import cloud.graal.gcn.model.GcnService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.build.dependencies.Dependency;

import static cloud.graal.gcn.model.GcnService.OBJECTSTORE;

/**
 * Base class for object storage service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractObjectStore extends AbstractGcnServiceFeature {

    @Override
    public final void processSelectedFeatures(GcnFeatureContext featureContext) {
        addFeature(featureContext);
    }

    /**
     * Add the cloud-specific starter feature.
     *
     * @param featureContext the context
     */
    protected abstract void addFeature(GcnFeatureContext featureContext);

    @Override
    public final void apply(GcnGeneratorContext generatorContext) {

        addConfig(generatorContext);

        applyForLib(generatorContext, () -> {

            generatorContext.addDependency(Dependency.builder()
                    .groupId("io.micronaut.objectstorage")
                    .artifactId("micronaut-object-storage-core")
                    .compile());

            generatorContext.addDependency(Dependency.builder()
                    .groupId("io.micronaut")
                    .artifactId("micronaut-http-server")
                    .compileOnly());
        });

        if (generatorContext.generateExampleCode()) {

            Project project = generatorContext.getProject();

            applyForLib(generatorContext, () -> {

                generatorContext.addTemplate(getDefaultModule(), "ProfilePicturesApi",
                        generatorContext.getSourcePath("/{packagePath}/ProfilePicturesApi"),
                        ProfilePicturesApiJava.template(project),
                        ProfilePicturesApiKotlin.template(project),
                        ProfilePicturesApiGroovy.template(project));

                generatorContext.addTemplate(getDefaultModule(), "ProfilePicturesController",
                        generatorContext.getSourcePath("/{packagePath}/ProfilePicturesController"),
                        ProfilePicturesControllerJava.template(project),
                        ProfilePicturesControllerKotlin.template(project),
                        ProfilePicturesControllerGroovy.template(project));
            });

            generatorContext.addTestTemplate(getModuleName(), "ProfilePicturesControllerTest-" + getModuleName(),
                    generatorContext.getTestSourcePath("/{packagePath}/ProfilePicturesController"),
                    ProfilePicturesControllerSpec.template(project),
                    ProfilePicturesControllerTestJava.template(project),
                    ProfilePicturesControllerTestGroovyJUnit.template(project),
                    ProfilePicturesControllerTestKotlinJUnit.template(project),
                    ProfilePicturesControllerTestKotest.template(project));

            applyForCloud(generatorContext, () -> {
                if (generatorContext.getFeatures().testFramework().isJunit() || generatorContext.getFeatures().testFramework().isKoTest()) {
                    if (generatorContext.getFeatures().language().isKotlin()) {
                        Dependency.Builder mockk = Dependency.builder()
                                .groupId("io.mockk")
                                .artifactId("mockk")
                                .test();
                        if (!generatorContext.getBuildTool().isGradle()) {
                            mockk.version(generatorContext.resolveCoordinate("mockk").getVersion());
                        }
                        generatorContext.addDependency(mockk);

                    } else {
                        generatorContext.addDependency(Dependency.builder()
                                .groupId("org.mockito")
                                .artifactId("mockito-core")
                                .test());
                    }
                }
            });
        } else {
            addLibPlaceholders(generatorContext);
        }
    }

    /**
     * Add the cloud-specific application configuration.
     *
     * @param generatorContext the context
     */
    protected abstract void addConfig(GcnGeneratorContext generatorContext);

    @NonNull
    @Override
    public final GcnService getService() {
        return OBJECTSTORE;
    }
}
