/*
 * Copyright 2024 Oracle and/or its affiliates
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
package cloud.graal.gdk.feature.replaced;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.dependencies.Coordinate;
import io.micronaut.starter.build.dependencies.DefaultPomDependencyVersionResolver;
import io.micronaut.starter.build.dependencies.StarterCoordinates;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static cloud.graal.gdk.build.dependencies.GdkDependencies.ALL_DEPENDENCIES;

@Singleton
@Replaces(DefaultPomDependencyVersionResolver.class)
public class GdkDefaultPomDependencyVersionResolver extends DefaultPomDependencyVersionResolver {

    private static final Map<String, Coordinate> COORDINATES;

    @NonNull
    public Optional<Coordinate> resolve(@NonNull String artifactId) {
        return Optional.ofNullable(COORDINATES.get(artifactId));
    }

    @NonNull
    public Map<String, Coordinate> getCoordinates() {
        return COORDINATES;
    }

    static {
        var coordinates = new HashMap<>(StarterCoordinates.ALL_COORDINATES);
        coordinates.putAll(ALL_DEPENDENCIES);
        COORDINATES = Collections.unmodifiableMap(coordinates);
    }
}
