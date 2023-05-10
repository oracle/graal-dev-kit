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
import cloud.graal.gcn.feature.service.AbstractGcnServiceFeature;
import cloud.graal.gcn.feature.service.security.template.AuthControllerGroovy;
import cloud.graal.gcn.feature.service.security.template.AuthControllerJava;
import cloud.graal.gcn.feature.service.security.template.AuthControllerKotlin;
import cloud.graal.gcn.feature.service.security.template.AuthHtml;
import cloud.graal.gcn.model.GcnService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.feature.security.SecurityJWT;
import io.micronaut.starter.feature.security.SecurityOAuth2;
import io.micronaut.starter.feature.view.Thymeleaf;
import io.micronaut.starter.template.RockerTemplate;

import static cloud.graal.gcn.model.GcnService.SECURITY;

/**
 * Base class for security service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractSecurityFeature extends AbstractGcnServiceFeature {

    private final SecurityOAuth2 securityOAuth2;
    private final SecurityJWT securityJWT;
    private final Thymeleaf thymeleaf;

    /**
     * @param securityOAuth2 SecurityOAuth2 feature
     * @param securityJWT    SecurityJWT feature
     * @param thymeleaf      Thymeleaf feature
     */
    protected AbstractSecurityFeature(SecurityOAuth2 securityOAuth2,
                                      SecurityJWT securityJWT,
                                      Thymeleaf thymeleaf) {
        this.securityOAuth2 = securityOAuth2;
        this.securityJWT = securityJWT;
        this.thymeleaf = thymeleaf;
    }

    @Override
    public final void apply(GcnGeneratorContext generatorContext) {

        if (generatorContext.generateExampleCode()) {

            Project project = generatorContext.getProject();

            generatorContext.addTemplate(getModuleName(), "AuthController-" + getCloud().getModuleName(),
                    generatorContext.getSourcePath("/{packagePath}/AuthController"),
                    AuthControllerJava.template(project, getAuthControllerNameAttr()),
                    AuthControllerKotlin.template(project, getAuthControllerNameAttr()),
                    AuthControllerGroovy.template(project, getAuthControllerNameAttr()));

            generatorContext.addTemplate("auth.html-" + getCloud().getModuleName(),
                    new RockerTemplate(getModuleName(), "src/main/resources/views/auth.html",
                            AuthHtml.template(getAuthHtmlTitle(), getAuthHtmlLogin())));
        }

        addLibPlaceholders(generatorContext);
        doApply(generatorContext);
    }

    /**
     * @return the OpenID name attribute, e.g. "email" or "sub"
     */
    protected String getAuthControllerNameAttr() {
        return "email";
    }

    /**
     * @return the HTML page title, e.g. "OpenID Connect" or "Cognito example"
     */
    protected String getAuthHtmlTitle() {
        return "OpenID Connect";
    }

    /**
     * @return the OpenID app name for the login URI, e.g. "gcn" or "cognito"
     */
    protected String getAuthHtmlLogin() {
        return "gcn";
    }

    @Override
    public void processSelectedFeatures(GcnFeatureContext featureContext) {

        featureContext.addFeature(securityOAuth2, SecurityOAuth2.class);
        featureContext.addFeature(securityJWT, SecurityJWT.class);

        if (featureContext.generateExampleCode()) {
            featureContext.addFeature(thymeleaf, Thymeleaf.class);
        }
    }

    /**
     * Implemented in subclasses to apply cloud-specific changes.
     *
     * @param generatorContext the generator context
     */
    protected abstract void doApply(GcnGeneratorContext generatorContext);

    @NonNull
    @Override
    public final GcnService getService() {
        return SECURITY;
    }
}
