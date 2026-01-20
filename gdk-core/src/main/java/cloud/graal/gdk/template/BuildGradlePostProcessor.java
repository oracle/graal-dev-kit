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
package cloud.graal.gdk.template;

import cloud.graal.gdk.GdkUtils;
import cloud.graal.gdk.build.dependencies.GdkDependencies;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.build.gradle.GradleDsl;
import io.micronaut.starter.options.JdkVersion;

import java.util.Objects;
import java.util.regex.Pattern;

import static cloud.graal.gdk.GdkUtils.MICRONAUT_PLATFORM_BOM_VERSION_SUFFIX;
import static io.micronaut.starter.build.gradle.GradleDsl.GROOVY;

/**
 * Various updates to build.gradle scripts:
 * - changes the version from "0.1" to "1.0-SNAPSHOT"
 * - adds configuration to set the Docker image name to be related to the project, e.g. "demo-oci"
 * - removes versions from plugin declarations (versions are declared in buildSrc/build.gradle)
 *   as a workaround for <a href="https://github.com/gradle/gradle/issues/17559">this Gradle bug</a>
 * - changes "implementation platform(...)" to "micronautBoms(platform(...))" for the GDK BOM
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
            """

                    tasks.named('dockerBuild') {
                        images = ["${rootProject.name}-${project.name}"]
                    }
                    """;

    private static final String DOCKER_IMAGE_NATIVE_NAME_GROOVY =
            """

                    tasks.named('dockerBuildNative') {
                        images = ["${rootProject.name}-${project.name}"]
                    }
                    """;

    private static final String SHADOW_JAR_ZIP_64_GROOVY =
            """

                    tasks.named('shadowJar') {
                        zip64 = true
                    }
                    """;

    private static final String DOCKER_IMAGE_NAME_KOTLIN =
            """

                    tasks.named<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("dockerBuild") {
                        images.add("${rootProject.name}-${project.name}")
                    }
                    """;

    private static final String DOCKER_IMAGE_NATIVE_NAME_KOTLIN =
            """

                    tasks.named<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("dockerBuildNative") {
                        images.add("${rootProject.name}-${project.name}")
                    }
                    """;
    private static final String SHADOW_JAR_ZIP_64_KOTLIN =
            """

                    tasks.shadowJar {
                        setProperty("zip64", true)
                    }
                    """;

    private static final String GRAAL_VM_METADATA_REPOSITORY_GROOVY =
    """
    graalvmNative {
        metadataRepository {
            version = "%s"
        }
    }
    """;

    private static final String GRAAL_VM_METADATA_REPOSITORY_KOTLIN =
            """
            graalvmNative {
                metadataRepository {
                    version.set("%s")
                }
            }
            """;

    private static final Pattern VERSION = Pattern.compile(" version \".+\"");

    private static final Pattern BOM_PLATFORM_REGEX = Pattern.compile(
            "implementation[ (](platform\\(\"cloud\\.graal\\.gdk:gdk-bom:[0-9.\\-]+(-SNAPSHOT)?\"\\))\\)?");

    private static final String BOM_ENFORCED_PLATFORM_REPLACEMENT = "micronautBoms($1)";

    private static final Pattern RESOLUTION_STRATEGY_REGEX = Pattern.compile(
            "(?s)(substitute\\(module\\(\"io\\.micronaut.+\"\\)\\).*\\.using\\(module\\(\"io\\.micronaut.+:[0-9.]+)(\"\\)\\))");

    private static final String RESOLUTION_STRATEGY_REPLACEMENT = String.format("$1%s$2", MICRONAUT_PLATFORM_BOM_VERSION_SUFFIX);

    private final GradleDsl dsl;
    private final boolean forCloudModule;
    private final boolean isGatewayFunction;
    private final ApplicationType applicationType;
    private final JdkVersion jdkVersion;

    /**
     * @param dsl               the Gradle DSL
     * @param forCloudModule    true if the build.gradle is for a cloud module, not lib or platform-independent
     * @param isGatewayFunction true if creating a gateway function app
     * @param applicationType   the app type
     */
    public BuildGradlePostProcessor(@NonNull GradleDsl dsl,
                                    boolean forCloudModule,
                                    boolean isGatewayFunction,
                                    ApplicationType applicationType,
                                    JdkVersion jdkVersion) {
        Objects.requireNonNull(dsl, "Gradle DSL is required");
        this.isGatewayFunction = isGatewayFunction;
        this.applicationType = applicationType;
        this.dsl = dsl;
        this.forCloudModule = forCloudModule;
        this.jdkVersion = jdkVersion;
    }

    @NonNull
    @Override
    public String process(@NonNull String buildGradle) {
        buildGradle = updateVersion(buildGradle);
        if (forCloudModule && applicationType == ApplicationType.DEFAULT && !isGatewayFunction) {
            buildGradle = configureDockerImageName(buildGradle);
            buildGradle = configureDockerNativeImageName(buildGradle);
            buildGradle = configureShadowJarZip64(buildGradle);
        }
        buildGradle = removePluginVersions(buildGradle);
        buildGradle = makeBomEnforced(buildGradle);
        buildGradle = updateResolutionStrategyVersions(buildGradle);
        buildGradle = fixLibDependency(buildGradle);
        buildGradle = replaceMavenCentral(buildGradle);
        buildGradle = configureGraalVmMetadataRepository(buildGradle);
        if (forCloudModule) {
            buildGradle = fixNativeName(buildGradle);
        }
        return buildGradle;
    }

    private String fixNativeName(String buildGradle) {
        return buildGradle + "\n" + "graalvmNative.binaries.main.imageName = \"${rootProject.name}-${project.name}\"" + "\n";
    }

    @NonNull
    private String updateVersion(@NonNull String buildGradle) {
        return buildGradle.replaceFirst(DEFAULT_VERSION, SNAPSHOT_VERSION);
    }

    private String replaceMavenCentral(@NonNull String buildGradle) {
        if (!StringUtils.isEmpty(GdkUtils.getenv("MIRROR_URL"))) {
            buildGradle = buildGradle.replace("mavenCentral()",
                    "maven { url \"%s\" }".formatted(GdkUtils.getenv("MIRROR_URL")));
        }
        return buildGradle;
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
    private String configureGraalVmMetadataRepository(@NonNull String buildGradle) {
        String graalVMRepositoryVersion = GdkDependencies.GRAALVM_METADATA_REPOSITORY_VERSION;
        String graalVMMetadata;
        if (dsl == GROOVY) {
            graalVMMetadata = GRAAL_VM_METADATA_REPOSITORY_GROOVY.formatted(graalVMRepositoryVersion);
        } else {
            graalVMMetadata = GRAAL_VM_METADATA_REPOSITORY_KOTLIN.formatted(graalVMRepositoryVersion);
        }
        return buildGradle.contains(graalVMMetadata) ? buildGradle : buildGradle + graalVMMetadata;
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
        return buildGradle.replace("implementation(\":lib-reference\")",
                "implementation(project(\":lib\"))");
    }
}
