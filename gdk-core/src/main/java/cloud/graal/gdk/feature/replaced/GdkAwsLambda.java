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
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.build.dependencies.MicronautDependencyUtils;
import io.micronaut.starter.feature.CodeContributingFeature;
import io.micronaut.starter.feature.architecture.X86;
import io.micronaut.starter.feature.aws.AwsLambdaEventsSerde;
import io.micronaut.starter.feature.aws.AwsLambdaSnapstart;
import io.micronaut.starter.feature.awslambdacustomruntime.AwsLambdaCustomRuntime;
import io.micronaut.starter.feature.crac.Crac;
import io.micronaut.starter.feature.function.awslambda.AwsLambda;
import io.micronaut.starter.feature.function.awslambda.DefaultAwsLambdaHandlerProvider;
import io.micronaut.starter.feature.function.awslambda.FunctionAwsLambdaHandlerProvider;
import io.micronaut.starter.feature.graalvm.GraalVM;
import io.micronaut.starter.feature.httpclient.HttpClientJdk;
import io.micronaut.starter.feature.other.ShadePlugin;
import io.micronaut.starter.options.BuildTool;
import jakarta.inject.Singleton;

/**
 * Replaces the default feature to conditionally apply only for AWS. This is
 * needed because AwsLambda is a default feature.
 *
 * @since 1.0.0
 */
@Replaces(AwsLambda.class)
@Singleton
public class GdkAwsLambda extends AwsLambda {

    private static final Dependency AWS_LAMBDA_JAVA_EVENTS = Dependency.builder().groupId("com.amazonaws").artifactId("aws-lambda-java-events").compile().build();
    private static final Dependency DEPENDENCY_MICRONAUT_FUNCTION_AWS = MicronautDependencyUtils.awsDependency().artifactId("micronaut-function-aws").compile().build();
    private static final Dependency DEPENDENCY_MICRONAUT_FUNCTION_AWS_API_PROXY = MicronautDependencyUtils.awsDependency().artifactId("micronaut-function-aws-api-proxy").compile().build();
    private static final Dependency DEPENDENCY_MICRONAUT_FUNCTION_AWS_API_PROXY_TEST = MicronautDependencyUtils.awsDependency().artifactId("micronaut-function-aws-api-proxy-test").test().build();

    /**
     * @param shadePlugin   ShadePlugin feature
     * @param customRuntime AwsLambdaCustomRuntime feature
     */
    public GdkAwsLambda(ShadePlugin shadePlugin,
                        AwsLambdaCustomRuntime customRuntime,
                        X86 x86, AwsLambdaSnapstart snapstart,
                        HttpClientJdk httpClientJdk,
                        AwsLambdaEventsSerde awsLambdaEventsSerde,
                        DefaultAwsLambdaHandlerProvider defaultAwsLambdaHandlerProvider,
                        FunctionAwsLambdaHandlerProvider functionAwsLambdaHandlerProvider) {
        super(shadePlugin, customRuntime, x86, snapstart, httpClientJdk, awsLambdaEventsSerde, defaultAwsLambdaHandlerProvider, functionAwsLambdaHandlerProvider);
    }

    @Override
    public void apply(GeneratorContext generatorContext) {
        if (generatorContext.isFeatureMissing(CodeContributingFeature.class)) {
            ApplicationType applicationType = generatorContext.getApplicationType();
            if (applicationType == ApplicationType.DEFAULT || applicationType == ApplicationType.FUNCTION) {
                this.addCode(generatorContext);
                if (applicationType == ApplicationType.FUNCTION) {
                    generatorContext.addDependency(AWS_LAMBDA_JAVA_EVENTS);
                }

                this.addHelpTemplate(generatorContext);
                this.disableSecurityFilterInTestConfiguration(generatorContext);
            }
        }

        this.addMicronautRuntimeBuildProperty(generatorContext);
        this.addDependencies(generatorContext);
    }

    private void addDependencies(@NonNull GeneratorContext generatorContext) {
        if (generatorContext.getApplicationType() == ApplicationType.FUNCTION) {
            generatorContext.addDependency(DEPENDENCY_MICRONAUT_FUNCTION_AWS);
        }

        if (generatorContext.getBuildTool() == BuildTool.MAVEN && generatorContext.getApplicationType() == ApplicationType.DEFAULT) {
            generatorContext.addDependency(DEPENDENCY_MICRONAUT_FUNCTION_AWS_API_PROXY);
            generatorContext.addDependency(DEPENDENCY_MICRONAUT_FUNCTION_AWS_API_PROXY_TEST);
        }

        if (generatorContext.getBuildTool() == BuildTool.MAVEN && generatorContext.hasFeature(GraalVM.class)) {
            generatorContext.addDependency(AwsLambdaCustomRuntime.DEPENDENCY_AWS_FUNCTION_AWS_CUSTOM_RUNTIME);
        }

        if (generatorContext.hasFeature(AwsLambdaSnapstart.class)) {
            generatorContext.addDependency(Crac.DEPENDENCY_MICRONAUT_CRAC);
        }

        if (generatorContext.getFeatures().testFramework().isSpock() && generatorContext.getBuildTool().isGradle()) {
            generatorContext.addDependency(DEPENDENCY_MICRONAUT_FUNCTION_TEST);
        }

    }

}
