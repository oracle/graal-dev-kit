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
package cloud.graal.gdk.feature.create;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.AbstractGdkFeature;
import cloud.graal.gdk.feature.create.gatewayfunction.AbstractGdkCloudGatewayFunction;
import cloud.graal.gdk.model.GdkCloud;
import cloud.graal.gdk.template.BuildGradlePostProcessor;
import cloud.graal.gdk.template.LogbackXmlPostProcessor;
import cloud.graal.gdk.template.MavenPlatformPostProcessor;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.gradle.GradleDsl;
import jakarta.inject.Singleton;

import static cloud.graal.gdk.model.GdkCloud.NONE;
import static io.micronaut.starter.build.gradle.GradleDsl.GROOVY;

/**
 * Added to platform-independent (non-cloud) apps.
 *
 * @since 1.0.0
 */
@Singleton
public class PlatformIndependent extends AbstractGdkFeature {

    /**
     * The feature name.
     */
    public static final String NAME = "gdk-platform-independent";

    @Override
    public void apply(GdkGeneratorContext generatorContext) {
        GradleDsl dsl = generatorContext.getBuildTool().getGradleDsl().orElse(GROOVY);
        generatorContext.addPostProcessor("build", new BuildGradlePostProcessor(
                dsl, false,
                generatorContext.getFeature(AbstractGdkCloudGatewayFunction.class).isPresent(),
                generatorContext.getApplicationType(), generatorContext.getJdkVersion()));
        generatorContext.addPostProcessor("loggingConfig", new LogbackXmlPostProcessor());
        generatorContext.addPostProcessor("mavenPom", new MavenPlatformPostProcessor());
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
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
