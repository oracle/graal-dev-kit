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

import cloud.graal.gcn.GcnProjectCreator;
import cloud.graal.gcn.model.GcnProjectType;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.starter.io.OutputHandler;
import picocli.CommandLine.Command;

import static cloud.graal.gcn.model.GcnProjectType.APPLICATION;

/**
 * The command class for "create-app".
 *
 * @since 1.0.0
 */
@Command(name = "create-app", description = "Creates an application")
@Prototype
public class GcnCreateAppCommand extends GcnCreateCommand {

    GcnCreateAppCommand(GcnProjectCreator projectCreator,
                        @Nullable OutputHandler outputHandler) {
        super(projectCreator, outputHandler);
    }

    @Override
    public GcnProjectType getProjectType() {
        return APPLICATION;
    }
}
