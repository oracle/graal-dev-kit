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
package cloud.graal.gdk.feature.service.metrics;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.feature.micrometer.AzureMonitor;
import io.micronaut.starter.feature.micrometer.Core;
import io.micronaut.starter.feature.micrometer.MicrometerAnnotations;
import io.micronaut.starter.feature.other.Management;
import jakarta.inject.Singleton;

import static cloud.graal.gdk.model.GdkCloud.AZURE;

/**
 * Azure metrics service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class AzureMetrics extends AbstractMetricsFeature {

    private static final Dependency HTTP_SERVER_NETTY = Dependency.builder()
            .groupId("io.micronaut")
            .artifactId("micronaut-http-server-netty")
            .testRuntime()
            .build();

    private final AzureMonitor azureMonitor;

    /**
     * @param azureMonitor AzureMonitor feature
     * @param core         the Core feature
     * @param management   the Management feature
     */
    public AzureMetrics(AzureMonitor azureMonitor,
                        Core core,
                        Management management,
                        MicrometerAnnotations micrometerAnnotations) {
        super(core, management, micrometerAnnotations);
        this.azureMonitor = azureMonitor;
    }

    @Override
    public void processSelectedFeatures(GdkFeatureContext featureContext) {
        super.processSelectedFeatures(featureContext);
        featureContext.addFeature(azureMonitor, AzureMonitor.class);
    }

    @NonNull
    @Override
    protected void doApply(GdkGeneratorContext generatorContext) {

        applyForLib(generatorContext, () -> {
            generatorContext.addDependency(HTTP_SERVER_NETTY);
        });
        if (generatorContext.generateExampleCode()) {
            generatorContext.getTestConfiguration().addNested("micronaut.metrics.export.azuremonitor.enabled", false);
        }
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return AZURE;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-azure-metrics";
    }
}
