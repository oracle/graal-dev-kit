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
package cloud.graal.gcn.feature;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.service.email.AbstractEmailFeature;
import cloud.graal.gcn.feature.service.security.AbstractSecurityFeature;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.feature.FeatureContext;
import io.micronaut.starter.feature.MultiProjectFeature;
import io.micronaut.starter.template.StringTemplate;

import static cloud.graal.gcn.GcnUtils.LIB_MODULE;
import static cloud.graal.gcn.model.GcnCloud.NONE;
import static io.micronaut.starter.template.Template.ROOT;

/**
 * Abstract base class for GCN "create" and "service" features.
 *
 * @since 1.0.0
 */
public abstract class AbstractGcnFeature implements GcnFeature, MultiProjectFeature {

    @Override
    public final void apply(GeneratorContext generatorContext) {
        addLibPlaceholders((GcnGeneratorContext) generatorContext);
        apply((GcnGeneratorContext) generatorContext);
    }

    /**
     * Method to implement using the GCN generator context instead of the default.
     *
     * @param generatorContext the GCN generator context
     */
    public abstract void apply(GcnGeneratorContext generatorContext);

    @Override
    public final void processSelectedFeatures(FeatureContext featureContext) {
        processSelectedFeatures((GcnFeatureContext) featureContext);
    }

    /**
     * Method to implement using the GCN feature context instead of the default.
     *
     * @param featureContext the GCN feature context
     */
    public void processSelectedFeatures(GcnFeatureContext featureContext) {
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
    private void addLibPlaceholders(GcnGeneratorContext generatorContext) {

        if (generatorContext.isPlatformIndependent()) {
            return;
        }

        Project project = generatorContext.getProject();

        addGitkeep(generatorContext, "gitkeep-resources", "src/main/resources");
        addGitkeep(generatorContext, "gitkeep-src",
                generatorContext.getLanguage().getSrcDir() + '/' + project.getPackagePath());

        if (generatorContext.hasFeature(AbstractEmailFeature.class) || generatorContext.hasFeature(AbstractSecurityFeature.class)) {
            addGitkeep(generatorContext, "gitkeep-jte", "src/main/jte");
        }
    }

    private void addGitkeep(GcnGeneratorContext generatorContext, String name, String path) {
        generatorContext.addTemplate(name,
                new StringTemplate(getDefaultModule(), path + "/.gitkeep", ""));
    }
}
