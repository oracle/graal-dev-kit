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
package cloud.graal.gdk.feature.create.gatewayfunction;

import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.feature.function.gcp.GoogleCloudRawFunction;
import jakarta.inject.Singleton;

import static cloud.graal.gdk.model.GdkCloud.GCP;

/**
 * GCP create-gateway-function feature.
 *
 * @since 1.0.0
 */
@Singleton
public class GdkGcpCloudGatewayFunction extends AbstractGdkCloudGatewayFunction {

    private final GoogleCloudRawFunction googleCloudFunction;

    /**
     * @param googleCloudFunction {@link GoogleCloudRawFunction} feature
     */
    public GdkGcpCloudGatewayFunction(GoogleCloudRawFunction googleCloudFunction) {
        this.googleCloudFunction = googleCloudFunction;
    }

    @Override
    public void processSelectedFeatures(GdkFeatureContext featureContext) {
        featureContext.addFeature(googleCloudFunction, GoogleCloudRawFunction.class);
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return GCP;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-gcp-cloud-gateway-function";
    }
}
