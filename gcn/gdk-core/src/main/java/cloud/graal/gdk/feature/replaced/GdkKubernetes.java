/*
 * Copyright 2024 Oracle and/or its affiliates
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

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.replaced.template.k8sYaml;
import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.FeatureContext;
import io.micronaut.starter.feature.jib.Jib;
import io.micronaut.starter.feature.k8s.Kubernetes;
import io.micronaut.starter.feature.other.Management;
import io.micronaut.starter.template.RockerTemplate;
import io.micronaut.starter.template.Template;
import jakarta.inject.Singleton;

import java.util.Map;

import static cloud.graal.gdk.model.GdkCloud.AWS;
import static cloud.graal.gdk.model.GdkCloud.AZURE;
import static cloud.graal.gdk.model.GdkCloud.GCP;
import static cloud.graal.gdk.model.GdkCloud.NONE;
import static cloud.graal.gdk.model.GdkCloud.OCI;

/**
 * Replaces {@link Kubernetes} and adds different Kubernetes config file depending on the selected clouds.
 */
@Singleton
@Replaces(Kubernetes.class)
public class GdkKubernetes extends Kubernetes {

    private static final Map<GdkCloud, String> IMAGES = Map.of(
            AWS, "<aws-account-id>.dkr.ecr.<aws-region>.amazonaws.com/%s:latest",
            AZURE, "TODO",
            GCP, "gcr.io/<gcp-project-id>/%s:latest",
            OCI, "<region-key>.ocir.io/<tenancy-namespace>/gdk-k8s/%s:latest",
            NONE, "%s"
    );

    public GdkKubernetes(Jib jib, Management management) {
        super(jib, management);
    }

    @Override
    public void apply(GeneratorContext gc) {
        GdkGeneratorContext generatorContext = (GdkGeneratorContext) gc;
        for (GdkCloud cloud : generatorContext.getClouds()) {
            addYamlTemplate(generatorContext, cloud);
        }
    }

    @Override
    public void processSelectedFeatures(FeatureContext featureContext) {
        super.processSelectedFeatures(featureContext);
    }

    protected void addYamlTemplate(GdkGeneratorContext generatorContext,
                                   GdkCloud cloud) {

        if (cloud.equals(NONE) && generatorContext.getClouds().size() > 1) {
            return;
        }

        String moduleName = cloud.equals(NONE) ? "" : "-" + cloud.getModuleName();

        generatorContext.addTemplate(cloud.name() + "k8sYaml",
                new RockerTemplate(
                        Template.ROOT,
                        "k8s" + moduleName + ".yml",
                        k8sYaml.template(
                                generatorContext.getProject(),
                                IMAGES.get(cloud).formatted(generatorContext.getProject().getName()), cloud)));
    }
}
