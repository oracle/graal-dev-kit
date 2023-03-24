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
package cloud.graal.gcn.feature.service.sdk;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.GcnFeatureContext;
import cloud.graal.gcn.feature.service.sdk.template.OciInstanceControllerGroovy;
import cloud.graal.gcn.feature.service.sdk.template.OciInstanceControllerJava;
import cloud.graal.gcn.feature.service.sdk.template.OciInstanceControllerKotlin;
import cloud.graal.gcn.feature.service.sdk.template.OciInstanceDataGroovy;
import cloud.graal.gcn.feature.service.sdk.template.OciInstanceDataJava;
import cloud.graal.gcn.feature.service.sdk.template.OciInstanceDataKotlin;
import cloud.graal.gcn.model.GcnCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.feature.oraclecloud.OracleCloudSdk;
import jakarta.inject.Singleton;

import static cloud.graal.gcn.model.GcnCloud.OCI;

/**
 * OCI sdk service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class OciSdk extends AbstractSdkFeature {

    private final OracleCloudSdk oracleCloudSdk;

    /**
     * @param oracleCloudSdk OracleCloudSdk feature
     */
    public OciSdk(OracleCloudSdk oracleCloudSdk) {
        this.oracleCloudSdk = oracleCloudSdk;
    }

    @Override
    public void processSelectedFeatures(GcnFeatureContext featureContext) {
        featureContext.addFeature(oracleCloudSdk, OracleCloudSdk.class);
    }

    @Override
    protected void doApply(GcnGeneratorContext generatorContext) {

        if (generatorContext.generateExampleCode()) {

            generatorContext.addDependency(Dependency.builder()
                    .groupId("com.oracle.oci.sdk")
                    .artifactId("oci-java-sdk-core")
                    .compile());

            Project project = generatorContext.getProject();

            generatorContext.addTemplate(getModuleName(), "OciInstanceData",
                    generatorContext.getSourcePath("/{packagePath}/OciInstanceData"),
                    OciInstanceDataJava.template(project),
                    OciInstanceDataKotlin.template(project),
                    OciInstanceDataGroovy.template(project));

            generatorContext.addTemplate(getModuleName(), "OciInstanceController",
                    generatorContext.getSourcePath("/{packagePath}/OciInstanceController"),
                    OciInstanceControllerJava.template(project),
                    OciInstanceControllerKotlin.template(project),
                    OciInstanceControllerGroovy.template(project));
        }
    }

    @NonNull
    @Override
    public GcnCloud getCloud() {
        return OCI;
    }

    @NonNull
    @Override
    public String getName() {
        return "gcn-oci-sdk";
    }
}
