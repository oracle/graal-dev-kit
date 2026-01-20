/*
 * Copyright 2026 Oracle and/or its affiliates
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
import io.micronaut.context.annotation.Requires;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.build.dependencies.DependencyCoordinate;
import io.micronaut.starter.build.gradle.GradlePlugin;
import io.micronaut.starter.feature.jib.Jib;
import jakarta.inject.Singleton;

import static io.micronaut.core.util.StringUtils.TRUE;

/**
 * Replaces the default feature to override the plugin version to one that supports JDK 25.
 *
 * @since 4.10.1.2
 */
@Replaces(Jib.class)
@Singleton
@Requires(property = "micronaut.starter.feature.jib.enabled", value = TRUE, defaultValue = TRUE)
public class GdkJib extends Jib {

    /**
     * The artifact id.
     */
    public static final String JIB_ARTIFACT_ID = "jib-gradle-plugin";

    /**
     * The group id.
     */
    public static final String JIB_GROUP_ID = "gradle.plugin.com.google.cloud.tools";

    /**
     * The plugin id.
     */
    public static final String JIB_PLUGIN_ID = "com.google.cloud.tools.jib";

    /**
     * The version override; version 2.8.0 doesn't work with Gradle 9.
     */
    public static final String JIB_VERSION = "3.5.2";

    /**
     * The full dependency.
     */
    public static final DependencyCoordinate COORDINATE = Dependency.builder()
            .groupId(JIB_GROUP_ID)
            .artifactId(JIB_ARTIFACT_ID)
            .version(JIB_VERSION)
            .buildCoordinate();

    @Override
    public void apply(GeneratorContext generatorContext) {
        if (generatorContext.getBuildTool().isGradle()) {
            generatorContext.addHelpLink("Jib Gradle Plugin",
                    "https://plugins.gradle.org/plugin/" + JIB_PLUGIN_ID);
            generatorContext.addBuildPlugin(GradlePlugin.builder()
                    .id(JIB_PLUGIN_ID)
                    .lookupArtifactId(JIB_ARTIFACT_ID)
                    .version(JIB_VERSION)
                    .build());
        }
    }
}
