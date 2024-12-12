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

import cloud.graal.gdk.GdkProjectCreator;
import cloud.graal.gdk.model.GdkCloud;
import cloud.graal.gdk.model.GdkCloud.AvailableClouds;
import cloud.graal.gdk.model.GdkProjectType;
import cloud.graal.gdk.model.GdkService;
import cloud.graal.gdk.model.GdkService.AvailableServices;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.core.util.StringUtils;
import io.micronaut.starter.application.DefaultAvailableFeatures;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.cli.command.BuildToolCandidates;
import io.micronaut.starter.cli.command.BuildToolConverter;
import io.micronaut.starter.cli.command.LanguageCandidates;
import io.micronaut.starter.cli.command.LanguageConverter;
import io.micronaut.starter.cli.command.TestFrameworkCandidates;
import io.micronaut.starter.cli.command.TestFrameworkConverter;
import io.micronaut.starter.io.FileSystemOutputHandler;
import io.micronaut.starter.io.OutputHandler;
import io.micronaut.starter.options.BuildTool;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.options.TestFramework;
import io.micronaut.starter.util.NameUtils;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cloud.graal.gdk.GdkGeneratorContext.EXAMPLE_CODE;

/**
 * Abstract base class for the "create" commands.
 *
 * @since 1.0.0
 */
public abstract class GdkCreateCommand extends GdkBaseCommand {

    /**
     * Project name.
     */
    @ReflectiveAccess
    @Parameters(arity = "0..1", paramLabel = "NAME", description = "The name of the application to create.")
    protected String name;

    /**
     * Language.
     */
    @ReflectiveAccess
    @Option(names = {"-l", "--lang"}, paramLabel = "LANG",
            description = "Which language to use. Possible values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = LanguageCandidates.class, converter = LanguageConverter.class)
    protected Language lang;

    /**
     * Test framework.
     */
    @ReflectiveAccess
    @Option(names = {"-t", "--test"}, paramLabel = "TEST",
            description = "Which test framework to use. Possible values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = TestFrameworkCandidates.class, converter = TestFrameworkConverter.class)
    protected TestFramework test;

    /**
     * Build tool.
     */
    @ReflectiveAccess
    @Option(names = {"-b", "--build"}, paramLabel = "BUILD-TOOL",
            description = "Which build tool to configure. Possible values: ${COMPLETION-CANDIDATES}.",
            completionCandidates = BuildToolCandidates.class, converter = BuildToolConverter.class)
    protected BuildTool build;

    /**
     * JDK version.
     */
    @ReflectiveAccess
    @Option(names = {"--jdk", "--java-version"},
            description = "The JDK version the project should target")
    protected Integer javaVersion;

    /**
     * Selected clouds.
     */
    @Option(names = {"-c", "--clouds"}, paramLabel = "CLOUD", split = ",",
            description = "The supported cloud providers. Possible values: ${COMPLETION-CANDIDATES}",
            completionCandidates = AvailableClouds.class, converter = CloudTypeConverter.class)
    @ReflectiveAccess
    protected List<GdkCloud> clouds = new ArrayList<>();

    /**
     * Selected services.
     */
    @Option(names = {"-s", "--services"}, paramLabel = "SERVICE", split = ",",
            description = "The supported services. Possible values: ${COMPLETION-CANDIDATES}",
            completionCandidates = AvailableServices.class, converter = ServiceTypeConverter.class)
    @ReflectiveAccess
    protected List<GdkService> services = new ArrayList<>();

    /**
     * Selected feature names.
     */
    @Option(names = {"-f", "--features"}, paramLabel = "FEATURE", split = ",",
            description = "The features to use. Possible values: ${COMPLETION-CANDIDATES}",
            completionCandidates = DefaultAvailableFeatures.class)
    @ReflectiveAccess
    protected List<String> features = new ArrayList<>();

    /**
     * Whether to generate example code.
     */
    @ReflectiveAccess
    @Option(names = {"-e", "--example-code"}, description = "Generate example code")
    protected boolean exampleCode = true;

    /**
     * Project creator.
     */
    protected final GdkProjectCreator projectCreator;

    /**
     * Output handler.
     */
    protected final OutputHandler outputHandler;

    /**
     * @param projectCreator project creator
     * @param outputHandler  output handler
     */
    protected GdkCreateCommand(GdkProjectCreator projectCreator,
                               @Nullable OutputHandler outputHandler) {
        this.outputHandler = outputHandler;
        this.projectCreator = projectCreator;
    }

    /**
     * @return addition options (currently only the "example code" boolean)
     */
    @NonNull
    protected Map<String, Object> getAdditionalOptions() {
        return Map.of(EXAMPLE_CODE, exampleCode);
    }

    @Override
    public Integer call() throws Exception {

        Project project;
        try {
            project = NameUtils.parse(name);
        } catch (IllegalArgumentException e) {
            throw new ParameterException(spec.commandLine(),
                    StringUtils.isEmpty(name) ? "Specify an application name" : e.getMessage());
        }

        // by default there's no bean of this type, but one will be present in tests
        OutputHandler outputHandler = this.outputHandler != null ? this.outputHandler :
                new FileSystemOutputHandler(project, false, this);

        generate(project, outputHandler);

        out("@|blue ||@ Application created at " + outputHandler.getOutputLocation());
        return 0;
    }

    /**
     * @return the project type
     */
    public abstract GdkProjectType getProjectType();

    /**
     * @param project       the project
     * @param outputHandler the output handler
     * @throws Exception if a problem occurs
     */
    public void generate(Project project, OutputHandler outputHandler) throws Exception {
        projectCreator.create(getProjectType(), project, lang, test, build, clouds, services, features,
                javaVersion, getOperatingSystem(), getAdditionalOptions(), outputHandler, this);
    }
}
