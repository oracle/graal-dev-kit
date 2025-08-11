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
package cloud.graal.gdk.feature.service.k8s;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.feature.service.AbstractGdkServiceFeature;
import cloud.graal.gdk.model.GdkService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.feature.k8s.Kubernetes;
import io.micronaut.starter.feature.other.Management;

import static cloud.graal.gdk.model.GdkService.K8S;
import static cloud.graal.gdk.feature.service.k8s.KubernetesClientOpenApi.MICRONAUT_KUBERNETES_GROUP_ID;

/**
 * Base class for Kubernetes service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractK8sFeature extends AbstractGdkServiceFeature {

    private static final Dependency DISCOVERY_CLIENT = Dependency.builder()
            .groupId(MICRONAUT_KUBERNETES_GROUP_ID)
            .artifactId("micronaut-kubernetes-client-openapi-discovery")
            .compile()
            .build();

    private final KubernetesClientOpenApi kubernetesClient;
    private final Management management;
    private final Kubernetes kubernetes;

    /**
     * @param kubernetesClient KubernetesClient feature
     */
    protected AbstractK8sFeature(KubernetesClientOpenApi kubernetesClient,
                                 Management management,
                                 Kubernetes kubernetes) {
        this.kubernetesClient = kubernetesClient;
        this.management = management;
        this.kubernetes = kubernetes;
    }

    @Override
    public final void processSelectedFeatures(GdkFeatureContext featureContext) {
        featureContext.addFeature(kubernetesClient, KubernetesClientOpenApi.class);
        featureContext.addFeature(management, Management.class);
        featureContext.addFeature(kubernetes, Kubernetes.class);
    }

    @Override
    public final void apply(GdkGeneratorContext generatorContext) {

        generatorContext.addDependency(DISCOVERY_CLIENT);

        applyForLib(generatorContext, () -> {
            generatorContext.addDependency(DISCOVERY_CLIENT);
            kubernetesClient.apply(generatorContext);
        });
    }

    @NonNull
    @Override
    public final GdkService getService() {
        return K8S;
    }
}
