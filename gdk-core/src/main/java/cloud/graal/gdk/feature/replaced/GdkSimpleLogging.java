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
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.logging.SimpleLogging;
import io.micronaut.starter.feature.logging.template.slf4jSimple;
import io.micronaut.starter.template.RockerTemplate;
import jakarta.inject.Singleton;

import static cloud.graal.gdk.model.GdkCloud.NONE;

/**
 * Replaces the default feature to render simplelogger.properties to cloud modules.
 *
 * @since 1.0.0
 */
@Replaces(SimpleLogging.class)
@Singleton
public class GdkSimpleLogging extends SimpleLogging {

    @Override
    public void apply(GeneratorContext generatorContext) {
        super.apply(generatorContext);

        String key = "loggingConfig";
        GdkCloud cloud = ((GdkGeneratorContext) generatorContext).getCloud();
        if (cloud != NONE) {
            key += '-' + cloud.getModuleName();
        }

        generatorContext.addTemplate(key,
                new RockerTemplate("src/main/resources/simplelogger.properties", slf4jSimple.template()));
    }
}
