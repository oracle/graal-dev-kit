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
import cloud.graal.gcn.feature.service.tracing.template.InventoryServiceGroovy;
import cloud.graal.gcn.feature.service.tracing.template.InventoryServiceJava;
import cloud.graal.gcn.feature.service.tracing.template.InventoryServiceKotlin;
import cloud.graal.gcn.feature.service.tracing.template.StoreControllerGroovy;
import cloud.graal.gcn.feature.service.tracing.template.StoreControllerJava;
import cloud.graal.gcn.feature.service.tracing.template.StoreControllerKotlin;
import cloud.graal.gcn.feature.service.tracing.template.StoreControllerSpec;
import cloud.graal.gcn.feature.service.tracing.template.StoreControllerTestGroovyJUnit;
import cloud.graal.gcn.feature.service.tracing.template.StoreControllerTestJava;
import cloud.graal.gcn.feature.service.tracing.template.StoreControllerTestKotest;
import cloud.graal.gcn.feature.service.tracing.template.StoreControllerTestKotlinJUnit;
import cloud.graal.gcn.feature.service.tracing.template.WarehouseClientGroovy;
import cloud.graal.gcn.feature.service.tracing.template.WarehouseClientJava;
import cloud.graal.gcn.feature.service.tracing.template.WarehouseClientKotlin;
import cloud.graal.gcn.feature.service.tracing.template.WarehouseControllerGroovy;
import cloud.graal.gcn.feature.service.tracing.template.WarehouseControllerJava;
import cloud.graal.gcn.feature.service.tracing.template.WarehouseControllerKotlin;
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
            generatorContext.addTestTemplate(getModuleName(), "StoreControllerTest-" + getModuleName(),
                    generatorContext.getTestSourcePath("/{packagePath}/StoreController"),
                    StoreControllerSpec.template(project),
                    StoreControllerTestJava.template(project),
                    StoreControllerTestGroovyJUnit.template(project),
                    StoreControllerTestKotlinJUnit.template(project),
                    StoreControllerTestKotest.template(project));
        }

        applyForLib(generatorContext, () -> {
            openTelemetry.apply(generatorContext);
            openTelemetryHttp.apply(generatorContext);
            openTelemetryAnnotations.apply(generatorContext);

            if (generatorContext.generateExampleCode() && generatorContext.getApplicationType() == DEFAULT) {

                generatorContext.addTemplate(getDefaultModule(), "InventoryService",
                        generatorContext.getSourcePath("/{packagePath}/InventoryService"),
                        InventoryServiceJava.template(project),
                        InventoryServiceKotlin.template(project),
                        InventoryServiceGroovy.template(project));

                generatorContext.addTemplate(getDefaultModule(), "StoreController",
                        generatorContext.getSourcePath("/{packagePath}/StoreController"),
                        StoreControllerJava.template(project),
                        StoreControllerKotlin.template(project),
                        StoreControllerGroovy.template(project));

                generatorContext.addTemplate(getDefaultModule(), "WarehouseClient",
                        generatorContext.getSourcePath("/{packagePath}/WarehouseClient"),
                        WarehouseClientJava.template(project),
                        WarehouseClientKotlin.template(project),
                        WarehouseClientGroovy.template(project));

                generatorContext.addTemplate(getDefaultModule(), "WarehouseController",
                        generatorContext.getSourcePath("/{packagePath}/WarehouseController"),
                        WarehouseControllerJava.template(project),
                        WarehouseControllerKotlin.template(project),
                        WarehouseControllerGroovy.template(project));
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
