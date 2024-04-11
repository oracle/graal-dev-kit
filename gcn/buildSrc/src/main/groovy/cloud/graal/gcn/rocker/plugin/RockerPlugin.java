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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;

/**
 * Based on io.micronaut.starter.rocker.plugin.RockerPlugin.
 */
public class RockerPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);
        project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().all(sourceSet -> processSourceSet(project, sourceSet));
    }

    private static void processSourceSet(Project project, SourceSet sourceSet) {

        var templateDirectorySet = new TemplateDirectorySet(project);
        sourceSet.getExtensions().add("rocker", templateDirectorySet);

        var taskName = sourceSet.getTaskName("generate", "RockerTemplateSource");
        var rockerClasspath = project.getConfigurations().detachedConfiguration(
            project.getDependencies().create("com.fizzed:rocker-compiler:" + RockerHelper.VERSION)
        );
        var rockerTaskProvider = project.getTasks()
            .register(taskName, ProcessRockerTemplatesTask.class, task -> {
                task.setGroup("build");
                task.setDescription("Generate Sources from " + sourceSet.getName() + " Rocker Templates");
                task.getTemplateDirs().from(templateDirectorySet.getSrcDirs());
                task.getOutputDirectory().convention(
                    project.getLayout().getBuildDirectory()
                        .dir("generated/rocker/" + taskName)
                );
                task.getRockerClasspath().from(rockerClasspath);
            });

        sourceSet.getJava().srcDir(rockerTaskProvider);
    }

}
