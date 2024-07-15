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
package cloud.graal.gdk.template;

import io.micronaut.starter.template.DefaultTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static cloud.graal.gdk.GdkUtils.LIB_MODULE;

/**
 * Replaces io.micronaut.starter.template.YamlTemplate to avoid using SnakeYAML
 * which bulks up the size of the generated Web Image files significantly.
 *
 * @since 1.0.0
 */
public class GdkYamlTemplate extends DefaultTemplate {

    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
    private static final String INDENT = "  ";
    private static final char[] INDICATORS = ":[]{},\"'|*&".toCharArray();

    private final Map<String, Object> config;
    private final Map<String, Object> originalConfig;

    public GdkYamlTemplate(String path, Map<String, Object> config) {
        this(LIB_MODULE, path, config);
    }

    public GdkYamlTemplate(String module, String path, Map<String, Object> config) {
        super(module, path);
        this.originalConfig = config;
        this.config = transform(config);
    }

    /**
     * @return the transformed config
     */
    public Map<String, Object> getConfig() {
        return Collections.unmodifiableMap(config);
    }

    /**
     * @return the config passed to the constructor, not the transformed config
     */
    public Map<String, Object> getOriginalConfig() {
        return Collections.unmodifiableMap(originalConfig);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        if (config.isEmpty()) {
            outputStream.write("# Place application configuration here".getBytes());
            return;
        }

        StringBuilder sb = new StringBuilder();
        render(config, sb, "");
        String yaml = sb.toString().trim() + '\n';

        outputStream.write(yaml.getBytes());
    }

    /**
     * Convert entries like "com.foo.bar: wahoo" to nested.
     *
     * @param config the initial config
     * @return the config nested
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> transform(Map<String, Object> config) {
        Map<String, Object> transformed = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String[] keys = DOT_PATTERN.split(key);

            Map<String, Object> currentNested = transformed;
            for (int i = 0; i < keys.length; i++) {
                String subKey = keys[i];
                if (i == keys.length - 1) {
                    Object currentValue = currentNested.get(subKey);
                    if (value instanceof Map && currentValue instanceof Map) {
                        currentNested.put(subKey, mergeMaps((Map<String, Object>) currentValue, (Map<String, Object>) value));
                    } else {
                        currentNested.put(subKey, value);
                    }
                } else {
                    currentNested = (Map<String, Object>) currentNested.computeIfAbsent(subKey, k -> new LinkedHashMap<>(5));
                }
            }
        }
        return transformed;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mergeMaps(Map<String, Object> map1, Map<String, Object> map2) {
        Map<String, Object> mergedMap = new HashMap<>(map1);

        for (String key : map2.keySet()) {
            if (mergedMap.containsKey(key)) {
                if (mergedMap.get(key) instanceof Map && map2.get(key) instanceof Map) {
                    Map<String, Object> nestedMergedMap = mergeMaps((Map<String, Object>) mergedMap.get(key), (Map<String, Object>) map2.get(key));
                    mergedMap.put(key, nestedMergedMap);
                } else {
                    mergedMap.put(key, map2.get(key));
                }
            } else {
                mergedMap.put(key, map2.get(key));
            }
        }

        return mergedMap;
    }

    @SuppressWarnings("unchecked")
    private void render(Object o, StringBuilder yaml, String indent) {
        if (o == null) {
            yaml.append(" ~\n");
            return;
        }

        if (o instanceof Map) {
            render((Map<String, Object>) o, yaml, indent);
        } else if (o instanceof Collection) {
            render((Collection<?>) o, yaml, indent);
        } else {
            render(o, yaml);
        }
    }

    private void render(Map<String, Object> m, StringBuilder yaml, String indent) {

        if (m.isEmpty()) {
            yaml.append(" {").append('\n').append(indent).append('}');
        }

        yaml.append('\n');

        for (Map.Entry<String, Object> entry : m.entrySet()) {
            yaml.append(indent).append(entry.getKey()).append(':');
            render(entry.getValue(), yaml, indent(indent));
        }
    }

    private void render(Collection<?> c, StringBuilder yaml, String indent) {

        if (c.isEmpty()) {
            yaml.append(" [").append('\n').append(indent).append(']');
        }

        yaml.append('\n');

        for (Object o : c) {
            yaml.append(unindent(indent)).append('-');
            render(o, yaml, indent(indent));
        }
    }

    private void render(Object o, StringBuilder yaml) {
        yaml.append(' ');
        if (o instanceof CharSequence || o instanceof Character) {
            yaml.append(escapeAndQuote(o.toString()));
        } else {
            yaml.append(o);
        }
        yaml.append('\n');
    }

    private String escapeAndQuote(String s) {

        if (s.length() == 0) {
            return "''";
        }

        boolean quote = false;

        if (s.trim().length() != s.length()) {
            quote = true;
        } else {
            for (char c : INDICATORS) {
                if (s.indexOf(c) != -1) {
                    quote = true;
                    break;
                }
            }
        }

        if (quote) {
            return "'" + s
                    .replace("\\", "\\\\")
                    .replace("\b", "\\b")
                    .replace("\0", "\\0")
                    .replace("\t", "\\t")
                    .replace("'", "''") +
                    "'";
        }

        return s;
    }

    private String indent(String s) {
        return INDENT + s;
    }

    private String unindent(String s) {
        return s.substring(INDENT.length());
    }

    @Override
    public String toString() {
        return "GdkYamlTemplate{" +
                "config=" + getConfig() +
                "originalConfig=" + originalConfig +
                ", path='" + path + '\'' +
                ", module='" + module + '\'' +
                ", useModule=" + useModule +
                '}';
    }
}
