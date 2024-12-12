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
package cloud.graal.gdk.feature.service.logging;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.dependencies.Dependency;
import jakarta.inject.Singleton;

import static cloud.graal.gdk.model.GdkCloud.GCP;

/**
 * GCP logging service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class GcpLogging extends AbstractLoggingFeature {

    // TODO replace with io.micronaut.starter.feature.gcp.GoogleLogging feature when
    //      https://github.com/micronaut-projects/micronaut-starter/pull/2547 is published
    private static final Dependency LOGGING_LOGBACK = Dependency.builder()
            .groupId("com.google.cloud")
            .artifactId("google-cloud-logging-logback")
            .compile()
            .build();

    private static final String APPENDER_NAME = "GOOGLE";
    private static final String APPENDER_CLASS = "com.google.cloud.logging.logback.LoggingAppender";
    private static final String JSON_FORMATTER = "";

    @Override
    public void apply(GdkGeneratorContext generatorContext) {
        generatorContext.addDependency(LOGGING_LOGBACK);
        applyCommon(generatorContext, APPENDER_NAME, APPENDER_CLASS, JSON_FORMATTER);
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return GCP;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-gcp-logging";
    }
}
