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
package cloud.graal.gdk.feature.create;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.AbstractGdkFeature;
import cloud.graal.gdk.model.GdkProjectType;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.feature.Features;
import io.micronaut.starter.feature.database.TransactionalNotSupported;
import io.micronaut.starter.feature.test.template.groovyJunit;
import io.micronaut.starter.feature.test.template.javaJunit;
import io.micronaut.starter.feature.test.template.koTest;
import io.micronaut.starter.feature.test.template.kotlinJunit;
import io.micronaut.starter.feature.test.template.spock;

import static io.micronaut.starter.application.ApplicationType.FUNCTION;
import static io.micronaut.starter.feature.FeaturePhase.LOW;
import static io.micronaut.starter.options.Language.GROOVY;
import static io.micronaut.starter.options.Language.JAVA;
import static io.micronaut.starter.options.Language.KOTLIN;

/**
 * Abstract base class for "create" features. For each user-selected cloud, the
 * feature for the specified project type will be selected, e.g.,
 * GdkAwsCloudApp for create-app + AWS, GdkAzureCloudFunction for create-function + Azure,
 * GdkOciCloudGatewayFunction for create-gateway-function + OCI, etc.
 *
 * @since 1.0.0
 */
public abstract class AbstractGdkCreateFeature extends AbstractGdkFeature {

    @Override
    public void apply(GdkGeneratorContext generatorContext) {

        if (generatorContext.getApplicationType() != FUNCTION) {
            createApplicationClass(generatorContext);
        }
    }

    private void createApplicationClass(GdkGeneratorContext generatorContext) {

        Project project = generatorContext.getProject();
        String path = generatorContext.getLanguage().getSourcePath("/" + project.getPackagePath() + "/Application");
        Features features = generatorContext.getFeatures(getCloud());

        generatorContext.addTemplate(getModuleName(), "application-" + getModuleName(), path,
                io.micronaut.starter.feature.lang.java.application.template(project, features, generatorContext.getApplicationRenderingContext(JAVA), false, false),
                io.micronaut.starter.feature.lang.kotlin.application.template(project, features, generatorContext.getApplicationRenderingContext(KOTLIN), false, false),
                io.micronaut.starter.feature.lang.groovy.application.template(project, features, generatorContext.getApplicationRenderingContext(GROOVY), false, false));

        String testSourcePath = generatorContext.getTestSourcePath("/{packagePath}/{className}");
        boolean transactional = !generatorContext.getFeatures().hasFeature(TransactionalNotSupported.class);
        generatorContext.addTestTemplate(getModuleName(), "applicationTest-" + getModuleName(), testSourcePath,
                spock.template(project, transactional),
                javaJunit.template(project, transactional),
                groovyJunit.template(project, transactional),
                kotlinJunit.template(project, transactional),
                koTest.template(project, transactional));
    }

    /**
     * @return the project type enum
     */
    @NonNull
    public abstract GdkProjectType getProjectType();

    @Override
    public int getOrder() {
        return LOW.getOrder();
    }
}
