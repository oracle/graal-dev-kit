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
import io.micronaut.starter.feature.messaging.kafka.Kafka;
import io.micronaut.starter.feature.oraclecloud.OracleCloudSdk;
import jakarta.inject.Singleton;

import java.util.Map;

import static cloud.graal.gdk.model.GdkCloud.OCI;

/**
 * OCI streaming service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class OciStreaming extends AbstractStreamingFeature {

    private final OracleCloudSdk oracleCloudSdk;

    /**
     * @param kafka          Kafka feature
     * @param oracleCloudSdk OracleCloudSdk feature
     */
    public OciStreaming(Kafka kafka,
                        OracleCloudSdk oracleCloudSdk) {
        super(kafka);
        this.oracleCloudSdk = oracleCloudSdk;
    }

    @Override
    public void processSelectedFeatures(GdkFeatureContext featureContext) {
        super.processSelectedFeatures(featureContext);
        featureContext.addFeature(oracleCloudSdk, OracleCloudSdk.class);
    }

    @Override
    protected void doApply(GdkGeneratorContext generatorContext) {

        //kafka:
        //  bootstrap:
        //    servers: ${OCI_STREAM_POOL_FQDN}:9092
        //  security:
        //    protocol: SASL_SSL
        //  sasl:
        //    mechanism: PLAIN
        //    jaas:
        //      config: org.apache.kafka.common.security.plain.PlainLoginModule required username="<tenancy-name>/<username>/<stream-pool-ocid>" password="<auth-token>";
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
                "kafka.retries", 3,
                "kafka.enable.idempotence", "false",
                "kafka.sasl.mechanism", "PLAIN"
        ));
        generatorContext.getCloudConfiguration().addNested(Map.of(
                "kafka.bootstrap.servers", "${OCI_STREAM_POOL_FQDN}:9092",
                "kafka.sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${OCI_TENANCY_NAME}/${OCI_USERNAME}/${OCI_STREAM_POOL_OCID}\" password=\"${OCI_AUTH_TOKEN}\";",
                "kafka.security.protocol", "SASL_SSL"
        ));

        addStreamingReflectConfig(generatorContext);
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return OCI;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-oci-streaming";
    }
}
