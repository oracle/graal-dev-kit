package cloud.graal.gcn.rocker.plugin

import groovy.transform.CompileStatic
import org.gradle.api.Project

import static org.gradle.api.PathValidation.DIRECTORY

/**
 * Based on io.micronaut.starter.rocker.plugin.TemplateDirectorySet.
 */
@CompileStatic
class TemplateDirectorySet {

    private Project project
    private final Set<File> templateDirs = []

    TemplateDirectorySet(Project project) {
        this.project = project
    }

    /**
     * Adds the given source directory to this set.
     *
     * @param dir the directory
     * @return this
     */
    TemplateDirectorySet srcDir(dir) {
        templateDirs << project.file(dir, DIRECTORY)
        this
    }

    /**
     * Adds the given source directories to this set.
     *
     * @param dirs the directories
     * @return this
     */
    TemplateDirectorySet srcDirs(Object... dirs) {
        for (dir in dirs) {
            srcDir dir
        }
        this
    }

    /**
     * Sets the source directories for this set.
     *
     * @param srcPaths the source directories
     * @return this
     */
    TemplateDirectorySet setSrcDirs(Iterable<?> srcPaths) {
        templateDirs.clear()
        srcPaths.forEach(this::srcDir)
        return this
    }

    /**
     * Adds the given source to this set.
     *
     * @param source the source
     * @return this
     */
    TemplateDirectorySet source(TemplateDirectorySet source) {
        source.getSrcDirs().forEach(this::srcDir)
        return this
    }

    /**
     * Returns the source directories that make up this set.
     *
     * @return the directories
     */
    Set<File> getSrcDirs() {
        return templateDirs
    }
}
