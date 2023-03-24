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
package cloud.graal.gcn.template;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.gradle.GradleDsl;

import java.util.regex.Pattern;

import static io.micronaut.starter.build.gradle.GradleDsl.GROOVY;

/**
 * Various updates to build.gradle scripts:
 * - changes the version from "0.1" to "1.0-SNAPSHOT"
 * - adds configuration to set the Docker image name to be related to the project, e.g. "demo-oci"
 * - removes versions from plugin declarations (versions are declared in buildSrc/build.gradle)
 *   as a workaround for <a href="https://github.com/gradle/gradle/issues/17559">this Gradle bug</a>
 *
 * @since 1.0.0
 */
public class BuildGradlePostProcessor implements TemplatePostProcessor {

    private static final String DEFAULT_VERSION = "version = \"0.1\"";
    private static final String SNAPSHOT_VERSION = "version = \"1.0-SNAPSHOT\"";

    private static final String DOCKER_IMAGE_NAME_GROOVY =
            "\ntasks.named('dockerBuild') {\n" +
            "    images = [\"${rootProject.name}-${project.name}\"]\n" +
            "}\n";

    private static final String DOCKER_IMAGE_NAME_KOTLIN =
            "\ntasks.named<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>(\"dockerBuild\") {\n" +
            "    images.add(\"${rootProject.name}-${project.name}\")\n" +
            "}\n";

    private static final Pattern VERSION = Pattern.compile(" version \".+\"");

    private final GradleDsl dsl;
    private final boolean forCloudModule;

    public BuildGradlePostProcessor(GradleDsl dsl, boolean forCloudModule) {
        this.dsl = dsl;
        this.forCloudModule = forCloudModule;
    }

    @NonNull
    @Override
    public String process(@NonNull String buildGradle) {
        buildGradle = updateVersion(buildGradle);
        if (forCloudModule) {
            buildGradle = configureDockerImageName(buildGradle);
        }
        buildGradle = removePluginVersions(buildGradle);
        return buildGradle;
    }

    private String updateVersion(@NonNull String buildGradle) {
        return buildGradle.replaceFirst(DEFAULT_VERSION, SNAPSHOT_VERSION);
    }

    private String configureDockerImageName(@NonNull String buildGradle) {
        if (dsl == GROOVY) {
            return buildGradle.contains(DOCKER_IMAGE_NAME_GROOVY) ? buildGradle : buildGradle + DOCKER_IMAGE_NAME_GROOVY;
        } else {
            return buildGradle.contains(DOCKER_IMAGE_NAME_KOTLIN) ? buildGradle : buildGradle + DOCKER_IMAGE_NAME_KOTLIN;
        }
    }

    private String removePluginVersions(@NonNull String buildGradle) {
        return VERSION.matcher(buildGradle).replaceAll("");
    }
}
