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
import cloud.graal.gcn.feature.GcnFeatureContext;
import cloud.graal.gcn.model.GcnCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetry;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryAnnotations;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryExporterOtlp;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryHttp;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryXray;
import io.micronaut.starter.feature.other.HttpClient;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.AWS;

/**
 * AWS tracing service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class AwsTracing extends AbstractTracingFeature {

    private final OpenTelemetryXray openTelemetryXray;
    private final OpenTelemetryExporterOtlp openTelemetryExporterOtlp;

    /**
     * @param openTelemetry             OpenTelemetry feature
     * @param openTelemetryHttp         OpenTelemetryHttp feature
     * @param openTelemetryAnnotations  OpenTelemetryAnnotations feature
     * @param openTelemetryXray         OpenTelemetryXray feature
     * @param openTelemetryExporterOtlp OpenTelemetryExporterOtlp feature
     */
    public AwsTracing(OpenTelemetry openTelemetry,
                      OpenTelemetryHttp openTelemetryHttp,
                      OpenTelemetryAnnotations openTelemetryAnnotations,
                      OpenTelemetryXray openTelemetryXray,
                      OpenTelemetryExporterOtlp openTelemetryExporterOtlp,
                      HttpClient httpClient) {
        super(openTelemetry, openTelemetryHttp, openTelemetryAnnotations, httpClient);
        this.openTelemetryXray = openTelemetryXray;
        this.openTelemetryExporterOtlp = openTelemetryExporterOtlp;
    }

    @Override
    public void processSelectedFeatures(GcnFeatureContext featureContext) {
        super.processSelectedFeatures(featureContext);
        featureContext.addFeature(openTelemetryXray, OpenTelemetryXray.class);
        featureContext.addFeature(openTelemetryExporterOtlp, OpenTelemetryExporterOtlp.class);
    }

    @Override
    protected void doApply(GcnGeneratorContext generatorContext) {
        // no-op
    }

    @NonNull
    @Override
    public GcnCloud getCloud() {
        return AWS;
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-aws-tracing";
    }
}
