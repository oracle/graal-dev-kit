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
package cloud.graal.gcn.feature.create.gatewayfunction;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.GcnFeatureContext;
import cloud.graal.gcn.feature.replaced.GcnAwsLambda;
import cloud.graal.gcn.model.GcnCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.feature.aws.AmazonApiGateway;
import io.micronaut.starter.feature.function.awslambda.AwsLambda;
import io.micronaut.starter.feature.json.SerializationJacksonFeature;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.AWS;

/**
 * AWS create-gateway-function feature.
 *
 * @since 1.0.0
 */
@Singleton
public class GcnAwsCloudGatewayFunction extends AbstractGcnCloudGatewayFunction {

    private final AmazonApiGateway amazonApiGateway;
    private final GcnAwsLambda awsLambda;
    private final SerializationJacksonFeature serializationJacksonFeature;

    /**
     * @param amazonApiGateway            AmazonApiGateway feature
     * @param awsLambda                   GcnAwsLambda feature
     * @param serializationJacksonFeature serialisation Jackson feature
     */
    public GcnAwsCloudGatewayFunction(AmazonApiGateway amazonApiGateway,
                                      GcnAwsLambda awsLambda,
                                      SerializationJacksonFeature serializationJacksonFeature) {
        this.amazonApiGateway = amazonApiGateway;
        this.awsLambda = awsLambda;
        this.serializationJacksonFeature = serializationJacksonFeature;
    }

    @Override
    public void processSelectedFeatures(GcnFeatureContext featureContext) {
        featureContext.addFeature(serializationJacksonFeature, SerializationJacksonFeature.class);
        featureContext.addFeature(awsLambda, AwsLambda.class);
        featureContext.addFeature(amazonApiGateway, AmazonApiGateway.class);
    }

    @Override
    public void apply(GcnGeneratorContext generatorContext) {
        super.apply(generatorContext);
        applyForLib(generatorContext, () -> {
            serializationJacksonFeature.apply(generatorContext);
        });
    }

    protected void applyForLib(GcnGeneratorContext generatorContext,
                               Runnable runnable) {

        GcnCloud cloud = generatorContext.getCloud();

        generatorContext.resetCloud();

        try {
            runnable.run();
        } finally {
            generatorContext.setCloud(cloud);
        }
    }

    @NonNull
    @Override
    public GcnCloud getCloud() {
        return AWS;
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-aws-cloud-gateway-function";
    }
}
