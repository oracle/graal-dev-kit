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
package cloud.graal.gdk.feature.service.objectstore;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.feature.objectstorage.ObjectStorageGcp;
import jakarta.inject.Singleton;

import java.util.Map;

import static cloud.graal.gdk.model.GdkCloud.GCP;

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
    protected void addFeature(GdkFeatureContext featureContext) {
        featureContext.addFeature(objectStorageGcp, ObjectStorageGcp.class);
    }

    @Override
    protected void addConfig(GdkGeneratorContext generatorContext) {
        //micronaut:
        //  object-storage:
        //    gcp:
        //      default:
        //        bucket:
        generatorContext.getCloudConfiguration().addNested(Map.of(
                "micronaut.object-storage.gcp.default.bucket", "",
                "micronaut.object-storage.gcp.default.enabled", "true"
        ));
        generatorContext.getTestConfiguration().addNested("micronaut.object-storage.gcp.default.enabled", "false");
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return GCP;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-gcp-objectstore";
    }
}
