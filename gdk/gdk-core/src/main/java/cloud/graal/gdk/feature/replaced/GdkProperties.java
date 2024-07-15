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

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.model.GdkCloud;
import cloud.graal.gdk.template.GdkPropertiesTemplate;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.config.Configuration;
import io.micronaut.starter.feature.config.Properties;
import io.micronaut.starter.template.Template;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static cloud.graal.gdk.model.GdkCloud.NONE;

/**
 * Replaces the default feature to customize properties file writing.
 *
 * @since 1.0.0
 */
@Replaces(Properties.class)
@Singleton
public class GdkProperties extends Properties {

    @Override
    public Function<Configuration, Template> createTemplate() {
        return (config) -> new GdkPropertiesTemplate(config.getFullPath("properties"), config);
    }

    @Override
    public void apply(GeneratorContext gc) {
        super.apply(gc);
        GdkGeneratorContext generatorContext = (GdkGeneratorContext) gc;

        createApplicationCloudProperties(generatorContext, generatorContext.getCloud());

        createExtraCloudConfigProperties(generatorContext);
    }

    private void createApplicationCloudProperties(GdkGeneratorContext generatorContext, GdkCloud cloud) {
        if (cloud == NONE) {
            return;
        }

        generatorContext.addTemplate("application-properties-" + cloud.getModuleName(),
                new GdkPropertiesTemplate(
                        cloud.getModuleName(),
                        "src/main/resources/application.properties",
                        generatorContext.getConfiguration()));

        generatorContext.addTemplate("application-properties-env-" + cloud.getModuleName(),
                new GdkPropertiesTemplate(
                        cloud.getModuleName(),
                        "src/main/resources/application" + cloud.getEnvironmentNameSuffix() + ".properties",
                        generatorContext.getConfiguration(cloud)));

        generatorContext.addTemplate("bootstrap-properties-" + cloud.getModuleName(),
                new GdkPropertiesTemplate(
                        cloud.getModuleName(),
                        "src/main/resources/bootstrap.properties",
                        generatorContext.getBootstrapConfiguration()));

        generatorContext.addTemplate("bootstrap-properties-env-" + cloud.getModuleName(),
                new GdkPropertiesTemplate(
                        cloud.getModuleName(),
                        "src/main/resources/bootstrap" + cloud.getEnvironmentNameSuffix() + ".properties",
                        generatorContext.getBootstrapConfiguration(cloud)));
    }

    private void createExtraCloudConfigProperties(GdkGeneratorContext generatorContext) {
        for (Map.Entry<GdkCloud, Collection<Configuration>> e : generatorContext.getExtraConfigurations().entrySet()) {
            GdkCloud cloud = e.getKey();
            for (Configuration c : e.getValue()) {
                generatorContext.addTemplate(c.getTemplateKey() + '-' + cloud.getModuleName(),
                        new GdkPropertiesTemplate(cloud.getModuleName(), c.getFullPath("properties"), c));
            }
        }
    }
}
