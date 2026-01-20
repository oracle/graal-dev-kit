/*
 * Copyright 2026 Oracle and/or its affiliates
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
package cloud.graal.gdk.feature.graalvm;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.GdkUtils;
import cloud.graal.gdk.feature.service.template.NativeImagePropertiesTemplate;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.DefaultFeature;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.options.Options;
import io.micronaut.starter.template.DefaultTemplate;
import io.micronaut.starter.template.RockerWritable;
import io.micronaut.starter.template.Writable;
import jakarta.inject.Singleton;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static io.micronaut.starter.feature.FeaturePhase.HIGH;

/**
 * Generates a native-image.properties file with classes to be initialized at build time.
 */
@Singleton
public class NativeImageProperties implements DefaultFeature {

    /**
     * The feature name.
     */
    public static final String NAME = "native-image-properties";

    private static final char SPACE = ' ';
    private static final String INITIALIZE_AT_BUILD_TIME_OPTION = "--initialize-at-build-time=";

    // Support for shared arenas (Arena.ofShared()) is still experimental, requires -H:+UnlockExperimentalVMOptions -H:+SharedArenaSupport in native image
    private static final String SHARED_ARENA_SUPPORT_FLAGS = "-H:+UnlockExperimentalVMOptions -H:+SharedArenaSupport";

    @Override
    public void apply(GeneratorContext gc) {
        GdkGeneratorContext generatorContext = (GdkGeneratorContext) gc;
        Set<Writable> nativeImageProperties = generatorContext.getInitializeAtBuildTimeClasses();

        // if nativeImageProperties.isEmpty() and using JDK 25, need to create native-image.properties with "-H:+UnlockExperimentalVMOptions -H:+SharedArenaSupport"
        if (nativeImageProperties.isEmpty() && !generatorContext.isJdkVersionAtLeast(25)) {
            return;
        }

        String module = GdkUtils.currentModule(generatorContext);
        generatorContext.addTemplate(module + "-native-image",
                new DefaultTemplate(module, "src/main/resources/META-INF/native-image/native-image.properties") {
                    @Override
                    public void write(OutputStream outputStream) throws IOException {
                        new RockerWritable(NativeImagePropertiesTemplate.template()).write(outputStream);

                        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));

                        if (generatorContext.isJdkVersionAtLeast(25)) {
                            writer.write(SPACE);
                            writer.write(SHARED_ARENA_SUPPORT_FLAGS);
                        }

                        if (!nativeImageProperties.isEmpty()) {
                            writer.write(SPACE);
                            writer.write(INITIALIZE_AT_BUILD_TIME_OPTION);

                            Set<String> renderedClasses = new HashSet<>();

                            for (Writable writable : nativeImageProperties) {
                                renderedClasses.add(render(writable));
                            }

                            Iterator<String> it = renderedClasses.iterator();
                            if (it.hasNext()) {
                                writer.write(it.next());
                            }

                            while (it.hasNext()) {
                                writer.write(',');
                                writer.write(it.next());
                            }
                        }

                        writer.flush();
                    }
                });
    }

    private String render(Writable writable) throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        writable.write(out);
        return out.toString();
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean supports(ApplicationType applicationType) {
        return true;
    }

    @Override
    public boolean shouldApply(ApplicationType applicationType, Options options, Set<Feature> selectedFeatures) {
        return true;
    }

    @Override
    public int getOrder() {
        return HIGH.getOrder();
    }

    @Override
    public boolean isVisible() {
        return false;
    }
}
