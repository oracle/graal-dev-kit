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
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.feature.function.gcp.AbstractGoogleCloudFunction;
import io.micronaut.starter.feature.function.gcp.GoogleCloudFunction;
import io.micronaut.starter.feature.function.gcp.GoogleCloudFunctionFeatureValidator;
import io.micronaut.starter.feature.graalvm.GraalVM;
import io.micronaut.starter.feature.validation.FeatureValidator;
import io.micronaut.starter.options.JdkVersion;
import io.micronaut.starter.options.Options;
import jakarta.inject.Singleton;

import java.util.Set;

import static io.micronaut.starter.options.JdkVersion.JDK_11;
import static io.micronaut.starter.options.JdkVersion.JDK_17;
import static io.micronaut.starter.options.JdkVersion.JDK_21;

@Replaces(GoogleCloudFunctionFeatureValidator.class)
@Singleton
public class GdkGoogleCloudFunctionFeatureValidator implements FeatureValidator {

    private static boolean supports(JdkVersion jdkVersion) {
        return jdkVersion == JDK_11 || jdkVersion == JDK_17 || jdkVersion == JDK_21;
    }

    @Override
    public void validatePreProcessing(Options options, ApplicationType applicationType, Set<Feature> features) {
        if (features.stream().anyMatch(AbstractGoogleCloudFunction.class::isInstance)) {
            if (features.stream().anyMatch(GraalVM.class::isInstance)) {
                throw new IllegalArgumentException("""
                        Google Cloud Function is not supported for GraalVM. \
                        Consider Google Cloud Run for deploying GraalVM native images as docker containers.\
                        """);
            }
        }
    }

    @Override
    public void validatePostProcessing(Options options, ApplicationType applicationType, Set<Feature> features) {
        if (features.stream().anyMatch(GoogleCloudFunction.class::isInstance) && !supports(options.getJavaVersion())) {
            throw new IllegalArgumentException("""
                    Google Cloud Function currently only supports JDK 11, 17 and 21 -- \
                    https://cloud.google.com/functions/docs/concepts/java-runtime""");
        }
    }
}
