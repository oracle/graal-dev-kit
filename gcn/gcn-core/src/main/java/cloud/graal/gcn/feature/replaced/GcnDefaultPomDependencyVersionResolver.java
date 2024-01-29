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
package cloud.graal.gcn.feature.replaced;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.dependencies.Coordinate;
import io.micronaut.starter.build.dependencies.DefaultPomDependencyVersionResolver;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.build.dependencies.StarterCoordinates;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//TODO: Remove this class after in new release.
@Singleton
@Replaces(DefaultPomDependencyVersionResolver.class)
public class GcnDefaultPomDependencyVersionResolver extends DefaultPomDependencyVersionResolver {

    public static final Coordinate MICRONAUT_GRADLE_PLUGIN = Dependency.builder().groupId("io.micronaut.gradle").artifactId("micronaut-gradle-plugin").version("4.2.1").pom(false).buildCoordinate();
    public static final Coordinate MICRONAUT_CRAC_PLUGIN = Dependency.builder().groupId("io.micronaut.gradle").artifactId("micronaut-crac-plugin").version("4.2.1").pom(false).buildCoordinate();
    public static final Coordinate MICRONAUT_TEST_RESOURCES_PLUGIN = Dependency.builder().groupId("io.micronaut.gradle").artifactId("micronaut-test-resources-plugin").version("4.2.1").pom(false).buildCoordinate();

    private static final Map<String, Coordinate> COORDINATES;

    public GcnDefaultPomDependencyVersionResolver() {
    }

    public @NonNull Optional<Coordinate> resolve(@NonNull String artifactId) {
        return Optional.ofNullable(COORDINATES.get(artifactId));
    }

    public @NonNull Map<String, Coordinate> getCoordinates() {
        return COORDINATES;
    }

    static {
        HashMap<String, Coordinate> coordinateHashMap = new HashMap<>(StarterCoordinates.ALL_COORDINATES);
        coordinateHashMap.put("micronaut-gradle-plugin", MICRONAUT_GRADLE_PLUGIN);
        coordinateHashMap.put("micronaut-test-resources-plugin", MICRONAUT_TEST_RESOURCES_PLUGIN);
        coordinateHashMap.put("micronaut-crac-plugin", MICRONAUT_CRAC_PLUGIN);
        COORDINATES = Collections.unmodifiableMap(coordinateHashMap);
    }

}
