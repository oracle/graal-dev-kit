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
package cloud.graal.gcn.buildtool;

import io.micronaut.starter.template.Writable;
import io.micronaut.starter.template.WritableUtils;

import java.util.List;

/**
 * Utility methods.
 *
 * @since 1.0.0
 */
final class Utils {

    private Utils() {
        // static only
    }

    static String renderRepositories(List<? extends Writable> repositories) {
        return WritableUtils.renderWritableList(repositories.stream()
                .map(repo -> (Writable) outputStream -> {
                    repo.write(outputStream);
                    outputStream.write('\n');
                })
                .toList(), 4);
    }
}
