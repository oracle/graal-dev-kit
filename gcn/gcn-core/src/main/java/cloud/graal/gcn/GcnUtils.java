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
package cloud.graal.gcn;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static io.micronaut.starter.options.JdkVersion.JDK_17;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility methods.
 *
 * @since 1.0.0
 */
public final class GcnUtils {

    /**
     * For now we aren't using version catalogs, but in the future may, or will
     * likely make it a CLI option.
     */
    public static final boolean USE_GRADLE_VERSION_CATALOG = false;

    /**
     * The name of the shared library module.
     */
    public static final String LIB_MODULE = "lib";

    public static final String APP_MODULE = "app";

    /**
     * The default version of micronaut plugin
     */
    public static final String MICRONAUT_MAVEN_PLUGIN_VERSION = "4.3.1";

    /**
     * The default version of test resources plugin
     */
    public static final String TEST_RESOURCES_VERSION = "2.3.3";

    /**
     * All supported JDK versions.
     */
    public static final List<Integer> SUPPORTED_JDKS = List.of(JDK_17.majorVersion());

    /**
     * The version suffix appended to the Micronaut version in the BOM.
     */
    public static final String BOM_VERSION_SUFFIX = "-oracle-00001";

    private static final String GCN_VERSION = loadVersion("version.txt");
    private static final String MICRONAUT_VERSION = loadVersion("micronautPlatformVersion.txt");

    private GcnUtils() {
    }

    private static String loadVersion(String resourcePath) {
        URL resource = GcnUtils.class.getResource("/" + resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Resource /" + resourcePath + " not found");
        }

        try (InputStream inputStream = resource.openStream()) {
            return new String(inputStream.readAllBytes(), UTF_8).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the version of Micronaut Platform
     */
    public static String getMicronautVersion() {
        return MICRONAUT_VERSION;
    }

    /**
     * @return the GCN version
     */
    public static String getVersion() {
        return GCN_VERSION;
    }
}
