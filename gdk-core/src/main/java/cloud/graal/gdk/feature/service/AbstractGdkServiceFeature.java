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
package cloud.graal.gdk.feature.service;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.AbstractGdkFeature;
import cloud.graal.gdk.feature.service.template.AzureNativeImageProperties;
import cloud.graal.gdk.feature.service.template.JDK25AuthenticationModeInitializeAtBuildTimeClass;
import cloud.graal.gdk.feature.service.template.JDK25AzureNativeImageProperties;
import cloud.graal.gdk.model.GdkCloud;
import cloud.graal.gdk.model.GdkService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.template.RockerWritable;

import static io.micronaut.starter.feature.FeaturePhase.DEFAULT;

/**
 * Base class for service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractGdkServiceFeature extends AbstractGdkFeature {

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public int getOrder() {
        return DEFAULT.getOrder() + 50;
    }

    /**
     * @return the service enum
     */
    @NonNull
    public abstract GdkService getService();

    /**
     * Set the cloud to NONE in GdkGeneratorContext to apply changes to the
     * "lib" module instead of the feature's cloud module, and switch back
     * after running the runnable.
     *
     * @param generatorContext the generator context
     * @param runnable         the code to run
     */
    protected void applyForLib(GdkGeneratorContext generatorContext,
                               Runnable runnable) {

        GdkCloud cloud = generatorContext.getCloud();

        generatorContext.resetCloud();

        try {
            runnable.run();
        } finally {
            generatorContext.setCloud(cloud);
        }
    }

    /**
     * Set the cloud to the feature's cloud in GdkGeneratorContext to apply
     * changes to the cloud module instead of the "lib" module, and switch back
     * after running the runnable.
     *
     * @param generatorContext the generator context
     * @param runnable         the code to run
     */
    protected void applyForCloud(GdkGeneratorContext generatorContext,
                                 Runnable runnable) {

        GdkCloud cloud = generatorContext.getCloud();

        generatorContext.setCloud(getCloud());

        try {
            runnable.run();
        } finally {
            generatorContext.setCloud(cloud);
        }
    }

    /**
     * Add --initialize-at-build-time for building Azure native executables.
     *
     * @param generatorContext generator context
     */
    protected void addAzureNativeImageProperties(GdkGeneratorContext generatorContext) {
        generatorContext.addInitializeBuildTimeClasses(new RockerWritable(AzureNativeImageProperties.template()));

        if (generatorContext.isJdkVersionAtLeast(25)) {
            generatorContext.addInitializeBuildTimeClasses(new RockerWritable(JDK25AzureNativeImageProperties.template()));
        }
    }

    protected void addAuthenticationModeInitializeAtBuildTimeClass(GdkGeneratorContext generatorContext) {
        generatorContext.addInitializeBuildTimeClasses(new RockerWritable(JDK25AuthenticationModeInitializeAtBuildTimeClass.template()));
    }
}
