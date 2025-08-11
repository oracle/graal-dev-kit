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
import io.micronaut.starter.feature.opentelemetry.OpenTelemetry;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryAnnotations;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryHttp;
import io.micronaut.starter.feature.other.HttpClient;
import jakarta.inject.Singleton;

import static cloud.graal.gdk.model.GdkCloud.AZURE;

/**
 * Azure tracing service feature.
 *
 * @since 4.9
 */
@Singleton
public class AzureTracing extends AbstractTracingFeature {

    private final io.micronaut.starter.feature.azure.AzureTracing azureTracing;

    /**
     * @param openTelemetry            OpenTelemetry feature
     * @param openTelemetryHttp        OpenTelemetryHttp feature
     * @param openTelemetryAnnotations OpenTelemetryAnnotations feature
     */
    public AzureTracing(OpenTelemetry openTelemetry,
                        OpenTelemetryHttp openTelemetryHttp,
                        OpenTelemetryAnnotations openTelemetryAnnotations,
                        HttpClient httpClient,
                        io.micronaut.starter.feature.azure.AzureTracing azureTracing) {
        super(openTelemetry, openTelemetryHttp, openTelemetryAnnotations, httpClient);
        this.azureTracing = azureTracing;
    }

    @Override
    public void processSelectedFeatures(GdkFeatureContext featureContext) {
        super.processSelectedFeatures(featureContext);
        featureContext.addFeature(azureTracing, io.micronaut.starter.feature.azure.AzureTracing.class);
    }

    @Override
    protected void doApply(GdkGeneratorContext generatorContext) {
        addAzureNativeImageProperties(generatorContext);
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return AZURE;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-azure-tracing";
    }
}
