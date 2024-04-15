package cloud.graal.gcn.pom

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
abstract class GcnBomPlugin implements Plugin<Project> {

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
        TaskProvider<GcnPomChecker> checkBom = registerPomChecker("checkBom", project, task -> { })
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

        publishing.publications.named("maven", MavenPublication, pub -> {
            pub.artifactId = "gcn-bom"
            pub.groupId = "cloud.graal.gcn"
            pub.from(project.components.named("javaPlatform").get())

            pub.pom(pom -> {
                pom.packaging = "pom"
                modelProvider.librariesTable.forEach(library -> {
                    if (!library.alias.startsWith("exclude-")) {
                        String alias = Optional.ofNullable(library.version.reference).map(a -> a.replace('-', '.')).orElse("")
                        String bomPropertyName = bomPropertyName(alias)
                        def version = modelProvider.versionsTable.find(x -> x.reference == library.version.reference).version
                        mapAliasToVersion[library.alias] = '\${' + bomPropertyName + '}'
                        pom.properties.put(bomPropertyName, version.require)
                    }
                })
                pom.withXml { xml ->
                    Node dependencies = childOf(childOf(xml.asNode(), "dependencyManagement"), "dependencies")
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
                existingDep.version { it.strictly(mapAliasToVersion.get(alias.replace(".", "-"))) }
            } else if (alias.endsWith(".bom")) {
                DefaultMutableMinimalDependency bomDependency = project.dependencies.platform(versionCatalog.findLibrary(alias)
                        .map(Provider::get)
                        .orElseThrow(() -> new RuntimeException("Unexpected missing alias in catalog: " + alias))
                ) as DefaultMutableMinimalDependency
                bomDependency.version { it.strictly(mapAliasToVersion.get(alias.replace(".", "-"))) }
                api.dependencies.add(bomDependency)
                catalogs.dependencies.add(bomDependency)

            } else if (!alias.startsWith("exclude.")) {
                DependencyConstraint dependencyConstraint = project.dependencies.constraints.create(lib)
                dependencyConstraint.version { it.strictly(mapAliasToVersion.get(alias.replace(".", "-"))) }
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
                    Provider<String> externalRepoUsername = project.providers.environmentVariable("PUBLISH_USERNAME")
                    Provider<String> externalRepoPassword = project.providers.environmentVariable("PUBLISH_PASSWORD")
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

    static TaskProvider<GcnPomChecker> registerPomChecker(String taskName, Project project,
                                                          Consumer<? super GcnPomChecker> configuration) {
        TaskContainer tasks = project.tasks
        TaskProvider<GcnPomChecker> pomChecker = tasks.register(taskName, GcnPomChecker, task -> {
            task.repositories.add("https://repo.maven.apache.org/maven2/")
            task.repositories.add("https://maven.oracle.com/public/")

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
            task.pomCoordinates.set("cloud.graal.gcn:gcn-bom:" + version)
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
