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
package cloud.graal.gcn.command;

import cloud.graal.gcn.model.GcnCloud;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

/**
 * Determines the <code>GcnCloud</code> for a selected cloud in the CLI "--clouds" arg.
 */
public class CloudTypeConverter implements ITypeConverter<GcnCloud> {

    @Override
    public GcnCloud convert(String value) {
        for (GcnCloud cloud : GcnCloud.supportedValues()) {
            if (cloud.name().equalsIgnoreCase(value)) {
                return cloud;
            }
        }

        throw new TypeConversionException("Invalid cloud selection: " + value);
    }
}
