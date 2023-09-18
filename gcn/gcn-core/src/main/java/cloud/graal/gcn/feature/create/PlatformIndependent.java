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
package cloud.graal.gcn.feature.create;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.AbstractGcnFeature;
import cloud.graal.gcn.feature.create.gatewayfunction.AbstractGcnCloudGatewayFunction;
import cloud.graal.gcn.model.GcnCloud;
import cloud.graal.gcn.template.BuildGradlePostProcessor;
import cloud.graal.gcn.template.LogbackXmlPostProcessor;
import cloud.graal.gcn.template.MavenPlatformPostProcessor;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.gradle.GradleDsl;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.NONE;
import static io.micronaut.starter.build.gradle.GradleDsl.GROOVY;

/**
 * Added to platform-independent (non-cloud) apps.
 *
 * @since 1.0.0
 */
@Singleton
public class PlatformIndependent extends AbstractGcnFeature {

    /**
     * The feature name.
     */
    public static final String NAME = "gcn-platform-independent";

    @Override
    public void apply(GcnGeneratorContext generatorContext) {
        GradleDsl dsl = generatorContext.getBuildTool().getGradleDsl().orElse(GROOVY);
        generatorContext.addPostProcessor("build", new BuildGradlePostProcessor(dsl, false, generatorContext.getFeature(AbstractGcnCloudGatewayFunction.class).orElse(null) != null, generatorContext.getApplicationType()));
        generatorContext.addPostProcessor("loggingConfig", new LogbackXmlPostProcessor());
        generatorContext.addPostProcessor("mavenPom", new MavenPlatformPostProcessor());
    }

    @NonNull
    @Override
    public GcnCloud getCloud() {
        return NONE;
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isVisible() {
        return false;
    }
}
