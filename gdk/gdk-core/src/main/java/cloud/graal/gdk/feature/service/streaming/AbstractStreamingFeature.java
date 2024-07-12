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
package cloud.graal.gdk.feature.service.streaming;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.feature.service.AbstractGdkServiceFeature;
import cloud.graal.gdk.model.GdkService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.feature.messaging.kafka.Kafka;

import static cloud.graal.gdk.model.GdkService.STREAMING;

/**
 * Base class for streaming service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractStreamingFeature extends AbstractGdkServiceFeature {

    private final Kafka kafka;

    /**
     * @param kafka Kafka feature
     */
    protected AbstractStreamingFeature(Kafka kafka) {
        this.kafka = kafka;
    }

    @Override
    public void processSelectedFeatures(GdkFeatureContext featureContext) {
        featureContext.addFeature(kafka, Kafka.class);
    }

    @Override
    public final void apply(GdkGeneratorContext generatorContext) {

        applyForLib(generatorContext, () -> {
            kafka.apply(generatorContext);
        });

        doApply(generatorContext);
    }

    /**
     * Implemented in subclasses to apply cloud-specific changes.
     *
     * @param generatorContext the generator context
     */
    protected abstract void doApply(GdkGeneratorContext generatorContext);

    @NonNull
    @Override
    public final GdkService getService() {
        return STREAMING;
    }
}
