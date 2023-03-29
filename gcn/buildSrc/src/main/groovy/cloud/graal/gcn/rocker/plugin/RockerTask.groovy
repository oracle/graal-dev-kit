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
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.TaskAction

import static org.gradle.api.tasks.PathSensitivity.RELATIVE

/**
 * Based on io.micronaut.starter.rocker.plugin.RockerTask.
 */
@CompileStatic
@CacheableTask
abstract class RockerTask extends DefaultTask {

    @InputFiles
    @PathSensitive(RELATIVE)
    abstract ConfigurableFileCollection getTemplateDirs()

    @TaskAction
    void compileRocker() {
        File outputDirectory = project.layout.projectDirectory.dir('build/generated/rocker').asFile
        for (File templateDirectory in templateDirs.files) {
            new JavaGeneratorRunnable(templateDirectory, outputDirectory).run()
        }
    }
}
