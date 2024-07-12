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

import cloud.graal.gdk.OracleCloudNettyClientDependencies;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.micrometer.Core;
import io.micronaut.starter.feature.micrometer.OracleCloud;
import io.micronaut.starter.feature.other.Management;
import jakarta.inject.Singleton;

/**
 * Replaces the default feature to also add Oracle Cloud Netty dependencies.
 *
 * @since 1.0.0
 */
@Replaces(OracleCloud.class)
@Singleton
public class GdkOracleCloudMicrometer extends OracleCloud implements OracleCloudNettyClientDependencies {

    /**
     * @param core       Core feature
     * @param management Management feature
     */
    public GdkOracleCloudMicrometer(Core core, Management management) {
        super(core, management);
    }

    @Override
    public void addDependencies(@NonNull GeneratorContext generatorContext) {
        generatorContext.addDependency(this.micrometerDependency());
        generatorContext.addDependency(DEPENDENCY_MICRONAUT_ORACLE_CLOUD_MICROMETER);
        addNettyDependencies(generatorContext);
    }
}
