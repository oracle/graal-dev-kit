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
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.feature.Features;
import io.micronaut.starter.options.Options;

import java.util.Optional;
import java.util.Set;

/**
 * Features for one service.
 */
public class GcnFeatures extends Features {

    private final GcnGeneratorContext context;
    /** pre-computed main class name. */
    private String mainClassName;
    private boolean mainClassNameSet;

    /**
     * @param context  generator context
     * @param features the features
     * @param options  Options
     */
    public GcnFeatures(GcnGeneratorContext context,
                       Set<Feature> features,
                       Options options) {
        super(context, features, options);
        this.context = context;
    }

    @Override
    public Optional<String> mainClass() {
        if (!mainClassNameSet) {
            mainClassNameSet = true;
            mainClassName = application() == null ? null : application().mainClassName(context);
        }
        return Optional.ofNullable(mainClassName);
    }

    @Override
    public String toString() {
        return "GcnFeatures(" + super.toString() + ')' +
                " BuildTool: " + build() +
                ", ApplicationFeature: " + application() +
                ", LanguageFeature: " + language() +
                ", TestFeature: " + testFramework() +
                ", Features: " + getFeatures() +
                ", JdkVersion: " + javaVersion() +
                ", mainClass: '" + mainClassName + '\'';
    }
}
