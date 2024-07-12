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
import cloud.graal.gdk.OracleCloudNettyClientDependencies;
import cloud.graal.gdk.feature.service.logging.template.LogControllerGroovy;
import cloud.graal.gdk.feature.service.logging.template.LogControllerJava;
import cloud.graal.gdk.feature.service.logging.template.LogControllerKotlin;
import cloud.graal.gdk.feature.service.logging.template.LogbackXml;
import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.template.RockerTemplate;
import jakarta.inject.Singleton;

import static cloud.graal.gdk.model.GdkCloud.OCI;

/**
 * OCI logging service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class OciLogging extends AbstractLoggingFeature implements OracleCloudNettyClientDependencies {

    private static final Dependency ORACLECLOUD_LOGGING = Dependency.builder()
            .groupId("io.micronaut.oraclecloud")
            .artifactId("micronaut-oraclecloud-logging")
            .compile()
            .build();

    @Override
    public void apply(GdkGeneratorContext generatorContext) {

        generatorContext.addDependency(ORACLECLOUD_LOGGING);

        addNettyDependencies(generatorContext);

        generatorContext.addTemplate("loggingConfig-oci",
                new RockerTemplate(getModuleName(), "src/main/resources/logback.xml",
                        LogbackXml.template("ORACLE",
                                "io.micronaut.oraclecloud.logging.OracleCloudAppender",
                                "io.micronaut.oraclecloud.logging.OracleCloudJsonFormatter",
                                getModuleName())));

        if (generatorContext.generateExampleCode()) {

            Project project = generatorContext.getProject();

            generatorContext.addTemplate(getModuleName(), "OciLogController",
                    generatorContext.getSourcePath("/{packagePath}/LogController"),
                    LogControllerJava.template(project),
                    LogControllerKotlin.template(project),
                    LogControllerGroovy.template(project));
        }
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return OCI;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-oci-logging";
    }
}
