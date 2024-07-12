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
package cloud.graal.gdk.feature.create;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.build.Repository;

/**
 * The Oracle Maven repo.
 *
 * @since 1.0.0
 */
public class GdkRepository implements Repository {

    @NonNull
    @Override
    public String getId() {
        return "gdk";
    }

    @NonNull
    @Override
    public String getUrl() {
        return "https://maven.oracle.com/public";
    }
}
