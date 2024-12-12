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
package cloud.graal.gdk.feature.replaced;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.starter.feature.database.TestContainers;
import io.micronaut.starter.feature.database.jdbc.JdbcFeature;
import io.micronaut.starter.feature.function.Cloud;
import io.micronaut.starter.feature.function.CloudFeature;
import io.micronaut.starter.feature.oraclecloud.OracleCloudAutonomousDatabase;
import io.micronaut.starter.feature.oraclecloud.OracleCloudSdk;
import io.micronaut.starter.feature.testresources.TestResources;
import jakarta.inject.Singleton;

@Replaces(OracleCloudAutonomousDatabase.class)
@Singleton
public class GdkOracleCloudAutonomousDatabase extends OracleCloudAutonomousDatabase implements CloudFeature {

    public GdkOracleCloudAutonomousDatabase(JdbcFeature jdbcFeature, TestContainers testContainers, TestResources testResources, OracleCloudSdk oracleCloudSdkFeature) {
        super(jdbcFeature, testContainers, testResources, oracleCloudSdkFeature);
    }

    @Override
    public Cloud getCloud() {
        return Cloud.ORACLE;
    }
}
