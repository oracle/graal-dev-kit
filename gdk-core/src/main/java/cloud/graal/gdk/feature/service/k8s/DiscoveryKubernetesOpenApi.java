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
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.feature.discovery.DiscoveryKubernetes;
import jakarta.inject.Singleton;

@Requires(
        property = "micronaut.starter.feature.discovery.kubernetes.enabled",
        value = "true",
        defaultValue = "true"
)
@Replaces(DiscoveryKubernetes.class)
@Singleton
public class DiscoveryKubernetesOpenApi extends DiscoveryKubernetes {
    private static final Dependency DEPENDENCY_MICRONAUT_DISCOVERY_K8S = Dependency.builder().groupId("io.micronaut.kubernetes").artifactId("micronaut-kubernetes-client-openapi-discovery").compile().build();

    public @NonNull String getName() {
        return "discovery-kubernetes";
    }

    public String getTitle() {
        return "Kubernetes Service Discovery";
    }

    public String getDescription() {
        return "Adds support for Service Discovery with Kubernetes";
    }

    public void apply(GeneratorContext generatorContext) {
        generatorContext.getBootstrapConfiguration().put("kubernetes.client.discovery.mode", "endpoint");
        generatorContext.getBootstrapConfiguration().put("kubernetes.client.discovery.mode-configuration.endpoint.watch.enabled", true);
        generatorContext.addDependency(DEPENDENCY_MICRONAUT_DISCOVERY_K8S);
    }

    public String getMicronautDocumentation() {
        return "https://micronaut-projects.github.io/micronaut-kubernetes/latest/guide/#service-discovery";
    }
}
