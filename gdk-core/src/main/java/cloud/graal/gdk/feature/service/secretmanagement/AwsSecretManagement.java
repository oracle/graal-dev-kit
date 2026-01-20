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
package cloud.graal.gdk.feature.service.secretmanagement;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.feature.service.secretmanagement.template.ClientIdControllerGroovy;
import cloud.graal.gdk.feature.service.secretmanagement.template.ClientIdControllerJava;
import cloud.graal.gdk.feature.service.secretmanagement.template.ClientIdControllerKotlin;
import cloud.graal.gdk.feature.service.secretmanagement.template.SdkHttpClientSpec;
import cloud.graal.gdk.feature.service.secretmanagement.template.SdkHttpClientTestGroovyJUnit;
import cloud.graal.gdk.feature.service.secretmanagement.template.SdkHttpClientTestJava;
import cloud.graal.gdk.feature.service.secretmanagement.template.SdkHttpClientTestKotest;
import cloud.graal.gdk.feature.service.secretmanagement.template.SdkHttpClientTestKotlinJUnit;
import cloud.graal.gdk.feature.service.secretmanagement.template.SecretsManagerClientSpec;
import cloud.graal.gdk.feature.service.secretmanagement.template.SecretsManagerClientTestGroovyJUnit;
import cloud.graal.gdk.feature.service.secretmanagement.template.SecretsManagerClientTestJava;
import cloud.graal.gdk.feature.service.secretmanagement.template.SecretsManagerClientTestKotest;
import cloud.graal.gdk.feature.service.secretmanagement.template.SecretsManagerClientTestKotlinJUnit;
import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.feature.aws.AwsV2Sdk;
import io.micronaut.starter.feature.awssecretsmanager.AwsSecretsManager;
import io.micronaut.starter.feature.config.ApplicationConfiguration;
import io.micronaut.starter.feature.security.SecurityOAuth2;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;

import static cloud.graal.gdk.model.GdkCloud.AWS;

/**
 * AWS secret management service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class AwsSecretManagement extends AbstractSecretManagementFeature {

    private final AwsSecretsManager awsSecretsManager;
    private final AwsV2Sdk awsV2Sdk;
    private final SecurityOAuth2 securityOAuth2;

    /**
     * @param awsSecretsManager AwsSecretsManager feature
     * @param awsV2Sdk          AwsV2Sdk feature
     * @param securityOAuth2    SecurityOAuth2 feature
     */
    public AwsSecretManagement(AwsSecretsManager awsSecretsManager,
                               AwsV2Sdk awsV2Sdk,
                               SecurityOAuth2 securityOAuth2) {
        this.awsSecretsManager = awsSecretsManager;
        this.awsV2Sdk = awsV2Sdk;
        this.securityOAuth2 = securityOAuth2;
    }

    @Override
    public void processSelectedFeatures(GdkFeatureContext featureContext) {
        featureContext.addFeature(awsV2Sdk, AwsV2Sdk.class);
        featureContext.addFeature(awsSecretsManager, AwsSecretsManager.class);
        featureContext.addFeature(securityOAuth2, SecurityOAuth2.class);
    }

    @Override
    public void doApply(GdkGeneratorContext generatorContext) {

        if (generatorContext.generateExampleCode()) {

            Project project = generatorContext.getProject();

            generatorContext.getBootstrapConfiguration().addNested("aws.secretsmanager.secrets", List.of(Map.of(
                    "secret-name", "demo-oauth",
                    "prefix", "micronaut.security.oauth2.clients.demo-oauth"
            )));

            generatorContext.addTemplate(getModuleName(), "ClientIdController",
                    generatorContext.getSourcePath("/{packagePath}/ClientIdController"),
                    ClientIdControllerJava.template(project),
                    ClientIdControllerKotlin.template(project),
                    ClientIdControllerGroovy.template(project)
            );

            generatorContext.addTestTemplate(getModuleName(), "SecretsManagerClient",
                    generatorContext.getTestSourcePath("/{packagePath}/SecretsManagerClient"),
                    SecretsManagerClientSpec.template(project),
                    SecretsManagerClientTestJava.template(project),
                    SecretsManagerClientTestGroovyJUnit.template(project),
                    SecretsManagerClientTestKotlinJUnit.template(project),
                    SecretsManagerClientTestKotest.template(project));

            generatorContext.addTestTemplate(getModuleName(), "SdkHttpClient",
                    generatorContext.getTestSourcePath("/{packagePath}/SdkHttpClient"),
                    SdkHttpClientSpec.template(project),
                    SdkHttpClientTestJava.template(project),
                    SdkHttpClientTestGroovyJUnit.template(project),
                    SdkHttpClientTestKotlinJUnit.template(project),
                    SdkHttpClientTestKotest.template(project));
        }

        for (ApplicationConfiguration config : List.of(generatorContext.getDevConfiguration(), generatorContext.getConfiguration())) {
            config.remove("micronaut.security.oauth2.clients.default.client-id");
            config.remove("micronaut.security.oauth2.clients.default.client-secret");
            config.addNested("micronaut.security.oauth2.clients.demo-oauth.client-id", "${OAUTH_CLIENT_ID:XXX}");
            config.addNested("micronaut.security.oauth2.clients.demo-oauth.client-secret", "${OAUTH_CLIENT_SECRET:YYY}");
        }

        if (generatorContext.isJdkVersionAtLeast(25)) {
            addAuthenticationModeInitializeAtBuildTimeClass(generatorContext);
        }
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return AWS;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-aws-secretmanagement";
    }
}
