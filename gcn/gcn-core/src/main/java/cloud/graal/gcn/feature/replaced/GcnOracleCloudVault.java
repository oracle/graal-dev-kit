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
package cloud.graal.gcn.feature.replaced;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.OracleCloudNettyClientDependencies;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.build.dependencies.MicronautDependencyUtils;
import io.micronaut.starter.feature.oraclecloud.OracleCloudVault;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;

import static cloud.graal.gcn.model.GcnCloud.NONE;

/**
 * Replaces the default feature to add Oracle Cloud Netty dependencies
 * and write "oci.vault.vaults[0].compartment-ocid=" and "oci.vault.vaults[0].ocid="
 * to bootstrap-oraclecloud.properties instead of bootstrap.properties.
 *
 * @since 1.0.0
 */
@Replaces(OracleCloudVault.class)
@Singleton
public class GcnOracleCloudVault
        extends OracleCloudVault
        implements OracleCloudNettyClientDependencies {

    private static final Dependency ORACLECLOUD_VAULT = MicronautDependencyUtils.oracleCloudDependency()
            .artifactId("micronaut-oraclecloud-vault")
            .compile()
            .build();

    @Override
    public void apply(GeneratorContext gc) {

        GcnGeneratorContext generatorContext = (GcnGeneratorContext) gc;

        generatorContext.addDependency(ORACLECLOUD_VAULT);
        addNettyDependencies(generatorContext);

        if (generatorContext.getCloud() != NONE) {
            generatorContext.getConfiguration().put("oci.config.profile", "DEFAULT");
            generatorContext.getCloudBootstrapConfiguration().put("oci.vault.config.enabled", "true");
            generatorContext.getCloudBootstrapConfiguration().put("micronaut.application.name", generatorContext.getProject().getPropertyName());
            generatorContext.getCloudBootstrapConfiguration().put("micronaut.config-client.enabled", true);

            generatorContext.getTestBootstrapConfiguration().addNested("oci.vault.config.enabled", false);
            generatorContext.getTestBootstrapConfiguration().addNested("micronaut.config-client.enabled", false);

            generatorContext.getCloudBootstrapConfiguration().put("micronaut.config-client.enabled", true);

            generatorContext.getCloudBootstrapConfiguration().put("oci.vault.vaults", List.of(Map.of(
                    "ocid", "",
                    "compartment-ocid", ""
            )));
        }
    }
}
