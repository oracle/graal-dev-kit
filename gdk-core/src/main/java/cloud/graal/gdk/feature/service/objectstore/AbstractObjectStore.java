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
package cloud.graal.gdk.feature.service.objectstore;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.feature.service.AbstractGdkServiceFeature;
import cloud.graal.gdk.feature.service.objectstore.template.ProfilePicturesApiGroovy;
import cloud.graal.gdk.feature.service.objectstore.template.ProfilePicturesApiJava;
import cloud.graal.gdk.feature.service.objectstore.template.ProfilePicturesApiKotlin;
import cloud.graal.gdk.feature.service.objectstore.template.ProfilePicturesControllerGroovy;
import cloud.graal.gdk.feature.service.objectstore.template.ProfilePicturesControllerJava;
import cloud.graal.gdk.feature.service.objectstore.template.ProfilePicturesControllerKotlin;
import cloud.graal.gdk.feature.service.objectstore.template.ProfilePicturesControllerSpec;
import cloud.graal.gdk.feature.service.objectstore.template.ProfilePicturesControllerTestGroovyJUnit;
import cloud.graal.gdk.feature.service.objectstore.template.ProfilePicturesControllerTestJava;
import cloud.graal.gdk.feature.service.objectstore.template.ProfilePicturesControllerTestKotest;
import cloud.graal.gdk.feature.service.objectstore.template.ProfilePicturesControllerTestKotlinJUnit;
import cloud.graal.gdk.model.GdkService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.build.dependencies.Dependency;

import static cloud.graal.gdk.model.GdkService.OBJECTSTORE;

/**
 * Base class for object storage service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractObjectStore extends AbstractGdkServiceFeature {

    private static final Dependency OBJECT_STORAGE_CORE = Dependency.builder()
            .groupId("io.micronaut.objectstorage")
            .artifactId("micronaut-object-storage-core")
            .compile()
            .build();

    private static final Dependency HTTP_SERVER = Dependency.builder()
            .groupId("io.micronaut")
            .artifactId("micronaut-http-server")
            .compileOnly()
            .build();

    private static final Dependency MOCKITO_CORE = Dependency.builder()
            .groupId("org.mockito")
            .artifactId("mockito-core")
            .test()
            .build();

    @Override
    public final void processSelectedFeatures(GdkFeatureContext featureContext) {
        addFeature(featureContext);
    }

    /**
     * Add the cloud-specific starter feature.
     *
     * @param featureContext the context
     */
    protected abstract void addFeature(GdkFeatureContext featureContext);

    @Override
    public final void apply(GdkGeneratorContext generatorContext) {

        addConfig(generatorContext);

        applyForLib(generatorContext, () -> {
            generatorContext.addDependency(OBJECT_STORAGE_CORE);
            generatorContext.addDependency(HTTP_SERVER);
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
                        generatorContext.addDependency(MOCKITO_CORE);
                    }
                }
            });
        }
    }

    /**
     * Add the cloud-specific application configuration.
     *
     * @param generatorContext the context
     */
    protected abstract void addConfig(GdkGeneratorContext generatorContext);

    @NonNull
    @Override
    public final GdkService getService() {
        return OBJECTSTORE;
    }
}
