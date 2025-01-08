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
package cloud.graal.gdk.model;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.ApplicationType;

/**
 * Represents a project type.
 */
public enum GdkProjectType {

    /**
     * An application project.
     */
    APPLICATION(ApplicationType.DEFAULT, "Application", "create-app"),

    /**
     * A function project.
     */
    FUNCTION(ApplicationType.FUNCTION, "Function", "create-function"),

    /**
     * A gateway function project.
     */
    GATEWAY_FUNCTION(ApplicationType.DEFAULT, "Gateway Function", "create-gateway-function");

    /**
     * The default type.
     */
    public static final GdkProjectType DEFAULT_OPTION = APPLICATION;

    private final ApplicationType applicationType;
    private final String title;
    private final String command;

    GdkProjectType(ApplicationType applicationType,
                   String title,
                   String command) {
        this.applicationType = applicationType;
        this.title = title;
        this.command = command;
    }

    /**
     * @return the associated application type
     */
    public ApplicationType applicationType() {
        return applicationType;
    }

    /**
     * @return the display name
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the lowercase name
     */
    @NonNull
    public String getName() {
        return name().toLowerCase();
    }

    /**
     * @return the associated create command
     */
    @NonNull
    public String getCommand() {
        return command;
    }
}
