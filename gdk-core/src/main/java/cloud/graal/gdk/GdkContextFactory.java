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
package cloud.graal.gdk;

import cloud.graal.gdk.feature.GdkFeature;
import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.ContextFactory;
import io.micronaut.starter.application.OperatingSystem;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.build.dependencies.CoordinateResolver;
import io.micronaut.starter.build.dependencies.DefaultCoordinateResolver;
import io.micronaut.starter.feature.AvailableFeatures;
import io.micronaut.starter.feature.DefaultFeature;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.feature.FeatureContext;
import io.micronaut.starter.feature.validation.FeatureValidator;
import io.micronaut.starter.feature.validation.ProjectNameValidator;
import io.micronaut.starter.io.ConsoleOutput;
import io.micronaut.starter.options.Options;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Replaces the default <code>ContextFactory</code> to create <code>GdkFeatureContext</code>
 * instead of <code>FeatureContext</code> and <code>GdkGeneratorContext</code>
 * instead of <code>GeneratorContext</code>.
 */
@Singleton
@Replaces(ContextFactory.class)
public class GdkContextFactory extends ContextFactory {

    private final FeatureValidator featureValidator;
    private final CoordinateResolver coordinateResolver;
    private final ProjectNameValidator projectNameValidator;

    /**
     * @param featureValidator   FeatureValidator
     * @param coordinateResolver DefaultCoordinateResolver
     */
    public GdkContextFactory(FeatureValidator featureValidator,
                             DefaultCoordinateResolver coordinateResolver,
                             ProjectNameValidator projectNameValidator) {
        super(featureValidator, coordinateResolver, projectNameValidator);
        this.coordinateResolver = coordinateResolver;
        this.featureValidator = featureValidator;
        this.projectNameValidator = projectNameValidator;
    }

    @Override
    public GdkFeatureContext createFeatureContext(AvailableFeatures available,
                                                  List<String> selectedNames,
                                                  ApplicationType type,
                                                  Options initialOptions,
                                                  @Nullable OperatingSystem os) {

        FeatureContext realContext = super.createFeatureContext(available, selectedNames, type, initialOptions, os);
        Options options = realContext.getOptions();
        Set<Feature> selectedAndDefaultFeatures = new HashSet<>(realContext.getSelectedFeatures());
        Set<GdkCloud> clouds = selectedAndDefaultFeatures.stream()
                .filter(feature -> feature instanceof GdkFeature)
                .map(GdkFeature.class::cast)
                .map(GdkFeature::getCloud)
                .collect(Collectors.toSet());

        available.getAllFeatures()
                .filter(DefaultFeature.class::isInstance)
                .map(DefaultFeature.class::cast)
                .filter(f -> f.shouldApply(type, options, selectedAndDefaultFeatures))
                .forEach(selectedAndDefaultFeatures::add);

        return new GdkFeatureContext(options, type, os, selectedAndDefaultFeatures, selectedNames, clouds);
    }

    public GdkGeneratorContext createGeneratorContext(Project project,
                                                      FeatureContext featureContext,
                                                      ConsoleOutput consoleOutput) {
        if (project != null) {
            projectNameValidator.validate(project);
        }

        featureContext.processSelectedFeatures();

        Set<Feature> finalFeatures = featureContext.getFinalFeatures(consoleOutput);

        featureValidator.validatePostProcessing(featureContext.getOptions(), featureContext.getApplicationType(), finalFeatures);

        List<Feature> finalFeaturesList = new ArrayList<>(finalFeatures);
        finalFeaturesList.sort(Comparator.comparingInt(Feature::getOrder));

        return new GdkGeneratorContext(
                project,
                (GdkFeatureContext) featureContext,
                new LinkedHashSet<>(finalFeaturesList),
                coordinateResolver);
    }
}
