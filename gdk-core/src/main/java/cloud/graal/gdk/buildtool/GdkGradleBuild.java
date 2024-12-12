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
package cloud.graal.gdk.buildtool;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.gradle.GradleBuild;
import io.micronaut.starter.build.gradle.GradleDependency;
import io.micronaut.starter.build.gradle.GradleDsl;
import io.micronaut.starter.build.gradle.GradlePlugin;
import io.micronaut.starter.build.gradle.GradleRepository;

import java.util.List;

/**
 * Overrides GradleBuild to properly render the repositories.
 *
 * @since 1.0.0
 */
public class GdkGradleBuild extends GradleBuild {

    private final List<GradleRepository> repositories;

    /**
     * @param gradleDsl    Gradle DSL
     * @param dependencies dependencies
     * @param plugins      plugins
     * @param repositories repos
     */
    public GdkGradleBuild(@NonNull GradleDsl gradleDsl,
                          @NonNull List<GradleDependency> dependencies,
                          @NonNull List<GradlePlugin> plugins,
                          @NonNull List<GradleRepository> repositories) {
        super(gradleDsl, dependencies, plugins, repositories);
        this.repositories = repositories;
    }

    @NonNull
    @Override
    public String renderRepositories() {
        return Utils.renderRepositories(repositories);
    }
}
