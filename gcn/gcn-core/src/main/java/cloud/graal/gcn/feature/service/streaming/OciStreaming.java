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
import io.micronaut.starter.feature.config.ApplicationConfiguration;
import io.micronaut.starter.feature.messaging.kafka.Kafka;
import io.micronaut.starter.feature.oraclecloud.OracleCloudSdk;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.OCI;

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
    public void processSelectedFeatures(GcnFeatureContext featureContext) {
        super.processSelectedFeatures(featureContext);
        featureContext.addFeature(oracleCloudSdk, OracleCloudSdk.class);
    }

    @Override
    protected void doApply(GcnGeneratorContext generatorContext) {

        //kafka:
        //  bootstrap:
        //    servers: cell-1.streaming.us-ashburn-1.oci.oraclecloud.com:9092
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
        ApplicationConfiguration config = generatorContext.getConfiguration();
        config.addNested("kafka.bootstrap.servers", "${OCI_STREAM_POOL_FQDN}:9092");
        config.addNested("kafka.max.partition.fetch.bytes", 1048576);
        config.addNested("kafka.max.request.size", 1048576);
        config.addNested("kafka.retries", 3);
        config.addNested("kafka.sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${OCI_TENANCY_NAME}/${OCI_USERNAME}/${OCI_STREAM_POOL_OCID}\" password=\"${OCI_AUTH_TOKEN}\";");
        config.addNested("kafka.sasl.mechanism", "PLAIN");
        config.addNested("kafka.security.protocol", "SASL_SSL");
        config.addNested("kafka.enable.idempotence", "false");
    }

    @NonNull
    @Override
    public GcnCloud getCloud() {
        return OCI;
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-oci-streaming";
    }
}
