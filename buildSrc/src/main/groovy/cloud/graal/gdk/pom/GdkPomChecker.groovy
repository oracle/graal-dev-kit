/*
 * Copyright 2025 Oracle and/or its affiliates
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

package cloud.graal.gdk.pom

import groovy.transform.CompileStatic
import io.micronaut.build.pom.PomFileAdapter
import io.micronaut.build.pom.PomValidation
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

import static org.gradle.api.tasks.PathSensitivity.NONE
import static org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

@CompileStatic
@CacheableTask
abstract class GdkPomChecker extends DefaultTask {

    @Input
    abstract ListProperty<String> getRepositories()

    @Input
    abstract Property<String> getPomCoordinates()

    @Input
    abstract Property<Boolean> getFailOnSnapshots()

    @Input
    abstract Property<Boolean> getFailOnError()

    @Input
    abstract ListProperty<String> getIgnoreDependencies()

    @InputFile
    @PathSensitive(NONE)
    @Optional
    abstract RegularFileProperty getPomFile()

    @OutputDirectory
    abstract DirectoryProperty getReportDirectory()

    @Internal
    abstract DirectoryProperty getPomsDirectory()

    @Inject
    abstract WorkerExecutor getWorkerExecutor()

    GdkPomChecker() {
        description = "Verifies a POM file"
        group = VERIFICATION_GROUP
        failOnError.convention(true)
    }

    @TaskAction
    void verifyBom() {
        ErrorCollector errorCollector = new ErrorCollector()

        String[] coordinates = pomCoordinates.get().split(':')
        if (coordinates.length != 3) {
            throw new GradleException("Incorrect POM coordinates '${pomCoordinates.get()}': should be of the form group:artifact:version ")
        }

        def queue = new ArrayDeque<List<Object>>()
        queue.add([coordinates[0], coordinates[1], coordinates[2], pomFile.get().asFile, pomCoordinates.get()] as List<Object>)
        def workQueue = workerExecutor.noIsolation()
        Set<String> seen = []
        while (!queue.isEmpty()) {
            List<File> reports = []
            queue.each { item ->
                def (group, artifact, version, pomFile, path) = [item[0], item[1], item[2], item[3], item[4]]
                String key = "${group}:${artifact}:${version}"
                if (seen.add(key)) {
                    workQueue.submit(CheckGdkPomAction) { params ->
                        params.pomFile.set((File) pomFile)
                        params.groupId.set((String) group)
                        params.artifactId.set((String) artifact)
                        params.version.set((String) version)
                        params.repositories.set(repositories)
                        params.ignoreDependencies.set(ignoreDependencies)
                        params.getDependencyPath().set((String) path)
                        def reportFile = reportDirectory.file("${group}-${artifact}-${version}.json")
                        reports.add(reportFile.get().asFile)
                        params.reportFile.set(reportFile)
                        params.pomDirectory.set(pomsDirectory)
                    }
                }
            }
            workQueue.await()
            queue.clear()
            for (File it in reports) {
                def validation = PomFileAdapter.parseFromFile(it)
                String bomPrefix = "POM ${validation.pomFile.groupId}:${validation.pomFile.artifactId}:${validation.pomFile.version} (via ${validation.dependencyPath})"
                if (validation.pomFile.bom) {
                    addTransitiveBomsToQueue(validation, queue)
                    if (validation.pomFile.dependencies.any { !it.managed }) {
                        errorCollector.errors.add("$bomPrefix has dependencies outside of <dependencyManagement> block.".toString())
                    }
                }
                for (String invalid in validation.invalidDependencies) {
                    errorCollector.error(bomPrefix + " declares a non-resolvable dependency: " + invalid)
                }
            }
        }

        File reportFile = writeReport(errorCollector.errors)
        if (failOnError.get() && errorCollector.errors) {
            throw new GradleException("POM verification failed. See report in ${reportFile}")
        }
    }

    private File writeReport(List<String> errors) {
        def reportFile = reportDirectory.file("report-${name}.txt").get().asFile
        reportFile.withWriter { writer ->
            errors.each {
                println it
                writer.println(it)
            }
        }
        reportFile
    }

    private static void addTransitiveBomsToQueue(PomValidation validation, Deque<List<Object>> queue) {
        validation.validDependencies.each { gav, file ->
            def coord = gav.split(':')
            String group = coord[0]
            String artifact = coord[1]
            String version = coord[2]
            def dependency = validation.pomFile.dependencies.find {
                it.groupId == group && it.artifactId == artifact && it.version == version
            }
            if (dependency.managed && dependency.import) {
                queue.add([group, artifact, version, new File(file), "${validation.dependencyPath} -> $gav".toString()] as List<Object>)
            }
        }
    }

    private static class ErrorCollector {
        final List<String> errors = []

        void error(String message) {
            errors << message
        }
    }

}
