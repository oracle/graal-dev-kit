package cloud.graal.gcn.rocker.plugin

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider

/**
 * Based on io.micronaut.starter.rocker.plugin.RockerPlugin.
 */
@CompileStatic
class RockerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.convention.getPlugin(JavaPluginConvention).sourceSets.all(sourceSet ->
                processSourceSet(project, sourceSet as SourceSet))
    }

    private static void processSourceSet(Project project, SourceSet sourceSet) {

        RockerSourceSetProperty rockerProperty = new RockerSourceSetProperty(project)
        new DslObject(sourceSet).convention.plugins.rocker = rockerProperty

        String taskName = sourceSet.getTaskName('generate', 'RockerTemplateSource')
        TaskProvider<RockerTask> rockerTaskProvider = project.tasks.register(taskName, RockerTask, (RockerTask rockerTask) -> {
            rockerTask.group = 'build'
            rockerTask.description = 'Generate Sources from ' + sourceSet.name + ' Rocker Templates'
            rockerTask.templateDirs.from rockerProperty.rocker.srcDirs
        })

        sourceSet.java.srcDir rockerTaskProvider
    }
}
