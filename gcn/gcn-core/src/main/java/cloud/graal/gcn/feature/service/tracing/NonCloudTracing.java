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
package cloud.graal.gcn.feature.service.tracing;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.model.GcnCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetry;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryAnnotations;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryHttp;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.NONE;
import static io.micronaut.starter.template.Template.ROOT;

/**
 * Non-cloud tracing service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class NonCloudTracing extends AbstractTracingFeature {

    /**
     * @param openTelemetry            OpenTelemetry feature
     * @param openTelemetryHttp        OpenTelemetryHttp feature
     * @param openTelemetryAnnotations OpenTelemetryAnnotations feature
     */
    public NonCloudTracing(OpenTelemetry openTelemetry,
                           OpenTelemetryHttp openTelemetryHttp,
                           OpenTelemetryAnnotations openTelemetryAnnotations) {
        super(openTelemetry, openTelemetryHttp, openTelemetryAnnotations);
    }

    @Override
    protected void doApply(GcnGeneratorContext generatorContext) {
        // no-op
    }

    @Override
    protected String getDefaultModule() {
        return ROOT;
    }

    @NonNull
    @Override
    public GcnCloud getCloud() {
        return NONE;
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-tracing";
    }
}
