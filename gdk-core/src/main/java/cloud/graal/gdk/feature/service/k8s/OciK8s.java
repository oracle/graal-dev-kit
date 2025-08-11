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

import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.feature.k8s.Kubernetes;
import io.micronaut.starter.feature.other.Management;
import jakarta.inject.Singleton;

import static cloud.graal.gdk.model.GdkCloud.OCI;

/**
 * OCI Kubernetes service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class OciK8s extends AbstractK8sFeature {

    /**
     * @param kubernetesClient KubernetesClient feature
     */
    public OciK8s(KubernetesClientOpenApi kubernetesClient,
                  Management management,
                  Kubernetes kubernetes) {
        super(kubernetesClient, management, kubernetes);
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return OCI;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-oci-k8s";
    }
}
