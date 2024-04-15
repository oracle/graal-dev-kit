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
import io.micronaut.starter.feature.security.SecurityJWT;
import io.micronaut.starter.feature.security.SecurityOAuth2;
import io.micronaut.starter.feature.view.JTE;
import jakarta.inject.Singleton;

import java.util.Map;

import static cloud.graal.gcn.model.GcnCloud.GCP;

/**
 * GCP security service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class GcpSecurity extends AbstractSecurityFeature {

    /**
     * @param securityOAuth2 SecurityOAuth2 feature
     * @param securityJWT    SecurityJWT feature
     * @param jte            JTE feature
     */
    public GcpSecurity(SecurityJWT securityJWT,
                       SecurityOAuth2 securityOAuth2,
                       JTE jte) {
        super(securityOAuth2, securityJWT, jte);
    }

    @Override
    protected void doApply(GcnGeneratorContext generatorContext) {

        //micronaut:
        //  security:
        //    authentication: idtoken
        //    oauth2:
        //      clients:
        //        gcn:
        //          client-id: ${OAUTH_CLIENT_ID:xxx}
        //          client-secret: ${OAUTH_CLIENT_SECRET:yyy}
        //          openid:
        //            issuer: 'https://accounts.google.com'
        //    endpoints:
        //      logout:
        //        enabled: true
        //        get-allowed: true
        generatorContext.getConfiguration().addNested(Map.of(
                "micronaut.security.authentication", "idtoken",
                "micronaut.security.oauth2.clients.gcn.client-id", "${OAUTH_CLIENT_ID:xxx}",
                "micronaut.security.oauth2.clients.gcn.client-secret", "${OAUTH_CLIENT_SECRET:yyy}",
                "micronaut.security.oauth2.clients.gcn.openid.issuer", "https://accounts.google.com",
                "micronaut.security.endpoints.logout.enabled", true,
                "micronaut.security.endpoints.logout.get-allowed", true
        ));
    }

    @NonNull
    @Override
    public GcnCloud getCloud() {
        return GCP;
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-gcp-security";
    }
}
