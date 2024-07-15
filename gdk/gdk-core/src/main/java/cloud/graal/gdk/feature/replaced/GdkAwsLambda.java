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
import io.micronaut.starter.feature.architecture.X86;
import io.micronaut.starter.feature.aws.AwsLambdaEventsSerde;
import io.micronaut.starter.feature.aws.AwsLambdaSnapstart;
import io.micronaut.starter.feature.awslambdacustomruntime.AwsLambdaCustomRuntime;
import io.micronaut.starter.feature.function.awslambda.AwsLambda;
import io.micronaut.starter.feature.function.awslambda.DefaultAwsLambdaHandlerProvider;
import io.micronaut.starter.feature.function.awslambda.FunctionAwsLambdaHandlerProvider;
import io.micronaut.starter.feature.httpclient.HttpClientJdk;
import io.micronaut.starter.feature.other.ShadePlugin;
import jakarta.inject.Singleton;

/**
 * Replaces the default feature to conditionally apply only for AWS. This is
 * needed because AwsLambda is a default feature.
 *
 * @since 1.0.0
 */
@Replaces(AwsLambda.class)
@Singleton
public class GdkAwsLambda extends AwsLambda {

    /**
     * @param shadePlugin   ShadePlugin feature
     * @param customRuntime AwsLambdaCustomRuntime feature
     */
    public GdkAwsLambda(ShadePlugin shadePlugin,
                        AwsLambdaCustomRuntime customRuntime,
                        X86 x86, AwsLambdaSnapstart snapstart,
                        HttpClientJdk httpClientJdk,
                        AwsLambdaEventsSerde awsLambdaEventsSerde,
                        DefaultAwsLambdaHandlerProvider defaultAwsLambdaHandlerProvider,
                        FunctionAwsLambdaHandlerProvider functionAwsLambdaHandlerProvider) {
        super(shadePlugin, customRuntime, x86, snapstart, httpClientJdk, awsLambdaEventsSerde, defaultAwsLambdaHandlerProvider, functionAwsLambdaHandlerProvider);
    }
}
