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
package cloud.graal.gcn.feature.graalvm;

import cloud.graal.gcn.template.TemplatePostProcessor;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.options.BuildTool;

public class GraalvmQuickBuildPostProcessor implements TemplatePostProcessor {

    private static final String PLUGIN_START = "<plugin>";
    private static final String GRAAL_VM_NATIVE_CONFIG = """
        
        graalvmNative.binaries.main.buildArgs.add("-Ob")
        graalvmNative.binaries.test.buildArgs.add("-Ob")
        """;

    private final BuildTool buildTool;

    public GraalvmQuickBuildPostProcessor(BuildTool buildTool) {
        this.buildTool = buildTool;
    }

    @NonNull
    @Override
    public String process(@NonNull String input) {
        if (buildTool.isGradle()) {
            return input + GRAAL_VM_NATIVE_CONFIG;
        }
        return addNativeArgs(input);
    }

    private String addNativeArgs(@NonNull String pom) {
        int start = pom.indexOf("<plugins>");
        if (start == -1) {
            return pom;
        }
        int end = pom.indexOf(PLUGIN_START, start) + PLUGIN_START.length();

        String top = pom.substring(0, start);
        String bottom = pom.substring(end);

        return top + """
 <plugins>
      <plugin>
        <groupId>org.graalvm.buildtools</groupId>
        <artifactId>native-maven-plugin</artifactId>
        <configuration>
          <buildArgs combine.children="append">
            <buildArg>-Ob</buildArg>
          </buildArgs>
        </configuration>
      </plugin>
      <plugin>""" + bottom;
    }

}
