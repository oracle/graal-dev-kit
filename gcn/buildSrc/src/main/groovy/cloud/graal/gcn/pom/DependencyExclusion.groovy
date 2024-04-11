/*
 * Copyright 2024 Oracle and/or its affiliates
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

package cloud.graal.gcn.pom

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory

import javax.inject.Inject

class DependencyExclusion {

    public static final String NAME = "dependencyExclusion"

    private final ObjectFactory objectFactory

    final NamedDomainObjectContainer<Exclusion> exclusions

    @Inject
    DependencyExclusion(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory
        exclusions = objectFactory.domainObjectContainer(Exclusion)
    }

    def exclusion(Action<NamedDomainObjectContainer<Exclusion>> action) {
        action.execute(exclusions)
    }
}
