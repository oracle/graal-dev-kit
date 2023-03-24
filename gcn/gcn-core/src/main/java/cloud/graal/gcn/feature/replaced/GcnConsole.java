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

import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.agorapulse.console.Console;
import jakarta.inject.Singleton;

/**
 * Replaces the default feature avoid using UUID.randomUUID() which brings in a
 * lot of cryptography classes that bloat the size of the Web Image files.
 *
 * @since 1.0.0
 */
@Replaces(Console.class)
@Singleton
public class GcnConsole extends Console {

    @Override
    public void apply(GeneratorContext generatorContext) {
        String secret = Long.toHexString(System.currentTimeMillis()) + Long.toHexString(System.nanoTime());
        addDependency(generatorContext);
        addExampleCode(generatorContext, secret);
        addConfiguration(generatorContext, secret);
    }
}
