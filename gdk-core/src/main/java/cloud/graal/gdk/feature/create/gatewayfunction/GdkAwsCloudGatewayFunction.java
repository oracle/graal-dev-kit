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

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.feature.replaced.GdkAwsLambda;
import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.feature.aws.AmazonApiGateway;
import io.micronaut.starter.feature.function.awslambda.AwsLambda;
import io.micronaut.starter.feature.json.SerializationJacksonFeature;
import jakarta.inject.Singleton;

import static cloud.graal.gdk.model.GdkCloud.AWS;

/**
 * AWS create-gateway-function feature.
 *
 * @since 1.0.0
 */
@Singleton
public class GdkAwsCloudGatewayFunction extends AbstractGdkCloudGatewayFunction {

    private final AmazonApiGateway amazonApiGateway;
    private final GdkAwsLambda awsLambda;
    private final SerializationJacksonFeature serializationJacksonFeature;

    /**
     * @param amazonApiGateway            AmazonApiGateway feature
     * @param awsLambda                   GdkAwsLambda feature
     * @param serializationJacksonFeature serialisation Jackson feature
     */
    public GdkAwsCloudGatewayFunction(AmazonApiGateway amazonApiGateway,
                                      GdkAwsLambda awsLambda,
                                      SerializationJacksonFeature serializationJacksonFeature) {
        this.amazonApiGateway = amazonApiGateway;
        this.awsLambda = awsLambda;
        this.serializationJacksonFeature = serializationJacksonFeature;
    }

    @Override
    public void processSelectedFeatures(GdkFeatureContext featureContext) {
        featureContext.addFeature(serializationJacksonFeature, SerializationJacksonFeature.class);
        featureContext.addFeature(awsLambda, AwsLambda.class);
        featureContext.addFeature(amazonApiGateway, AmazonApiGateway.class);
    }

    @Override
    public void apply(GdkGeneratorContext generatorContext) {
        super.apply(generatorContext);
        applyForLib(generatorContext, () -> {
            serializationJacksonFeature.apply(generatorContext);
        });
    }

    protected void applyForLib(GdkGeneratorContext generatorContext,
                               Runnable runnable) {

        GdkCloud cloud = generatorContext.getCloud();

        generatorContext.resetCloud();

        try {
            runnable.run();
        } finally {
            generatorContext.setCloud(cloud);
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
        return "gdk-aws-cloud-gateway-function";
    }
}
