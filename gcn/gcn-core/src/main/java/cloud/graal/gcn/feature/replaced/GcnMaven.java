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
import cloud.graal.gcn.feature.replaced.template.LibPom;
import com.fizzed.rocker.RockerModel;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.Property;
import io.micronaut.starter.build.maven.MavenBuild;
import io.micronaut.starter.build.maven.MavenBuildCreator;
import io.micronaut.starter.feature.build.maven.Maven;
import jakarta.inject.Singleton;

import java.util.ListIterator;

/**
 * Replaces the default feature to render lib/pom.xml without application-related parts.
 *
 * @since 1.0.0
 */
@Replaces(Maven.class)
@Singleton
public class GcnMaven extends Maven {

    private static final String PROPERTY_MAINCLASS = "exec.mainClass";
    private static final String PROPERTY_RUNTIME = "micronaut.runtime";
    private static final String PROPERTY_TEST_RESOURCES = "micronaut.test.resources.enabled";

    /**
     * @param mavenBuildCreator MavenBuildCreator bean
     */
    public GcnMaven(MavenBuildCreator mavenBuildCreator) {
        super(mavenBuildCreator);
    }

    @Override
    protected MavenBuild createBuild(GeneratorContext generatorContext) {

        if (((GcnGeneratorContext) generatorContext).isPlatformIndependent()) {
            return super.createBuild(generatorContext);
        }

        MavenBuild build = super.createBuild(generatorContext);
        for (ListIterator<Property> iter = build.getProperties().listIterator(); iter.hasNext(); ) {
            Property property = iter.next();
            String key = property.getKey();
            if (PROPERTY_MAINCLASS.equals(key) || PROPERTY_RUNTIME.equals(key) || PROPERTY_TEST_RESOURCES.equals(key)) {
                iter.remove();
            }
        }
        return build;
    }

    @Override
    protected RockerModel pom(GeneratorContext generatorContext, MavenBuild mavenBuild) {

        if (((GcnGeneratorContext) generatorContext).isPlatformIndependent()) {
            return super.pom(generatorContext, mavenBuild);
        }

        return LibPom.template(
                generatorContext.getProject(),
                generatorContext.getFeatures(),
                mavenBuild);
    }
}
