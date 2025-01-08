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
package cloud.graal.gdk.feature.replaced;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.dependencies.CoordinateResolver;
import io.micronaut.starter.feature.build.MicronautBuildPlugin;
import io.micronaut.starter.feature.build.gradle.Dockerfile;
import io.micronaut.starter.feature.build.gradle.MicronautApplicationGradlePlugin;
import io.micronaut.starter.feature.function.awslambda.AwsLambda;
import io.micronaut.starter.options.JdkVersion;
import jakarta.inject.Singleton;

import static io.micronaut.starter.feature.graalvm.GraalVM.FEATURE_NAME_GRAALVM;

/**
 * Fix MicronautBuildPlugin by removing baseImage from build.gradle.
 *
 * @since 4.7
 */
@Replaces(MicronautBuildPlugin.class)
@Singleton
public class GdkMicronautBuildPlugin extends MicronautBuildPlugin {

    public GdkMicronautBuildPlugin(CoordinateResolver coordinateResolver) {
        super(coordinateResolver);
    }

    @Override
    protected MicronautApplicationGradlePlugin.Builder micronautGradleApplicationPluginBuilder(GeneratorContext generatorContext) {
        MicronautApplicationGradlePlugin.Builder builder = micronautGradleApplicationPluginBuilder(generatorContext, MicronautApplicationGradlePlugin.Builder.APPLICATION);
        if (generatorContext.getFeatures().contains(AwsLambda.FEATURE_NAME_AWS_LAMBDA) && (
                (generatorContext.getApplicationType() == ApplicationType.FUNCTION && generatorContext.getFeatures().contains(FEATURE_NAME_GRAALVM)) ||
                        (generatorContext.getApplicationType() == ApplicationType.DEFAULT))) {
            builder.dockerNative(Dockerfile.builder()
                    .javaVersion(generatorContext.getJdkVersion().asString())
                    .arg("-XX:MaximumHeapSizePercent=80")
                    .arg("-Dio.netty.allocator.numDirectArenas=0")
                    .arg("-Dio.netty.noPreferDirect=true")
                    .build());
        } else if (generatorContext.getJdkVersion() != JdkVersion.JDK_17) {
            builder.dockerNative(Dockerfile.builder().javaVersion(generatorContext.getJdkVersion().asString()).build());
        }
        return builder;
    }

}
