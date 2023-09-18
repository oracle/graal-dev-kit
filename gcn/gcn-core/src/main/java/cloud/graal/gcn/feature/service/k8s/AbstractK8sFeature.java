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
package cloud.graal.gcn.feature.service.k8s;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.GcnFeatureContext;
import cloud.graal.gcn.feature.service.AbstractGcnServiceFeature;
import cloud.graal.gcn.model.GcnService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.feature.k8s.KubernetesClient;
import io.micronaut.starter.feature.other.Management;

import static cloud.graal.gcn.model.GcnService.K8S;
import static io.micronaut.starter.feature.k8s.KubernetesClient.MICRONAUT_KUBERNETES_GROUP_ID;

/**
 * Base class for Kubernetes service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractK8sFeature extends AbstractGcnServiceFeature {

    private final KubernetesClient kubernetesClient;
    private final Management management;

    /**
     * @param kubernetesClient KubernetesClient feature
     */
    protected AbstractK8sFeature(KubernetesClient kubernetesClient, Management management) {
        this.kubernetesClient = kubernetesClient;
        this.management = management;
    }

    @Override
    public final void processSelectedFeatures(GcnFeatureContext featureContext) {
        featureContext.addFeature(kubernetesClient, KubernetesClient.class);
        featureContext.addFeature(management, Management.class);
    }

    @Override
    public final void apply(GcnGeneratorContext generatorContext) {

        addLibPlaceholders(generatorContext);

        Dependency discoveryClient = Dependency.builder()
                .groupId(MICRONAUT_KUBERNETES_GROUP_ID)
                .artifactId("micronaut-kubernetes-discovery-client")
                .compile()
                .build();

        generatorContext.addDependency(discoveryClient);

        applyForLib(generatorContext, () -> {
            generatorContext.addDependency(discoveryClient);
            kubernetesClient.apply(generatorContext);
        });
    }

    @NonNull
    @Override
    public final GcnService getService() {
        return K8S;
    }
}
