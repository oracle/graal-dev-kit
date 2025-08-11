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
package cloud.graal.gdk.feature.service.tracing;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetry;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryAnnotations;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryGoogleCloudTrace;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryHttp;
import io.micronaut.starter.feature.other.HttpClient;
import jakarta.inject.Singleton;

import static cloud.graal.gdk.model.GdkCloud.GCP;

/**
 * GCP tracing service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class GcpTracing extends AbstractTracingFeature {

    private static final Dependency OPENTELEMETRY_SEMCONV = Dependency.builder()
            .groupId("io.opentelemetry.semconv")
            .artifactId("opentelemetry-semconv")
            .compile()
            .build();

    private final OpenTelemetryGoogleCloudTrace openTelemetryGoogleCloudTrace;

    /**
     * @param openTelemetry                 OpenTelemetry feature
     * @param openTelemetryHttp             OpenTelemetryHttp feature
     * @param openTelemetryAnnotations      OpenTelemetryAnnotations feature
     * @param openTelemetryGoogleCloudTrace OpenTelemetryGoogleCloudTrace feature
     */
    public GcpTracing(OpenTelemetry openTelemetry,
                      OpenTelemetryHttp openTelemetryHttp,
                      OpenTelemetryAnnotations openTelemetryAnnotations,
                      OpenTelemetryGoogleCloudTrace openTelemetryGoogleCloudTrace,
                      HttpClient httpClient) {
        super(openTelemetry, openTelemetryHttp, openTelemetryAnnotations, httpClient);
        this.openTelemetryGoogleCloudTrace = openTelemetryGoogleCloudTrace;
    }

    @Override
    public void processSelectedFeatures(GdkFeatureContext featureContext) {
        super.processSelectedFeatures(featureContext);
        featureContext.addFeature(openTelemetryGoogleCloudTrace, OpenTelemetryGoogleCloudTrace.class);
    }

    @Override
    protected void doApply(GdkGeneratorContext generatorContext) {
        generatorContext.addDependency(OPENTELEMETRY_SEMCONV);
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return GCP;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-gcp-tracing";
    }
}
