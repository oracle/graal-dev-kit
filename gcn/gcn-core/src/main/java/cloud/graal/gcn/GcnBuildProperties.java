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
package cloud.graal.gcn;

import cloud.graal.gcn.model.GcnCloud;
import io.micronaut.starter.build.BuildProperties;
import io.micronaut.starter.build.Comment;
import io.micronaut.starter.build.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.micronaut.starter.feature.MicronautRuntimeFeature.PROPERTY_MICRONAUT_RUNTIME;

/**
 * Cloud-aware replacement for BuildProperties sets most build properties in
 * all clouds except for cloud-specific values.
 *
 * @since 1.0.0
 */
public class GcnBuildProperties extends BuildProperties {

    private static final String EXEC_MAIN_CLASS = "exec.mainClass";

    private final Map<GcnCloud, Map<String, Property>> propertyMaps = new HashMap<>();
    private final GcnGeneratorContext generatorContext;

    /**
     * @param generatorContext generator context
     * @param clouds           selected clouds
     */
    public GcnBuildProperties(GcnGeneratorContext generatorContext,
                              Set<GcnCloud> clouds) {
        this.generatorContext = generatorContext;
        for (GcnCloud cloud : clouds) {
            propertyMaps.put(cloud, new LinkedHashMap<>());
        }
    }

    @Override
    public void put(String key, String value) {

        Collection<Map<String, Property>> maps;
        if (EXEC_MAIN_CLASS.equals(key) || PROPERTY_MICRONAUT_RUNTIME.equals(key) || key.startsWith("jib.")) {
            maps = Collections.singleton(propertyMaps.get(generatorContext.getCloud()));
        } else {
            maps = propertyMaps.values();
        }

        put(key, new DefaultProperty(key, value), maps);
    }

    @Override
    public void addComment(String commentValue) {
        put(commentValue, new DefaultComment(commentValue), propertyMaps.values());
    }

    @Override
    public List<Property> getProperties() {
        return getProperties(generatorContext.getCloud());
    }

    /**
     * @param cloud the cloud
     * @return properties for the specified cloud
     */
    public List<Property> getProperties(GcnCloud cloud) {
        return new ArrayList<>(propertyMaps.get(cloud).values()).stream().sorted(Comparator.comparing(Property::getKey)).
                collect(Collectors.toList());
    }

    private void put(String key,
                     Property property,
                     Collection<Map<String, Property>> maps) {
        for (Map<String, Property> map : maps) {
            map.put(key, property);
        }
    }

    private static final class DefaultProperty implements Property {

        private final String key;
        private final String value;

        private DefaultProperty(String key,
                                String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Property{key='" + key + "', value='" + value + "'}";
        }
    }

    private static final class DefaultComment implements Comment {

        private final String comment;

        private DefaultComment(String comment) {
            this.comment = comment;
        }

        @Override
        public String getComment() {
            return comment;
        }

        @Override
        public String toString() {
            return "Comment{comment='" + comment + "'}";
        }
    }
}
