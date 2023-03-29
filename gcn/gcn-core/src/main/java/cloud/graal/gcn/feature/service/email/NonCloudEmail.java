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
package cloud.graal.gcn.feature.service.email;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.GcnFeatureContext;
import cloud.graal.gcn.feature.service.email.template.NonCloudEmailSenderReplacementGroovy;
import cloud.graal.gcn.feature.service.email.template.NonCloudEmailSenderReplacementJava;
import cloud.graal.gcn.feature.service.email.template.NonCloudEmailSenderReplacementKotlin;
import cloud.graal.gcn.feature.service.email.template.NonCloudMailControllerGroovy;
import cloud.graal.gcn.feature.service.email.template.NonCloudMailControllerJava;
import cloud.graal.gcn.feature.service.email.template.NonCloudMailControllerKotlin;
import cloud.graal.gcn.feature.service.email.template.NonCloudMailControllerSpec;
import cloud.graal.gcn.feature.service.email.template.NonCloudMailControllerTestGroovyJUnit;
import cloud.graal.gcn.feature.service.email.template.NonCloudMailControllerTestJava;
import cloud.graal.gcn.feature.service.email.template.NonCloudMailControllerTestKotest;
import cloud.graal.gcn.feature.service.email.template.NonCloudMailControllerTestKotlinJUnit;
import cloud.graal.gcn.model.GcnCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.feature.email.SendGridEmailFeature;
import io.micronaut.starter.feature.reactor.Reactor;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.NONE;
import static io.micronaut.starter.template.Template.ROOT;

/**
 * Non-cloud email service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class NonCloudEmail extends AbstractEmailFeature {
    private final SendGridEmailFeature sendGridEmailFeature;
    private final Reactor reactor;

    public NonCloudEmail(SendGridEmailFeature sendGridEmailFeature, Reactor reactor) {
        this.sendGridEmailFeature = sendGridEmailFeature;
        this.reactor = reactor;
    }

    @Override
    public void processSelectedFeatures(GcnFeatureContext featureContext) {
        featureContext.addFeature(sendGridEmailFeature, SendGridEmailFeature.class);
        if (featureContext.generateExampleCode()) {
            featureContext.addFeature(reactor, Reactor.class);
        }
    }

    @Override
    protected void doApply(GcnGeneratorContext generatorContext) {

//        micronaut:
//          email:
//            from:
//              email: 'mo@gcn.example'
        generatorContext.getConfiguration().addNested("micronaut.email.from.email", "mo@gcn.example");

        if (generatorContext.generateExampleCode()) {

            Project project = generatorContext.getProject();

            generatorContext.addTemplate(getModuleName(), "NonCloudMailController",
                    generatorContext.getSourcePath("/{packagePath}/MailController"),
                    NonCloudMailControllerJava.template(project),
                    NonCloudMailControllerKotlin.template(project),
                    NonCloudMailControllerGroovy.template(project));

            generatorContext.addTestTemplate(getModuleName(), "NonCloudMailControllerTest",
                    generatorContext.getTestSourcePath("/{packagePath}/MailController"),
                    NonCloudMailControllerSpec.template(project),
                    NonCloudMailControllerTestJava.template(project),
                    NonCloudMailControllerTestGroovyJUnit.template(project),
                    NonCloudMailControllerTestKotlinJUnit.template(project),
                    NonCloudMailControllerTestKotest.template(project));

            generatorContext.addTestHelperTemplate(getModuleName(), "NonCloudEmailSenderReplacement",
                    "/{packagePath}/EmailSenderReplacement",
                    NonCloudEmailSenderReplacementJava.template(project),
                    NonCloudEmailSenderReplacementKotlin.template(project),
                    NonCloudEmailSenderReplacementGroovy.template(project));
        }
    }

    @Override
    protected String getDefaultModule() {
        return ROOT;
    }

    @NonNull
    @Override
    protected String getModuleName() {
        return ROOT;
    }

    @NonNull
    @Override
    public GcnCloud getCloud() {
        return NONE;
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-email";
    }
}
