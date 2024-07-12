/*
 * Copyright 2023 Oracle and/or its affiliates
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

package cloud.graal.gdk.rocker.plugin

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
