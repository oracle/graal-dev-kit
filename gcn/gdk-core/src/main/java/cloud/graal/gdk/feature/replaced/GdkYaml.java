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

import cloud.graal.gdk.template.GdkYamlTemplate;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.feature.config.Configuration;
import io.micronaut.starter.feature.config.Yaml;
import io.micronaut.starter.template.Template;
import jakarta.inject.Singleton;

import java.util.function.Function;

/**
 * Replaces the default feature to customize yaml file writing.
 *
 * @since 1.0.0
 */
@Replaces(Yaml.class)
@Singleton
public class GdkYaml extends Yaml {

    @Override
    public Function<Configuration, Template> createTemplate() {
        return (config) -> new GdkYamlTemplate(config.getFullPath("yml"), config);
    }
}
