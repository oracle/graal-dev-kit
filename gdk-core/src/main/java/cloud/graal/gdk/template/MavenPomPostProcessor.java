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

import cloud.graal.gdk.build.dependencies.GdkDependencies;
import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.ApplicationType;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cloud.graal.gdk.GdkUtils.BOM_VERSION_SUFFIX;
import static cloud.graal.gdk.GdkUtils.LIB_MODULE;

/**
 * Fixes a few issues in Maven pom.xml files:
 * <ul>
 * <li>
 * The logic in pom.rocker.raw causes the parent pom element to use the
 * Micronaut parent POM, so this processor updates to use the "lib" module as
 * the parent in cloud module POM files
 * <li>
 * The artifact ID is incorrect for the "lib" module POM (the root name is
 * used, e.g., "demo" for "com.example.demo")
 * <li>
 * Like the previous, the artifact ID is incorrect for the "lib" module POM
 * in the "-Amicronaut.processing.module" arg for the maven-compiler-plugin
 * plugin, so for com.example.demo, "-Amicronaut.processing.module=demo" will
 * be changed to "-Amicronaut.processing.module=lib"
 * <li>
 * Appends the BOM version suffix to Micronaut version in cloud pom.xml files,
 * e.g., &lt;micronaut.version&gt;3.8.5&lt;/micronaut.version&gt; -> &lt;micronaut.version&gt;3.8.5-oracle-00001&lt;/micronaut.version&gt;
 * </ul>
 *
 * @since 1.0.0
 */
public class MavenPomPostProcessor implements TemplatePostProcessor {

    private static final String ARTIFACT_ID_END = "</artifactId>";
    private static final String ARTIFACT_ID_START = "  <artifactId>";
    private static final String MICRONAUT_VERSION_END = "</micronaut.version>";
    private static final String MICRONAUT_VERSION_START = "<micronaut.version>";
    private static final String PARENT_END = "  </parent>";
    private static final String PARENT_START = "  <parent>";
    private static final String PLUGINS_START = "<plugins>\n";

    private static final String PLUGIN_CONFIGURATION = "<configuration>";

    private static final String PLUGIN_START = "<plugin>";
    private static final String PROCESSING_MODULE_END = "</arg>";
    private static final String PROCESSING_MODULE_END_KOTLIN = "</annotationProcessorArg>";
    private static final String PROCESSING_MODULE_START = "micronaut.processing.module=";

    private static final Pattern ARTIFACT_ID_PATTERN = Pattern.compile("<artifactId>.+</artifactId>");
    private static final Pattern MICRONAUT_VERSION_PATTERN = Pattern.compile(MICRONAUT_VERSION_START + "(.+)" + MICRONAUT_VERSION_END);
    private static final Pattern VERSION_PATTERN = Pattern.compile("<version>.+</version>");
    private static final String ARTIFACT_ID_NATIVE_MAVEN_PLUGIN_ARTIFACT_ID = "<artifactId>native-maven-plugin</artifactId>";

    private final String artifactId;
    private final String groupId;
    private final boolean libModule;
    private final boolean isGatewayFunction;
    private final ApplicationType applicationType;
    private final GdkCloud cloud;

    public MavenPomPostProcessor(String artifactId,
                                 String groupId,
                                 ApplicationType applicationType,
                                 boolean isGatewayFunction,
                                 GdkCloud cloud,
                                 boolean libModule) {
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.applicationType = applicationType;
        this.isGatewayFunction = isGatewayFunction;
        this.cloud = cloud;
        this.libModule = libModule;
    }

    @NonNull
    @Override
    public String process(@NonNull String pom) {
        if (libModule || cloud != GdkCloud.NONE) {
            pom = fixParent(pom);
            pom = fixVersion(pom);
        }

        pom = fixMicronautVersion(pom);
        pom = fixSourceDirectory(pom);
        pom = addMetadataRepositoryId(pom);

        if (libModule) {
            pom = fixArtifactId(pom);
            pom = fixProcessingModule(pom);
        }

        return pom;
    }

    private String fixSourceDirectory(@NonNull String pom) {
        if (libModule) {
            pom = pom.replaceFirst(" <sourceDirectory>src/main/jte</sourceDirectory>", " <sourceDirectory>lib/src/main/jte</sourceDirectory>");
        } else if (cloud != GdkCloud.NONE) {
            pom = pom.replaceFirst(" <sourceDirectory>src/main/jte</sourceDirectory>", " <sourceDirectory>%s/src/main/jte</sourceDirectory>".formatted(this.cloud.getModuleName()));
        }
        return pom;
    }

    @NonNull
    private String fixParent(@NonNull String pom) {
        int start = pom.indexOf(PARENT_START);
        int end = pom.indexOf(PARENT_END, start) + PARENT_END.length();

        String top = pom.substring(0, start);
        String bottom = pom.substring(end);
        String parent = pom.substring(start, end);

        parent = parent.replace("<groupId>io.micronaut</groupId>", "<groupId>" + groupId + "</groupId>");
        parent = parent.replace("<groupId>io.micronaut.platform</groupId>", "<groupId>" + groupId + "</groupId>");
        parent = parent.replace("<artifactId>micronaut-parent</artifactId>", "<artifactId>" + artifactId + "-parent</artifactId>");
        parent = VERSION_PATTERN.matcher(parent).replaceAll("<version>1.0-SNAPSHOT</version>");

        return top + parent + bottom;
    }

    @NonNull
    private String fixVersion(@NonNull String pom) {
        return pom.replaceFirst("<version>0.1</version>", "<version>1.0-SNAPSHOT</version>");
    }

    @NonNull
    private String fixArtifactId(@NonNull String pom) {
        int start = pom.indexOf(ARTIFACT_ID_START);
        int end = pom.indexOf(ARTIFACT_ID_END, start) + ARTIFACT_ID_END.length();

        String top = pom.substring(0, start);
        String bottom = pom.substring(end);
        String artifactId = pom.substring(start, end);

        artifactId = ARTIFACT_ID_PATTERN.matcher(artifactId).replaceAll("<artifactId>lib</artifactId>");

        return top + artifactId + bottom;
    }

    @NonNull
    private String fixProcessingModule(@NonNull String pom) {

        if (!pom.contains(PROCESSING_MODULE_START)) {
            return pom;
        }

        int start = pom.indexOf(PROCESSING_MODULE_START) + PROCESSING_MODULE_START.length();
        int end = pom.indexOf(PROCESSING_MODULE_END, start);
        if (end == -1) {
            end = pom.indexOf(PROCESSING_MODULE_END_KOTLIN, start); // kotlin plugin
        }

        String top = pom.substring(0, start);
        String bottom = pom.substring(end);

        return top + LIB_MODULE + bottom;
    }

    @NonNull
    private String fixMicronautVersion(@NonNull String pom) {

        if (!libModule && !pom.contains(BOM_VERSION_SUFFIX)) {
            Matcher m = MICRONAUT_VERSION_PATTERN.matcher(pom);
            if (m.find() && m.groupCount() == 1) {
                pom = m.replaceAll(MICRONAUT_VERSION_START + m.group(1) + BOM_VERSION_SUFFIX + MICRONAUT_VERSION_END);
            }
        }

        return pom;
    }

    @NonNull
    private String addMetadataRepositoryId(@NonNull String pom) {
        String graalVMRepositoryVersion = GdkDependencies.GRAALVM_METADATA_REPOSITORY_VERSION;
        if (pom.contains("<metadataRepository>")) {
            return pom;
        }

        int start = pom.indexOf(ARTIFACT_ID_NATIVE_MAVEN_PLUGIN_ARTIFACT_ID);
        if (start > 0) {
            start = pom.indexOf(PLUGIN_CONFIGURATION, start) + PLUGIN_CONFIGURATION.length();
            String top = pom.substring(0, start);
            String bottom = pom.substring(start);
            String graalVMMetadataRepository =
            """
             <metadataRepository>
               <enabled>true</enabled>
               <version>%s</version>
             </metadataRepository>
             """.formatted(graalVMRepositoryVersion);
            return top + graalVMMetadataRepository + bottom;
        }
        start = pom.indexOf(PLUGINS_START);
        if (start == -1) {
            return pom;
        }
        int end = pom.indexOf(PLUGIN_START, start) + PLUGIN_START.length();

        String top = pom.substring(0, start);
        String bottom = pom.substring(end);

        return top + """
 <plugins>
      <plugin>
        <groupId>org.graalvm.buildtools</groupId>
        <artifactId>native-maven-plugin</artifactId>
        <configuration>
         <metadataRepository>
           <enabled>true</enabled>
           <version>%s</version>
         </metadataRepository>
        </configuration>
      </plugin>
      <plugin>""".formatted(graalVMRepositoryVersion) + bottom;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MavenPomPostProcessor other = (MavenPomPostProcessor) o;
        return libModule == other.libModule &&
                artifactId.equals(other.artifactId) &&
                groupId.equals(other.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactId, groupId, libModule);
    }
}
