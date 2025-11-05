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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyConstraint
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.plugins.JavaPlatformExtension
import org.gradle.api.plugins.JavaPlatformPlugin
import org.gradle.api.plugins.PluginManager
import org.gradle.api.plugins.catalog.CatalogPluginExtension
import org.gradle.api.plugins.catalog.VersionCatalogPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskContainer

import static org.gradle.api.plugins.JavaPlatformPlugin.API_CONFIGURATION_NAME


@CompileStatic
@CacheableTask
abstract class GdkParentPlugin implements Plugin<Project> {

    Map<String, String> pluginsMap = Map.of(
            "exclude-io-micronaut-maven-plugin", "micronaut-maven-plugin.version",
            "exclude-io-micronaut-maven-test-resources-plugin", "micronaut.test.resources.version",
    )

    @Override
    void apply(Project project) {
        PluginManager plugins = project.pluginManager
        plugins.apply(MavenPublishPlugin)
        plugins.apply(JavaPlatformPlugin)
        plugins.apply(VersionCatalogPlugin)
        project.extensions.add(DependencyExclusion.NAME, DependencyExclusion)
        configureParent(project)
    }

    private void configureParent(Project project) {
        project.extensions.getByType(JavaPlatformExtension).allowDependencies()
        PublishingExtension publishing = project.extensions.getByType(PublishingExtension)
        DependencyExclusion dependencyExclusion = project.extensions.getByType(DependencyExclusion)
        project.afterEvaluate { unused -> configureLate(project, publishing, dependencyExclusion, project.tasks) }
    }


    private void configureLate(Project project, PublishingExtension publishing, DependencyExclusion dependencyExclusion, TaskContainer tasks) {

        final VersionCatalogConverter modelConverter = new VersionCatalogConverter(
                project.rootProject.file("gradle/templates.versions.toml"),
                project.extensions.findByType(CatalogPluginExtension)
        )

        VersionCatalog versionCatalog = project.extensions.findByType(VersionCatalogsExtension).find("templateLibs").get()
        VersionCatalogTomlModel modelProvider = project.provider(modelConverter::getModel).get()

        Configuration api = project.configurations.named(API_CONFIGURATION_NAME).get()

        Map<String, String> pomProperties = new HashMap<>()

        Map<String, String> mapAliasToVersion = new HashMap<>()
        publishing.publications.named("maven", MavenPublication, pub -> {
            pub.artifactId = "gdk-parent"
            pub.groupId = "cloud.graal.gdk"
            pub.from(project.components.named("javaPlatform").get())
            pub.pom(pom -> {
                pom.packaging = "pom"
                def lib = modelProvider.librariesTable.find(l -> {l.alias == "exclude-io-micronaut-platform-micronaut-parent"})
                def platformVersion = modelProvider.versionsTable.find(x -> x.reference == lib.version.reference).version

                modelProvider.librariesTable.forEach(library -> {
                    if (!library.alias.startsWith("exclude-")) {
                        String alias = Optional.ofNullable(library.version.reference).map(a -> a.replace('-', '.')).orElse("")
                        String bomPropertyName = bomPropertyName(library.alias, alias)
                        def version = modelProvider.versionsTable.find(x -> x.reference == library.version.reference).version
                        mapAliasToVersion[library.alias] = '\${' + bomPropertyName + '}'
                        pomProperties.put(bomPropertyName, version.require)
                    }
                })

                pom.withXml { xml ->
                    Node parent = xml.asNode().appendNode('parent')
                    xml.asNode().children().remove(parent)
                    // add it after packaging
                    xml.asNode().children().add(5, parent)

                    new Node(parent, "groupId", "io.micronaut.platform")
                    new Node(parent, "artifactId", "micronaut-parent")
                    new Node(parent, "version", platformVersion.require)
                }

                pom.packaging = "pom"
                pomProperties.put("micronaut.version", platformVersion.require)


                modelProvider.librariesTable.forEach(library -> {
                    if (!library.alias.startsWith("exclude-") || pluginsMap.containsKey(library.alias) ) {
                        String alias = Optional.ofNullable(library.version.reference).map(a -> a.replace('-', '.')).orElse("")
                        String bomPropertyName = pluginsMap.containsKey(library.alias) ? pluginsMap.get(library.alias) : bomPropertyName(library.alias, alias)
                        def version = modelProvider.versionsTable.find(x -> x.reference == library.version.reference).version
                        pomProperties.put(bomPropertyName, version.require)
                    }
                })

                pom.withXml { xml ->
                    Node properties = new Node(xml.asNode(), "properties")

                    for(String key: pomProperties.keySet().sort()) {
                        new Node(properties, key, pomProperties.get(key))
                    }


                    Node dependencyManagement =  childOf(xml.asNode(), "dependencyManagement")
                    xml.asNode().remove(dependencyManagement)
                    xml.asNode().append(dependencyManagement)

                    Node dependencies = childOf(dependencyManagement, "dependencies")
                    Node gdkBomDependency = new Node(dependencies, "dependency", "")
                    new Node(gdkBomDependency, "groupId", "cloud.graal.gdk")
                    new Node(gdkBomDependency, "artifactId", "gdk-bom")
                    new Node(gdkBomDependency, "version", project.version)
                    new Node(gdkBomDependency, "type", "pom")
                    new Node(gdkBomDependency, "scope", "import")

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

        versionCatalog.libraryAliases.sort { alias -> versionCatalog.findLibrary(alias).get().get().module.toString() }.forEach(alias -> {
            MinimalExternalModuleDependency lib = versionCatalog.findLibrary(alias).map(Provider::get)
                    .orElseThrow(() -> new RuntimeException("Unexpected missing alias in catalog: " + alias)) as MinimalExternalModuleDependency
            DefaultExternalModuleDependency existingDep = api.dependencies.find(x -> x.name == lib.name) as DefaultExternalModuleDependency

            if (existingDep != null) {
                existingDep.version { it.strictly(mapAliasToVersion.get(alias.replace(".", "-"))) }
            } else if (!alias.startsWith("exclude.") && !alias.endsWith(".bom")) {
                DependencyConstraint dependencyConstraint = project.dependencies.constraints.create(lib)
                dependencyConstraint.version { it.strictly(mapAliasToVersion.get(alias.replace(".", "-"))) }
                api.dependencyConstraints.add(dependencyConstraint)
            }
        })

    }

    private static String bomPropertyName(String alias, String version) {
        version = version.replaceFirst(/^boms\./, '')
        String baseName = toPropertyName(version)
        if (baseName.endsWith(".version")) {
            return baseName
        }
        if (alias.contains("bom")) {
            return baseName + '.version'
        } else {
            return baseName + ".gdk.version"
        }
    }

    private static String toPropertyName(String alias) {
        return alias.split("(?=[A-Z])")
                .collect { it.toLowerCase(Locale.US) }
                .join("-")
                .replace('-', '.')
    }

    private static Node childOf(Node node, String name) {
        List<Node> children = (List<Node>) node.children()
        return children.stream().findAll(n -> n instanceof Node).find(n -> nameOf(n as Node) == name) as Node
    }

    private static String nameOf(Node n) {
        def name = n.name()
        if (name instanceof String) {
            return (String) name
        }
        return ((QName) n.name()).localPart
    }


}
