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
import cloud.graal.gcn.model.GcnCloud;
import cloud.graal.gcn.template.GcnPropertiesTemplate;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.config.Configuration;
import io.micronaut.starter.feature.config.Properties;
import io.micronaut.starter.template.Template;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static cloud.graal.gcn.model.GcnCloud.NONE;

/**
 * Replaces the default feature to customize properties file writing.
 *
 * @since 1.0.0
 */
@Replaces(Properties.class)
@Singleton
public class GcnProperties extends Properties {

    @Override
    public Function<Configuration, Template> createTemplate() {
        return (config) -> new GcnPropertiesTemplate(config.getFullPath("properties"), config);
    }

    @Override
    public void apply(GeneratorContext generatorContext) {
        super.apply(generatorContext);
        GcnGeneratorContext gcnGeneratorContext = (GcnGeneratorContext) generatorContext;

        createApplicationCloudProperties(gcnGeneratorContext, gcnGeneratorContext.getCloud());

        createExtraCloudConfigProperties(gcnGeneratorContext);
    }

    private void createApplicationCloudProperties(GcnGeneratorContext generatorContext, GcnCloud gcnCloud) {
        if (gcnCloud == NONE) {
            return;
        }

        generatorContext.addTemplate("application-properties-" + gcnCloud.getModuleName(),
                new GcnPropertiesTemplate(
                        gcnCloud.getModuleName(),
                        "src/main/resources/application.properties",
                        generatorContext.getConfiguration()));

        generatorContext.addTemplate("application-properties-env-" + gcnCloud.getModuleName(),
                new GcnPropertiesTemplate(
                        gcnCloud.getModuleName(),
                        "src/main/resources/application" + gcnCloud.getEnvironmentNameSuffix() + ".properties",
                        generatorContext.getConfiguration(gcnCloud)));

        generatorContext.addTemplate("bootstrap-properties-" + gcnCloud.getModuleName(),
                new GcnPropertiesTemplate(
                        gcnCloud.getModuleName(),
                        "src/main/resources/bootstrap.properties",
                        generatorContext.getBootstrapConfiguration()));

        generatorContext.addTemplate("bootstrap-properties-env-" + gcnCloud.getModuleName(),
                new GcnPropertiesTemplate(
                        gcnCloud.getModuleName(),
                        "src/main/resources/bootstrap" + gcnCloud.getEnvironmentNameSuffix() + ".properties",
                        generatorContext.getBootstrapConfiguration(gcnCloud)));
    }

    private void createExtraCloudConfigProperties(GcnGeneratorContext generatorContext) {
        for (Map.Entry<GcnCloud, Collection<Configuration>> e : generatorContext.getExtraConfigurations().entrySet()) {
            GcnCloud cloud = e.getKey();
            for (Configuration c : e.getValue()) {
                generatorContext.addTemplate(c.getTemplateKey() + '-' + cloud.getModuleName(),
                        new GcnPropertiesTemplate(cloud.getModuleName(), c.getFullPath("properties"), c));
            }
        }
    }
}
