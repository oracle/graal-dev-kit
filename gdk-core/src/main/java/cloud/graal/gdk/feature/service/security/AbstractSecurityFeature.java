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
package cloud.graal.gdk.feature.service.security;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.feature.service.AbstractGdkServiceFeature;
import cloud.graal.gdk.feature.service.security.template.AuthControllerGroovy;
import cloud.graal.gdk.feature.service.security.template.AuthControllerJava;
import cloud.graal.gdk.feature.service.security.template.AuthControllerKotlin;
import cloud.graal.gdk.feature.service.security.template.AuthHtml;
import cloud.graal.gdk.model.GdkService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.feature.security.SecurityJWT;
import io.micronaut.starter.feature.security.SecurityOAuth2;
import io.micronaut.starter.feature.view.JTE;
import io.micronaut.starter.template.RockerTemplate;

import static cloud.graal.gdk.model.GdkService.SECURITY;

/**
 * Base class for security service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractSecurityFeature extends AbstractGdkServiceFeature {

    private final SecurityOAuth2 securityOAuth2;
    private final SecurityJWT securityJWT;
    private final JTE jte;

    /**
     * @param securityOAuth2 SecurityOAuth2 feature
     * @param securityJWT    SecurityJWT feature
     * @param jte            JTE feature
     */
    protected AbstractSecurityFeature(SecurityOAuth2 securityOAuth2,
                                      SecurityJWT securityJWT,
                                      JTE jte) {
        this.securityOAuth2 = securityOAuth2;
        this.securityJWT = securityJWT;
        this.jte = jte;
    }

    @Override
    public final void apply(GdkGeneratorContext generatorContext) {

        if (generatorContext.generateExampleCode()) {

            Project project = generatorContext.getProject();

            generatorContext.addTemplate(getModuleName(), "AuthController-" + getCloud().getModuleName(),
                    generatorContext.getSourcePath("/{packagePath}/AuthController"),
                    AuthControllerJava.template(project, getAuthControllerNameAttr()),
                    AuthControllerKotlin.template(project, getAuthControllerNameAttr()),
                    AuthControllerGroovy.template(project, getAuthControllerNameAttr()));

            generatorContext.addTemplate("auth.jte-" + getCloud().getModuleName(),
                    new RockerTemplate(getModuleName(), "src/main/jte/auth.jte",
                            AuthHtml.template(getAuthHtmlTitle(), getAuthHtmlLogin())));
        }

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
     * @return the OpenID app name for the login URI, e.g. "gdk" or "cognito"
     */
    protected String getAuthHtmlLogin() {
        return "gdk";
    }

    @Override
    public void processSelectedFeatures(GdkFeatureContext featureContext) {

        featureContext.addFeature(securityOAuth2, SecurityOAuth2.class);
        featureContext.addFeature(securityJWT, SecurityJWT.class);

        if (featureContext.generateExampleCode()) {
            featureContext.addFeature(jte, JTE.class);
        }
    }

    /**
     * Implemented in subclasses to apply cloud-specific changes.
     *
     * @param generatorContext the generator context
     */
    protected abstract void doApply(GdkGeneratorContext generatorContext);

    @NonNull
    @Override
    public final GdkService getService() {
        return SECURITY;
    }
}
