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
import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.feature.gcp.secretsmanager.GoogleSecretManager;
import jakarta.inject.Singleton;

import static cloud.graal.gdk.model.GdkCloud.GCP;

/**
 * GCP secret management service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class GcpSecretManagement extends AbstractSecretManagementFeature {

    private final GoogleSecretManager googleSecretManager;

    /**
     * @param googleSecretManager GoogleSecretManager feature
     */
    public GcpSecretManagement(GoogleSecretManager googleSecretManager) {
        this.googleSecretManager = googleSecretManager;
    }

    @Override
    public void processSelectedFeatures(GdkFeatureContext featureContext) {
        featureContext.addFeature(googleSecretManager, GoogleSecretManager.class);
    }

    @Override
    public void apply(GdkGeneratorContext generatorContext) {
        // TODO move this to base class so it's applied for all clouds - will need to update AWS and OCI guides
        generatorContext.getTestBootstrapConfiguration().addNested("micronaut.config-client.enabled", false);
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return GCP;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-gcp-secretmanagement";
    }
}
