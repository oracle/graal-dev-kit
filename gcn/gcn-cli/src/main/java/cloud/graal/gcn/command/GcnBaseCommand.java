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
package cloud.graal.gcn.command;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.starter.application.OperatingSystem;
import io.micronaut.starter.io.ConsoleOutput;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.util.Optional;
import java.util.concurrent.Callable;

import static io.micronaut.starter.application.OperatingSystem.LINUX;
import static io.micronaut.starter.application.OperatingSystem.MACOS;
import static io.micronaut.starter.application.OperatingSystem.SOLARIS;
import static io.micronaut.starter.application.OperatingSystem.WINDOWS;
import static picocli.CommandLine.Help.Ansi.AUTO;

/**
 * Base class for all GCN Picocli command classes.
 * Based on io.micronaut.starter.cli.command.BaseCommand.
 *
 * @since 1.0.0
 */
public abstract class GcnBaseCommand implements ConsoleOutput, Callable<Integer> {

    /**
     * Command spec.
     */
    @Spec
    @ReflectiveAccess
    protected CommandSpec spec;

    /**
     * Common options mixin.
     */
    @Mixin
    @ReflectiveAccess
    protected final GcnCommonOptionsMixin commonOptions = new GcnCommonOptionsMixin();

    @Override
    public void out(String message) {
        outWriter().ifPresent(writer -> writer.println(AUTO.string(message)));
    }

    @Override
    public void err(String message) {
        errWriter().ifPresent(writer -> writer.println(AUTO.string("@|bold,red | Error|@ " + message)));
    }

    @Override
    public void warning(String message) {
        outWriter().ifPresent(writer -> writer.println(AUTO.string("@|bold,red | Warning|@ " + message)));
    }

    @Override
    public void green(String message) {
        outWriter().ifPresent(writer -> writer.println(AUTO.string("@|bold,green " + message + "|@")));
    }

    @Override
    public void red(String message) {
        outWriter().ifPresent(writer -> writer.println(AUTO.string("@|bold,red " + message + "|@")));
    }

    @Override
    public boolean showStacktrace() {
        return commonOptions.showStacktrace;
    }

    @Override
    public boolean verbose() {
        return commonOptions.verbose;
    }

    /**
     * @return the inferred OS
     */
    @Nullable
    public OperatingSystem getOperatingSystem() {
        io.micronaut.context.condition.OperatingSystem operatingSystem = io.micronaut.context.condition.OperatingSystem.getCurrent();

        if (operatingSystem.isMacOs()) {
            return MACOS;
        }

        if (operatingSystem.isLinux()) {
            return LINUX;
        }

        if (operatingSystem.isWindows()) {
            return WINDOWS;
        }

        if (operatingSystem.isSolaris()) {
            return SOLARIS;
        }

        return null;
    }

    /**
     * @return the stdout writer, if available
     */
    @NonNull
    protected Optional<PrintWriter> outWriter() {
        return getSpec().map(spec -> spec.commandLine().getOut());
    }

    /**
     * @return the stderr writer, if available
     */
    @NonNull
    protected Optional<PrintWriter> errWriter() {
        return getSpec().map(spec -> spec.commandLine().getErr());
    }

    /**
     * @return the spec, if available
     */
    @NonNull
    protected Optional<CommandSpec> getSpec() {
        return Optional.ofNullable(spec);
    }
}
