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
package cloud.graal.gcn.feature.create;

import cloud.graal.gcn.buildtool.GcnMavenBuild;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.Repository;
import io.micronaut.starter.build.dependencies.Coordinate;
import io.micronaut.starter.build.maven.MavenBuild;
import io.micronaut.starter.build.maven.MavenBuildCreator;
import io.micronaut.starter.build.maven.MavenPlugin;
import io.micronaut.starter.build.maven.MavenRepository;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import static io.micronaut.starter.build.Repository.micronautRepositories;

/**
 * Extends MavenBuildCreator to add the Oracle Maven repo.
 *
 * @since 1.0.0
 */
@Singleton
public class GcnMavenBuildCreator extends MavenBuildCreator {

    @NonNull
    @Override
    public MavenBuild create(GeneratorContext generatorContext) {
        MavenBuild build = super.create(generatorContext);

        List<MavenPlugin> plugins = generatorContext.getBuildPlugins()
                .stream()
                .filter(MavenPlugin.class::isInstance)
                .map(MavenPlugin.class::cast)
                .sorted(OrderUtil.COMPARATOR)
                .collect(Collectors.toList());

        return new GcnMavenBuild(
                build.getArtifactId(),
                deduplicate(build.getAnnotationProcessors()),
                deduplicate(build.getTestAnnotationProcessors()),
                build.getDependencies(),
                build.getProperties(),
                plugins,
                getRepositories(),
                build.getAnnotationProcessorCombineAttribute(),
                build.getTestAnnotationProcessorCombineAttribute(),
                build.getProfiles());
    }

    @NonNull
    @Override
    protected List<MavenRepository> getRepositories() {
        List<Repository> repositories = micronautRepositories();
        repositories.add(0, new GcnRepository());
        return MavenRepository.listOf(repositories);
    }

    private List<Coordinate> deduplicate(List<Coordinate> annotationProcessors) {
        return new ArrayList<>(new LinkedHashSet<>(annotationProcessors));
    }
}
