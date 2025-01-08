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
package cloud.graal.gdk.dependencies

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

class GdkVersionCatalogUpdatePlugin implements Plugin<Project> {

    void apply(Project project) {
        TaskContainer tasks = project.tasks
        Directory gradleDirectory = project.layout.projectDirectory.dir('../gradle')
        TaskProvider<VersionCatalogUpdate> updater = tasks.register('updateVersionCatalogs', VersionCatalogUpdate.class, task -> {
            task.catalogsDirectory.convention(gradleDirectory)
            task.outputDirectory.convention(project.layout.buildDirectory.dir('catalogs-update'))
            task.rejectedQualifiers.convention(['alpha', 'beta', 'rc', 'cr', 'm', 'preview', 'b', 'ea'])
            task.ignoredModules.convention([
                    'org.jetbrains.kotlin:kotlin-annotation-processing-embeddable',
                    'org.jetbrains.kotlin:kotlin-compiler-embeddable',
                    'org.jetbrains.kotlin:kotlin-reflect',
                    'org.jetbrains.kotlin:kotlin-stdlib',
                    'org.jetbrains.kotlin:kotlin-stdlib-jdk8',
                    'org.jetbrains.kotlin:kotlin-test'
            ])
            task.allowMajorUpdates.convention(false)
            task.allowMinorUpdates.convention(true)
        })
        tasks.register('useLatestVersions', Copy, task -> {
            VersionCatalogUpdate dependent = updater.get()
            task.from(dependent.outputDirectory)
            task.into(project.providers.environmentVariable('CI')
                    .map(value -> gradleDirectory)
                    .orElse(dependent.catalogsDirectory))
        })
        tasks.register('dependencyUpdates', task -> task.setDescription('Compatibility task with the old update mechanism'))
    }
}
