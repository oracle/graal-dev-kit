/*
 * Copyright 2017-2022 original authors
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

import cloud.graal.gdk.command.GdkCommonOptionsMixin;
import cloud.graal.gdk.command.GdkCreateAppCommand;
import cloud.graal.gdk.command.GdkCreateFunctionCommand;
import cloud.graal.gdk.command.GdkCreateGatewayFunctionCommand;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.starter.cli.command.BuildToolCandidates;
import io.micronaut.starter.cli.command.BuildToolConverter;
import io.micronaut.starter.cli.command.LanguageCandidates;
import io.micronaut.starter.cli.command.LanguageConverter;
import io.micronaut.starter.cli.command.TestFrameworkCandidates;
import io.micronaut.starter.cli.command.TestFrameworkConverter;
import io.micronaut.starter.cli.feature.acme.AcmeServerOption;
import picocli.CommandLine.Command;

/**
 * The main class for the library. Starts an interactive shell if passed no
 * arguments, otherwise instantiates the requested command class
 * (GdkCreateAppCommand / GdkCreateFunctionCommand / GdkCreateGatewayFunctionCommand)
 * and attempts to generate an application.
 * <p>
 * Based on io.micronaut.starter.cli.MicronautStarter.
 */
@Command(name = "gdk", description = {
        "GDK CLI command line interface for generating projects and services.",
        "Application generation commands are:",
        "",
        "*  @|bold create-app|@ @|yellow NAME|@",
        "*  @|bold create-function|@ @|yellow NAME|@",
        "*  @|bold create-gateway-function|@ @|yellow NAME|@"},
        synopsisHeading = "@|bold,underline Usage:|@ ",
        optionListHeading = "%n@|bold,underline Options:|@%n",
        commandListHeading = "%n@|bold,underline Commands:|@%n",
        subcommands = {
                GdkCreateAppCommand.class,
                GdkCreateFunctionCommand.class,
                GdkCreateGatewayFunctionCommand.class
        })
@TypeHint({
        GdkStarter.class,
        LanguageCandidates.class,
        LanguageConverter.class,
        BuildToolCandidates.class,
        BuildToolConverter.class,
        GdkCommonOptionsMixin.class,
        TestFrameworkCandidates.class,
        TestFrameworkConverter.class,
        AcmeServerOption.class
})
@Prototype
public class GdkStarter extends AbstractStarter {
// TODO disable commands create-test / create-command / feature-diff / create-bean / create-job

    /**
     * Entry point for the CLI. Starts an interactive shell if passed no
     * arguments, otherwise instantiates the requested command class and
     * attempts to generate an application.
     *
     * @param args args
     */
    public static void main(String[] args) {
        GdkUtils.configureJdkVersions();
        GdkStarter starter = new GdkStarter();
        if (args.length == 0) {
            // The first command line isn't technically in the shell yet so this is called before setting the static flag
            starter.startShell();
        } else {
            System.exit(starter.execute(args));
        }
    }
}
