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
package cloud.graal.gcn.feature.service.security;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.GcnFeatureContext;
import cloud.graal.gcn.feature.service.security.template.AuthControllerGroovy;
import cloud.graal.gcn.feature.service.security.template.AuthControllerJava;
import cloud.graal.gcn.feature.service.security.template.AuthControllerKotlin;
import cloud.graal.gcn.feature.service.security.template.AuthHtml;
import cloud.graal.gcn.model.GcnCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.feature.config.ApplicationConfiguration;
import io.micronaut.starter.feature.security.SecurityJWT;
import io.micronaut.starter.feature.security.SecurityOAuth2;
import io.micronaut.starter.feature.view.Thymeleaf;
import io.micronaut.starter.template.RockerTemplate;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.AWS;

/**
 * AWS security service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class AwsSecurity extends AbstractSecurityFeature {

    private final SecurityOAuth2 securityOAuth2;
    private final SecurityJWT securityJWT;
    private final Thymeleaf thymeleaf;

    /**
     * @param securityOAuth2 SecurityOAuth2 feature
     * @param securityJWT    SecurityJWT feature
     * @param thymeleaf      Thymeleaf feature
     */
    public AwsSecurity(SecurityOAuth2 securityOAuth2,
                       SecurityJWT securityJWT,
                       Thymeleaf thymeleaf) {
        this.securityOAuth2 = securityOAuth2;
        this.securityJWT = securityJWT;
        this.thymeleaf = thymeleaf;
    }

    @Override
    public void processSelectedFeatures(GcnFeatureContext featureContext) {

        featureContext.addFeature(securityOAuth2, SecurityOAuth2.class);
        featureContext.addFeature(securityJWT, SecurityJWT.class);

        if (featureContext.generateExampleCode()) {
            featureContext.addFeature(thymeleaf, Thymeleaf.class);
        }
    }

    @Override
    protected void doApply(GcnGeneratorContext generatorContext) {
        //micronaut:
        //  security:
        //    authentication: idtoken
        //    oauth2:
        //      clients:
        //        cognito:
        //          client-id: '${OAUTH_CLIENT_ID:xxx}'
        //          client-secret: '${OAUTH_CLIENT_SECRET:yyy}'
        //          openid:
        //            issuer: 'https://cognito-idp.${COGNITO_REGION:zzz}.amazonaws.com/${COGNITO_POOL_ID:www}/'
        //      endpoints:
        //        logout:
        //          get-allowed: true
        ApplicationConfiguration config = generatorContext.getConfiguration();
        config.addNested("micronaut.security.authentication", "idtoken");
        config.addNested("micronaut.security.oauth2.clients.cognito.client-id", "${OAUTH_CLIENT_ID:xxx}");
        config.addNested("micronaut.security.oauth2.clients.cognito.client-secret", "${OAUTH_CLIENT_SECRET:yyy}");
        config.addNested("micronaut.security.oauth2.clients.cognito.openid.issuer", "https://cognito-idp.${COGNITO_REGION:zzz}.amazonaws.com/${COGNITO_POOL_ID:www}/");
        config.addNested("micronaut.security.endpoints.logout.get-allowed", true);

        if (generatorContext.generateExampleCode()) {

            Project project = generatorContext.getProject();

            generatorContext.addTemplate(getModuleName(), "AuthControllerAWS",
                    generatorContext.getSourcePath("/{packagePath}/AuthController"),
                    AuthControllerJava.template(project, "email"),
                    AuthControllerKotlin.template(project, "email"),
                    AuthControllerGroovy.template(project, "email"));

            generatorContext.addTemplate("auth.html-AWS",
                    new RockerTemplate(getModuleName(), "src/main/resources/views/auth.html",
                            AuthHtml.template("Cognito example", "cognito")));
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
        return "gcn-aws-security";
    }
}
