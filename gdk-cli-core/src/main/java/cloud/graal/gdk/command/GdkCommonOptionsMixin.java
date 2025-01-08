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
package cloud.graal.gdk.command;

import cloud.graal.gdk.GdkUtils;
import io.micronaut.core.annotation.ReflectiveAccess;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;

/**
 * Mixin for common options across commands.
 */
@Command(mixinStandardHelpOptions = true,
         versionProvider = GdkCommonOptionsMixin.GdkVersionProvider.class)
@SuppressWarnings("checkstyle:VisibilityModifier")
public class GdkCommonOptionsMixin {

    /**
     * Whether to show full stack trace when exceptions occur.
     */
    @Option(names = {"-x", "--stacktrace"}, defaultValue = "false",
            description = "Show full stack trace when exceptions occur.")
    @ReflectiveAccess
    public boolean showStacktrace;

    /**
     * Whether to create verbose output.
     */
    @Option(names = {"-v", "--verbose"}, defaultValue = "false",
            description = "Create verbose output.")
    @ReflectiveAccess
    public boolean verbose;

    /**
     * Generates a version description.
     */
    public static class GdkVersionProvider implements IVersionProvider {
        public String[] getVersion() {
            return new String[]{"GDK Version: " + GdkUtils.getVersion()};
        }
    }
}
