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

import cloud.graal.gdk.feature.GdkFeature;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.feature.DefaultFeature;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.feature.FeatureContext;
import io.micronaut.starter.feature.httpclient.HttpClientJdk;
import io.micronaut.starter.feature.other.HttpClient;
import io.micronaut.starter.feature.other.HttpClientTest;
import io.micronaut.starter.options.Options;
import jakarta.inject.Singleton;

import java.util.Set;

/**
 * Replaces the default feature to make it a default feature, and exclude HttpClientTest.
 *
 * @since 4.0
 */
@Replaces(HttpClient.class)
@Singleton
public class GdkHttpClient extends HttpClient implements DefaultFeature {

    private static final String HTTP_CLIENT_TEST_NAME = new HttpClientTest().getName();

    @Override
    public boolean shouldApply(ApplicationType applicationType, Options options, Set<Feature> selectedFeatures) {
        return selectedFeatures.stream().anyMatch(GdkFeature.class::isInstance) && selectedFeatures.stream().noneMatch(HttpClientJdk.class::isInstance);
    }

    @Override
    public void processSelectedFeatures(FeatureContext featureContext) {
        super.processSelectedFeatures(featureContext);
        featureContext.exclude(x -> x.getName().equals(HTTP_CLIENT_TEST_NAME));
    }
}
