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

package cloud.graal.gcn.rocker.plugin

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider

/**
 * Based on io.micronaut.starter.rocker.plugin.RockerPlugin.
 */
@CompileStatic
class RockerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.convention.getPlugin(JavaPluginConvention).sourceSets.all(sourceSet ->
                processSourceSet(project, sourceSet as SourceSet))
    }

    private static void processSourceSet(Project project, SourceSet sourceSet) {

        RockerSourceSetProperty rockerProperty = new RockerSourceSetProperty(project)
        new DslObject(sourceSet).convention.plugins.rocker = rockerProperty

        String taskName = sourceSet.getTaskName('generate', 'RockerTemplateSource')
        TaskProvider<RockerTask> rockerTaskProvider = project.tasks.register(taskName, RockerTask, (RockerTask rockerTask) -> {
            rockerTask.group = 'build'
            rockerTask.description = 'Generate Sources from ' + sourceSet.name + ' Rocker Templates'
            rockerTask.templateDirs.from rockerProperty.rocker.srcDirs
        })

        sourceSet.java.srcDir rockerTaskProvider
    }
}
