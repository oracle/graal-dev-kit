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
package cloud.graal.gcn.feature.service.metrics;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.GcnFeatureContext;
import cloud.graal.gcn.feature.service.AbstractGcnServiceFeature;
import cloud.graal.gcn.feature.service.metrics.template.MetricsServiceGroovy;
import cloud.graal.gcn.feature.service.metrics.template.MetricsServiceJava;
import cloud.graal.gcn.feature.service.metrics.template.MetricsServiceKotlin;
import cloud.graal.gcn.feature.service.metrics.template.MetricsServiceSpec;
import cloud.graal.gcn.feature.service.metrics.template.MetricsServiceTestGroovyJUnit;
import cloud.graal.gcn.feature.service.metrics.template.MetricsServiceTestJavaJUnit;
import cloud.graal.gcn.feature.service.metrics.template.MetricsServiceTestKotest;
import cloud.graal.gcn.feature.service.metrics.template.MetricsServiceTestKotlinJUnit;
import cloud.graal.gcn.model.GcnService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.feature.micrometer.Core;
import io.micronaut.starter.feature.micrometer.MicrometerAnnotations;
import io.micronaut.starter.feature.other.Management;

import java.util.Map;

import static cloud.graal.gcn.model.GcnService.METRICS;
import static io.micronaut.starter.application.ApplicationType.DEFAULT;

/**
 * Base class for metrics service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractMetricsFeature extends AbstractGcnServiceFeature {

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
    public void processSelectedFeatures(GcnFeatureContext featureContext) {
        featureContext.addFeature(core, Core.class);
        featureContext.addFeature(management, Management.class);
        featureContext.addFeature(micrometerAnnotations, MicrometerAnnotations.class);
    }

    @Override
    public final void apply(GcnGeneratorContext generatorContext) {

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
    public final GcnService getService() {
        return METRICS;
    }

    /**
     * Implemented in subclasses to apply cloud-specific changes.
     *
     * @param generatorContext the generator context
     */
    protected abstract void doApply(GcnGeneratorContext generatorContext);
}
