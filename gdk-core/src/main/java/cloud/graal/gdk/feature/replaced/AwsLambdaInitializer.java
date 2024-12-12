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

import io.micronaut.context.annotation.Context;
import jakarta.inject.Singleton;

/**
 * Workaround for Web Image where the use of jakarta.inject.Provider in AwsLambdaCustomRuntime
 * to lazily load the AwsLambda dependency leaves a reference to the bean context in the
 * implemented io.micronaut.inject.provider.JakartaProviderBeanDefinition, which causes the
 * generated JavaScript to be unnecessarily large. Since this is annotated with @Context,
 * it loads eagerly and both beans will be initialized, so we can initialize the reference
 * early and discard the provider.
 *
 * @since 1.0.0
 */
@Context
@Singleton
class AwsLambdaInitializer {

    @SuppressWarnings("unused")
    AwsLambdaInitializer(GdkAwsLambda awsLambda,
                         GdkAwsLambdaCustomRuntime customRuntime) {
        customRuntime.init();
    }
}
