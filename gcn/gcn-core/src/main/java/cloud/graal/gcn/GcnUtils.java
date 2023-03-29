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

import io.micronaut.starter.options.JdkVersion;
import io.micronaut.starter.util.VersionInfo;

import java.util.List;

import static io.micronaut.starter.options.JdkVersion.JDK_17;

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

    /**
     * The default JDK version if none is specified.
     */
    public static final JdkVersion DEFAULT_JDK = JDK_17;

    /**
     * All supported JDK versions.
     */
    public static final List<Integer> SUPPORTED_JDKS = List.of(JDK_17.majorVersion());

    private GcnUtils() {
    }

    /**
     * Used by the REST API.
     *
     * @return the version of Micronaut
     */
    public static String getMicronautVersion() {
        return VersionInfo.getMicronautVersion();
    }
}
