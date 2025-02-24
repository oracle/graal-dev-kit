/*
 * Copyright 2025 Oracle and/or its affiliates
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
package cloud.graal.gdk.feature.misc;

import cloud.graal.gdk.template.TemplatePostProcessor;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.options.BuildTool;

public class CycloneDXPluginPostProcessor implements TemplatePostProcessor {

    private static final String PLUGIN_START = "plugins {";
    private static final String CYCLONE_DX_PLUGIN = "\n\tid 'org.cyclonedx.bom' version '2.1.0'";

    private static final String CYCLONE_DX_CONFIG = """
            cyclonedxBom {
                includeConfigs = ["compileClasspath", "runtimeClasspath"]
            }
            """;

    private final BuildTool buildTool;

    public CycloneDXPluginPostProcessor(BuildTool buildTool) {
        this.buildTool = buildTool;
    }

    @NonNull
    @Override
    public String process(@NonNull String input) {
        String result = input;
        if (buildTool.isGradle()) {
            int start =  input.indexOf(PLUGIN_START);
            if (start >= 0) {
                start += PLUGIN_START.length();
                String top = input.substring(0, start);
                String bottom = input.substring(start);
                result = top + CYCLONE_DX_PLUGIN + bottom + CYCLONE_DX_CONFIG;
            }
        }
        return result;
    }

}
