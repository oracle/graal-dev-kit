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
package cloud.graal.gdk.model;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Represents a service.
 */
public enum GdkService {

    /**
     * Database service.
     */
    DATABASE(
            "Database",
            "Provides a database access toolkit for repository interfaces",
            "database"
    ),

    /**
     * Email service.
     */
    EMAIL(
            "Email",
            "Provides integration with multiple email providers",
            "email"
    ),

    /**
     * Kubernetes service.
     */
    K8S(
            "Kubernetes",
            "Provides integration with Kubernetes",
            "kubernetes"
    ),

    /**
     * Logging service.
     */
    LOGGING(
            "Logging",
            "Provides integration with multiple logging frameworks and various other appenders (including email and databases)",
            "logging"
    ),

    /**
     * Metrics service.
     */
    METRICS(
            "Metrics",
            "Provides integration with cloud-specific monitoring services",
            "metrics"
    ),

    /**
     * Object Storage service.
     */
    OBJECTSTORE(
            "Object Storage",
            "Provides integration with cloud-specific storage solutions for large objects",
            "object-storage"
    ),

    /**
     * SDK service.
     */
    SDK(
            "SDK",
            "Provides an API for developer access to cloud services"
    ),

    /**
     * Secret management service.
     */
    SECRETMANAGEMENT("" +
            "Secret Management",
            "Provides integration with the Secret Manager of the corresponding cloud platform",
            "secret-management"
    ),

    /**
     * Security service.
     */
    SECURITY(
            "Security",
            "Provides functionality to authenticate with OAuth 2.0 servers, including support for the OpenID standard",
            "authentication"
    ),

    /**
     * Streaming service.
     */
    STREAMING(
            "Streaming",
            "Provides integration with Apache Kafka",
            "streaming"
    ),

    /**
     * Tracing service.
     */
    TRACING(
            "Tracing",
            "Provides integration with Zipkin and Jaeger (via the Open Tracing API)",
            "tracing"
    ),

    /**
     * For testing.
     */
    TESTING(
            "Testing",
            "A service used only for testing"
    );

    private static final GdkService[] SUPPORTED;

    static {
        SUPPORTED = Arrays.stream(values())
                .filter(c -> c != GdkService.TESTING)
                .toArray(GdkService[]::new);
    }

    private final String title;
    private final String description;
    private final String documentationModuleName;

    GdkService(String title, String description) {
        this.title = title;
        this.description = description;
        this.documentationModuleName = null;
    }

    GdkService(String title, String description, String documentationModuleName) {
        this.title = title;
        this.description = description;
        this.documentationModuleName = documentationModuleName;
    }

    /**
     * @return the display name
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the name of module that this service is associated with
     */
    public String getDocumentationModuleName() {
        return documentationModuleName;
    }

    /**
     * @return the subset of GdkService enum values that are currently supported
     */
    public static GdkService[] supportedValues() {
        return SUPPORTED;
    }

    /**
     * Provides the "completionCandidates" for the CLI "--services" argument.
     */
    public static class AvailableServices implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            return Arrays.stream(GdkService.supportedValues())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .iterator();
        }
    }
}
