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
package cloud.graal.gcn.feature.misc;

import cloud.graal.gcn.feature.service.logging.GcpLogging;
import cloud.graal.gcn.feature.service.metrics.GcpMetrics;
import cloud.graal.gcn.feature.service.objectstore.GcpObjectStore;
import cloud.graal.gcn.feature.service.secretmanagement.GcpSecretManagement;
import cloud.graal.gcn.feature.service.tracing.GcpTracing;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.feature.DefaultFeature;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.feature.gcp.secretsmanager.GoogleSecretManager;
import io.micronaut.starter.feature.micrometer.Stackdriver;
import io.micronaut.starter.feature.objectstorage.ObjectStorageGcp;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryExporterGoogleCloudTrace;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryGoogleCloudTrace;
import io.micronaut.starter.options.Options;
import jakarta.inject.Singleton;

import java.util.Set;

/**
 * Adds grpc-netty if any of gcp-secrets-manager, micrometer-stackdriver,
 * object-storage-gcp, tracing-opentelemetry-exporter-gcp, or tracing-opentelemetry-gcp
 * is selected, or if GCP and logging, metrics, objectstore, secretmanagement,
 * or tracing is selected.
 *
 * @since 4.2.1
 */
@Singleton
public class GrpcNetty implements DefaultFeature {

    private static final Dependency GRPC_NETTY = Dependency.builder()
            .groupId("io.grpc")
            .artifactId("grpc-netty")
            .runtime()
            .build();

    @Override
    public boolean shouldApply(ApplicationType applicationType,
                               Options options,
                               Set<Feature> selectedFeatures) {

        return selectedFeatures.stream().anyMatch(feature ->
                // features
                feature instanceof GoogleSecretManager ||
                        feature instanceof Stackdriver ||
                        feature instanceof ObjectStorageGcp ||
                        feature instanceof OpenTelemetryExporterGoogleCloudTrace ||
                        feature instanceof OpenTelemetryGoogleCloudTrace ||
                        // services
                        feature instanceof GcpTracing ||
                        feature instanceof GcpSecretManagement ||
                        feature instanceof GcpMetrics ||
                        feature instanceof GcpLogging ||
                        feature instanceof GcpObjectStore
        );
    }

    @Override
    public void apply(GeneratorContext generatorContext) {
        generatorContext.addDependency(GRPC_NETTY);
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-grpc-netty";
    }

    @Override
    public boolean supports(ApplicationType applicationType) {
        return true;
    }

    @Override
    public boolean isVisible() {
        return false;
    }
}
