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
package cloud.graal.gcn.feature.service.tracing;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.service.AbstractGcnServiceFeature;
import cloud.graal.gcn.feature.service.tracing.template.TracingClientGroovy;
import cloud.graal.gcn.feature.service.tracing.template.TracingClientJava;
import cloud.graal.gcn.feature.service.tracing.template.TracingClientKotlin;
import cloud.graal.gcn.feature.service.tracing.template.TracingControllerGroovy;
import cloud.graal.gcn.feature.service.tracing.template.TracingControllerJava;
import cloud.graal.gcn.feature.service.tracing.template.TracingControllerKotlin;
import cloud.graal.gcn.feature.service.tracing.template.TracingControllerSpec;
import cloud.graal.gcn.feature.service.tracing.template.TracingControllerTestGroovyJUnit;
import cloud.graal.gcn.feature.service.tracing.template.TracingControllerTestJavaJUnit;
import cloud.graal.gcn.feature.service.tracing.template.TracingControllerTestKotest;
import cloud.graal.gcn.feature.service.tracing.template.TracingControllerTestKotlinJUnit;
import cloud.graal.gcn.model.GcnService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetry;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryAnnotations;
import io.micronaut.starter.feature.opentelemetry.OpenTelemetryHttp;

import static cloud.graal.gcn.model.GcnService.TRACING;
import static io.micronaut.starter.application.ApplicationType.DEFAULT;

/**
 * Base class for tracing service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractTracingFeature extends AbstractGcnServiceFeature {

    private final OpenTelemetryHttp openTelemetryHttp;
    private final OpenTelemetry openTelemetry;
    private final OpenTelemetryAnnotations openTelemetryAnnotations;

    /**
     * @param openTelemetry            OpenTelemetry feature
     * @param openTelemetryHttp        OpenTelemetryHttp feature
     * @param openTelemetryAnnotations OpenTelemetryAnnotations feature
     */
    protected AbstractTracingFeature(OpenTelemetry openTelemetry,
                                     OpenTelemetryHttp openTelemetryHttp,
                                     OpenTelemetryAnnotations openTelemetryAnnotations) {
        this.openTelemetry = openTelemetry;
        this.openTelemetryHttp = openTelemetryHttp;
        this.openTelemetryAnnotations = openTelemetryAnnotations;
    }

    @Override
    public final void apply(GcnGeneratorContext generatorContext) {

        if (!generatorContext.generateExampleCode()) {
            addLibPlaceholders(generatorContext);
        }

        doApply(generatorContext);

        Project project = generatorContext.getProject();

        if (generatorContext.generateExampleCode() && generatorContext.getApplicationType() == DEFAULT) {
            generatorContext.addTestTemplate(getModuleName(), "TracingControllerTest-" + getModuleName(),
                    generatorContext.getTestSourcePath("/{packagePath}/controller/TracingController"),
                    TracingControllerSpec.template(project),
                    TracingControllerTestJavaJUnit.template(project),
                    TracingControllerTestGroovyJUnit.template(project),
                    TracingControllerTestKotlinJUnit.template(project),
                    TracingControllerTestKotest.template(project));
        }

        applyForLib(generatorContext, () -> {

            openTelemetry.apply(generatorContext);
            openTelemetryHttp.apply(generatorContext);
            openTelemetryAnnotations.apply(generatorContext);

            if (generatorContext.generateExampleCode() && generatorContext.getApplicationType() == DEFAULT) {

                generatorContext.addTemplate(getDefaultModule(), "tracingController",
                        generatorContext.getSourcePath("/{packagePath}/controller/TracingController"),
                        TracingControllerJava.template(project),
                        TracingControllerKotlin.template(project),
                        TracingControllerGroovy.template(project));

                generatorContext.addTemplate(getDefaultModule(), "tracingClient",
                        generatorContext.getSourcePath("/{packagePath}/client/TracingClient"),
                        TracingClientJava.template(project),
                        TracingClientKotlin.template(project),
                        TracingClientGroovy.template(project));
            }
        });
    }

    @NonNull
    @Override
    public final GcnService getService() {
        return TRACING;
    }

    /**
     * Implemented in subclasses to apply cloud-specific changes.
     *
     * @param generatorContext the generator context
     */
    protected abstract void doApply(GcnGeneratorContext generatorContext);
}
