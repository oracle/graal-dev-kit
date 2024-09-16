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

import java.util.Map;

import static cloud.graal.gdk.model.GdkCloud.AZURE;

/**
 * Azure logging service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class AzureLogging extends AbstractLoggingFeature {

    // TODO replace with io.micronaut.starter.feature.azure.AzureLogging feature when
    //      https://github.com/micronaut-projects/micronaut-starter/pull/2547 is published
    private static final Dependency AZURE_LOGGING_DEPENDENCY = Dependency.builder()
            .groupId("io.micronaut.azure")
            .artifactId("micronaut-azure-logging")
            .compile()
            .build();

    // default is runtime scope, needs to be compile
    private static final Dependency LOGBACK_DEPENDENCY = Dependency.builder()
            .groupId("ch.qos.logback")
            .artifactId("logback-classic")
            .compile()
            .build();

    private static final String APPENDER_NAME = "AZURE";
    private static final String APPENDER_CLASS = "io.micronaut.azure.logging.AzureAppender";
    private static final String JSON_FORMATTER = "io.micronaut.azure.logging.AzureJsonFormatter";

    @Override
    public void apply(GdkGeneratorContext generatorContext) {

        generatorContext.addDependency(AZURE_LOGGING_DEPENDENCY);
        generatorContext.addDependency(LOGBACK_DEPENDENCY);

        applyCommon(generatorContext, APPENDER_NAME, APPENDER_CLASS, JSON_FORMATTER);

        generatorContext.getCloudConfiguration().addNested(Map.of(
                "azure.logging.enabled", true,
                "azure.logging.data-collection-endpoint", "",
                "azure.logging.rule-id", "",
                "azure.logging.stream-name", ""
        ));

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
        return "gdk-azure-logging";
    }
}
