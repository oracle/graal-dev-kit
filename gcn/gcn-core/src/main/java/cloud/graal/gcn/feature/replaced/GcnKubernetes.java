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
package cloud.graal.gcn.feature.replaced;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.replaced.template.k8sYaml;
import cloud.graal.gcn.model.GcnCloud;
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

import static cloud.graal.gcn.model.GcnCloud.AWS;
import static cloud.graal.gcn.model.GcnCloud.GCP;
import static cloud.graal.gcn.model.GcnCloud.NONE;
import static cloud.graal.gcn.model.GcnCloud.OCI;

/**
 * GcnKubernetes replaces {@link Kubernetes} bean and add different kubernetes config file depending on the selected clouds.
 */
@Singleton
@Replaces(Kubernetes.class)
public class GcnKubernetes extends Kubernetes {

    private static final Map<GcnCloud, String> IMAGES =  Map.of(
            AWS,  "<aws-account-id>.dkr.ecr.<aws-region>.amazonaws.com/%s:latest",
            GCP,  "gcr.io/<gcp-project-id>/%s:latest",
            OCI,  "<region-key>.ocir.io/<tenancy-namespace>/gcn-k8s/%s-oci:latest",
            NONE, "%s"
    );

    public GcnKubernetes(Jib jib, Management management) {
        super(jib, management);
    }

    @Override
    public void apply(GeneratorContext generatorContext) {
        GcnGeneratorContext gcnGeneratorContext = (GcnGeneratorContext) generatorContext;
        gcnGeneratorContext.getClouds().forEach(gcnCloud -> addYamlTemplate(gcnGeneratorContext, gcnCloud));
    }

    @Override
    public void processSelectedFeatures(FeatureContext featureContext) {
        super.processSelectedFeatures(featureContext);
    }

    protected void addYamlTemplate(GcnGeneratorContext generatorContext,
                                   GcnCloud gcnCloud) {

        if (gcnCloud.equals(GcnCloud.NONE) && generatorContext.getClouds().size() > 1) {
            return;
        }

        String moduleName = gcnCloud.equals(GcnCloud.NONE) ? "" : "-" + gcnCloud.getModuleName();

        generatorContext.addTemplate(gcnCloud.name() + "k8sYaml",
                new RockerTemplate(
                        Template.ROOT,
                        "k8s" + moduleName + ".yml",
                        k8sYaml.template(
                                generatorContext.getProject(),
                                IMAGES.get(gcnCloud).formatted(generatorContext.getProject().getName()), gcnCloud)));
    }

}
