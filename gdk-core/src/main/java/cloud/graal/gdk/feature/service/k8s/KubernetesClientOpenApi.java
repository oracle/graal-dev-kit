/*
 * Copyright 2025 Oracle and/or its affiliates
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
package cloud.graal.gdk.feature.service.k8s;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.build.dependencies.MicronautDependencyUtils;
import io.micronaut.starter.feature.discovery.DiscoveryCore;
import io.micronaut.starter.feature.k8s.KubernetesClient;
import io.micronaut.starter.options.BuildTool;
import io.micronaut.starter.options.Language;
import jakarta.inject.Singleton;

@Replaces(KubernetesClient.class)
@Singleton
public class KubernetesClientOpenApi extends KubernetesClient {
    public static final String MICRONAUT_KUBERNETES_GROUP_ID = "io.micronaut.kubernetes";

    public boolean supports(ApplicationType applicationType) {
        return true;
    }

    public @NonNull String getName() {
        return "kubernetes-client-openapi";
    }

    public String getTitle() {
        return "The Micronaut Kubernetes Client OpenApi";
    }

    public String getDescription() {
        return "The Micronaut Kubernetes Client OpenApi is a kubernetes client which uses Micronaut Netty HTTP Client and generated apis and modules from the OpenApi";
    }

    public String getCategory() {
        return "Client";
    }

    public String getMicronautDocumentation() {
        return "https://micronaut-projects.github.io/micronaut-kubernetes/latest/guide/#kubernetes-client-openapi";
    }

    public String getThirdPartyDocumentation() {
        return null;
    }

    public void apply(GeneratorContext generatorContext) {
        generatorContext.addDependency(Dependency.builder().groupId("io.micronaut.kubernetes").artifactId("micronaut-kubernetes-client-openapi").compile());
        fixupDependencies(generatorContext);
    }

    static void fixupDependencies(GeneratorContext generatorContext) {
        if (!generatorContext.hasFeature(DiscoveryCore.class) && generatorContext.getBuildTool() == BuildTool.MAVEN && generatorContext.getLanguage() == Language.GROOVY) {
            generatorContext.addDependency(MicronautDependencyUtils.coreDependency().artifactId("micronaut-discovery-core").compileOnly());
        }

    }
}
