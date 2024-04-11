package cloud.graal.gcn.dependencies

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

class GcnVersionCatalogUpdatePlugin implements Plugin<Project> {

    void apply(Project project) {
        TaskContainer tasks = project.tasks
        Directory gradleDirectory = project.layout.projectDirectory.dir("../gradle")
        TaskProvider<VersionCatalogUpdate> updater = tasks.register("updateVersionCatalogs", VersionCatalogUpdate.class, task -> {
            task.catalogsDirectory.convention(gradleDirectory)
            task.outputDirectory.convention(project.getLayout().getBuildDirectory().dir("catalogs-update"))
            task.rejectedQualifiers.convention(["alpha", "beta", "rc", "cr", "m", "preview", "b", "ea"])
            task.ignoredModules.convention([
                    "org.jetbrains.kotlin:kotlin-annotation-processing-embeddable",
                    "org.jetbrains.kotlin:kotlin-compiler-embeddable",
                    "org.jetbrains.kotlin:kotlin-reflect",
                    "org.jetbrains.kotlin:kotlin-stdlib",
                    "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
                    "org.jetbrains.kotlin:kotlin-test"
            ])
            task.allowMajorUpdates.convention(false)
            task.allowMinorUpdates.convention(false)
        })
        tasks.register("useLatestVersions", Copy, task -> {
            VersionCatalogUpdate dependent = updater.get()
            task.from(dependent.outputDirectory)
            task.into(project.providers.environmentVariable("CI").map(value -> gradleDirectory).orElse(dependent.catalogsDirectory))
        })
        tasks.register("dependencyUpdates", task -> task.setDescription("Compatibility task with the old update mechanism"))
    }
}
