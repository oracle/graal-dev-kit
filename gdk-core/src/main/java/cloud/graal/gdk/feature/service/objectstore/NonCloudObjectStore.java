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
import cloud.graal.gdk.feature.service.AbstractGdkServiceFeature;
import cloud.graal.gdk.model.GdkCloud;
import cloud.graal.gdk.model.GdkService;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

import static cloud.graal.gdk.model.GdkCloud.NONE;
import static cloud.graal.gdk.model.GdkService.OBJECTSTORE;

/**
 * Non-cloud objectstore service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class NonCloudObjectStore extends AbstractGdkServiceFeature {

    @Override
    public void apply(GdkGeneratorContext generatorContext) {
        // no-op
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return NONE;
    }

    @NonNull
    @Override
    public final GdkService getService() {
        return OBJECTSTORE;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-objectstore";
    }
}
