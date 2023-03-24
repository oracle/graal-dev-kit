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
package cloud.graal.gcn.feature.replaced;

import cloud.graal.gcn.GcnGeneratorContext;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.awslambdacustomruntime.AwsLambdaCustomRuntime;
import io.micronaut.starter.feature.function.awslambda.AwsLambda;
import io.micronaut.starter.feature.other.ShadePlugin;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.AWS;

/**
 * Replaces the default feature to conditionally apply only for AWS. This is
 * needed because AwsLambda is a default feature.
 *
 * @since 1.0.0
 */
@Replaces(AwsLambda.class)
@Singleton
public class GcnAwsLambda extends AwsLambda {

    /**
     * @param shadePlugin   ShadePlugin feature
     * @param customRuntime AwsLambdaCustomRuntime feature
     */
    public GcnAwsLambda(ShadePlugin shadePlugin,
                        AwsLambdaCustomRuntime customRuntime) {
        super(shadePlugin, customRuntime);
    }

    @Override
    public void apply(GeneratorContext generatorContext) {
        if (AWS == ((GcnGeneratorContext) generatorContext).getCloud()) {
            super.apply(generatorContext);
        }
    }
}
