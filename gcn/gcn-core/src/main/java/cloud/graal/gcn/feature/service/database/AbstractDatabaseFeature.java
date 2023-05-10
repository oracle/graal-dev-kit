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
package cloud.graal.gcn.feature.service.database;

import cloud.graal.gcn.GcnGeneratorContext;
import cloud.graal.gcn.feature.GcnFeatureContext;
import cloud.graal.gcn.feature.service.AbstractGcnServiceFeature;
import cloud.graal.gcn.feature.service.database.template.FlywayResourcesConfigJson;
import cloud.graal.gcn.feature.service.database.template.GenreControllerGroovy;
import cloud.graal.gcn.feature.service.database.template.GenreControllerJava;
import cloud.graal.gcn.feature.service.database.template.GenreControllerKotlin;
import cloud.graal.gcn.feature.service.database.template.GenreControllerSpec;
import cloud.graal.gcn.feature.service.database.template.GenreControllerTestGroovyJUnit;
import cloud.graal.gcn.feature.service.database.template.GenreControllerTestJava;
import cloud.graal.gcn.feature.service.database.template.GenreControllerTestKotest;
import cloud.graal.gcn.feature.service.database.template.GenreControllerTestKotlinJUnit;
import cloud.graal.gcn.feature.service.database.template.GenreFlywayMigrationH2;
import cloud.graal.gcn.feature.service.database.template.GenreFlywayMigrationMySQL;
import cloud.graal.gcn.feature.service.database.template.GenreFlywayMigrationOracle;
import cloud.graal.gcn.feature.service.database.template.GenreFlywayMigrationPostgreSQL;
import cloud.graal.gcn.feature.service.database.template.GenreFlywayMigrationSQLServer;
import cloud.graal.gcn.feature.service.database.template.GenreGroovy;
import cloud.graal.gcn.feature.service.database.template.GenreJava;
import cloud.graal.gcn.feature.service.database.template.GenreKotlin;
import cloud.graal.gcn.feature.service.database.template.GenreRepositoryGroovy;
import cloud.graal.gcn.feature.service.database.template.GenreRepositoryJava;
import cloud.graal.gcn.feature.service.database.template.GenreRepositoryKotlin;
import cloud.graal.gcn.feature.service.database.template.GenreServiceGroovy;
import cloud.graal.gcn.feature.service.database.template.GenreServiceJava;
import cloud.graal.gcn.feature.service.database.template.GenreServiceKotlin;
import cloud.graal.gcn.model.GcnService;
import com.fizzed.rocker.RockerModel;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.feature.database.Data;
import io.micronaut.starter.feature.database.DataJdbc;
import io.micronaut.starter.feature.database.DatabaseDriverFeature;
import io.micronaut.starter.feature.database.H2;
import io.micronaut.starter.feature.database.MySQL;
import io.micronaut.starter.feature.database.jdbc.JdbcFeature;
import io.micronaut.starter.feature.migration.Flyway;
import io.micronaut.starter.template.RockerTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

import static cloud.graal.gcn.model.GcnService.DATABASE;

/**
 * Base class for database service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractDatabaseFeature extends AbstractGcnServiceFeature {

    private final Data data;
    private final DataJdbc dataJdbc;
    private final Flyway flyway;
    private final JdbcFeature jdbcFeature;
    private final MySQL defaultDriverFeature;
    private DatabaseDriverFeature driverFeature;
    private boolean applyDriverFeature;

    /**
     * @param data                 Data feature
     * @param dataJdbc             DataJdbc feature
     * @param flyway               Flyway feature
     * @param jdbcFeature          JdbcFeature feature
     * @param defaultDriverFeature default driver feature (MySQL)
     */
    protected AbstractDatabaseFeature(Data data,
                                      DataJdbc dataJdbc,
                                      Flyway flyway,
                                      JdbcFeature jdbcFeature,
                                      MySQL defaultDriverFeature) {
        this.data = data;
        this.dataJdbc = dataJdbc;
        this.flyway = flyway;
        this.jdbcFeature = jdbcFeature;
        this.defaultDriverFeature = defaultDriverFeature;
    }

    @Override
    public void processSelectedFeatures(GcnFeatureContext featureContext) {

        // H2 is @Primary, so it will be auto-selected if no other driver was explicitly requested
        boolean h2WasSelected = featureContext.getSelectedNames().contains("h2");

        driverFeature = (DatabaseDriverFeature) featureContext.getSelectedAndDefaultFeatures().stream()
                .filter(f -> f instanceof DatabaseDriverFeature)
                .findFirst()
                .orElse(null);
        if (driverFeature == null || (driverFeature instanceof H2 && !h2WasSelected)) {
            driverFeature = defaultDriverFeature;
            applyDriverFeature = true;
            featureContext.addFeature(defaultDriverFeature);
            if (!h2WasSelected) {
                featureContext.exclude(f -> f instanceof H2);
            }
        } else {
            applyDriverFeature = false;
        }

        featureContext.addFeature(flyway, Flyway.class);

        // TODO add support for JPA/etc.
        // TODO what if a user selects JPA or other DataFeature
        featureContext.addFeature(dataJdbc, DataJdbc.class);
    }

    @Override
    public final void apply(GcnGeneratorContext generatorContext) {

        // Enable flyway
        //flyway:
        //  datasources:
        //    default:
        //      enabled: true
        generatorContext.getConfiguration().addNested("flyway.datasources.default.enabled", true);

        // set datasource and flyway properties in application-test.yml
        Map<String, Object> jdbcConfig = new LinkedHashMap<>();
        jdbcConfig.put("flyway.datasources.default.enabled", true);
        jdbcConfig.put("datasources.default.schema-generate", "NONE");
        jdbcConfig.put("datasources.default.dialect", driverFeature.getDataDialect());
        jdbcFeature.applyDefaultConfig(generatorContext, driverFeature, jdbcConfig);
        generatorContext.getTestConfiguration().addNested(jdbcConfig);

        applyForLib(generatorContext, () -> {
            data.apply(generatorContext);
            dataJdbc.apply(generatorContext);
            jdbcFeature.apply(generatorContext);
            if (applyDriverFeature) {
                driverFeature.apply(generatorContext);
            }
        });

        if (!generatorContext.isPlatformIndependent()) {
            applyForLib(generatorContext, () -> {
                generatorContext.addTemplate("flyway-resource-config",
                        new RockerTemplate(getDefaultModule(), "src/main/resources/META-INF/native-image/resource-config.json", FlywayResourcesConfigJson.template()));
            });
        }

        if (generatorContext.generateExampleCode()) {

            Project project = generatorContext.getProject();
            String templateRootPackage = project.getPackageName() == null ? "" : project.getPackageName() + '.';
            String dialect = driverFeature.getDataDialect();
            String cloudEnv = genreControllerTestEnv();

            generatorContext.addTestTemplate(getModuleName(), "GenreControllerTest-" + getModuleName(),
                    generatorContext.getTestSourcePath("/{packagePath}/GenreController"),
                    GenreControllerSpec.template(project, templateRootPackage, cloudEnv),
                    GenreControllerTestJava.template(project, templateRootPackage, cloudEnv),
                    GenreControllerTestGroovyJUnit.template(project, templateRootPackage, cloudEnv),
                    GenreControllerTestKotlinJUnit.template(project, templateRootPackage, cloudEnv),
                    GenreControllerTestKotest.template(project, templateRootPackage, cloudEnv));

            applyForLib(generatorContext, () -> {

                RockerModel flywayModel;
                switch (dialect) {
                    case "H2":
                        flywayModel = GenreFlywayMigrationH2.template();
                        break;
                    case "MYSQL":
                        flywayModel = GenreFlywayMigrationMySQL.template();
                        break;
                    case "POSTGRES":
                        flywayModel = GenreFlywayMigrationPostgreSQL.template();
                        break;
                    case "SQL_SERVER":
                        flywayModel = GenreFlywayMigrationSQLServer.template();
                        break;
                    case "ORACLE":
                        flywayModel = GenreFlywayMigrationOracle.template();
                        break;
                    default:
                        throw new IllegalStateException("Unknown SQL dialect '" + dialect + "'");
                }
                generatorContext.addTemplate("flyway",
                        new RockerTemplate(getDefaultModule(), "src/main/resources/db/migration/V1__schema.sql", flywayModel));

                generatorContext.addTemplate(getDefaultModule(), "Genre",
                        generatorContext.getSourcePath("/{packagePath}/domain/Genre"),
                        GenreJava.template(project),
                        GenreKotlin.template(project),
                        GenreGroovy.template(project));

                generatorContext.addTemplate(getDefaultModule(), "Genre",
                        generatorContext.getSourcePath("/{packagePath}/domain/Genre"),
                        GenreJava.template(project),
                        GenreKotlin.template(project),
                        GenreGroovy.template(project));

                generatorContext.addTemplate(getDefaultModule(), "GenreController",
                        generatorContext.getSourcePath("/{packagePath}/controller/GenreController"),
                        GenreControllerJava.template(project, templateRootPackage),
                        GenreControllerKotlin.template(project, templateRootPackage),
                        GenreControllerGroovy.template(project, templateRootPackage));

                generatorContext.addTemplate(getDefaultModule(), "GenreRepository",
                        generatorContext.getSourcePath("/{packagePath}/repository/GenreRepository"),
                        GenreRepositoryJava.template(project, templateRootPackage, dialect),
                        GenreRepositoryKotlin.template(project, templateRootPackage, dialect),
                        GenreRepositoryGroovy.template(project, templateRootPackage, dialect));

                generatorContext.addTemplate(getDefaultModule(), "GenreService",
                        generatorContext.getSourcePath("/{packagePath}/service/GenreService"),
                        GenreServiceJava.template(project, templateRootPackage),
                        GenreServiceKotlin.template(project, templateRootPackage),
                        GenreServiceGroovy.template(project, templateRootPackage));
            });
        } else {
            addLibPlaceholders(generatorContext);
        }
    }

    @NonNull
    @Override
    public final GcnService getService() {
        return DATABASE;
    }

    protected String genreControllerTestEnv() {
        return "";
    }
}
