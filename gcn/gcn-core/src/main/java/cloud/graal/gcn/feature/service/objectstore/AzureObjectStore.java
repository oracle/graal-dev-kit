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
import io.micronaut.starter.feature.objectstorage.ObjectStorageAzure;
import jakarta.inject.Singleton;

import java.util.Map;

import static cloud.graal.gcn.model.GcnCloud.AZURE;

/**
 * Azure objectstore service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class AzureObjectStore extends AbstractObjectStore {

    private final ObjectStorageAzure objectStorageAzure;

    /**
     * @param objectStorageAzure ObjectStorageAzure feature
     */
    public AzureObjectStore(ObjectStorageAzure objectStorageAzure) {
        this.objectStorageAzure = objectStorageAzure;
    }

    @Override
    protected void addFeature(GcnFeatureContext featureContext) {
        featureContext.addFeature(objectStorageAzure, ObjectStorageAzure.class);
    }

    @Override
    protected void addConfig(GcnGeneratorContext generatorContext) {

        generatorContext.getCloudConfiguration().addNested(Map.of(
                "micronaut.object-storage.azure.default.bucket", "true",
                "micronaut.object-storage.azure.default.container", "${OBJECT_STORAGE_CONTAINER}",
                "micronaut.object-storage.azure.default.endpoint", "${OBJECT_STORAGE_ENDPOINT}"
        ));
        generatorContext.getTestConfiguration().addNested("micronaut.object-storage.azure.default.enabled", "false");
    }

    @NonNull
    @Override
    public GcnCloud getCloud() {
        return AZURE;
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-azure-objectstore";
    }
}
