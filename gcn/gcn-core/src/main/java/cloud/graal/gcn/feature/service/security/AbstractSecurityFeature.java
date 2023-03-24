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
package cloud.graal.gcn.feature.service.security;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.service.AbstractGcnServiceFeature;
import cloud.graal.gcn.model.GcnService;
import io.micronaut.core.annotation.NonNull;

import static cloud.graal.gcn.model.GcnService.SECURITY;

/**
 * Base class for security service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractSecurityFeature extends AbstractGcnServiceFeature {

    @Override
    public final void apply(GcnGeneratorContext generatorContext) {
        addLibPlaceholders(generatorContext);
        doApply(generatorContext);
    }

    /**
     * Implemented in subclasses to apply cloud-specific changes.
     *
     * @param generatorContext the generator context
     */
    protected abstract void doApply(GcnGeneratorContext generatorContext);

    @NonNull
    @Override
    public final GcnService getService() {
        return SECURITY;
    }
}
