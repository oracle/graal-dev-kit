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
package cloud.graal.gdk.feature.misc;

import cloud.graal.gdk.GdkGeneratorContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.feature.graalvm.GraalVM;
import io.micronaut.starter.options.BuildTool;
import jakarta.inject.Singleton;

@Singleton
public class CycloneDXPlugin implements Feature {

    /**
     * The feature name.
     */
    public static final String NAME = "cyclone-dx-plugin";

    private static final CycloneDXPluginPostProcessor CYCLONE_DX_POST_PROCESSOR = new CycloneDXPluginPostProcessor(BuildTool.GRADLE);

    private final GraalVM graalvm;

    public CycloneDXPlugin(GraalVM graalvm) {
        this.graalvm = graalvm;
    }

    @Override
    public void apply(GeneratorContext ctx) {
        if (ctx.getBuildTool().isGradle()) {
            String template = "build";
            if (!((GdkGeneratorContext) ctx).getCloud().getModuleName().isBlank()) {
                template = template + "-" + ((GdkGeneratorContext) ctx).getCloud().getModuleName();
            }
            ((GdkGeneratorContext) ctx).addPostProcessor(template, CYCLONE_DX_POST_PROCESSOR);
        }
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
    public boolean isVisible() {
        return false;
    }

}
