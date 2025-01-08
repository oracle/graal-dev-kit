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
package cloud.graal.gdk.feature.service.email;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.service.AbstractGdkServiceFeature;
import cloud.graal.gdk.model.GdkService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.feature.validator.MicronautValidationFeature;

import static cloud.graal.gdk.model.GdkService.EMAIL;
import static io.micronaut.starter.application.ApplicationType.FUNCTION;

/**
 * Base class for email service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractEmailFeature extends AbstractGdkServiceFeature {

    private static final Dependency HTTP_SERVER_NETTY = Dependency.builder()
            .groupId("io.micronaut")
            .artifactId("micronaut-http-server-netty")
            .testRuntime()
            .build();

    private final MicronautValidationFeature micronautValidationFeature;

    protected AbstractEmailFeature(MicronautValidationFeature micronautValidationFeature) {
        this.micronautValidationFeature = micronautValidationFeature;
    }

    @Override
    public final void apply(GdkGeneratorContext generatorContext) {

        applyForLib(generatorContext, () -> {
            micronautValidationFeature.apply(generatorContext);
        });

        doApply(generatorContext);
        if (generatorContext.getApplicationType() == FUNCTION) {
            generatorContext.addDependency(HTTP_SERVER_NETTY);
        }
    }

    /**
     * Implemented in subclasses to apply cloud-specific changes.
     *
     * @param generatorContext the generator context
     */
    protected abstract void doApply(GdkGeneratorContext generatorContext);

    @NonNull
    @Override
    public final GdkService getService() {
        return EMAIL;
    }
}
