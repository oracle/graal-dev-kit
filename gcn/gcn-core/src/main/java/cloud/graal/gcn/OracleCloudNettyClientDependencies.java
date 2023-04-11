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
package cloud.graal.gcn;

import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.dependencies.Dependency;

public interface OracleCloudNettyClientDependencies {

    default void addNettyDependencies(GeneratorContext generatorContext) {
        generatorContext.addDependency(Dependency.builder()
                .groupId("io.micronaut.oraclecloud")
                .artifactId("micronaut-oraclecloud-httpclient-netty")
                .compile());

        if (!generatorContext.getBuildTool().isGradle() && generatorContext.getFeatures().hasGraalvm()) {
            generatorContext.addDependency(Dependency.builder()
                    .groupId("com.oracle.oci.sdk")
                    .artifactId("oci-java-sdk-addons-graalvm")
                    .compile());
        }
    }
}
