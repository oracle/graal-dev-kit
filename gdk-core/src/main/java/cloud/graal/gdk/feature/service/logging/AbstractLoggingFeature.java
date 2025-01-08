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
import cloud.graal.gdk.feature.service.AbstractGdkServiceFeature;
import cloud.graal.gdk.feature.service.logging.template.LogControllerGroovy;
import cloud.graal.gdk.feature.service.logging.template.LogControllerJava;
import cloud.graal.gdk.feature.service.logging.template.LogControllerKotlin;
import cloud.graal.gdk.feature.service.logging.template.LogbackXml;
import cloud.graal.gdk.model.GdkService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.template.RockerTemplate;

import static cloud.graal.gdk.model.GdkService.LOGGING;

/**
 * Base class for logging service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractLoggingFeature extends AbstractGdkServiceFeature {

    @NonNull
    @Override
    public final GdkService getService() {
        return LOGGING;
    }

    protected void applyCommon(GdkGeneratorContext generatorContext,
                               String appenderName,
                               String appenderClass,
                               String jsonFormatter) {

        generatorContext.addTemplate("loggingConfig-" + getModuleName(),
                new RockerTemplate(getModuleName(), "src/main/resources/logback.xml",
                        LogbackXml.template(appenderName, appenderClass, jsonFormatter, getModuleName())));

        if (generatorContext.generateExampleCode()) {

            Project project = generatorContext.getProject();

            generatorContext.addTemplate(getModuleName(), getModuleName() + "LogController",
                    generatorContext.getSourcePath("/{packagePath}/LogController"),
                    LogControllerJava.template(project),
                    LogControllerKotlin.template(project),
                    LogControllerGroovy.template(project));
        }
    }
}
