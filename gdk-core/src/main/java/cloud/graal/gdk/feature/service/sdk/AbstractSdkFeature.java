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
package cloud.graal.gdk.feature.service.sdk;

import cloud.graal.gdk.feature.service.AbstractGdkServiceFeature;
import cloud.graal.gdk.model.GdkService;
import io.micronaut.core.annotation.NonNull;

import static cloud.graal.gdk.model.GdkService.SDK;

/**
 * Base class for SDK service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractSdkFeature extends AbstractGdkServiceFeature {

    @NonNull
    @Override
    public final GdkService getService() {
        return SDK;
    }
}
