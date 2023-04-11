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
package cloud.graal.gcn.buildtool;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.Property;
import io.micronaut.starter.build.dependencies.Coordinate;
import io.micronaut.starter.build.maven.MavenBuild;
import io.micronaut.starter.build.maven.MavenCombineAttribute;
import io.micronaut.starter.build.maven.MavenDependency;
import io.micronaut.starter.build.maven.MavenPlugin;
import io.micronaut.starter.build.maven.MavenRepository;
import io.micronaut.starter.feature.build.maven.Profile;

import java.util.Collection;
import java.util.List;

/**
 * Overrides MavenBuild to properly render the repositories.
 *
 * @since 1.0.0
 */
public class GcnMavenBuild extends MavenBuild {

    private final List<MavenRepository> repositories;

    public GcnMavenBuild(String artifactId,
                         List<Coordinate> annotationProcessors,
                         List<Coordinate> testAnnotationProcessors,
                         List<MavenDependency> dependencies,
                         List<Property> properties,
                         List<MavenPlugin> plugins,
                         List<MavenRepository> repositories,
                         MavenCombineAttribute annotationProcessorCombineAttribute,
                         MavenCombineAttribute testAnnotationProcessorCombineAttribute,
                         Collection<Profile> profiles) {
        super(artifactId, annotationProcessors, testAnnotationProcessors, dependencies, properties, plugins,
                repositories, annotationProcessorCombineAttribute, testAnnotationProcessorCombineAttribute, profiles);
        this.repositories = repositories;
    }

    @NonNull
    @Override
    public String renderRepositories(int indentationSpaces) {
        return Utils.renderRepositories(repositories);
    }
}
