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
package cloud.graal.gdk.feature.service.database;

import cloud.graal.gdk.model.GdkCloud;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.feature.database.Data;
import io.micronaut.starter.feature.database.DataJdbc;
import io.micronaut.starter.feature.database.MySQL;
import io.micronaut.starter.feature.database.jdbc.JdbcFeature;
import io.micronaut.starter.feature.migration.Flyway;
import io.micronaut.starter.feature.validator.MicronautValidationFeature;
import jakarta.inject.Singleton;

import static cloud.graal.gdk.model.GdkCloud.GCP;

/**
 * GCP database service feature.
 *
 * @since 1.0.0
 */
@Singleton
public class GcpDatabase extends AbstractDatabaseFeature {

    /**
     * @param data                 Data feature
     * @param dataJdbc             DataJdbc feature
     * @param flyway               Flyway feature
     * @param jdbcFeature          JdbcFeature feature
     * @param defaultDriverFeature MySQL feature
     */
    public GcpDatabase(Data data,
                       DataJdbc dataJdbc,
                       Flyway flyway,
                       JdbcFeature jdbcFeature,
                       MySQL defaultDriverFeature,
                       MicronautValidationFeature micronautValidationFeature) {
        super(data, dataJdbc, flyway, jdbcFeature, defaultDriverFeature, micronautValidationFeature);
    }

    @NonNull
    @Override
    public GdkCloud getCloud() {
        return GCP;
    }

    @NonNull
    @Override
    public String getName() {
        return "gdk-gcp-database";
    }
}
