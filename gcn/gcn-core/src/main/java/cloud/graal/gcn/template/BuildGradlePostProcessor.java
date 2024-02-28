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
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.build.gradle.GradleDsl;

import java.util.Objects;
import java.util.regex.Pattern;

import static cloud.graal.gcn.GcnUtils.BOM_VERSION_SUFFIX;
import static io.micronaut.starter.build.gradle.GradleDsl.GROOVY;

/**
 * Various updates to build.gradle scripts:
 * - changes the version from "0.1" to "1.0-SNAPSHOT"
 * - adds configuration to set the Docker image name to be related to the project, e.g. "demo-oci"
 * - removes versions from plugin declarations (versions are declared in buildSrc/build.gradle)
 *   as a workaround for <a href="https://github.com/gradle/gradle/issues/17559">this Gradle bug</a>
 * - changes "implementation platform(...)" to "micronautBoms(platform(...))" for the GCN BOM
 * - append "-oracle-00001" to version when resolutionStrategy.dependencySubstitution
 *   is added (currently for serde feature dependencies, e.g. serialization-jackson)
 * - fixes the rendered lib project dependency (`implementation(":lib-reference")` -> `implementation(project(":lib"))`)
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

    private static final String DOCKER_IMAGE_NATIVE_NAME_GROOVY =
            "\ntasks.named('dockerBuildNative') {\n" +
                    "    images = [\"${rootProject.name}-${project.name}\"]\n" +
                    "}\n";

    private static final String SHADOW_JAR_ZIP_64_GROOVY =
            "\ntasks.named('shadowJar') {\n" +
                    "    zip64 = true\n" +
                    "}\n";

    private static final String DOCKER_IMAGE_NAME_KOTLIN =
            "\ntasks.named<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>(\"dockerBuild\") {\n" +
            "    images.add(\"${rootProject.name}-${project.name}\")\n" +
            "}\n";

    private static final String DOCKER_IMAGE_NATIVE_NAME_KOTLIN =
            "\ntasks.named<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>(\"dockerBuildNative\") {\n" +
                    "    images.add(\"${rootProject.name}-${project.name}\")\n" +
                    "}\n";
    private static final String SHADOW_JAR_ZIP_64_KOTLIN =
            "\ntasks.shadowJar {\n" +
                    "    setProperty(\"zip64\", true)\n" +
                    "}\n";

    private static final Pattern VERSION = Pattern.compile(" version \".+\"");

    private static final Pattern BOM_PLATFORM_REGEX = Pattern.compile(
      "implementation[ (](platform\\(\"cloud\\.graal\\.gcn:gcn-bom:[0-9.]+\"\\))\\)?");

    private static final String BOM_ENFORCED_PLATFORM_REPLACEMENT = "micronautBoms($1)";

    private static final Pattern RESOLUTION_STRATEGY_REGEX = Pattern.compile(
      "(?s)(substitute\\(module\\(\"io\\.micronaut.+\"\\)\\).*\\.using\\(module\\(\"io\\.micronaut.+:[0-9.]+)(\"\\)\\))");

    private static final String RESOLUTION_STRATEGY_REPLACEMENT = String.format("$1%s$2", BOM_VERSION_SUFFIX);

    private final GradleDsl dsl;
    private final boolean forCloudModule;
    private final boolean isGatewayFunction;
    private final ApplicationType applicationType;

    /**
     * @param dsl               the Gradle DSL
     * @param forCloudModule    true if the build.gradle is for a cloud module, not lib or platform-independent
     * @param isGatewayFunction
     * @param applicationType
     */
    public BuildGradlePostProcessor(@NonNull GradleDsl dsl,
                                    boolean forCloudModule, boolean isGatewayFunction, ApplicationType applicationType) {
        this.isGatewayFunction = isGatewayFunction;
        this.applicationType = applicationType;
        Objects.requireNonNull(dsl, "Gradle DSL is required");
        this.dsl = dsl;
        this.forCloudModule = forCloudModule;
    }

    @NonNull
    @Override
    public String process(@NonNull String buildGradle) {
        buildGradle = updateVersion(buildGradle);
        if (forCloudModule && applicationType == ApplicationType.DEFAULT && !isGatewayFunction) {
            buildGradle = configureDockerImageName(buildGradle);
        }
        if (forCloudModule && applicationType == ApplicationType.DEFAULT && !isGatewayFunction) {
            buildGradle = configureDockerNativeImageName(buildGradle);
            buildGradle = configureShadowJarZip64(buildGradle);
        }
        buildGradle = removePluginVersions(buildGradle);
        buildGradle = makeBomEnforced(buildGradle);
        buildGradle = updateResolutionStrategyVersions(buildGradle);
        buildGradle = fixLibDependency(buildGradle);
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
    private String configureDockerNativeImageName(@NonNull String buildGradle) {
        if (dsl == GROOVY) {
            return buildGradle.contains(DOCKER_IMAGE_NATIVE_NAME_GROOVY) ? buildGradle : buildGradle + DOCKER_IMAGE_NATIVE_NAME_GROOVY;
        } else {
            return buildGradle.contains(DOCKER_IMAGE_NATIVE_NAME_KOTLIN) ? buildGradle : buildGradle + DOCKER_IMAGE_NATIVE_NAME_KOTLIN;
        }
    }

    @NonNull
    private String configureShadowJarZip64(@NonNull String buildGradle) {
        if (dsl == GROOVY) {
            return buildGradle.contains(SHADOW_JAR_ZIP_64_GROOVY) ? buildGradle : buildGradle + SHADOW_JAR_ZIP_64_GROOVY;
        } else {
            return buildGradle.contains(SHADOW_JAR_ZIP_64_KOTLIN) ? buildGradle : buildGradle + SHADOW_JAR_ZIP_64_KOTLIN;
        }
    }

    @NonNull
    private String removePluginVersions(@NonNull String buildGradle) {
        return VERSION.matcher(buildGradle).replaceAll("");
    }

    @NonNull
    private String makeBomEnforced(@NonNull String buildGradle) {
        return BOM_PLATFORM_REGEX.matcher(buildGradle).replaceFirst(BOM_ENFORCED_PLATFORM_REPLACEMENT);
    }

    @NonNull
    private String updateResolutionStrategyVersions(@NonNull String buildGradle) {
        return RESOLUTION_STRATEGY_REGEX.matcher(buildGradle).replaceAll(RESOLUTION_STRATEGY_REPLACEMENT);
    }

    @NonNull
    private String fixLibDependency(@NonNull String buildGradle) {
        return buildGradle.replace("implementation(\":lib-reference\")", "implementation(project(\":lib\"))");
    }
}
