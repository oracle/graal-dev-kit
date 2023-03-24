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
