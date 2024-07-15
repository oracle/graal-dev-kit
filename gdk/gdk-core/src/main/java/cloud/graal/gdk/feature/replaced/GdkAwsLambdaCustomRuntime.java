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
package cloud.graal.gdk.feature.replaced;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.feature.FeatureContext;
import io.micronaut.starter.feature.awslambdacustomruntime.AwsLambdaCustomRuntime;
import io.micronaut.starter.feature.function.awslambda.AwsLambda;
import io.micronaut.starter.feature.httpclient.HttpClientJdk;
import io.micronaut.starter.feature.other.HttpClient;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

/**
 * Replaces the default feature to initialize the provider as early as possible
 * (via AwsLambdaInitializer) and discard it.
 *
 * @since 1.0.0
 */
@Replaces(AwsLambdaCustomRuntime.class)
@Singleton
public class GdkAwsLambdaCustomRuntime extends AwsLambdaCustomRuntime {

    private Provider<AwsLambda> awsLambdaProvider;
    private AwsLambda awsLambda;
    private final HttpClientJdk httpClient;

    GdkAwsLambdaCustomRuntime(Provider<AwsLambda> awsLambda, HttpClientJdk httpClientJdk) {
        super(null, httpClientJdk);
        this.awsLambdaProvider = awsLambda;
        this.httpClient = httpClientJdk;
    }

    @Override
    public void processSelectedFeatures(FeatureContext featureContext) {
        if (awsLambda.supports(featureContext.getApplicationType()) && !featureContext.isPresent(AwsLambda.class)) {
            featureContext.addFeature(awsLambda);
        }
        if (!featureContext.isPresent(HttpClient.class)) {
            featureContext.addFeature(httpClient);
        }
    }

    void init() {
        if (awsLambda == null) {
            awsLambda = awsLambdaProvider.get();
            awsLambdaProvider = null;
        }
    }
}
