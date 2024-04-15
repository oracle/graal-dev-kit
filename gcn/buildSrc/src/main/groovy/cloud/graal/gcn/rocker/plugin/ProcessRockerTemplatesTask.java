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

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;

import static org.gradle.api.tasks.PathSensitivity.RELATIVE;

@CacheableTask
public abstract class ProcessRockerTemplatesTask extends DefaultTask {
    @InputFiles
    @PathSensitive(RELATIVE)
    public abstract ConfigurableFileCollection getTemplateDirs();

    @Classpath
    public abstract ConfigurableFileCollection getRockerClasspath();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @TaskAction
    public void processTemplates() {
        var rockerClasspath = getRockerClasspath().getFiles();
        var outputDirectory = getOutputDirectory().get().getAsFile();
        var templates = getTemplateDirs().getFiles();
        for (var templateDirectory : templates) {
            getWorkerExecutor().classLoaderIsolation(spec ->
                spec.getClasspath().from(rockerClasspath)
            ).submit(RockerWorkAction.class, params -> {
                params.getTemplateDirectory().set(templateDirectory);
                params.getOutputDirectory().set(outputDirectory);
            });
        }
    }
}
