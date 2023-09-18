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
package cloud.graal.gcn;

import cloud.graal.gcn.feature.GcnDefaultAvailableFeatures;
import cloud.graal.gcn.feature.GcnFunctionAvailableFeatures;
import cloud.graal.gcn.template.GcnTemplateRenderer;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.OperatingSystem;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.application.generator.ProjectGenerator;
import io.micronaut.starter.feature.AvailableFeatures;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.feature.FeatureContext;
import io.micronaut.starter.feature.cli;
import io.micronaut.starter.io.ConsoleOutput;
import io.micronaut.starter.io.OutputHandler;
import io.micronaut.starter.options.Options;
import io.micronaut.starter.template.RenderResult;
import io.micronaut.starter.template.RockerTemplate;
import io.micronaut.starter.template.Template;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.micronaut.starter.application.ApplicationType.FUNCTION;
import static io.micronaut.starter.template.Template.ROOT;

/**
 * Replaces the default generator to customize generation with
 * GcnContextFactory, GcnGeneratorContext, GcnTemplateRenderer.
 */
@Singleton
@Replaces(ProjectGenerator.class)
public class GcnProjectGenerator implements ProjectGenerator {

    private final List<Feature> features;
    private final GcnContextFactory contextFactory;

    /**
     * @param contextFactory context factory
     * @param features       all feature beans
     */
    public GcnProjectGenerator(GcnContextFactory contextFactory,
                               List<Feature> features) {
        this.features = features;
        this.contextFactory = contextFactory;
    }

    @Override
    public void generate(ApplicationType applicationType,
                         Project project,
                         Options options,
                         @Nullable OperatingSystem operatingSystem,
                         List<String> selectedFeatures,
                         OutputHandler outputHandler,
                         ConsoleOutput consoleOutput) throws Exception {

        GeneratorContext generatorContext = createGeneratorContext(
                applicationType, project, options, operatingSystem, selectedFeatures, consoleOutput);

        generate(applicationType, project, outputHandler, generatorContext);
    }

    @Override
    public void generate(ApplicationType applicationType,
                         Project project,
                         OutputHandler outputHandler,
                         GeneratorContext gc) throws Exception {

        GcnGeneratorContext generatorContext = (GcnGeneratorContext) gc;

        addMicronautCli(generatorContext, applicationType);
        generatorContext.applyFeatures();

        try (GcnTemplateRenderer templateRenderer = new GcnTemplateRenderer(project, outputHandler,
                generatorContext.getPostProcessors(), generatorContext.getRegexPostProcessors(), generatorContext.getClouds())) {
            for (Map.Entry<String, Template> entry : generatorContext.getTemplates().entrySet()) {
                RenderResult renderResult = templateRenderer.render(entry.getValue(), entry.getKey());
                if (renderResult.getError() != null) {
                    throw renderResult.getError();
                }
            }
        }
    }

    /**
     * Add a feature; only used in test project generation.
     *
     * @param feature the feature
     */
    public void addFeature(Feature feature) {
        features.add(feature);
    }

    private void addMicronautCli(GcnGeneratorContext generatorContext,
                                 ApplicationType applicationType) {

        List<String> featureNames = new ArrayList<>(generatorContext.getFeatures());
        featureNames.sort(Comparator.comparing(Function.identity()));

        generatorContext.addTemplate("micronautCli",
                new RockerTemplate(ROOT, "micronaut-cli.yml", cli.template(
                        generatorContext.getLanguage(),
                        generatorContext.getTestFramework(),
                        generatorContext.getBuildTool(),
                        generatorContext.getLibProject(),
                        featureNames,
                        applicationType)));
    }

    @Override
    public GeneratorContext createGeneratorContext(ApplicationType applicationType,
                                                   Project project,
                                                   Options options,
                                                   @Nullable OperatingSystem operatingSystem,
                                                   List<String> selectedFeatures,
                                                   ConsoleOutput consoleOutput) {

        AvailableFeatures availableFeatures = applicationType == FUNCTION
                ? new GcnFunctionAvailableFeatures(features)
                : new GcnDefaultAvailableFeatures(features);
        FeatureContext featureContext = contextFactory.createFeatureContext(
                availableFeatures, selectedFeatures, applicationType, options, operatingSystem);
        return contextFactory.createGeneratorContext(project, featureContext, consoleOutput);
    }
}
