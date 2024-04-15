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
package cloud.graal.gcn.feature.service.objectstore;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.GcnFeatureContext;
import cloud.graal.gcn.model.GcnCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.feature.objectstorage.ObjectStorageOracleCloud;
import jakarta.inject.Singleton;

import java.util.Map;

import static cloud.graal.gcn.model.GcnCloud.OCI;

/**
 * OCI objectstore service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class OciObjectStore extends AbstractObjectStore {

    private final ObjectStorageOracleCloud objectStorageOracleCloud;

    /**
     * @param objectStorageOracleCloud ObjectStorageOracleCloud feature
     */
    public OciObjectStore(ObjectStorageOracleCloud objectStorageOracleCloud) {
        this.objectStorageOracleCloud = objectStorageOracleCloud;
    }

    @Override
    protected void addFeature(GcnFeatureContext featureContext) {
        featureContext.addFeature(objectStorageOracleCloud, ObjectStorageOracleCloud.class);
    }

    @Override
    protected void addConfig(GcnGeneratorContext generatorContext) {
        //micronaut:
        //  object-storage:
        //    oracle-cloud:
        //      default:
        //        bucket:
        //        namespace:
        generatorContext.getCloudConfiguration().addNested(Map.of(
                "micronaut.object-storage.oracle-cloud.default.enabled", "true",
                "micronaut.object-storage.oracle-cloud.default.bucket", "",
                "micronaut.object-storage.oracle-cloud.default.namespace", ""
        ));
        generatorContext.getTestConfiguration().addNested("micronaut.object-storage.oracle-cloud.default.enabled", "false");
    }

    @NonNull
    @Override
    public GcnCloud getCloud() {
        return OCI;
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-oci-objectstore";
    }
}
