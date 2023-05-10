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
import cloud.graal.gcn.model.GcnCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.feature.config.ApplicationConfiguration;
import io.micronaut.starter.feature.security.SecurityJWT;
import io.micronaut.starter.feature.security.SecurityOAuth2;
import io.micronaut.starter.feature.view.Thymeleaf;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.AWS;

/**
 * AWS security service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class AwsSecurity extends AbstractSecurityFeature {

    /**
     * @param securityOAuth2 SecurityOAuth2 feature
     * @param securityJWT    SecurityJWT feature
     * @param thymeleaf      Thymeleaf feature
     */
    public AwsSecurity(SecurityOAuth2 securityOAuth2,
                       SecurityJWT securityJWT,
                       Thymeleaf thymeleaf) {
        super(securityOAuth2, securityJWT, thymeleaf);
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
    }

    @Override
    protected String getAuthHtmlTitle() {
        return "Cognito example";
    }

    @Override
    protected String getAuthHtmlLogin() {
        return "cognito";
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
