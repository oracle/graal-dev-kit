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
package cloud.graal.gdk.feature.replaced;

import cloud.graal.gdk.GdkGeneratorContext;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.function.oraclefunction.OracleFunction;
import io.micronaut.starter.feature.function.oraclefunction.OracleRawFunction;
import io.micronaut.starter.feature.function.oraclefunction.template.raw.oracleRawFunctionGroovy;
import io.micronaut.starter.feature.function.oraclefunction.template.raw.oracleRawFunctionJava;
import io.micronaut.starter.feature.function.oraclefunction.template.raw.oracleRawFunctionKotlin;
import io.micronaut.starter.feature.json.JacksonDatabindFeature;
import io.micronaut.starter.feature.logging.SimpleLogging;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.template.RockerTemplate;
import jakarta.inject.Singleton;

import static cloud.graal.gdk.model.GdkCloud.OCI;
import static io.micronaut.starter.application.ApplicationType.FUNCTION;
import static io.micronaut.starter.options.BuildTool.MAVEN;

@Singleton
@Replaces(OracleRawFunction.class)
public class GdkOracleRawFunction extends OracleRawFunction {

    public GdkOracleRawFunction(SimpleLogging simpleLogging,
                                OracleFunction httpFunction,
                                JacksonDatabindFeature jacksonDatabindFeature) {
        super(simpleLogging, httpFunction, jacksonDatabindFeature);
    }

    @Override
    public void apply(GeneratorContext gc) {
        var generatorContext = (GdkGeneratorContext) gc;

        ApplicationType type = generatorContext.getApplicationType();

        if (type == FUNCTION && (generatorContext.isPlatformIndependent() || generatorContext.getCloud() == OCI)) {
            applyFunction(generatorContext, type);
            Language language = generatorContext.getLanguage();
            Project project = generatorContext.getProject();
            String sourceFile = generatorContext.getSourcePath("/{packagePath}/Function");
            switch (language) {
                case GROOVY:
                    generatorContext.addTemplate("function",
                            new RockerTemplate(sourceFile, oracleRawFunctionGroovy.template(project)));
                    break;
                case KOTLIN:
                    generatorContext.addTemplate("function",
                            new RockerTemplate(sourceFile, oracleRawFunctionKotlin.template(project)));
                    break;
                case JAVA:
                default:
                    generatorContext.addTemplate("function",
                            new RockerTemplate(sourceFile, oracleRawFunctionJava.template(project)));
            }

            if (generatorContext.getBuildTool() == MAVEN) {
                addMicronautRuntimeBuildProperty(generatorContext);
                generatorContext.getBuildProperties().put("jib.docker.tag", "${project.version}");
                generatorContext.getBuildProperties().put("exec.mainClass", "com.fnproject.fn.runtime.EntryPoint");
                generatorContext.getBuildProperties().put("jib.docker.image", "[REGION].ocir.io/[TENANCY]/[REPO]/${project.artifactId}");
                generatorContext.getBuildProperties().put("function.entrypoint", project.getPackageName() + ".Function::handleRequest");
            }

            applyTestTemplate(generatorContext, project, "Function");
        }

        addDependencies(generatorContext);
    }
}
