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
 * - changes "platform" to "enforcedPlatform" for the GCN BOM, since Micronaut Starter doesn't support enforcedPlatform yet
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

    private static final String BOM = "cloud.graal.gcn:gcn-bom:";
    private static final Pattern BOM_PLATFORM = Pattern.compile("platform\\(\"" + BOM);
    private static final String BOM_ENFORCED_PLATFORM = "enforcedPlatform(\"" + BOM;

    private final GradleDsl dsl;
    private final boolean forCloudModule;
    private final boolean platformIndependent;

    /**
     * @param dsl the Gradle DSL, only needed when <code>forCloudModule</code> is <code>true</code>
     * @param forCloudModule true if the build.gradle is for a cloud module, not lib or platform-independent
     * @param platformIndependent true if the build.gradle is for platform-independent
     */
    public BuildGradlePostProcessor(GradleDsl dsl,
                                    boolean forCloudModule,
                                    boolean platformIndependent) {
        this.dsl = dsl;
        this.forCloudModule = forCloudModule;
        this.platformIndependent = platformIndependent;
    }

    @NonNull
    @Override
    public String process(@NonNull String buildGradle) {
        buildGradle = updateVersion(buildGradle);
        if (forCloudModule) {
            buildGradle = configureDockerImageName(buildGradle);
        }
        if (!platformIndependent) {
            buildGradle = removePluginVersions(buildGradle);
        } else {
            buildGradle = updatePluginVersions(buildGradle);
        }
        buildGradle = makeBomEnforced(buildGradle);
        return buildGradle;
    }

    @NonNull
    private String updateVersion(@NonNull String buildGradle) {
        return buildGradle.replaceFirst(DEFAULT_VERSION, SNAPSHOT_VERSION);
    }

    @NonNull
    private String configureDockerImageName(@NonNull String buildGradle) {
        if (dsl == GROOVY) {
            return buildGradle.contains(DOCKER_IMAGE_NAME_GROOVY) ? buildGradle : buildGradle + DOCKER_IMAGE_NAME_GROOVY;
        } else {
            return buildGradle.contains(DOCKER_IMAGE_NAME_KOTLIN) ? buildGradle : buildGradle + DOCKER_IMAGE_NAME_KOTLIN;
        }
    }

    @NonNull
    private String removePluginVersions(@NonNull String buildGradle) {
        return VERSION.matcher(buildGradle).replaceAll("");
    }

    // TODO remove this once we upgrade to a version of Micronaut that uses these plugin versions or higher
    @NonNull
    private String updatePluginVersions(@NonNull String buildGradle) {
        buildGradle = buildGradle.replace("id(\"io.micronaut.application\") version \"3.7.2\"", "id(\"io.micronaut.application\") version \"3.7.7\"");
        buildGradle = buildGradle.replace("id(\"io.micronaut.test-resources\") version \"3.7.2\"", "id(\"io.micronaut.test-resources\") version \"3.7.7\"");
        return buildGradle;
    }

    @NonNull
    private String makeBomEnforced(@NonNull String buildGradle) {
        return BOM_PLATFORM.matcher(buildGradle).replaceAll(BOM_ENFORCED_PLATFORM);
    }
}
