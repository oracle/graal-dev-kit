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
package cloud.graal.gdk;

import cloud.graal.gdk.build.dependencies.GdkDependencies;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.starter.options.JdkVersion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.micronaut.starter.options.JdkVersion.JDK_17;
import static io.micronaut.starter.options.JdkVersion.JDK_21;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility methods.
 *
 * @since 1.0.0
 */
public final class GdkUtils {

    /**
     * For now we aren't using version catalogs, but in the future may, or will
     * likely make it a CLI option.
     */
    public static final boolean USE_GRADLE_VERSION_CATALOG = false;

    /**
     * The name of the shared library module.
     */
    public static final String LIB_MODULE = "lib";

    /**
     * The name of the Micronaut default module.
     */
    public static final String APP_MODULE = "app";

    /**
     * All supported JDK versions.
     */
    public static final List<Integer> SUPPORTED_JDKS = List.of(JDK_17.majorVersion(), JDK_21.majorVersion());

    /**
     * The version suffix appended to the Micronaut version in the BOM.
     */
    public static final String MICRONAUT_PLATFORM_BOM_VERSION_SUFFIX = getMicronautPlatformBomVersionSuffix();

    private static final String GDK_BOM_VERSION = loadVersion("version.txt");

    private static String loadVersion(String resourcePath) {
        URL resource = GdkUtils.class.getResource("/" + resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Resource /" + resourcePath + " not found");
        }

        try (InputStream inputStream = resource.openStream()) {
            return new String(inputStream.readAllBytes(), UTF_8).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getMicronautPlatformBomVersionSuffix() {
        Matcher m = Pattern.compile("(-oracle-\\d+)").matcher(GdkDependencies.IO_MICRONAUT_PLATFORM_MICRONAUT_PARENT.getVersion());
        m.find();
        return m.group();
    }

    /**
     * @return the GDK version
     */
    public static String getVersion() {
        return GDK_BOM_VERSION;
    }

    /**
     * @return the version of Micronaut Platform
     */
    public static String getMicronautVersion() {
        return GdkDependencies.IO_MICRONAUT_PLATFORM_MICRONAUT_PARENT.getVersion().replaceAll("-oracle-\\d+", "");
    }

    /**
     * @return the BOM version
     */
    public static String getGdkBomVersion() {
        return GDK_BOM_VERSION;
    }

    /**
     * Add any supported JDK versions that aren't supported in Micronaut.
     */
    public static void configureJdkVersions() {
        new JdkVersion(22);
    }

    /**
     * Safely gets an environment variable (System.getenv fails when running inside the launcher).
     *
     * @param name the name
     * @return the value
     */
    @Nullable
    public static String getenv(@NonNull String name) {
        try {
            return System.getenv(name);
        } catch (Throwable t) {
            return null;
        }
    }
}
