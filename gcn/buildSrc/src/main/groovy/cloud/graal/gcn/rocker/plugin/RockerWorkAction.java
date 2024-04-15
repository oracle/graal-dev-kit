/*
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

package cloud.graal.gcn.rocker.plugin;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

public abstract class RockerWorkAction implements WorkAction<RockerWorkAction.Params> {
    public interface Params extends WorkParameters {
        DirectoryProperty getTemplateDirectory();
        DirectoryProperty getOutputDirectory();
    }

    @Override
    public void execute() {
        var templateDirectory = getParameters().getTemplateDirectory().get().getAsFile();
        var outputDirectory = getParameters().getOutputDirectory().get().getAsFile();
        new JavaGeneratorRunnable(templateDirectory, outputDirectory).run();
    }
}
