package cloud.graal.gcn.rocker.plugin

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

/**
 * Based on io.micronaut.starter.rocker.plugin.RockerSourceSetProperty.
 */
@CompileStatic
class RockerSourceSetProperty {

    final TemplateDirectorySet rocker

    RockerSourceSetProperty(Project project) {
        rocker = new TemplateDirectorySet(project)
    }

    RockerSourceSetProperty rocker(Closure<?> configureClosure) {
        ConfigureUtil.configure configureClosure, rocker
        this
    }

    RockerSourceSetProperty rocker(Action<? super TemplateDirectorySet> configureAction) {
        configureAction.execute rocker
        this
    }
}
