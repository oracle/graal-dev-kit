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
package cloud.graal.gdk.feature.replaced;

import cloud.graal.gdk.GdkGeneratorContext;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.dependencies.CoordinateResolver;
import io.micronaut.starter.feature.function.azure.AzureHttpFunction;
import io.micronaut.starter.feature.function.azure.AzureRawFunction;
import jakarta.inject.Singleton;

@Replaces(AzureRawFunction.class)
@Singleton
public class GdkAzureRawFunction extends AzureRawFunction {
    public GdkAzureRawFunction(CoordinateResolver coordinateResolver, AzureHttpFunction httpFunction) {
        super(coordinateResolver, httpFunction);
    }

    @Override
    public void apply(GeneratorContext generatorContext) {
        super.apply(generatorContext);

        GdkGeneratorContext context = (GdkGeneratorContext) generatorContext;
        context.addUrlTemplate("azure", "host.json", "host.json", "functions/azure/host.json");
        context.addUrlTemplate("azure", "local.settings.json", "local.settings.json", "functions/azure/local.settings.json");
    }
}
