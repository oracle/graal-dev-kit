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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GcnVersionInfo {

    private static final Properties VERSIONS = new Properties();

    static {
        URL resource = GcnVersionInfo.class.getResource("/gcn-platform-versions.properties");
        if (resource != null) {
            try (Reader reader = new InputStreamReader(resource.openStream(), UTF_8)) {
                VERSIONS.load(reader);
            } catch (IOException ignored) {
                // ignore
            }
        }
    }

    public static String getMicronautVersion() {
        Object micronautVersion = VERSIONS.get("micronaut.platform.version");
        if (micronautVersion != null) {
            return micronautVersion.toString();
        }
        return "2.0.0";
    }
}
