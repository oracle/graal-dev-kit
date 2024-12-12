/*
 * Copyright 2017-2022 original authors
 * Copyright 2024 Oracle and/or its affiliates
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

import cloud.graal.gdk.command.GdkBaseCommand;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.starter.cli.CodeGenConfig;
import io.micronaut.starter.cli.InteractiveShell;
import io.micronaut.starter.cli.MicronautFactory;
import io.micronaut.starter.cli.command.CodeGenCommand;
import io.micronaut.starter.io.ConsoleOutput;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

import java.util.function.BiFunction;

/**
 * Base class for CLI main classes. Starts an interactive shell if passed no
 * arguments, otherwise instantiates the requested command class.
 * <p>
 * Based on io.micronaut.starter.cli.MicronautStarter.
 */
public abstract class AbstractStarter extends GdkBaseCommand {

    protected static final BiFunction<Throwable, CommandLine, Integer> EXCEPTION_HANDLER = (e, commandLine) -> {
        GdkBaseCommand command = commandLine.getCommand();
        command.err(e.getMessage());
        if (command.showStacktrace()) {
            e.printStackTrace(commandLine.getErr());
        }
        return 1;
    };

    protected boolean interactiveShell = false;

    protected CommandLine createCommandLine() {
        boolean noOpConsole = interactiveShell;
        try (BeanContext beanContext = ApplicationContext.builder().deduceEnvironment(false).start()) {
            return createCommandLine(beanContext, noOpConsole);
        }
    }

    protected int execute(String[] args) {
        boolean noOpConsole = args.length > 0 && args[0].startsWith("update-cli-config");
        try (BeanContext beanContext = ApplicationContext.builder().deduceEnvironment(false).start()) {
            return createCommandLine(beanContext, noOpConsole).execute(args);
        }
    }

    protected CommandLine createCommandLine(BeanContext beanContext, boolean noOpConsole) {
        CommandLine commandLine = new CommandLine(this, new MicronautFactory(beanContext));
        commandLine.setExecutionExceptionHandler((ex, commandLine1, parseResult) -> EXCEPTION_HANDLER.apply(ex, commandLine1));
        commandLine.setUsageHelpWidth(100);

        addCodegenCommands(beanContext, noOpConsole, commandLine);

        return commandLine;
    }

    protected void addCodegenCommands(BeanContext beanContext, boolean noOpConsole, CommandLine commandLine) {
        CodeGenConfig codeGenConfig = CodeGenConfig.load(beanContext, noOpConsole ? ConsoleOutput.NOOP : this);
        if (codeGenConfig != null) {
            beanContext.getBeanDefinitions(CodeGenCommand.class).stream()
                    .map(BeanDefinition::getBeanType)
                    .map(bt -> beanContext.createBean(bt, codeGenConfig))
                    .filter(CodeGenCommand::applies)
                    .forEach(commandLine::addSubcommand);
        }
    }

    /**
     * @return the name of the cli, for use in the interactive shell.
     */
    protected String getCliName() {
        return "gdk";
    }

    protected void startShell() {
        CommandLine commandLine = createCommandLine();
        interactiveShell = true;
        new InteractiveShell(commandLine, this::execute, EXCEPTION_HANDLER, "@|blue " + getCliName() + ">|@ ").start();
    }

    @Override
    public Integer call() {
        throw new ParameterException(spec.commandLine(), "No command specified");
    }
}
