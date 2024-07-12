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
package cloud.graal.gdk.feature.create;

import cloud.graal.gdk.buildtool.GdkGradleBuild;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.MavenLocal;
import io.micronaut.starter.build.Repository;
import io.micronaut.starter.build.gradle.GradleBuild;
import io.micronaut.starter.build.gradle.GradleBuildCreator;
import io.micronaut.starter.build.gradle.GradlePlugin;
import io.micronaut.starter.build.gradle.GradleRepository;
import jakarta.inject.Singleton;

import java.util.List;

import static io.micronaut.starter.build.gradle.GradleDsl.GROOVY;

/**
 * Extends GradleBuildCreator to add the Oracle Maven repo.
 *
 * @since 1.0.0
 */
@Singleton
public class GdkGradleBuildCreator extends GradleBuildCreator {

    private static final GdkRepository REPO = new GdkRepository();

    @NonNull
    @Override
    public GradleBuild create(@NonNull GeneratorContext generatorContext,
                              List<Repository> repositories,
                              boolean useVersionCatalogue) {

        GradleBuild build = super.create(generatorContext, repositories, useVersionCatalogue);

        List<GradlePlugin> plugins = generatorContext.getBuildPlugins()
                .stream()
                .filter(GradlePlugin.class::isInstance)
                .map(GradlePlugin.class::cast)
                .sorted(OrderUtil.COMPARATOR)
                .toList();

        return new GdkGradleBuild(
                build.getDsl(),
                build.getDependencies(),
                plugins,
                getRepositories(generatorContext, repositories));
    }

    @Override
    @NonNull
    protected List<GradleRepository> getRepositories(@NonNull GeneratorContext generatorContext,
                                                     List<Repository> repositories) {

        if (repositories.stream().noneMatch(it -> it.getId().equals(REPO.getId()))) {
            repositories.add(REPO);
        }

        MavenLocal mavenLocal = new MavenLocal();

        if (repositories.stream().anyMatch(it -> it.getId().equals(mavenLocal.getId()))) {
            repositories.removeIf(it -> it.getId().equals(mavenLocal.getId()));
            repositories.add(0, mavenLocal);
        }

        return GradleRepository.listOf(
                generatorContext.getBuildTool().getGradleDsl().orElse(GROOVY),
                repositories
        );
    }
}
