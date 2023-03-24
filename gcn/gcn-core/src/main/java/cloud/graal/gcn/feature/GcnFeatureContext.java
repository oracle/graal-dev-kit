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
package cloud.graal.gcn.feature;

import cloud.graal.gcn.model.GcnCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.OperatingSystem;
import io.micronaut.starter.feature.DefaultFeature;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.feature.FeatureContext;
import io.micronaut.starter.feature.FeaturePredicate;
import io.micronaut.starter.feature.build.BuildFeature;
import io.micronaut.starter.feature.database.DatabaseDriverFeature;
import io.micronaut.starter.io.ConsoleOutput;
import io.micronaut.starter.options.Options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cloud.graal.gcn.GcnGeneratorContext.EXAMPLE_CODE;
import static cloud.graal.gcn.model.GcnCloud.NONE;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

/**
 * Cloud-aware feature context.
 */
public class GcnFeatureContext extends FeatureContext {

    private final Set<Feature> selectedAndDefaultFeatures;
    private final Collection<String> selectedNames;
    private final Set<GcnCloud> selectedClouds;
    private final boolean generateExampleCode;
    private final List<Feature> features = new ArrayList<>();
    private final List<FeaturePredicate> exclusions = new ArrayList<>();
    private ListIterator<Feature> iterator;

    private final Map<GcnCloud, Collection<Feature>> addedFeatures = new HashMap<>();

    private GcnCloud cloud = NONE;

    /**
     * @param options                    Options
     * @param applicationType            application type
     * @param operatingSystem            OS
     * @param selectedAndDefaultFeatures features that the user selected and default features
     * @param selectedNames              the names of the features selected by the user, and the
     *                                   names of the GCN "create" and "service" features that
     *                                   were auto-selected based on the selected clouds and services
     * @param selectedClouds             the selected clouds
     */
    public GcnFeatureContext(Options options,
                             ApplicationType applicationType,
                             OperatingSystem operatingSystem,
                             Set<Feature> selectedAndDefaultFeatures,
                             Collection<String> selectedNames,
                             Set<GcnCloud> selectedClouds) {
        super(options, applicationType, operatingSystem, selectedAndDefaultFeatures);
        this.selectedAndDefaultFeatures = selectedAndDefaultFeatures;
        this.selectedNames = selectedNames;
        this.selectedClouds = selectedClouds;
        generateExampleCode = getOptions().get(EXAMPLE_CODE, Boolean.class).orElse(true);
    }

    @Override
    public void processSelectedFeatures() {

        processSelectedFeatures(NONE);

        for (GcnCloud cloud : selectedClouds) {
            processSelectedFeatures(cloud);
        }

        cloud = NONE;
    }

    private void processSelectedFeatures(GcnCloud cloud) {
        this.cloud = cloud;
        addedFeatures.put(cloud, new HashSet<>());

        features.clear();
        features.addAll(selectedAndDefaultFeatures.stream()
                .filter(f -> {

                    if (f instanceof BuildFeature || f instanceof DatabaseDriverFeature) {
                        return true;
                    }

                    if (f instanceof GcnFeature) {
                        return cloud == ((GcnFeature) f).getCloud();
                    }

                    if (!(f instanceof DefaultFeature)) {
                        // selected, apply to all
                        return true;
                    }

                    return cloud == NONE;
                })
                .collect(Collectors.toSet()));

        features.sort(Comparator.comparingInt(Feature::getOrder));
        iterator = features.listIterator();
        while (iterator.hasNext()) {
            Feature feature = iterator.next();
            feature.processSelectedFeatures(this);
        }
        iterator = null;
    }

    @Override
    public void exclude(FeaturePredicate exclusion) {
        exclusions.add(exclusion);
    }

    @Override
    public Set<Feature> getFinalFeatures(ConsoleOutput consoleOutput) {

        Collection<Feature> allFeatures = new HashSet<>(selectedAndDefaultFeatures);
        for (Collection<Feature> added : addedFeatures.values()) {
            allFeatures.addAll(added);
        }

        return allFeatures.stream().filter(feature -> {
            for (FeaturePredicate predicate : exclusions) {
                if (predicate.test(feature)) {
                    predicate.getWarning().ifPresent(consoleOutput::warning);
                    return false;
                }
            }
            return true;
        }).collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    @Override
    public void addFeature(Feature feature) {

        addedFeatures.get(cloud).add(feature);

        if (iterator != null) {
            iterator.add(feature);
        } else {
            features.add(feature);
        }
        feature.processSelectedFeatures(this);
    }

    /**
     * Add the feature if it hasn't been added yet.
     *
     * @param feature      the feature
     * @param featureClass the feature type
     */
    public void addFeature(Feature feature, Class<? extends Feature> featureClass) {
        addFeatureIfNotPresent(featureClass, feature);
    }

    @Override
    public boolean isPresent(Class<? extends Feature> feature) {
        return features.stream()
                .filter(f -> exclusions.stream().noneMatch(e -> e.test(f)))
                .map(Feature::getClass)
                .anyMatch(feature::isAssignableFrom);
    }

    /**
     * Features add other features by calling {@link #addFeature(Feature) addFeature}
     * from {@link AbstractGcnFeature#processSelectedFeatures(GcnFeatureContext)}, and
     * this happens in <code>GcnContextFactory</code> before the generator
     * context is constructed. Features are partitioned by cloud before invoking
     * these methods, so the cloud is known and is used to record the addition
     * here. Later when GcnGeneratorContext is being constructed, the cloud
     * information is used when partitioning features between modules.
     *
     * @param feature the feature
     * @return the cloud(s) that caused the feature to be added
     */
    @NonNull
    public Set<GcnCloud> getAddedFeatureClouds(Feature feature) {

        Set<GcnCloud> clouds = new HashSet<>();

        for (Map.Entry<GcnCloud, Collection<Feature>> e : addedFeatures.entrySet()) {
            if (e.getKey() == NONE) {
                continue;
            }
            if (e.getValue().contains(feature)) {
                clouds.add(e.getKey());
            }
        }

        return clouds;
    }

    /**
     * @return the names of the features selected by the user, and the names of
     * the GCN "create" and "service" features that were auto-selected based on
     * the selected clouds and services
     */
    public Collection<String> getSelectedNames() {
        return Collections.unmodifiableCollection(selectedNames);
    }

    /**
     * @return features that the user selected and default features
     */
    public Set<Feature> getSelectedAndDefaultFeatures() {
        return Collections.unmodifiableSet(selectedAndDefaultFeatures);
    }

    /**
     * @return true to generate example code
     */
    public boolean generateExampleCode() {
        return generateExampleCode;
    }
}
