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
package cloud.graal.gdk.feature;

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.service.email.AbstractEmailFeature;
import cloud.graal.gdk.feature.service.security.AbstractSecurityFeature;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.FeatureContext;
import io.micronaut.starter.feature.MultiProjectFeature;
import io.micronaut.starter.template.StringTemplate;

import static cloud.graal.gdk.GdkUtils.LIB_MODULE;
import static cloud.graal.gdk.model.GdkCloud.NONE;
import static io.micronaut.starter.template.Template.ROOT;

/**
 * Abstract base class for GDK "create" and "service" features.
 *
 * @since 1.0.0
 */
public abstract class AbstractGdkFeature implements GdkFeature, MultiProjectFeature {

    @Override
    public final void apply(GeneratorContext generatorContext) {
        addLibPlaceholders((GdkGeneratorContext) generatorContext);
        apply((GdkGeneratorContext) generatorContext);
    }

    /**
     * Method to implement using the GDK generator context instead of the default.
     *
     * @param generatorContext the GDK generator context
     */
    public abstract void apply(GdkGeneratorContext generatorContext);

    @Override
    public final void processSelectedFeatures(FeatureContext featureContext) {
        processSelectedFeatures((GdkFeatureContext) featureContext);
    }

    /**
     * Method to implement using the GDK feature context instead of the default.
     *
     * @param featureContext the GDK feature context
     */
    public void processSelectedFeatures(GdkFeatureContext featureContext) {
        // override as needed
    }

    @Override
    public final boolean supports(ApplicationType applicationType) {
        return true;
    }

    /**
     * Accessor for the feature cloud module name.
     *
     * @return the module name ("aws", "oci", etc.)
     */
    @NonNull
    protected String getModuleName() {
        return getCloud().getModuleName();
    }

    protected String getDefaultModule() {
        return getCloud() == NONE ? ROOT : LIB_MODULE;
    }

    /**
     * Create .gitkeep files if not generating any files in the lib module
     * to retain the directory structure.
     *
     * @param generatorContext the generator context
     */
    private void addLibPlaceholders(GdkGeneratorContext generatorContext) {

        if (generatorContext.isPlatformIndependent()) {
            return;
        }

        Project project = generatorContext.getProject();

        // add lib/src/main/resources/.gitkeep
        addGitkeep(generatorContext, "gitkeep-resources-lib", "src/main/resources", getDefaultModule());

        // add lib/src/main/java/com/example/.gitkeep
        addGitkeep(generatorContext, "gitkeep-src-lib",
                generatorContext.getLanguage().getSrcDir() + '/' + project.getPackagePath(), getDefaultModule());

        // add cloud/src/test/resources/.gitkeep
        addGitkeep(generatorContext, "gitkeep-resources-test-" + getModuleName(), "src/test/resources", getModuleName());

        if (generatorContext.hasFeature(AbstractEmailFeature.class) || generatorContext.hasFeature(AbstractSecurityFeature.class)) {
            addGitkeep(generatorContext, "gitkeep-jte", "src/main/jte", getDefaultModule());
        }
    }

    private void addGitkeep(GdkGeneratorContext generatorContext,
                            String name, String path, String module) {
        generatorContext.addTemplate(name,
                new StringTemplate(module, path + "/.gitkeep", ""));
    }
}
