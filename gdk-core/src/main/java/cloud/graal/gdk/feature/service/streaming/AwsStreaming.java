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
package cloud.graal.gdk.feature.service.streaming;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.feature.aws.AwsV2Sdk;
import io.micronaut.starter.feature.messaging.kafka.Kafka;
import jakarta.inject.Singleton;

import java.util.Map;

import static cloud.graal.gdk.model.GdkCloud.AWS;

/**
 * AWS streaming service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class AwsStreaming extends AbstractStreamingFeature {

    private static final Dependency AWS_IAM_AUTH_LIB = Dependency.builder()
            .groupId("software.amazon.msk")
            .artifactId("aws-msk-iam-auth")
            .runtime()
            .build();

    private final AwsV2Sdk awsV2Sdk;

    /**
     * @param kafka    Kafka feature
     * @param awsV2Sdk AwsV2Sdk feature
     */
    public AwsStreaming(Kafka kafka,
                        AwsV2Sdk awsV2Sdk) {
        super(kafka);
        this.awsV2Sdk = awsV2Sdk;
    }

    @Override
    public void processSelectedFeatures(GdkFeatureContext featureContext) {
        super.processSelectedFeatures(featureContext);
        featureContext.addFeature(awsV2Sdk, AwsV2Sdk.class);
    }

    @Override
    protected void doApply(GdkGeneratorContext generatorContext) {

        generatorContext.addDependency(AWS_IAM_AUTH_LIB);

        //kafka:
        //  security:
        //    protocol: SASL_SSL
        //  sasl:
        //    mechanism: AWS_MSK_IAM
        //    jaas:
        //      config: software.amazon.msk.auth.iam.IAMLoginModule required;
        //    client:
        //      callback:
        //        handler:
        //          class: software.amazon.msk.auth.iam.IAMClientCallbackHandler
        //  retries: 3
        //  max:
        //    request:
        //      size: 1048576
        //    partition:
        //      fetch:
        //        bytes: 1048576
        generatorContext.getConfiguration().addNested(Map.of(
                "kafka.max.partition.fetch.bytes", 1048576,
                "kafka.max.request.size", 1048576,
                "kafka.retries", 3
        ));
        generatorContext.getCloudConfiguration().addNested(Map.of(
                "kafka.sasl.client.callback.handler.class", "software.amazon.msk.auth.iam.IAMClientCallbackHandler",
                "kafka.sasl.jaas.config", "software.amazon.msk.auth.iam.IAMLoginModule required;",
                "kafka.sasl.mechanism", "AWS_MSK_IAM",
                "kafka.security.protocol", "SASL_SSL"
        ));

        addStreamingReflectConfig(generatorContext);
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return AWS;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-aws-streaming";
    }
}
