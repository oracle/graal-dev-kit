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

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.FunctionAvailableFeatures;
import io.micronaut.starter.feature.Feature;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.micronaut.starter.application.ApplicationType.FUNCTION;

/**
 * Replaces <code>FunctionAvailableFeatures</code> to hide auto-selected GCN
 * features from autocompletion in the CLI.
 *
 * @since 1.0.0
 */
@Named("function")
@Replaces(FunctionAvailableFeatures.class)
@Singleton
public class GcnFunctionAvailableFeatures extends FunctionAvailableFeatures {

    private final Map<String, GcnFeature> gcnFeatures;

    /**
     * @param features all feature beans
     */
    public GcnFunctionAvailableFeatures(List<Feature> features) {
        super(features.stream()
                .filter(f -> !(f instanceof GcnFeature))
                .sorted(Comparator.comparing(Feature::getName))
                .collect(Collectors.toList()));

        // filter out our features so they're not visible in the cli,
        // but we need these features for findFeature below

        gcnFeatures = features.stream()
                .filter(f -> f.supports(FUNCTION))
                .filter(f -> f instanceof GcnFeature)
                .map(GcnFeature.class::cast)
                .collect(Collectors.toMap(Feature::getName, Function.identity()));
    }

    @Override
    public Optional<Feature> findFeature(@NonNull String name, boolean ignoreVisibility) {
        Optional<Feature> feature = super.findFeature(name, ignoreVisibility);
        if (feature.isPresent()) {
            return feature;
        }
        return Optional.ofNullable(gcnFeatures.get(name));
    }
}
