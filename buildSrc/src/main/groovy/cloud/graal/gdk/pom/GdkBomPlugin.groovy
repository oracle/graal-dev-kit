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

import groovy.namespace.QName
import groovy.transform.CompileStatic
import io.micronaut.build.catalogs.internal.VersionCatalogTomlModel
import io.micronaut.build.pom.VersionCatalogConverter
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyConstraint
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultMutableMinimalDependency
import org.gradle.api.plugins.JavaPlatformExtension
import org.gradle.api.plugins.JavaPlatformPlugin
import org.gradle.api.plugins.PluginManager
import org.gradle.api.plugins.catalog.CatalogPluginExtension
import org.gradle.api.plugins.catalog.VersionCatalogPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

import java.util.function.Consumer

import static org.gradle.api.plugins.JavaPlatformPlugin.API_CONFIGURATION_NAME

@CompileStatic
@CacheableTask
abstract class GdkBomPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        PluginManager plugins = project.pluginManager
        plugins.apply(MavenPublishPlugin)
        plugins.apply(JavaPlatformPlugin)
        plugins.apply(VersionCatalogPlugin)
        project.extensions.add(DependencyExclusion.NAME, DependencyExclusion)
        configureBOM(project)
    }

    private static String toPropertyName(String alias) {
        return alias.split("(?=[A-Z])")
                .collect { it.toLowerCase(Locale.US) }
                .join("-")
                .replace('-', '.')
    }

    private void configureBOM(Project project) {
        project.extensions.getByType(JavaPlatformExtension).allowDependencies()
        PublishingExtension publishing = project.extensions.getByType(PublishingExtension)
        DependencyExclusion dependencyExclusion = project.extensions.getByType(DependencyExclusion)
        project.afterEvaluate { unused -> configureLate(project, publishing, dependencyExclusion, project.tasks) }
        registerCheckBomTask(project)
    }

    private static String bomPropertyName(String alias) {
        alias = alias.replaceFirst(/^boms\./, '')
        String baseName = toPropertyName(alias)
        if (baseName.endsWith(".version")) {
            return baseName
        }
        return baseName + '.version'
    }

    private static void registerCheckBomTask(Project project) {
        TaskProvider<GdkPomChecker> checkBom = registerPomChecker("checkBom", project, task -> { })
        project.tasks.register("checkPom", task -> task.dependsOn(checkBom))
    }

    private static String nameOf(Node n) {
        def name = n.name()
        if (name instanceof String) {
            return (String) name
        }
        return ((QName) n.name()).localPart
    }

    private static Node childOf(Node node, String name) {
        List<Node> children = (List<Node>) node.children()
        return children.stream().findAll(n -> n instanceof Node).find(n -> nameOf(n as Node) == name) as Node
    }

    private void configureLate(Project project, PublishingExtension publishing, DependencyExclusion dependencyExclusion, TaskContainer tasks) {

        final VersionCatalogConverter modelConverter = new VersionCatalogConverter(
                project.rootProject.file("gradle/templates.versions.toml"),
                project.extensions.findByType(CatalogPluginExtension)
        )

        VersionCatalog versionCatalog = project.extensions.findByType(VersionCatalogsExtension).find("templateLibs").get()
        VersionCatalogTomlModel modelProvider = project.provider(modelConverter::getModel).get()

        Configuration api = project.configurations.named(API_CONFIGURATION_NAME).get()
        Configuration catalogs = project.configurations.detachedConfiguration()

        Map<String, String> mapAliasToVersion = new HashMap<>()
        Map<String, String> pomProperties = new HashMap<>()

        publishing.publications.named("maven", MavenPublication, pub -> {
            pub.artifactId = "gdk-bom"
            pub.groupId = "cloud.graal.gdk"
            pub.from(project.components.named("javaPlatform").get())

            pub.pom(pom -> {
                pom.packaging = "pom"
                modelProvider.librariesTable.forEach(library -> {
                    if (!library.alias.startsWith("exclude-")) {
                        String alias = Optional.ofNullable(library.version.reference).map(a -> a.replace('-', '.')).orElse("")
                        String bomPropertyName = bomPropertyName(alias)
                        def version = modelProvider.versionsTable.find(x -> x.reference == library.version.reference).version
                        String key = library.alias.replace("-",".").replace("_",".")
                        mapAliasToVersion[key] = '\${' + bomPropertyName + '}'
                        pomProperties.put(bomPropertyName, version.require)
                    }
                })
                pom.withXml { xml ->
                    Node properties = new Node(xml.asNode(), "properties")

                    for(String key: pomProperties.keySet().sort()) {
                        new Node(properties, key, pomProperties.get(key))
                    }

                    Node depManagement = childOf(xml.asNode(), "dependencyManagement")
                    xml.asNode().remove(depManagement)
                    xml.asNode().append(depManagement)

                    Node dependencies = childOf(depManagement, "dependencies")
                    dependencyExclusion.exclusions.forEach {
                        String[] exclusionDependencyStrings = it.name.split(':')
                        it.from.forEach { parentDependency ->
                            def dependencyStrings = parentDependency.split(":")
                            def pomDep = dependencies.children()
                                    .find {
                                        n -> childOf(n as Node, "groupId").text() == dependencyStrings[0] && childOf(n as Node, "artifactId").text() == dependencyStrings[1]
                                    }

                            Node dependency = pomDep as Node
                            if (dependency == null) {
                                dependency = new Node(dependencies, "dependency")
                                new Node(dependency, "groupId", dependencyStrings[0])
                                new Node(dependency, "artifactId", dependencyStrings[1])
                            }

                            def pomExc = dependency.children().find { n -> childOf(n as Node, "exclusions") }

                            def exclusions = pomExc as Node
                            if (exclusions == null) {
                                exclusions = new Node(dependency, "exclusions")
                            }
                            def exclusion = new Node(exclusions, "exclusion")
                            new Node(exclusion, "groupId", exclusionDependencyStrings[0])
                            new Node(exclusion, "artifactId", exclusionDependencyStrings[1])
                        }
                    }
                }
            })
        })

        versionCatalog.libraryAliases.sort { alias -> versionCatalog.findLibrary(alias).get().get().module.toString() }.forEach(alias -> {
            MinimalExternalModuleDependency lib = versionCatalog.findLibrary(alias).map(Provider::get)
                    .orElseThrow(() -> new RuntimeException("Unexpected missing alias in catalog: " + alias)) as MinimalExternalModuleDependency
            DefaultExternalModuleDependency existingDep = api.dependencies.find(x -> x.name == lib.name) as DefaultExternalModuleDependency

            if (existingDep != null) {
                existingDep.version { it.strictly(mapAliasToVersion.get(alias)) }
            } else if (alias.endsWith(".bom")) {
                DefaultMutableMinimalDependency bomDependency = project.dependencies.platform(versionCatalog.findLibrary(alias)
                        .map(Provider::get)
                        .orElseThrow(() -> new RuntimeException("Unexpected missing alias in catalog: " + alias))
                ) as DefaultMutableMinimalDependency
                bomDependency.version { it.strictly(mapAliasToVersion.get(alias)) }
                api.dependencies.add(bomDependency)
                catalogs.dependencies.add(bomDependency)

            } else if (!alias.startsWith("exclude.")) {
                DependencyConstraint dependencyConstraint = project.dependencies.constraints.create(lib)
                dependencyConstraint.version { it.strictly(mapAliasToVersion.get(alias)) }
                api.dependencyConstraints.add(dependencyConstraint)
            }
        })

        publishing.repositories(repositories -> {
            Provider<String> externalRepoUri = project.providers.environmentVariable("PUBLISH_REPO")
            if (externalRepoUri.isPresent()) {
                repositories.maven(maven -> {
                    MavenArtifactRepository repository = (MavenArtifactRepository) maven
                    repository.name = "External"
                    repository.url = externalRepoUri
                    Provider<String> externalRepoUsername = project.providers.environmentVariable("ARTIFACTHUB_ACCESS_TOKEN_USERNAME")
                    Provider<String> externalRepoPassword = project.providers.environmentVariable("ARTIFACTHUB_ACCESS_TOKEN")
                    if (externalRepoUsername.isPresent() && externalRepoPassword.isPresent()) {
                        repository.credentials(credentials -> {
                            credentials.username = externalRepoUsername.get()
                            credentials.password = externalRepoPassword.get()
                        })
                    }
                })
            }
            repositories.maven(maven -> {
                MavenArtifactRepository repository = (MavenArtifactRepository) maven
                repository.name = "Build"
                repository.url = project.rootProject.layout.buildDirectory.dir("repo")
            })
        })
    }

    static TaskProvider<GdkPomChecker> registerPomChecker(String taskName, Project project,
                                                          Consumer<? super GdkPomChecker> configuration) {
        TaskContainer tasks = project.tasks
        TaskProvider<GdkPomChecker> pomChecker = tasks.register(taskName, GdkPomChecker, task -> {
            task.repositories.add("https://repo.maven.apache.org/maven2/")
            if (System.getenv("LIB_RELEASE_CHECK")) {
                task.repositories.add("https://artifactory.oci.oraclecorp.com/libs-release")
            } else {
                task.repositories.add("https://maven.oracle.com/public/")
            }

            if (System.getenv("STAGE_URL")) {
                task.repositories.add(System.getenv("STAGE_URL"))
            }

            task.ignoreDependencies.add("io.zipkin.brave:brave-instrumentation-benchmarks")

            project.repositories.forEach(r -> {
                if (r instanceof MavenArtifactRepository) {
                    task.repositories.add(((MavenArtifactRepository) r).url.toString())
                }
            })

            task.pomFile
                    .fileProvider(tasks.named("generatePomFileForMavenPublication", GenerateMavenPom)
                    .map(GenerateMavenPom::getDestination))
            String version = assertVersion(project)
            task.pomCoordinates.set("cloud.graal.gdk:gdk-bom:" + version)
            task.reportDirectory.set(project.layout.buildDirectory.dir("reports/" + taskName))
            task.pomsDirectory.set(project.layout.buildDirectory.dir("poms"))
            ProviderFactory providers = project.providers
            Provider<Boolean> failOnSnapshots =
                    providers.systemProperty("micronaut.fail.on.snapshots")
                            .orElse(providers.environmentVariable("MICRONAUT_FAIL_ON_SNAPSHOTS"))
                            .map(Boolean::parseBoolean)
                            .orElse(false)
            task.failOnSnapshots.set(failOnSnapshots)
            task.failOnError.set(true)
            configuration.accept(task)
        })
        tasks.named("check").configure(task -> task.dependsOn(pomChecker))
        return pomChecker
    }

    static String assertVersion(Project p) {
        String version = p.version
        String path = p.path
        return assertVersion(version, path)
    }

    static String assertVersion(String version, String projectPath) {
        if (!version || "unspecified" == version) {
            throw new GradleException("Version of $projectPath is undefined!")
        }
        return version
    }
}
