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
import cloud.graal.gdk.feature.service.AbstractGdkServiceFeature;
import cloud.graal.gdk.feature.service.metrics.template.MetricsServiceGroovy;
import cloud.graal.gdk.feature.service.metrics.template.MetricsServiceJava;
import cloud.graal.gdk.feature.service.metrics.template.MetricsServiceKotlin;
import cloud.graal.gdk.feature.service.metrics.template.MetricsServiceSpec;
import cloud.graal.gdk.feature.service.metrics.template.MetricsServiceTestGroovyJUnit;
import cloud.graal.gdk.feature.service.metrics.template.MetricsServiceTestJavaJUnit;
import cloud.graal.gdk.feature.service.metrics.template.MetricsServiceTestKotest;
import cloud.graal.gdk.feature.service.metrics.template.MetricsServiceTestKotlinJUnit;
import cloud.graal.gdk.model.GdkService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.feature.micrometer.Core;
import io.micronaut.starter.feature.micrometer.MicrometerAnnotations;
import io.micronaut.starter.feature.other.Management;

import java.util.Map;

import static cloud.graal.gdk.model.GdkService.METRICS;
import static io.micronaut.starter.application.ApplicationType.DEFAULT;

/**
 * Base class for metrics service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractMetricsFeature extends AbstractGdkServiceFeature {

    private final Core core;
    private final Management management;
    private final MicrometerAnnotations micrometerAnnotations;

    /**
     * @param core                  the Core feature
     * @param management            the Management feature
     * @param micrometerAnnotations the MicrometerAnnotations feature
     */
    protected AbstractMetricsFeature(Core core,
                                     Management management,
                                     MicrometerAnnotations micrometerAnnotations) {
        this.core = core;
        this.management = management;
        this.micrometerAnnotations = micrometerAnnotations;
    }

    @Override
    public void processSelectedFeatures(GdkFeatureContext featureContext) {
        featureContext.addFeature(core, Core.class);
        featureContext.addFeature(management, Management.class);
        featureContext.addFeature(micrometerAnnotations, MicrometerAnnotations.class);
    }

    @Override
    public final void apply(GdkGeneratorContext generatorContext) {

        doApply(generatorContext);

        Project project = generatorContext.getProject();

        applyForLib(generatorContext, () -> {

            core.apply(generatorContext);
            management.apply(generatorContext);
            micrometerAnnotations.apply(generatorContext);

            if (generatorContext.generateExampleCode() && generatorContext.getApplicationType() == DEFAULT) {
                generatorContext.addTemplate(getDefaultModule(), "metricsService",
                        generatorContext.getSourcePath("/{packagePath}/service/MetricsService"),
                        MetricsServiceJava.template(project),
                        MetricsServiceKotlin.template(project),
                        MetricsServiceGroovy.template(project));
            }
        });

        generatorContext.getConfiguration().addNested(Map.of(
                "micronaut.metrics.enabled", "true",
                "micronaut.metrics.binders.files.enabled", "true",
                "micronaut.metrics.binders.jvm.enabled", "true",
                "micronaut.metrics.binders.logback.enabled", "true",
                "micronaut.metrics.binders.processor.enabled", "true",
                "micronaut.metrics.binders.uptime.enabled", "true",
                "micronaut.metrics.binders.web.enabled", "true"
        ));

        if (generatorContext.generateExampleCode() && generatorContext.getApplicationType() == DEFAULT) {

            generatorContext.getTestConfiguration().addNested(Map.of(
                    "custom.thread.count.initialDelay", "10h",
                    "micronaut.metrics.enabled", true
            ));

            generatorContext.addTestTemplate(getModuleName(), "MetricsServiceTest-" + getModuleName(),
                    generatorContext.getTestSourcePath("/{packagePath}/service/MetricsService"),
                    MetricsServiceSpec.template(project),
                    MetricsServiceTestJavaJUnit.template(project),
                    MetricsServiceTestGroovyJUnit.template(project),
                    MetricsServiceTestKotlinJUnit.template(project),
                    MetricsServiceTestKotest.template(project));
        }
    }

    @NonNull
    @Override
    public final GdkService getService() {
        return METRICS;
    }

    /**
     * Implemented in subclasses to apply cloud-specific changes.
     *
     * @param generatorContext the generator context
     */
    protected abstract void doApply(GdkGeneratorContext generatorContext);
}
