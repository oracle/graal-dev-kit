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

import cloud.graal.gdk.GdkUtils;
import io.micronaut.core.annotation.NonNull;

import java.util.regex.Pattern;

/**
 * Fixes parent element in platform independent pom.xml and multi-module root pom.xml.
 */
public class MavenPlatformPostProcessor implements TemplatePostProcessor {

    private static final String PARENT_END = "  </parent>";
    private static final String PARENT_START = "  <parent>";
    private static final Pattern VERSION_PATTERN = Pattern.compile("<version>.+</version>");

    @NonNull
    @Override
    public String process(@NonNull String pom) {
        return fixParent(pom);

    }

    @NonNull
    private String fixParent(@NonNull String pom) {
        int start = pom.indexOf(PARENT_START);
        int end = pom.indexOf(PARENT_END, start) + PARENT_END.length();

        String top = pom.substring(0, start);
        String bottom = pom.substring(end);
        String parent = pom.substring(start, end);

        parent = parent.replace("<groupId>io.micronaut.platform</groupId>", "<groupId>cloud.graal.gdk</groupId>");
        parent = parent.replace("<artifactId>micronaut-parent</artifactId>", "<artifactId>gdk-parent</artifactId>");
        parent = VERSION_PATTERN.matcher(parent).replaceAll(String.format("<version>%s</version>", GdkUtils.getGdkBomVersion()));

        return top + parent + bottom;
    }
}
