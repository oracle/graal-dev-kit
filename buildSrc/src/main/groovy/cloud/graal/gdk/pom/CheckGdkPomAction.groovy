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

import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import io.micronaut.build.pom.PomDownloader
import io.micronaut.build.pom.PomValidation
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

abstract class CheckGdkPomAction implements WorkAction<Parameters> {

    static interface Parameters extends WorkParameters {
        Property<String> getDependencyPath()

        ListProperty<String> getRepositories()

        ListProperty<String> getIgnoreDependencies()

        Property<String> getGroupId()

        Property<String> getArtifactId()

        Property<String> getVersion()

        RegularFileProperty getPomFile()

        RegularFileProperty getReportFile()

        DirectoryProperty getPomDirectory()
    }

    @Override
    void execute() {
        String dependencyPath = parameters.dependencyPath.get()
        File pomFile = parameters.pomFile.asFile.get()
        File reportFile = parameters.reportFile.asFile.get()
        String groupId = parameters.groupId.get()
        String artifactId = parameters.artifactId.get()
        String version = parameters.version.get()
        File pomDirectory = parameters.pomDirectory.asFile.get()
        List<String> repositories = parameters.repositories.get()
        List<String> ignoreDependencies = parameters.ignoreDependencies.get()
        def downloader = new PomDownloader(repositories, pomDirectory)
        def parser = new PomParser(downloader)
        def pom = parser.parse(pomFile, groupId, artifactId, version)
        Map<String, String> foundDependencies = [:].withDefault { new LinkedHashSet<>() }.asSynchronized() as Map
        Set<String> missingDependencies = new LinkedHashSet<>().asSynchronized()
        pom.dependencies.parallelStream().forEach { dependency ->
            if (dependency.version) {
                String key = "${dependency.groupId}:${dependency.artifactId}:${dependency.version}"
                if (!ignoreDependencies.contains(dependency.groupId + ":" + dependency.artifactId)) {
                    def downloadedFile = downloader.tryDownloadPom(dependency)
                    if (downloadedFile.present) {
                        foundDependencies[key] = downloadedFile.get().absolutePath
                    } else {
                        missingDependencies << key
                    }
                }
            }
        }
        def validation = new PomValidation(dependencyPath, pom, foundDependencies, missingDependencies)
        def generator = new JsonGenerator.Options()
                .excludeFieldsByName("import", "importingBom")
                .build()
        reportFile << JsonOutput.prettyPrint(generator.toJson(validation))
    }
}
