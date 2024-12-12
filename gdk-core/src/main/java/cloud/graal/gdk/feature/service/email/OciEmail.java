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
package cloud.graal.gdk.feature.service.email;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.feature.service.email.template.EmailHtml;
import cloud.graal.gdk.feature.service.email.template.OciEmailControllerGroovy;
import cloud.graal.gdk.feature.service.email.template.OciEmailControllerJava;
import cloud.graal.gdk.feature.service.email.template.OciEmailControllerKotlin;
import cloud.graal.gdk.feature.service.email.template.OciEmailControllerSpec;
import cloud.graal.gdk.feature.service.email.template.OciEmailControllerTestGroovyJUnit;
import cloud.graal.gdk.feature.service.email.template.OciEmailControllerTestJava;
import cloud.graal.gdk.feature.service.email.template.OciEmailControllerTestKotest;
import cloud.graal.gdk.feature.service.email.template.OciEmailControllerTestKotlinJUnit;
import cloud.graal.gdk.feature.service.email.template.OciSessionProviderGroovy;
import cloud.graal.gdk.feature.service.email.template.OciSessionProviderJava;
import cloud.graal.gdk.feature.service.email.template.OciSessionProviderKotlin;
import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.feature.email.JavamailFeature;
import io.micronaut.starter.feature.email.TemplateEmailFeature;
import io.micronaut.starter.feature.validator.MicronautValidationFeature;
import io.micronaut.starter.feature.view.JTE;
import io.micronaut.starter.template.RockerTemplate;
import jakarta.inject.Singleton;

import java.util.Map;

import static cloud.graal.gdk.model.GdkCloud.OCI;

/**
 * OCI email service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class OciEmail extends AbstractEmailFeature {

    private final JavamailFeature javamailFeature;
    private final TemplateEmailFeature templateEmailFeature;
    private final JTE jte;

    /**
     * @param javamailFeature      JavamailFeature feature
     * @param templateEmailFeature TemplateEmailFeature feature
     * @param jte                  JTE feature
     */
    public OciEmail(JavamailFeature javamailFeature,
                    TemplateEmailFeature templateEmailFeature,
                    JTE jte,
                    MicronautValidationFeature micronautValidationFeature) {
        super(micronautValidationFeature);
        this.javamailFeature = javamailFeature;
        this.templateEmailFeature = templateEmailFeature;
        this.jte = jte;
    }

    @Override
    public void processSelectedFeatures(GdkFeatureContext featureContext) {

        featureContext.addFeature(javamailFeature, JavamailFeature.class);

        if (featureContext.generateExampleCode()) {
            featureContext.addFeature(templateEmailFeature, TemplateEmailFeature.class);
            featureContext.addFeature(jte, JTE.class);
        }
    }

    @Override
    protected void doApply(GdkGeneratorContext generatorContext) {

        //micronaut:
        //  email:
        //    from:
        //      email: ${FROM_EMAIL:''}
        //      name: ${FROM_NAME:''}
        //smtp:
        //  password: ${SMTP_PASSWORD:''}
        //  user: ${SMTP_USER:''}
        //javamail:
        //  properties:
        //    mail:
        //      smtp:
        //        port: 587
        //        auth: true
        //        starttls:
        //          enable: true
        //        host: ${SMTP_HOST:''}
        generatorContext.getConfiguration().addNested(Map.of(
                "micronaut.email.from.email", "${FROM_EMAIL:''}",
                "micronaut.email.from.name", "${FROM_NAME:''}",
                "smtp.password", "${SMTP_PASSWORD:''}",
                "smtp.user", "${SMTP_USER:''}",
                "javamail.properties.mail.smtp.port", 587,
                "javamail.properties.mail.smtp.auth", true,
                "javamail.properties.mail.smtp.starttls.enable", true,
                "javamail.properties.mail.smtp.host", "${SMTP_HOST:''}"
        ));

        if (generatorContext.generateExampleCode()) {

//            micronaut:
//              email:
//                from:
//                  email: xyz@gdk.example
//                  name: Email Test
//            smtp:
//              password: example-password
//              user: gdkdemo
//            javamail:
//              properties:
//                mail:
//                  smtp:
//                   host: smtp.com
            generatorContext.getTestConfiguration().addNested(Map.of(
                    "micronaut.email.from.email", "xyz@gdk.example",
                    "micronaut.email.from.name", "Email Test",
                    "smtp.password", "example-password",
                    "smtp.user", "gdkdemo",
                    "javamail.properties.mail.smtp.host", "smtp.com"
            ));

            Project project = generatorContext.getProject();

            generatorContext.addTemplate(getModuleName(), "OciSessionProvider",
                    generatorContext.getSourcePath("/{packagePath}/OciSessionProvider"),
                    OciSessionProviderJava.template(project),
                    OciSessionProviderKotlin.template(project),
                    OciSessionProviderGroovy.template(project));

            generatorContext.addTemplate(getModuleName(), "OciEmailController",
                    generatorContext.getSourcePath("/{packagePath}/OciEmailController"),
                    OciEmailControllerJava.template(project),
                    OciEmailControllerKotlin.template(project),
                    OciEmailControllerGroovy.template(project));

            generatorContext.addTemplate("email.html-OCI",
                    new RockerTemplate(getModuleName(), "src/main/jte/email.jte",
                            EmailHtml.template()));

            generatorContext.addTestTemplate(getModuleName(), "OciEmailControllerTest",
                    generatorContext.getTestSourcePath("/{packagePath}/EmailController"),
                    OciEmailControllerSpec.template(project),
                    OciEmailControllerTestJava.template(project),
                    OciEmailControllerTestGroovyJUnit.template(project),
                    OciEmailControllerTestKotlinJUnit.template(project),
                    OciEmailControllerTestKotest.template(project));
        }
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return OCI;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-oci-email";
    }
}
