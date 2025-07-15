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

package cloud.graal.gdk.dependencies

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import java.nio.file.Files
import java.nio.file.Path

@CacheableTask
abstract class DependenciesSourceGenerator extends DefaultTask {

    @Internal
    abstract Property<VersionCatalog> getVersionCatalog()

    @Input
    def getDependencies() {
        versionCatalog.map { versionCatalog ->
            versionCatalog.libraryAliases
                    .collect { alias ->
                        def library = versionCatalog.findLibrary(alias).get()
                        if (library.isPresent()) {
                            def lib = library.get()
                            String artifactId = lib.module.name
                            String groupId = lib.module.group
                            String version = lib.versionConstraint.requiredVersion
                            boolean pom = alias.startsWith('bom-')
                            "${artifactId}:${groupId}:${version}:${pom}"
                        } else {
                            null
                        }
                    }
                    .findAll { it }
                    .toSet()
        }
    }

    @Input
    abstract Property<String> getPackageName()

    @OutputDirectory
    abstract DirectoryProperty getOutputDirectory()

    @Inject
    abstract FileOperations getFileOperations()

    @TaskAction
    void generateSources() {
        def outputDirectory = outputDirectory.get().asFile
        String packageName = packageName.get()
        Path packageDirectory = outputDirectory.toPath().resolve(packageName.replace('.', '/'))
        fileOperations.delete(outputDirectory)
        Files.createDirectories(packageDirectory)
        new File(packageDirectory.toFile(), 'GdkDependencies.java').withPrintWriter { writer ->
            writer.println("package $packageName;")
            writer.println()
            writer.println('import java.util.Collections;')
            writer.println('import java.util.HashMap;')
            writer.println('import java.util.Map;')
            writer.println('import io.micronaut.starter.build.dependencies.Dependency;')
            writer.println()
            writer.println('public class GdkDependencies {')
            writer.println('    public static final Map<String, Dependency> ALL_DEPENDENCIES;')
            writer.println('    public static final String GRAALVM_METADATA_REPOSITORY_VERSION = ' + '"%s";'.formatted(versionCatalog.get().findVersion("graalvm-metadata-version").get()) )
            def dependenciesMap = [:]
            def versionCatalog = getVersionCatalog().get()
            writeDependencies(writer, dependenciesMap, versionCatalog, true)
            writeDependencies(writer, dependenciesMap, versionCatalog, false)
            writer.println()
            writer.println('    static {')
            writer.println('        Map<String, Dependency> dependencies = new HashMap<>();')
            dependenciesMap.each { key, value ->
                writer.println("        dependencies.put(\"$key\", $value);")
            }
            writer.println('        ALL_DEPENDENCIES = Collections.unmodifiableMap(dependencies);')
            writer.println('    }')
            writer.println('}')
        }
    }

    static void writeDependencies(writer, Map dependenciesMap, versionCatalog, boolean bom) {
        versionCatalog.libraryAliases
                .findAll { alias -> bom == alias.startsWith('bom.') }
                .collect { versionCatalog.findLibrary(it).orElse(null) }
                .findAll { it != null }
                .sort { a, b -> a.get().module.name <=> b.get().module.name }
                .each { lib ->
                    String artifactId = lib.get().module.name
                    String groupId = lib.get().module.group
                    String version = lib.get().versionConstraint.requiredVersion
                    String groupIdArtifactId = groupId + '_' + artifactId
                    String name = groupIdArtifactId.toUpperCase().replaceAll('-', '_').replaceAll('\\.', '_')
                    writer.println("    public static final Dependency $name = Dependency.builder()\n" +
                            "                .groupId(\"$groupId\")\n" +
                            "                .artifactId(\"$artifactId\")\n" +
                            "                .version(\"$version\")\n" +
                            "                .pom($bom)\n" +
                            "                .build();")
                    dependenciesMap[groupId + ':' + artifactId] = name
                }
    }
}
