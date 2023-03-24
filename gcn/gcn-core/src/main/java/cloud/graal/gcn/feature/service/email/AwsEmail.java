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
import cloud.graal.gcn.feature.service.email.template.AwsEmailSenderReplacementGroovy;
import cloud.graal.gcn.feature.service.email.template.AwsEmailSenderReplacementJava;
import cloud.graal.gcn.feature.service.email.template.AwsEmailSenderReplacementKotlin;
import cloud.graal.gcn.feature.service.email.template.AwsMailControllerGroovy;
import cloud.graal.gcn.feature.service.email.template.AwsMailControllerJava;
import cloud.graal.gcn.feature.service.email.template.AwsMailControllerKotlin;
import cloud.graal.gcn.feature.service.email.template.AwsMailControllerSpec;
import cloud.graal.gcn.feature.service.email.template.AwsMailControllerTestGroovyJUnit;
import cloud.graal.gcn.feature.service.email.template.AwsMailControllerTestJava;
import cloud.graal.gcn.feature.service.email.template.AwsMailControllerTestKotest;
import cloud.graal.gcn.feature.service.email.template.AwsMailControllerTestKotlinJUnit;
import cloud.graal.gcn.model.GcnCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.feature.aws.AwsV2Sdk;
import io.micronaut.starter.feature.email.AmazonSesEmailFeature;
import io.micronaut.starter.feature.reactor.Reactor;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.AWS;
import static io.micronaut.starter.options.TestFramework.SPOCK;

/**
 * AWS email service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class AwsEmail extends AbstractEmailFeature {

    private final AmazonSesEmailFeature amazonSesEmailFeature;
    private final AwsV2Sdk awsV2Sdk;
    private final Reactor reactor;

    /**
     * @param amazonSesEmailFeature AmazonSesEmailFeature feature
     * @param awsV2Sdk              AwsV2Sdk feature
     * @param reactor               Reactor feature
     */
    public AwsEmail(AmazonSesEmailFeature amazonSesEmailFeature,
                    AwsV2Sdk awsV2Sdk,
                    Reactor reactor) {
        this.awsV2Sdk = awsV2Sdk;
        this.amazonSesEmailFeature = amazonSesEmailFeature;
        this.reactor = reactor;
    }

    @Override
    public void processSelectedFeatures(GcnFeatureContext featureContext) {

        featureContext.addFeature(awsV2Sdk, AwsV2Sdk.class);
        featureContext.addFeature(amazonSesEmailFeature, AmazonSesEmailFeature.class);

        if (featureContext.generateExampleCode()) {
            featureContext.addFeature(reactor, Reactor.class);
        }
    }

    @Override
    protected void doApply(GcnGeneratorContext generatorContext) {

        if (generatorContext.generateExampleCode()) {

            Project project = generatorContext.getProject();

            generatorContext.addTemplate(getModuleName(), "AwsMailController",
                    generatorContext.getSourcePath("/{packagePath}/MailController"),
                    AwsMailControllerJava.template(project),
                    AwsMailControllerKotlin.template(project),
                    AwsMailControllerGroovy.template(project));

            generatorContext.addTestTemplate(getModuleName(), "AwsMailControllerTest",
                    generatorContext.getTestSourcePath("/{packagePath}/MailController"),
                    AwsMailControllerSpec.template(project),
                    AwsMailControllerTestJava.template(project),
                    AwsMailControllerTestGroovyJUnit.template(project),
                    AwsMailControllerTestKotlinJUnit.template(project),
                    AwsMailControllerTestKotest.template(project));

            String specName = generatorContext.getTestFramework() == SPOCK ? "MailControllerSpec" : "MailControllerTest";
            generatorContext.addTestHelperTemplate(getModuleName(), "AwsEmailSenderReplacement",
                    "/{packagePath}/EmailSenderReplacement",
                    AwsEmailSenderReplacementJava.template(project),
                    AwsEmailSenderReplacementKotlin.template(project),
                    AwsEmailSenderReplacementGroovy.template(project, specName));
        }
    }

    @NonNull
    @Override
    public GcnCloud getCloud() {
        return AWS;
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-aws-email";
    }
}
