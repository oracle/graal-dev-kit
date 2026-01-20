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

import cloud.graal.gdk.GdkGeneratorContext;
import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.feature.service.AbstractGdkServiceFeature;
import cloud.graal.gdk.feature.service.database.template.FlywayResourcesConfigJson;
import cloud.graal.gdk.feature.service.database.template.GenreControllerGroovy;
import cloud.graal.gdk.feature.service.database.template.GenreControllerJava;
import cloud.graal.gdk.feature.service.database.template.GenreControllerKotlin;
import cloud.graal.gdk.feature.service.database.template.GenreControllerSpec;
import cloud.graal.gdk.feature.service.database.template.GenreControllerTestGroovyJUnit;
import cloud.graal.gdk.feature.service.database.template.GenreControllerTestJava;
import cloud.graal.gdk.feature.service.database.template.GenreControllerTestKotest;
import cloud.graal.gdk.feature.service.database.template.GenreControllerTestKotlinJUnit;
import cloud.graal.gdk.feature.service.database.template.GenreFlywayMigrationH2;
import cloud.graal.gdk.feature.service.database.template.GenreFlywayMigrationMySQL;
import cloud.graal.gdk.feature.service.database.template.GenreFlywayMigrationOracle;
import cloud.graal.gdk.feature.service.database.template.GenreFlywayMigrationPostgreSQL;
import cloud.graal.gdk.feature.service.database.template.GenreFlywayMigrationSQLServer;
import cloud.graal.gdk.feature.service.database.template.GenreGroovy;
import cloud.graal.gdk.feature.service.database.template.GenreJava;
import cloud.graal.gdk.feature.service.database.template.GenreKotlin;
import cloud.graal.gdk.feature.service.database.template.GenreRepositoryGroovy;
import cloud.graal.gdk.feature.service.database.template.GenreRepositoryJava;
import cloud.graal.gdk.feature.service.database.template.GenreRepositoryKotlin;
import cloud.graal.gdk.feature.service.database.template.GenreServiceGroovy;
import cloud.graal.gdk.feature.service.database.template.GenreServiceJava;
import cloud.graal.gdk.feature.service.database.template.GenreServiceKotlin;
import cloud.graal.gdk.feature.service.template.JDK25FlywayInitializeAtBuildTimeClasses;
import cloud.graal.gdk.model.GdkService;
import com.fizzed.rocker.RockerModel;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.feature.database.Data;
import io.micronaut.starter.feature.database.DataJdbc;
import io.micronaut.starter.feature.database.DatabaseDriverFeature;
import io.micronaut.starter.feature.database.H2;
import io.micronaut.starter.feature.database.MySQL;
import io.micronaut.starter.feature.database.Oracle;
import io.micronaut.starter.feature.database.jdbc.JdbcFeature;
import io.micronaut.starter.feature.migration.Flyway;
import io.micronaut.starter.feature.oraclecloud.OracleCloudAutonomousDatabase;
import io.micronaut.starter.feature.validator.MicronautValidationFeature;
import io.micronaut.starter.template.RockerTemplate;
import io.micronaut.starter.template.RockerWritable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cloud.graal.gdk.model.GdkService.DATABASE;

/**
 * Base class for database service features.
 *
 * @since 1.0.0
 */
public abstract class AbstractDatabaseFeature extends AbstractGdkServiceFeature {

    private static final Dependency TESTCONTAINERS_ORACLE_XE = Dependency.builder()
            .groupId("org.testcontainers")
            .artifactId("oracle-xe")
            .testRuntime()
            .build();

    private final Data data;
    private final DataJdbc dataJdbc;
    private final Flyway flyway;
    private final JdbcFeature jdbcFeature;
    private final MySQL defaultDriverFeature;
    private DatabaseDriverFeature driverFeature;
    private final MicronautValidationFeature micronautValidationFeature;
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
                                      MySQL defaultDriverFeature,
                                      MicronautValidationFeature micronautValidationFeature) {
        this.data = data;
        this.dataJdbc = dataJdbc;
        this.flyway = flyway;
        this.jdbcFeature = jdbcFeature;
        this.defaultDriverFeature = defaultDriverFeature;
        this.micronautValidationFeature = micronautValidationFeature;
    }

    @Override
    public void processSelectedFeatures(GdkFeatureContext featureContext) {

        // H2 is @Primary, so it will be auto-selected if no other driver was explicitly requested
        boolean h2WasSelected = featureContext.getSelectedNames().contains("h2");

        List<DatabaseDriverFeature> drivers = featureContext.getSelectedAndDefaultFeatures().stream()
                .filter(f -> f instanceof DatabaseDriverFeature)
                .map(DatabaseDriverFeature.class::cast)
                .toList();
        if (drivers.size() > 1) {
            // let OneOfFeatureValidator fail the build
            return;
        }

        driverFeature = drivers.isEmpty() ? null : drivers.get(0);
        if (driverFeature == null || (driverFeature instanceof H2 && !h2WasSelected)) {
            driverFeature = defaultDriverFeature;
            applyDriverFeature = true;
            featureContext.addFeature(defaultDriverFeature);
        } else {
            applyDriverFeature = false;
        }

        if (!h2WasSelected) {
            featureContext.exclude(H2.class::isInstance);
        }

        featureContext.addFeature(flyway, Flyway.class);
        featureContext.addFeature(micronautValidationFeature, MicronautValidationFeature.class);

        // TODO add support for JPA/etc.
        // TODO what if a user selects JPA or other DataFeature
        featureContext.addFeature(dataJdbc, DataJdbc.class);
    }

    @Override
    public final void apply(GdkGeneratorContext generatorContext) {

        // Enable flyway
        //flyway:
        //  datasources:
        //    default:
        //      enabled: true
        generatorContext.getConfiguration().addNested("flyway.datasources.default.enabled", true);

        // set datasource and flyway properties in application-test.properties
        Map<String, Object> jdbcConfig = new LinkedHashMap<>(Map.of(
                "flyway.datasources.default.enabled", true,
                "datasources.default.dialect", driverFeature.getDataDialect()
        ));
        jdbcFeature.applyDefaultConfig(generatorContext, driverFeature, jdbcConfig);
        if (driverFeature instanceof OracleCloudAutonomousDatabase) {
            generatorContext.getTestConfiguration().addNested(Map.of(
                    "datasources.default.url", "jdbc:tc:oracle:thin:@/xe",
                    "datasources.default.driverClassName", "org.testcontainers.jdbc.ContainerDatabaseDriver",
                    "datasources.default.username", "system",
                    "datasources.default.password", "oracle",
                    "datasources.default.connectionTimeout", "60000",
                    "flyway.datasources.default.locations", "classpath:db/migration",
                    "flyway.datasources.default.baseline-version", "0",
                    "flyway.datasources.default.baseline-on-migrate", "true"));
            generatorContext.addDependency(TESTCONTAINERS_ORACLE_XE);
        } else if (driverFeature instanceof Oracle) {
            generatorContext.getTestConfiguration().addNested(Map.of(
                    "datasources.default.url", "jdbc:tc:oracle:thin:@/xe",
                    "datasources.default.driverClassName", "org.testcontainers.jdbc.ContainerDatabaseDriver",
                    "datasources.default.username", "system",
                    "datasources.default.password", "oracle",
                    "datasources.default.connectionTimeout", "60000",
                    "flyway.datasources.default.locations", "classpath:db/migration",
                    "flyway.datasources.default.baseline-version", "0",
                    "flyway.datasources.default.baseline-on-migrate", "true",
                    "flyway.datasources.default.enabled", "true"));
            generatorContext.addDependency(TESTCONTAINERS_ORACLE_XE);
        } else {
            generatorContext.getTestConfiguration().addNested(jdbcConfig);
        }

        applyForLib(generatorContext, () -> {
            data.apply(generatorContext);
            dataJdbc.apply(generatorContext);
            jdbcFeature.apply(generatorContext);
            micronautValidationFeature.apply(generatorContext);
            if (applyDriverFeature) {
                driverFeature.apply(generatorContext);
            }
        });

        if (!generatorContext.isPlatformIndependent()) {
            applyForLib(generatorContext, () -> {
                generatorContext.addTemplate("flyway-resource-config",
                        new RockerTemplate(
                                getDefaultModule(),
                                "src/main/resources/META-INF/native-image/resource-config.json",
                                FlywayResourcesConfigJson.template())
                );
            });
        }

        if (generatorContext.generateExampleCode()) {

            Project project = generatorContext.getProject();
            String templateRootPackage = project.getPackageName() == null ? "" : project.getPackageName() + '.';
            String dialect = driverFeature.getDataDialect();

            generatorContext.addTestTemplate(getModuleName(), "GenreControllerTest-" + getModuleName(),
                    generatorContext.getTestSourcePath("/{packagePath}/GenreController"),
                    GenreControllerSpec.template(project, templateRootPackage),
                    GenreControllerTestJava.template(project, templateRootPackage),
                    GenreControllerTestGroovyJUnit.template(project, templateRootPackage),
                    GenreControllerTestKotlinJUnit.template(project, templateRootPackage),
                    GenreControllerTestKotest.template(project, templateRootPackage));

            applyForLib(generatorContext, () -> {

                RockerModel flywayModel = switch (dialect) {
                    case "H2" -> GenreFlywayMigrationH2.template();
                    case "MYSQL" -> GenreFlywayMigrationMySQL.template();
                    case "POSTGRES" -> GenreFlywayMigrationPostgreSQL.template();
                    case "SQL_SERVER" -> GenreFlywayMigrationSQLServer.template();
                    case "ORACLE" -> GenreFlywayMigrationOracle.template();
                    default -> throw new IllegalStateException("Unknown SQL dialect '" + dialect + "'");
                };
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
        }

        if (generatorContext.isJdkVersionAtLeast(25)) {
            generatorContext.addInitializeBuildTimeClasses(new RockerWritable(JDK25FlywayInitializeAtBuildTimeClasses.template()));
        }
    }

    @NonNull
    @Override
    public final GdkService getService() {
        return DATABASE;
    }
}
