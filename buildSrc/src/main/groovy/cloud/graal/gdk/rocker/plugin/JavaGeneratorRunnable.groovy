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

import cloud.graal.gdk.rocker.WhitespaceProcessor
import com.fizzed.rocker.compiler.JavaGenerator
import com.fizzed.rocker.compiler.RockerConfiguration
import com.fizzed.rocker.compiler.RockerUtil
import com.fizzed.rocker.compiler.TemplateParser
import com.fizzed.rocker.model.TemplateModel
import com.fizzed.rocker.runtime.ParserException
import groovy.transform.CompileStatic

import static com.fizzed.rocker.compiler.PlainTextStrategy.STATIC_STRINGS

/**
 * Based on io.micronaut.starter.rocker.plugin.JavaGeneratorRunnable.
 */
@CompileStatic
class JavaGeneratorRunnable implements Runnable {

    private static final String SUFFIX_REGEX = '.*\\.rocker\\.(raw|html)$'

    private final RockerConfiguration configuration = new RockerConfiguration()
    private final TemplateParser parser = new TemplateParser(configuration)
    private final JavaGenerator generator = new JavaGenerator(configuration)

    JavaGeneratorRunnable(File templateDirectory,
                          File outputDirectory) {
        generator.configuration.outputDirectory = outputDirectory
        generator.plainTextStrategy = STATIC_STRINGS
        parser.configuration.options.discardLogicWhitespace = true
        parser.configuration.options.optimize = true
        parser.configuration.options.postProcessing = [WhitespaceProcessor.name] as String[]
        parser.configuration.templateDirectory = templateDirectory
    }

    void run() {
        Collection<File> templateFiles = RockerUtil.listFileTree(configuration.templateDirectory)
                .findAll { it.name.matches(SUFFIX_REGEX) }
        println "Parsing ${templateFiles.size()} Rocker templates"

        int errors = 0
        int generated = 0

        for (File f in templateFiles) {
            try {
                TemplateModel model = parser.parse(f)
                try {
                    generator.generate model
                    generated++
                } catch (Exception e) {
                    throw new RuntimeException("Generating java source failed for $f: $e.message", e)
                }
            } catch (ParserException e) {
                println "Parsing failed for $f:[$e.lineNumber,$e.columnNumber] $e.message"
                errors++
            } catch (IOException e) {
                println "Unable to parse template $f"
                e.printStackTrace()
                errors++
            }
        }

        println "Generated $generated Rocker Java source files"

        if (errors) {
            throw new RuntimeException("Caught $errors errors.")
        }
    }
}
