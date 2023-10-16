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
package cloud.graal.gcn.feature.create.app;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.GcnFeatureContext;
import cloud.graal.gcn.feature.create.AbstractGcnCreateFeature;
import cloud.graal.gcn.model.GcnCloud;
import cloud.graal.gcn.model.GcnProjectType;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.feature.lang.java.JavaApplication;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.NONE;
import static cloud.graal.gcn.model.GcnProjectType.SPRING_BOOT_APPLICATION;

/**
 * Base class for Spring Boot create-app features.
 *
 * @since 4.0.0
 */
@Singleton
public class GcnSpringBootCloudApp extends AbstractGcnCreateFeature {

    @NonNull
    @Override
    public GcnProjectType getProjectType() {
        return SPRING_BOOT_APPLICATION;
    }

    @Override
    public GcnCloud getCloud() {
        return NONE;
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-spring-boot-app";
    }

    @Override
    public void processSelectedFeatures(GcnFeatureContext featureContext) {
        // exclude default Application and ApplicationTest classes
        featureContext.exclude(feature -> feature instanceof JavaApplication);
    }

    @Override
    public void apply(GcnGeneratorContext generatorContext) {
        // Do nothing
        // gradle or maven files and Application file are copied
    }

}
