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
import io.micronaut.starter.feature.objectstorage.ObjectStorageGcp;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.GCP;

/**
 * GCP objectstore service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class GcpObjectStore extends AbstractObjectStore {

    private final ObjectStorageGcp objectStorageGcp;

    /**
     * @param objectStorageGcp ObjectStorageGcp feature
     */
    public GcpObjectStore(ObjectStorageGcp objectStorageGcp) {
        this.objectStorageGcp = objectStorageGcp;
    }

    @Override
    protected void addFeature(GcnFeatureContext featureContext) {
        featureContext.addFeature(objectStorageGcp, ObjectStorageGcp.class);
    }

    @Override
    protected void addConfig(GcnGeneratorContext generatorContext) {
        //micronaut:
        //  object-storage:
        //    gcp:
        //      default:
        //        bucket:
        generatorContext.getCloudConfiguration().addNested("micronaut.object-storage.gcp.default.bucket", "");
        generatorContext.getCloudConfiguration().addNested("micronaut.object-storage.gcp.default.enabled", "true");
        generatorContext.getTestConfiguration().addNested("micronaut.object-storage.gcp.default.enabled", "false");

    }

    @NonNull
    @Override
    public GcnCloud getCloud() {
        return GCP;
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-gcp-objectstore";
    }
}
