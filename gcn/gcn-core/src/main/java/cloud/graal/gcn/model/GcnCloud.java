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
package cloud.graal.gcn.model;

import io.micronaut.context.env.Environment;

import java.util.Arrays;
import java.util.Iterator;

import static io.micronaut.starter.template.Template.ROOT;

/**
 * Represents a cloud vendor.
 */
public enum GcnCloud {

    /**
     * Amazon.
     */
    AWS(Environment.AMAZON_EC2, "-" + Environment.AMAZON_EC2, "AWS", "aws", "AMAZON_EC2"),

    /**
     * Azure.
     */
    AZURE(Environment.AZURE, "-" + Environment.AZURE, "Azure", "azure", "AZURE"),

    /**
     * Google.
     */
    GCP(Environment.GOOGLE_COMPUTE, "-" + Environment.GOOGLE_COMPUTE, "GCP", "gcp", "GOOGLE_COMPUTE"),

    /**
     * Oracle.
     */
    OCI(Environment.ORACLE_CLOUD, "-" + Environment.ORACLE_CLOUD, "OCI", "oci", "ORACLE_CLOUD"),

    /**
     * Used when no cloud is selected.
     */
    NONE(null, "", "NONE", ROOT, ""),

    /**
     * For testing.
     */
    TESTING("testing", "-testing", "TESTING", "testing", "");

    private static final GcnCloud[] SUPPORTED = Arrays.stream(values())
            .filter(c -> c != GcnCloud.TESTING && c != GcnCloud.NONE)
            .toArray(GcnCloud[]::new);

    private final String environmentName;
    private final String environmentNameSuffix;
    private final String title;
    private final String moduleName;
    private final String environmentConstantName;

    GcnCloud(String environmentName,
             String environmentNameSuffix,
             String title,
             String moduleName,
             String environmentConstantName) {
        this.environmentName = environmentName;
        this.environmentNameSuffix = environmentNameSuffix;
        this.title = title;
        this.moduleName = moduleName;
        this.environmentConstantName = environmentConstantName;
    }

    /**
     * @return the environment name
     */
    public String getEnvironmentName() {
        return environmentName;
    }

    /**
     * @return the environment name suffix, used for config file suffix,
     * e.g., "-oraclecloud" for application-oraclecloud.yml
     */
    public String getEnvironmentNameSuffix() {
        return environmentNameSuffix;
    }

    /**
     * @return the constant name in io.micronaut.context.env.Environment for the
     * cloud's environment name, for code generation
     */
    public String getEnvironmentConstantName() {
        return environmentConstantName;
    }

    /**
     * @return the display name
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the name of the module (subproject) for this cloud
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * @return the subset of GcnCloud enum values that are currently supported
     */
    public static GcnCloud[] supportedValues() {
//        return SUPPORTED; // TODO only supporting AWS and OCI for now
        return new GcnCloud[]{GcnCloud.AWS, GcnCloud.OCI};
    }

    /**
     * Provides the "completionCandidates" for the CLI "--clouds" argument.
     */
    public static class AvailableClouds implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            return Arrays.stream(GcnCloud.supportedValues()).map(GcnCloud::getModuleName).iterator();
        }
    }
}
