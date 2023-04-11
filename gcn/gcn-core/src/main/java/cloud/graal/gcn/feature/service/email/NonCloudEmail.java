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
import io.micronaut.starter.feature.config.ApplicationConfiguration;
import io.micronaut.starter.feature.email.JavamailFeature;

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

    private final JavamailFeature javamailFeature;

    /**
     * @param javamailFeature      JavamailFeature feature
     */
    public NonCloudEmail(JavamailFeature javamailFeature) {
        this.javamailFeature = javamailFeature;
    }

    @Override
    public void processSelectedFeatures(GcnFeatureContext featureContext) {
        featureContext.addFeature(javamailFeature, JavamailFeature.class);
    }

    @Override
    protected void doApply(GcnGeneratorContext generatorContext) {

//        micronaut:
//            email:
//                from:
//                    email: ${FROM_EMAIL}
//                    name: ${FROM_NAME:}
//        javamail:
//            properties:
//                mail:
//                    smtp:
//                        port: ${SMTP_PORT} || 465 (example code)
//                        auth: true
//                        host: ${SMTP_HOST} || smtp.gmail.com
//                        ssl:
//                            enable: false || true
//                        starttls:
//                            enable: false
//            authentication:
//                username: ${SMTP_USERNAME} || ${FROM_EMAIL}
//                password: ${SMTP_PASSWORD} || ${FROM_PASSWORD}
        ApplicationConfiguration config = generatorContext.getConfiguration();
        config.addNested("micronaut.email.from.email", "${FROM_EMAIL}");
        config.addNested("micronaut.email.from.name", "${FROM_NAME:}");

        config.addNested("javamail.properties.mail.smtp.auth", true);
        config.addNested("javamail.properties.mail.smtp.starttls.enable", false);

        if (generatorContext.generateExampleCode()) {

            config.addNested("javamail.properties.mail.smtp.port", 465);
            config.addNested("javamail.properties.mail.smtp.ssl.enable", true);
            config.addNested("javamail.properties.mail.smtp.host", "smtp.gmail.com");
            config.addNested("javamail.authentication.username", "${FROM_EMAIL}");
            config.addNested("javamail.authentication.password", "${FROM_PASSWORD}");

            Project project = generatorContext.getProject();

            generatorContext.addTemplate(getModuleName(), "NonCloudMailController",
                    generatorContext.getSourcePath("/{packagePath}/EmailController"),
                    NonCloudMailControllerJava.template(project),
                    NonCloudMailControllerKotlin.template(project),
                    NonCloudMailControllerGroovy.template(project));

            generatorContext.addTestTemplate(getModuleName(), "NonCloudMailControllerTest",
                    generatorContext.getTestSourcePath("/{packagePath}/EmailController"),
                    NonCloudMailControllerSpec.template(project),
                    NonCloudMailControllerTestJava.template(project),
                    NonCloudMailControllerTestGroovyJUnit.template(project),
                    NonCloudMailControllerTestKotlinJUnit.template(project),
                    NonCloudMailControllerTestKotest.template(project));

        } else {
            config.addNested("javamail.properties.mail.smtp.port", "${SMTP_PORT}");
            config.addNested("javamail.properties.mail.smtp.ssl.enable", false);
            config.addNested("javamail.properties.mail.smtp.host", "${SMTP_HOST}");
            config.addNested("javamail.authentication.username", "${SMTP_USERNAME}");
            config.addNested("javamail.authentication.password", "${SMTP_PASSWORD}");
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
