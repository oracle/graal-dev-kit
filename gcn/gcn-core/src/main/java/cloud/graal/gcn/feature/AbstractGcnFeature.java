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
package cloud.graal.gcn.feature;

import cloud.graal.gcn.GcnGeneratorContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.FeatureContext;
import io.micronaut.starter.feature.MultiProjectFeature;

/**
 * Abstract base class for GCN "create" and "service" features.
 *
 * @since 1.0.0
 */
public abstract class AbstractGcnFeature implements GcnFeature, MultiProjectFeature {

    @Override
    public final void apply(GeneratorContext generatorContext) {
        apply((GcnGeneratorContext) generatorContext);
    }

    /**
     * Method to implement using the GCN generator context instead of the default.
     *
     * @param generatorContext the GCN generator context
     */
    public abstract void apply(GcnGeneratorContext generatorContext);

    @Override
    public final void processSelectedFeatures(FeatureContext featureContext) {
        processSelectedFeatures((GcnFeatureContext) featureContext);
    }

    /**
     * Method to implement using the GCN feature context instead of the default.
     *
     * @param featureContext the GCN feature context
     */
    public void processSelectedFeatures(GcnFeatureContext featureContext) {
        // override as needed
    }

    @Override
    public final boolean supports(ApplicationType applicationType) {
        return true;
    }

    /**
     * Accessor for the feature cloud module name.
     *
     * @return the module name ("aws", "oci", etc.)
     */
    @NonNull
    protected String getModuleName() {
        return getCloud().getModuleName();
    }
}
