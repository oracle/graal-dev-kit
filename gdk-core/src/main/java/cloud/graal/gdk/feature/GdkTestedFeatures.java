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
package cloud.graal.gdk.feature;

import io.micronaut.starter.feature.Feature;

import java.util.Set;

/**
 * Stores all the features officially tested by GDK team.
 * Used for providing information in the UI.
 */
public final class GdkTestedFeatures {

    /**
     * Names of tested features.
     */
    public static final Set<String> TESTED_FEATURE_NAMES = Set.of(
            // COMMON
            "graalvm",
            "reactor",
            "test-resources",
            // SECURITY
            "security-annotations",
            // SECURITY OCI
            "security-oauth2",
            "security-jwt",
            "views-jte",
            // DATABASE
            "data",
            "data-jdbc",
            "jdbc-hikari",
            "mysql",
            "flyway",
            "validation",
            "oracle",
            // DATABASE OCI
            "oracle-cloud-atp",
            // EMAIL AWS
            "email-amazon-ses",
            // EMAIL OCI
            "email-javamail",
            "email-template",
            // K8S
            "kubernetes-client",
            // LOGGING
            "logback",
            // METRICS
            "micrometer",
            "micrometer-annotation",
            "management",
            // METRICS AWS
            "micrometer-cloudwatch",
            // METRICS OCI
            "micrometer-oracle-cloud",
            // METRICS GCP
            "micrometer-stackdriver",
            // METRICS Azure
            "micrometer-azure-monitor",
            // OBJECTSTORAGE AWS
            "object-storage-aws",
            // OBJECTSTORAGE Azure
            "object-storage-azure",
            // OBJECTSTORAGE GCP
            "object-storage-gcp",
            // OBJECTSTORAGE OCI
            "object-storage-oracle-cloud",
            // SDK AWS
            "aws-v2-sdk",
            // SDK OCI
            "oracle-cloud-sdk",
            // SECRETMANAGEMENT AWS
            "aws-secrets-manager",
            // SECRETMANAGEMENT GCP
            "gcp-secrets-manager",
            // SECRETMANAGEMENT OCI
            "oracle-cloud-vault",
            // SECRETMANAGEMENT Azure
            "azure-key-vault",
            "discovery-client",
            // STREAMING
            "kafka",
            // TRACING
            "tracing-opentelemetry-http",
            "tracing-opentelemetry",
            "tracing-opentelemetry-annotations",
            // TRACING AWS
            "tracing-opentelemetry-exporter-otlp",
            "tracing-opentelemetry-xray",
            // TRACING GCP
            "tracing-opentelemetry-exporter-gcp",
            "tracing-opentelemetry-gcp"
    );

    private GdkTestedFeatures() {
    }

    /**
     * @param feature a feature
     * @return true if the feature was tested
     */
    public static boolean isFeatureGdkTested(Feature feature) {
        return TESTED_FEATURE_NAMES.contains(feature.getName());
    }

    /**
     * @param featureName the name of a feature
     * @return true if the feature was tested
     */
    public static boolean isFeatureGdkTested(String featureName) {
        return TESTED_FEATURE_NAMES.contains(featureName);
    }
}
