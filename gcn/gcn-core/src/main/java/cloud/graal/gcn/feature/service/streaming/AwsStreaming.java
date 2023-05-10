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
package cloud.graal.gcn.feature.service.streaming;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.GcnFeatureContext;
import cloud.graal.gcn.model.GcnCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.feature.aws.AwsV2Sdk;
import io.micronaut.starter.feature.config.ApplicationConfiguration;
import io.micronaut.starter.feature.messaging.kafka.Kafka;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.AWS;

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
            .version("1.1.5")
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
    public void processSelectedFeatures(GcnFeatureContext featureContext) {
        super.processSelectedFeatures(featureContext);
        featureContext.addFeature(awsV2Sdk, AwsV2Sdk.class);
    }

    @Override
    protected void doApply(GcnGeneratorContext generatorContext) {

        generatorContext.addDependency(AWS_IAM_AUTH_LIB);

        //kafka:
        //  bootstrap:
        //    servers: '${KAFKA_BOOTSTRAP_SERVERS}'
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
        ApplicationConfiguration config = generatorContext.getConfiguration();
        config.addNested("kafka.bootstrap.servers", "${KAFKA_BOOTSTRAP_SERVERS}");
        config.addNested("kafka.max.partition.fetch.bytes", 1048576);
        config.addNested("kafka.max.request.size", 1048576);
        config.addNested("kafka.retries", 3);
        config.addNested("kafka.sasl.client.callback.handler.class", "software.amazon.msk.auth.iam.IAMClientCallbackHandler");
        config.addNested("kafka.sasl.jaas.config", "software.amazon.msk.auth.iam.IAMLoginModule required;");
        config.addNested("kafka.sasl.mechanism", "AWS_MSK_IAM");
        config.addNested("kafka.security.protocol", "SASL_SSL");
    }

    @NonNull
    @Override
    public GcnCloud getCloud() {
        return AWS;
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-aws-streaming";
    }
}
