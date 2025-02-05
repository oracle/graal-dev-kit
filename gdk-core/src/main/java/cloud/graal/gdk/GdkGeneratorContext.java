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
package cloud.graal.gdk;

import cloud.graal.gdk.build.dependencies.GdkDependencies;
import cloud.graal.gdk.feature.GdkFeature;
import cloud.graal.gdk.feature.GdkFeatureContext;
import cloud.graal.gdk.feature.GdkFeatures;
import cloud.graal.gdk.feature.replaced.GdkJTE;
import cloud.graal.gdk.model.GdkCloud;
import cloud.graal.gdk.template.GdkPropertiesTemplate;
import cloud.graal.gdk.template.TemplatePostProcessor;
import com.fizzed.rocker.RockerModel;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.application.generator.DependencyContextImpl;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.BuildPlugin;
import io.micronaut.starter.build.Property;
import io.micronaut.starter.build.dependencies.Coordinate;
import io.micronaut.starter.build.dependencies.CoordinateResolver;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.build.dependencies.DependencyContext;
import io.micronaut.starter.build.dependencies.StarterCoordinates;
import io.micronaut.starter.build.gradle.GradlePlugin;
import io.micronaut.starter.build.maven.MavenPlugin;
import io.micronaut.starter.feature.ApplicationFeature;
import io.micronaut.starter.feature.DefaultFeature;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.feature.RequireEagerSingletonInitializationFeature;
import io.micronaut.starter.feature.aws.AwsCloudFeature;
import io.micronaut.starter.feature.build.BuildFeature;
import io.micronaut.starter.feature.build.BuildPluginFeature;
import io.micronaut.starter.feature.config.ApplicationConfiguration;
import io.micronaut.starter.feature.config.BootstrapConfiguration;
import io.micronaut.starter.feature.config.Configuration;
import io.micronaut.starter.feature.database.DatabaseDriverFeature;
import io.micronaut.starter.feature.function.Cloud;
import io.micronaut.starter.feature.function.CloudFeature;
import io.micronaut.starter.feature.function.awslambda.AwsLambda;
import io.micronaut.starter.feature.lang.ApplicationRenderingContext;
import io.micronaut.starter.feature.lang.groovy.GroovyApplicationRenderingContext;
import io.micronaut.starter.feature.lang.java.JavaApplicationRenderingContext;
import io.micronaut.starter.feature.lang.kotlin.KotlinApplicationRenderingContext;
import io.micronaut.starter.feature.testresources.TestResources;
import io.micronaut.starter.options.DefaultTestRockerModelProvider;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.options.TestRockerModelProvider;
import io.micronaut.starter.template.PropertiesTemplate;
import io.micronaut.starter.template.RockerTemplate;
import io.micronaut.starter.template.Template;
import io.micronaut.starter.template.URLTemplate;
import io.micronaut.starter.util.NameUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cloud.graal.gdk.GdkUtils.LIB_MODULE;
import static cloud.graal.gdk.model.GdkCloud.AWS;
import static cloud.graal.gdk.model.GdkCloud.NONE;
import static io.micronaut.context.env.Environment.DEVELOPMENT;
import static io.micronaut.context.env.Environment.FUNCTION;
import static io.micronaut.context.env.Environment.TEST;
import static io.micronaut.starter.feature.build.gradle.MicronautApplicationGradlePlugin.Builder.APPLICATION;
import static io.micronaut.starter.feature.build.gradle.MicronautApplicationGradlePlugin.Builder.LIBRARY;
import static io.micronaut.starter.template.Template.DEFAULT_MODULE;
import static io.micronaut.starter.template.Template.ROOT;

/**
 * Replacement for GeneratorContext that partitions dependencies, config
 * settings, etc. by cloud.
 *
 * @since 1.0.0
 */
public class GdkGeneratorContext extends GeneratorContext {

    /**
     * Additional options map key for the example code boolean.
     */
    public static final String EXAMPLE_CODE = "exampleCode";

    /**
     * The id of the Gradle Shadow plugin.
     */
    public static final String PLUGIN_SHADOW = "com.github.johnrengelman.shadow";

    private static final String PLUGIN_GRADLE_AZUREFUNCTIONS = "com.microsoft.azure.azurefunctions";
    private static final String PLUGIN_MAVEN_AZUREFUNCTIONS = "azure-functions-maven-plugin";

    private static final Map<Cloud, GdkCloud> MICRONAUT_CLOUD_TO_GDK_CLOUD = Map.of(
            Cloud.AWS, GdkCloud.AWS,
            Cloud.GCP, GdkCloud.GCP,
            Cloud.AZURE, GdkCloud.AZURE,
            Cloud.ORACLE, GdkCloud.OCI
    );

    private static final Map<String, String> PLUGIN_GAVS = Map.ofEntries(
            gavMapEntry("com.github.johnrengelman.shadow", "com.github.johnrengelman:shadow", StarterCoordinates.SHADOW),
            gavMapEntry("io.micronaut.application", "io.micronaut.gradle:micronaut-gradle-plugin", GdkDependencies.IO_MICRONAUT_GRADLE_MICRONAUT_GRADLE_PLUGIN),
            gavMapEntry("io.micronaut.library", "io.micronaut.gradle:micronaut-gradle-plugin", GdkDependencies.IO_MICRONAUT_GRADLE_MICRONAUT_GRADLE_PLUGIN),
            gavMapEntry("io.micronaut.test-resources", "io.micronaut.gradle:micronaut-test-resources-plugin", GdkDependencies.IO_MICRONAUT_GRADLE_MICRONAUT_GRADLE_PLUGIN),
            gavMapEntry("org.jetbrains.kotlin.jvm", "org.jetbrains.kotlin:kotlin-gradle-plugin", StarterCoordinates.KOTLIN_GRADLE_PLUGIN),
            gavMapEntry("org.jetbrains.kotlin.kapt", "org.jetbrains.kotlin:kotlin-gradle-plugin", StarterCoordinates.KOTLIN_GRADLE_PLUGIN),
            gavMapEntry("org.jetbrains.kotlin.plugin.allopen", "org.jetbrains.kotlin:kotlin-allopen", StarterCoordinates.KOTLIN_GRADLE_PLUGIN),
            gavMapEntry("com.google.cloud.tools.jib", "com.google.cloud.tools.jib:com.google.cloud.tools.jib.gradle.plugin", StarterCoordinates.JIB_GRADLE_PLUGIN),
            gavMapEntry("io.micronaut.aot", "io.micronaut.gradle:micronaut-aot-plugin", GdkDependencies.IO_MICRONAUT_GRADLE_MICRONAUT_GRADLE_PLUGIN),
            gavMapEntry("com.google.devtools.ksp", "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin", StarterCoordinates.COM_GOOGLE_DEVTOOLS_KSP_GRADLE_PLUGIN),
            gavMapEntry("gg.jte.gradle", "gg.jte:jte-gradle-plugin", StarterCoordinates.JTE_GRADLE_PLUGIN)
    );

    private static final ThreadLocal<GdkGeneratorContext> INSTANCE = new ThreadLocal<>();

    private GdkCloud cloud = NONE;
    private boolean hideLibFeatures;
    private final Map<GdkCloud, ApplicationConfiguration> applicationConfigurations = new HashMap<>(GdkCloud.supportedValues().length);
    private final Map<GdkCloud, ApplicationConfiguration> cloudApplicationConfigurations = new HashMap<>(GdkCloud.supportedValues().length);
    private final Map<GdkCloud, BootstrapConfiguration> bootstrapConfigurations = new HashMap<>(GdkCloud.supportedValues().length);
    private final Map<GdkCloud, BootstrapConfiguration> cloudBootstrapConfigurations = new HashMap<>(GdkCloud.supportedValues().length);
    private final Map<GdkCloud, DependencyContext> cloudDependencyContexts = new HashMap<>(GdkCloud.supportedValues().length);
    private final Map<GdkCloud, Project> cloudProjects = new HashMap<>(GdkCloud.supportedValues().length);
    private final Map<GdkCloud, GdkFeatures> cloudFeatures;
    private final Map<GdkCloud, Map<String, ApplicationConfiguration>> cloudApplicationEnvConfigurations = new HashMap<>(GdkCloud.supportedValues().length);
    private final Map<GdkCloud, Map<String, BootstrapConfiguration>> cloudBootstrapEnvConfigurations = new HashMap<>(GdkCloud.supportedValues().length);
    private final Map<GdkCloud, Set<BuildPlugin>> buildPlugins = new HashMap<>();
    private final Map<String, Set<TemplatePostProcessor>> postProcessors = new HashMap<>();
    private final Map<Pattern, Set<TemplatePostProcessor>> regexPostProcessors = new HashMap<>();
    private final Set<String> presentTemplatePaths = new HashSet<>();
    private final CoordinateResolver coordinateResolver;
    private final GdkFeatureContext featureContext;
    private final boolean generateExampleCode;
    private final Set<GdkCloud> clouds;
    private final GdkBuildProperties buildProperties;

    /**
     * @param project            the project
     * @param featureContext     feature context
     * @param features           all features, including selected, default, and added
     * @param coordinateResolver dependency coordinate resolver
     */
    public GdkGeneratorContext(Project project,
                               GdkFeatureContext featureContext,
                               Set<Feature> features,
                               CoordinateResolver coordinateResolver) {
        super(project, featureContext.getApplicationType(), featureContext.getOptions(),
                featureContext.getOperatingSystem(), features, coordinateResolver);
        this.coordinateResolver = coordinateResolver;
        this.featureContext = featureContext;
        cloudFeatures = splitFeatures(features, featureContext, this);
        generateExampleCode = featureContext.getOptions().get(EXAMPLE_CODE, Boolean.class).orElse(true);

        clouds = new HashSet<>(cloudFeatures.keySet());
        clouds.add(NONE); // for lib module
        buildProperties = new GdkBuildProperties(this, clouds);
        if (featureContext.getOptions().getBuildTool().isGradle()) {
            buildProperties.put("micronautVersion", GdkUtils.getMicronautVersion() + GdkUtils.BOM_VERSION_SUFFIX);
        }
        INSTANCE.set(this);
    }

    /**
     * @return the current instance
     */
    public static GdkGeneratorContext getInstance() {
        return INSTANCE.get();
    }

    private static Map.Entry<String, String> gavMapEntry(String keyStart, String valueStart,
                                                         Coordinate coordinate) {
        return Map.entry(keyStart + ':' + coordinate.getVersion(), valueStart + ':' + coordinate.getVersion());
    }

    private static Map<GdkCloud, GdkFeatures> splitFeatures(Set<Feature> allFeatures,
                                                            GdkFeatureContext featureContext,
                                                            GdkGeneratorContext self) {
        Map<GdkCloud, Set<Feature>> featuresByCloud = new HashMap<>(GdkCloud.supportedValues().length);

        ApplicationFeature applicationFeature = null;
        DatabaseDriverFeature databaseDriverFeature = null;
        Set<Feature> defaultFeatures = new HashSet<>();
        Set<Feature> specifiedFeatures = new HashSet<>();

        Collection<GdkCloud> initialClouds = new HashSet<>();
        for (Feature feature : allFeatures) {
            if (feature instanceof GdkFeature) {
                initialClouds.add(((GdkFeature) feature).getCloud());
            }
        }

        for (Feature feature : allFeatures) {

            if (feature instanceof AwsCloudFeature && !initialClouds.contains(AWS)) {
                // AwsLambda implements DefaultFeature but causes problems when generating for non-AWS apps,
                // and brings in other AWS features that implement AwsCloudFeature.
                continue;
            }

            if (feature instanceof DefaultFeature &&
                    !(feature instanceof AwsLambda) &&
                    !(feature instanceof BuildFeature)) {
                defaultFeatures.add(feature);
            }
            if (feature instanceof GdkFeature) {
                GdkCloud cloud = ((GdkFeature) feature).getCloud();
                featuresByCloud.computeIfAbsent(cloud, k -> new HashSet<>()).add(feature);
            } else {
                if (feature instanceof CloudFeature cloudFeature && initialClouds.contains(MICRONAUT_CLOUD_TO_GDK_CLOUD.get(cloudFeature.getCloud()))) {
                    featuresByCloud.computeIfAbsent(MICRONAUT_CLOUD_TO_GDK_CLOUD.get(cloudFeature.getCloud()), k -> new HashSet<>()).add(feature);
                }

                if (applicationFeature == null && feature instanceof ApplicationFeature) {
                    applicationFeature = (ApplicationFeature) feature;
                }
                if (databaseDriverFeature == null && feature instanceof DatabaseDriverFeature) {
                    databaseDriverFeature = (DatabaseDriverFeature) feature;
                }

                if (featureContext.getSelectedNames().contains(feature.getName())) {
                    specifiedFeatures.add(feature);
                }

                for (GdkCloud cloud : featureContext.getAddedFeatureClouds(feature)) {
                    featuresByCloud.computeIfAbsent(cloud, k -> new HashSet<>()).add(feature);
                }
            }
        }

        for (Set<Feature> features : featuresByCloud.values()) {

            features.addAll(defaultFeatures);
            features.addAll(specifiedFeatures);

            if (applicationFeature != null) {
                features.add(applicationFeature);
            }

            if (databaseDriverFeature != null) {
                features.add(databaseDriverFeature);
            }
        }

        Map<GdkCloud, GdkFeatures> split = new HashMap<>(featuresByCloud.size());
        for (Map.Entry<GdkCloud, Set<Feature>> entry : featuresByCloud.entrySet()) {
            List<Feature> finalFeaturesList = new ArrayList<>(entry.getValue());
            finalFeaturesList.sort(Comparator.comparingInt(Feature::getOrder));
            split.put(entry.getKey(), new GdkFeatures(self, new LinkedHashSet<>(finalFeaturesList), featureContext.getOptions()));
        }

        return split;
    }

    /**
     * Set the current cloud.
     *
     * @param cloud the cloud
     */
    public void setCloud(GdkCloud cloud) {
        this.cloud = cloud;
        if (cloud != NONE) {
            applicationConfigurations.putIfAbsent(cloud, new ApplicationConfiguration(cloud.getModuleName()));
            cloudApplicationConfigurations.putIfAbsent(cloud, new ApplicationConfiguration(cloud.getModuleName()));
            bootstrapConfigurations.putIfAbsent(cloud, new BootstrapConfiguration(cloud.getModuleName()));
            cloudBootstrapConfigurations.putIfAbsent(cloud, new BootstrapConfiguration(cloud.getModuleName()));
            cloudDependencyContexts.putIfAbsent(cloud, new DependencyContextImpl(coordinateResolver));
            cloudProjects.putIfAbsent(cloud, createProject(cloud, getProject().getName()));
        }
    }

    /**
     * Reset to the default.
     */
    public void resetCloud() {
        cloud = NONE;
    }

    /**
     * @return the current cloud, or NONE indicating the lib module
     */
    public GdkCloud getCloud() {
        return cloud;
    }

    /**
     * @return the selected clouds
     */
    public Set<GdkCloud> getClouds() {
        return clouds;
    }

    @Override
    public boolean hasConfigurationEnvironment(@NonNull String env) {

        if (cloud == NONE) {
            return super.hasConfigurationEnvironment(env);
        }

        return cloudApplicationEnvConfigurations
                .computeIfAbsent(cloud, gdkCloud -> new HashMap<>())
                .containsKey(env);
    }

    @NonNull
    @Override
    public GdkBuildProperties getBuildProperties() {
        return buildProperties;
    }

    @NonNull
    @Override
    public ApplicationConfiguration getConfiguration() {
        return applicationConfigurations.getOrDefault(cloud, super.getConfiguration());
    }

    /**
     * @return application-dev configuration
     */
    @NonNull
    public ApplicationConfiguration getDevConfiguration() {
        return getConfiguration(DEVELOPMENT, ApplicationConfiguration.devConfig());
    }

    /**
     * @return application-test configuration
     */
    @NonNull
    public ApplicationConfiguration getTestConfiguration() {
        return getConfiguration(TEST, ApplicationConfiguration.testConfig());
    }

    /**
     * @return application-function test configuration
     */
    @NonNull
    public ApplicationConfiguration getFunctionTestConfiguration() {
        return getConfiguration(FUNCTION, ApplicationConfiguration.functionTestConfig());
    }

    @NonNull
    @Override
    public ApplicationConfiguration getConfiguration(String env, ApplicationConfiguration defaultConfig) {

        if (cloud == NONE) {
            return super.getConfiguration(env, defaultConfig);
        }

        return cloudApplicationEnvConfigurations
                .computeIfAbsent(cloud, gdkCloud -> new HashMap<>())
                .computeIfAbsent(env, (key) -> defaultConfig);
    }

    /**
     * @return application environment configurations grouped by cloud
     */
    public Map<GdkCloud, Map<String, ApplicationConfiguration>> getCloudApplicationEnvConfigurations() {
        return Collections.unmodifiableMap(cloudApplicationEnvConfigurations);
    }

    /**
     * @return application configuration for the lib module
     */
    public ApplicationConfiguration getLibConfiguration() {
        return super.getConfiguration();
    }

    /**
     * @return application configurations grouped by cloud
     */
    public Map<GdkCloud, ApplicationConfiguration> getApplicationConfigurations() {
        return Collections.unmodifiableMap(applicationConfigurations);
    }

    /**
     * @return cloud application configurations grouped by cloud
     */
    public Map<GdkCloud, ApplicationConfiguration> getCloudApplicationConfigurations() {
        return Collections.unmodifiableMap(cloudApplicationConfigurations);
    }

    /**
     * @return the application configuration for the current cloud
     */
    public ApplicationConfiguration getCloudConfiguration() {
        return getConfiguration(cloud);
    }

    /**
     * @param cloud the cloud
     * @return the application configuration for the specified cloud
     */
    public ApplicationConfiguration getConfiguration(GdkCloud cloud) {
        return cloudApplicationConfigurations.get(cloud);
    }

    @NonNull
    @Override
    public BootstrapConfiguration getBootstrapConfiguration() {
        return bootstrapConfigurations.getOrDefault(cloud, super.getBootstrapConfiguration());
    }

    @NonNull
    @Override
    public BootstrapConfiguration getBootstrapConfiguration(String env, BootstrapConfiguration defaultConfig) {
        if (cloud == NONE) {
            return super.getBootstrapConfiguration(env, defaultConfig);
        }

        return cloudBootstrapEnvConfigurations
                .computeIfAbsent(cloud, gdkCloud -> new HashMap<>())
                .computeIfAbsent(env, (key) -> defaultConfig);
    }

    /**
     * @return bootstrap-test configuration
     */
    @NonNull
    public BootstrapConfiguration getTestBootstrapConfiguration() {
        return getBootstrapConfiguration(TEST, BootstrapConfiguration.testConfig());
    }

    /**
     * @return bootstrap environment configurations grouped by cloud
     */
    public Map<GdkCloud, Map<String, BootstrapConfiguration>> getCloudBootstrapEnvConfigurations() {
        return Collections.unmodifiableMap(cloudBootstrapEnvConfigurations);
    }

    /**
     * @return the bootstrap configuration for the current cloud
     */
    public BootstrapConfiguration getCloudBootstrapConfiguration() {
        return getBootstrapConfiguration(cloud);
    }

    /**
     * @param cloud the cloud
     * @return the bootstrap configuration for the specified cloud
     */
    public BootstrapConfiguration getBootstrapConfiguration(GdkCloud cloud) {
        return cloudBootstrapConfigurations.getOrDefault(cloud, super.getBootstrapConfiguration());
    }

    /**
     * @return bootstrap configuration for the lib module
     */
    public BootstrapConfiguration getLibBootstrapConfiguration() {
        return super.getBootstrapConfiguration();
    }

    /**
     * @return bootstrap configurations grouped by cloud
     */
    public Map<GdkCloud, BootstrapConfiguration> getBootstrapConfigurations() {
        return Collections.unmodifiableMap(bootstrapConfigurations);
    }

    /**
     * @return cloud bootstrap configurations grouped by cloud
     */
    public Map<GdkCloud, BootstrapConfiguration> getCloudBootstrapConfigurations() {
        return Collections.unmodifiableMap(cloudBootstrapConfigurations);
    }

    @NonNull
    @Override
    public Project getProject() {
        return cloudProjects.getOrDefault(cloud, super.getProject());
    }

    /**
     * @return the project for the lib module
     */
    @NonNull
    public Project getLibProject() {
        return super.getProject();
    }

    /**
     * @return project grouped by cloud
     */
    public Map<GdkCloud, Project> getCloudProjects() {
        return Collections.unmodifiableMap(cloudProjects);
    }

    @NonNull
    @Override
    public GdkFeatures getFeatures() {
        if (cloud != NONE) {
            return cloudFeatures.get(cloud);
        }

        if (hideLibFeatures) {
            // hide GDK features and features added by them so the
            // 'features.contains("...")' checks in Rocker templates
            // return false for the lib module build
            return new GdkFeatures(this, libFeatures(), featureContext.getOptions());
        }

        return getLibFeatures();
    }

    /**
     * @return features for the lib module
     */
    @NonNull
    public GdkFeatures getLibFeatures() {
        return new GdkFeatures(this, super.getFeatures().getFeatures(), featureContext.getOptions());
    }

    /**
     * @param cloud the cloud
     * @return the features for the cloud
     */
    public GdkFeatures getFeatures(GdkCloud cloud) {

        if (cloud != NONE) {
            return cloudFeatures.get(cloud);
        }

        if (hideLibFeatures) {
            // hide GDK features and features added by them so the
            // 'features.contains("...")' checks in Rocker templates
            // return false for the lib module build
            return new GdkFeatures(this, libFeatures(), featureContext.getOptions());
        }

        return getLibFeatures();
    }

    /**
     * @return features grouped by cloud
     */
    public Map<GdkCloud, GdkFeatures> getCloudFeatures() {
        return Collections.unmodifiableMap(cloudFeatures);
    }

    /**
     * Add a template post-processor.
     *
     * @param templateKey  the template key
     * @param postPocessor the processor
     */
    public void addPostProcessor(@NonNull String templateKey,
                                 @NonNull TemplatePostProcessor postPocessor) {
        postProcessors.computeIfAbsent(templateKey, k -> new HashSet<>()).add(postPocessor);
    }

    /**
     * Add a Regex template post-processor.
     *
     * @param templatePattern the template path pattern
     * @param postPocessor    the processor
     */
    public void addPostProcessor(@NonNull Pattern templatePattern,
                                 @NonNull TemplatePostProcessor postPocessor) {
        regexPostProcessors.computeIfAbsent(templatePattern, k -> new HashSet<>()).add(postPocessor);
    }

    /**
     * @return template post-processors grouped by template key
     */
    @NonNull
    public Map<String, Set<TemplatePostProcessor>> getPostProcessors() {
        return Collections.unmodifiableMap(postProcessors);
    }

    /**
     * @return regex template post-processors grouped by template key
     */
    @NonNull
    public Map<Pattern, Set<TemplatePostProcessor>> getRegexPostProcessors() {
        return Collections.unmodifiableMap(regexPostProcessors);
    }

    @Override
    public void applyFeatures() {

        if (getApplicationType() == ApplicationType.FUNCTION) {
            hideLibFeatures = true;
        }

        for (Map.Entry<GdkCloud, GdkFeatures> e : cloudFeatures.entrySet()) {
            applyFeatures(e.getValue().getFeatures(), e.getKey());
        }

        applyFeatures(libFeatures(), NONE);
    }

    private void applyFeatures(Set<Feature> features, GdkCloud cloud) {

        setCloud(cloud);

        List<Feature> sorted = new ArrayList<>(features);
        sorted.sort(Comparator.comparingInt(Feature::getOrder));

        for (Feature feature : sorted) {
            if (feature instanceof CloudFeature cloudFeature) {
                if (MICRONAUT_CLOUD_TO_GDK_CLOUD.get(cloudFeature.getCloud()) == getCloud() || isPlatformIndependent()) {
                    feature.apply(this);
                }
            } else {
                feature.apply(this);
            }
        }
    }

    @Override
    public void addBuildPlugin(BuildPlugin buildPlugin) {
        if (buildPlugin.requiresLookup()) {
            buildPlugin = buildPlugin.resolved(coordinateResolver);
        }
        buildPlugins.computeIfAbsent(cloud, k -> new HashSet<>()).add(buildPlugin);

        // also add most to lib module
        if (cloud != NONE && shouldIncludePluginInLib(buildPlugin)) {
            buildPlugins.computeIfAbsent(NONE, k -> new HashSet<>()).add(buildPlugin);
        }
    }

    private boolean shouldIncludePluginInLib(BuildPlugin buildPlugin) {

        if (buildPlugin instanceof MavenPlugin) {
            MavenPlugin plugin = (MavenPlugin) buildPlugin;
            // cloud-specific
            return !PLUGIN_MAVEN_AZUREFUNCTIONS.equals(plugin.getArtifactId());
        }

        GradlePlugin plugin = (GradlePlugin) buildPlugin;
        String id = plugin.getId();
        if (PLUGIN_GRADLE_AZUREFUNCTIONS.equals(id)) {
            // cloud-specific
            return false;
        }

        // will already be there
        return !PLUGIN_SHADOW.equals(id) && !APPLICATION.equals(id) && !LIBRARY.equals(id);
    }

    @Override
    public Set<BuildPlugin> getBuildPlugins() {
        if (cloud == NONE && getApplicationType() == ApplicationType.FUNCTION) {
            Set<BuildPlugin> plugins = new HashSet<>();
            for (Set<BuildPlugin> cloudPlugins : buildPlugins.values()) {
                for (BuildPlugin plugin : cloudPlugins) {
                    if (plugin instanceof GradlePlugin && ((GradlePlugin) plugin).getId().equals(APPLICATION)) {
                        continue;
                    }
                    plugins.add(plugin);
                }
            }
            return plugins;
        }

        return buildPlugins.getOrDefault(cloud, Collections.emptySet());
    }

    @Override
    public void addDependency(@NonNull Dependency dependency) {
        DependencyContext cloudDependencyContext = cloudDependencyContexts.get(cloud);
        if (cloudDependencyContext == null) {
            super.addDependency(dependency);
        } else {
            cloudDependencyContext.addDependency(dependency);
        }
    }

    @NonNull
    @Override
    public Collection<Dependency> getDependencies() {
        DependencyContext cloudDependencyContext = cloudDependencyContexts.get(cloud);
        if (cloudDependencyContext == null) {
            return super.getDependencies();
        }

        return cloudDependencyContext.getDependencies();
    }

    /**
     * @return dependencies for the lib module
     */
    public Collection<Dependency> getLibDependencies() {
        return super.getDependencies();
    }

    /**
     * @return dependency contexts grouped by cloud
     */
    public Map<GdkCloud, DependencyContext> getCloudDependencyContexts() {
        return Collections.unmodifiableMap(cloudDependencyContexts);
    }

    // compute explicitly since we know what the names are, and the template for
    // settings.gradle is registered before the cloud modules
    @Override
    public Collection<String> getModuleNames() {

        if (isPlatformIndependent()) {
            return Collections.emptyList();
        }

        List<String> names = new ArrayList<>(cloudFeatures.size() + 1);
        names.add(LIB_MODULE);

        names.addAll(cloudFeatures.keySet().stream()
                .filter(Objects::nonNull)
                .filter(cloud -> cloud != NONE)
                .map(GdkCloud::getModuleName)
                .sorted()
                .toList());

        return names;
    }

    /**
     * @return environment configurations grouped by cloud
     */
    @NonNull
    public Map<GdkCloud, Collection<Configuration>> getExtraConfigurations() {

        Map<GdkCloud, Collection<Configuration>> extra = new HashMap<>();

        for (Map.Entry<GdkCloud, Map<String, ApplicationConfiguration>> e : cloudApplicationEnvConfigurations.entrySet()) {
            extra.computeIfAbsent(e.getKey(), gdkCloud -> new HashSet<>()).addAll(e.getValue().values());
        }

        for (Map.Entry<GdkCloud, Map<String, BootstrapConfiguration>> e : cloudBootstrapEnvConfigurations.entrySet()) {
            extra.computeIfAbsent(e.getKey(), gdkCloud -> new HashSet<>()).addAll(e.getValue().values());
        }

        return extra;
    }

    /**
     * @return whether to generate example code
     */
    public boolean generateExampleCode() {
        return generateExampleCode;
    }

    @Override
    public void addTemplate(String name, Template template) {

        if (isIgnoredAppTemplate(name, template)) {
            return;
        }

        if (!template.getModule().equals(ROOT)) {
            if (template instanceof GdkPropertiesTemplate) {
                if (isPlatformIndependent() && (template.getModule().equals(LIB_MODULE) || template.getModule().equals(GdkUtils.APP_MODULE))) {
                    // for platform independent, re-route non-root templates to root
                    template = new GdkPropertiesTemplate(ROOT, template.getPath(), ((GdkPropertiesTemplate) template).getOriginalConfig());
                }
            } else if (template instanceof RockerTemplate) {

                String newModule = null;
                if (name.equals("k8sYaml")) {
                    newModule = ROOT;
                } else if (cloud != NONE && !template.getModule().equals(cloud.getModuleName())) {
                    // re-route non-root templates added by features that were added by a cloud service feature to the cloud module
                    newModule = cloud.getModuleName();
                    name += '-' + newModule;
                } else if (isPlatformIndependent() && (template.getModule().equals(LIB_MODULE) || template.getModule().equals(DEFAULT_MODULE))) {
                    // for platform independent, re-route non-root templates to root
                    newModule = ROOT;
                } else if (template.getModule().equals(DEFAULT_MODULE)) {
                    // re-route to lib
                    newModule = LIB_MODULE;
                }

                if (newModule != null) {
                    RockerModel model = ((RockerTemplate) template).getWritable().getModel();
                    template = new RockerTemplate(newModule, template.getPath(), model, template.isExecutable());
                }
            } else if (template instanceof URLTemplate urlTemplate) {
                if (name.endsWith(".html")) {
                    // re-route to lib
                    template = new URLTemplate(LIB_MODULE, urlTemplate.getPath(), urlTemplate.getUrl(), urlTemplate.isExecutable());
                }
            }
        }

        addTemplateInternal(name, template);
    }

    /**
     * Internal method for adding templates. The GDK feature templates are added first,
     * so we want to avoid overwriting those with micronaut feature templates.
     * Therefore, all added templates are stored in a set and a template is not added if
     * one exists with the same path already.
     *
     * @param name     the name of the template
     * @param template template
     */
    private void addTemplateInternal(String name, Template template) {
        String templatePath = isPlatformIndependent() ? template.getPath() : template.getModule() + "/" + template.getPath();
        if (presentTemplatePaths.contains(templatePath) && !name.equals("multi-module-pom")) {
            return;
        }
        presentTemplatePaths.add(templatePath);

        super.addTemplate(name, template);
    }

    @Override
    public void removeTemplate(String name) {
        Template template = super.getTemplates().get(name);
        if (template != null) {
            String templatePath = isPlatformIndependent() ? template.getPath() : template.getModule() + "/" + template.getPath();
            super.removeTemplate(name);
            presentTemplatePaths.remove(templatePath);
        }
    }

    // the lib module is created as an application, but we convert it to a
    // library, so these shouldn't be created
    private boolean isIgnoredAppTemplate(String name, Template template) {

        if (isPlatformIndependent()) {
            return false;
        }

        if (template instanceof PropertiesTemplate &&
                template.getModule().equals(LIB_MODULE) &&
                "application-config".equals(name)) {

            return true; // application.properties
        }

        if (!(template instanceof RockerTemplate rockerTemplate)) {
            return false;
        }

        if (!rockerTemplate.getModule().equals(DEFAULT_MODULE)) {
            return false;
        }

        return "application".equals(name) || // Application.java
                "applicationTest".equals(name) || // Application Test.java
                "loggingConfig".equals(name); // logback.xml
    }

    /**
     * @return true if bclouds uilding a platform-independent app,
     * i.e. a single-module app with not cloud modules
     */
    public boolean isPlatformIndependent() {
        return clouds.size() == 1 && clouds.contains(NONE);
    }

    @NonNull
    public Set<Configuration> getAllConfigurations() {
        Set<Configuration> allConfigurations = super.getAllConfigurations();

        if (!isPlatformIndependent()) {
            allConfigurations.remove(super.getConfiguration());
            allConfigurations.remove(super.getBootstrapConfiguration());
        }

        return allConfigurations;
    }

    @Override
    public void addTemplate(String name,
                            String path,
                            TestRockerModelProvider testRockerModelProvider) {
        String moduleName = cloud == NONE ? LIB_MODULE : cloud.getModuleName();

        if (isPlatformIndependent()) {
            moduleName = "";
        }

        RockerModel rockerModel = testRockerModelProvider.findModel(getLanguage(), getTestFramework());
        if (rockerModel != null) {
            addTemplateInternal(name, new RockerTemplate(moduleName, path, rockerModel));
        }
    }

    @Override
    public void addTemplate(String templateKey,
                            String path,
                            RockerModel javaTemplate,
                            RockerModel kotlinTemplate,
                            RockerModel groovyTemplate) {
        addTemplateInternal(templateKey, new RockerTemplate(
                cloud == NONE ? isPlatformIndependent() ? "" : LIB_MODULE : cloud.getModuleName(),
                path,
                parseModel(javaTemplate, kotlinTemplate, groovyTemplate)));
    }

    /**
     * Register a template to be rendered for the specified module. Pass
     * templates for all 3 languages and the one corresponding to the
     * user-selected language will be used.
     *
     * @param module         the module
     * @param templateKey    the template name (map key, must be unique)
     * @param path           the file location
     * @param javaTemplate   the Java template
     * @param kotlinTemplate the Kotlin template
     * @param groovyTemplate the Groovy template
     */
    public void addTemplate(String module,
                            String templateKey,
                            String path,
                            RockerModel javaTemplate,
                            RockerModel kotlinTemplate,
                            RockerModel groovyTemplate) {
        RockerModel rockerModel = parseModel(javaTemplate, kotlinTemplate, groovyTemplate);
        addTemplateInternal(templateKey, new RockerTemplate(module, path, rockerModel));
    }

    /**
     * Register a test template to be rendered for the specified module.
     * Pass templates for a test class for all 5 language/framework combinations,
     * and the one corresponding to the user-selected language and test
     * framework will be used.
     *
     * @param module      the module
     * @param templateKey the template name (map key, must be unique)
     * @param path        the file location
     * @param spock       the Spock test model
     * @param javaJunit   the Java/JUnit test model
     * @param groovyJunit the Groovy/JUnit test model
     * @param kotlinJunit the Kotlin/JUnit test model
     * @param kotest      the Kotest test model
     */
    public void addTestTemplate(String module,
                                String templateKey,
                                String path,
                                RockerModel spock,
                                RockerModel javaJunit,
                                RockerModel groovyJunit,
                                RockerModel kotlinJunit,
                                RockerModel kotest) {

        RockerModel rockerModel = new DefaultTestRockerModelProvider(spock, javaJunit, groovyJunit, kotlinJunit, kotest)
                .findModel(getLanguage(), getTestFramework());
        if (rockerModel != null) {
            addTemplateInternal(templateKey, new RockerTemplate(module, path, rockerModel));
        }
    }

    /**
     * Register a test helper class template to be rendered for the specified
     * module. Pass templates for all 3 languages, and the one corresponding to
     * the user-selected language will be used.
     *
     * @param module         the module
     * @param templateKey    the template name (map key, must be unique)
     * @param path           the file location
     * @param javaTemplate   the Java template
     * @param kotlinTemplate the Kotlin template
     * @param groovyTemplate the Groovy template
     */
    public void addTestHelperTemplate(String module,
                                      String templateKey,
                                      String path,
                                      RockerModel javaTemplate,
                                      RockerModel kotlinTemplate,
                                      RockerModel groovyTemplate) {

        RockerModel rockerModel = parseModel(javaTemplate, kotlinTemplate, groovyTemplate);
        addTemplateInternal(templateKey, new RockerTemplate(
                module,
                getLanguage().getTestSrcDir() + path + '.' + getLanguage().getExtension(),
                rockerModel));
    }

    /**
     * Register a URLTemplate.
     *
     * @param module       the module
     * @param templateKey  the template name (map key, must be unique)
     * @param path         the file location
     * @param resourcePath the location of the resource to load
     */
    public void addUrlTemplate(String module,
                               String templateKey,
                               String path,
                               String resourcePath) {
        addTemplate(templateKey, new URLTemplate(module, path,
                Thread.currentThread().getContextClassLoader().getResource(resourcePath)));
    }

    /**
     * @param language the language
     * @return the language-specific ApplicationRenderingContext
     */
    public ApplicationRenderingContext getApplicationRenderingContext(Language language) {
        boolean eagerInitSingleton = getFeatures().isFeaturePresent(RequireEagerSingletonInitializationFeature.class);
        return switch (language) {
            case JAVA -> new JavaApplicationRenderingContext(null, eagerInitSingleton);
            case GROOVY -> new GroovyApplicationRenderingContext(null, eagerInitSingleton);
            case KOTLIN -> new KotlinApplicationRenderingContext(null, eagerInitSingleton);
        };
    }

    private Project createProject(GdkCloud cloud, String originalModuleName) {
        return NameUtils.parse(getLibProject().getPackageName() + '.' + originalModuleName + "-" + cloud.getModuleName());
    }

    private Set<Feature> libFeatures() {
        return super.getFeatures().getFeatures().stream().filter(f -> {
            if (f instanceof GdkFeature) {
                return false;
            }

            if (f instanceof BuildPluginFeature || f instanceof TestResources) {
                return true;
            }

            return featureContext.getAddedFeatureClouds(f).isEmpty();
        }).collect(Collectors.toSet());
    }

    /**
     * Workaround for <a href="https://github.com/gradle/gradle/issues/17559">this Gradle bug</a>.
     * This is called from the buildSrc/build.gradle template to lookup the Maven G/A/V
     * coordinates for all plugins in all modules.
     *
     * @return the GAV strings, e.g. "io.micronaut.gradle:micronaut-test-resources-plugin:3.7.7"
     */
    public Set<String> getBuildPluginsGAV() {

        Set<BuildPlugin> allPlugins = new HashSet<>(super.getBuildPlugins()); // lib
        for (Set<BuildPlugin> cloudPlugins : buildPlugins.values()) {
            allPlugins.addAll(cloudPlugins);
        }

        Set<String> gavs = new HashSet<>();
        for (BuildPlugin p : allPlugins) {
            GradlePlugin plugin = (GradlePlugin) p;
            if (plugin.getVersion() == null) {
                // 'groovy', etc.
                continue;
            }
            String key = plugin.getId() + ':' + plugin.getVersion();
            String gav = PLUGIN_GAVS.get(key);

            if (gav == null) {
                throw new IllegalStateException("Unexpected Gradle build plugin or version mismatch for '" + key + "'");
            }

            if (key.contains(GdkJTE.JTE_GROUP_ID)) {
                gavs.add(GdkJTE.JTE_GROUP_ID + ":" + GdkJTE.JTE_NATIVE_RESOURCES + ":" + plugin.getVersion());
            }

            gavs.add(gav);
        }

        return gavs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GdkGeneratorContext:\n");

        sb.append("\n   ApplicationType: ").append(getApplicationType());
        sb.append("\n   Language: ").append(StringUtils.capitalize(getLanguage().getName()));
        sb.append("\n   BuildTool: ").append(getBuildTool().getTitle());
        sb.append("\n   TestFramework: ").append(getTestFramework().getTitle());
        sb.append("\n   JdkVersion: ").append(getJdkVersion().majorVersion());
        sb.append("\n   OperatingSystem: ").append(getOperatingSystem()).append('\n');

        sb.append("\n   Templates:").append(toString(getTemplates())).append('\n');
        sb.append("\n   Module Names: ").append(getModuleNames()).append('\n');

        sb.append("\n   Lib Configuration: ").append(getLibConfiguration()).append('\n');
        sb.append("\n   App Configurations: ").append(getApplicationConfigurations()).append('\n');
        sb.append("\n   Cloud App Configurations: ").append(getCloudApplicationConfigurations()).append('\n');
        sb.append("\n   Cloud App Env Configurations: ").append(getCloudApplicationEnvConfigurations()).append('\n');

        sb.append("\n   Lib Bootstrap Configuration: ").append(getLibBootstrapConfiguration()).append('\n');
        sb.append("\n   Bootstrap Configurations: ").append(getBootstrapConfigurations()).append('\n');
        sb.append("\n   Cloud Bootstrap Configurations: ").append(getCloudBootstrapConfigurations()).append('\n');
        sb.append("\n   Cloud Bootstrap Env Configurations: ").append(getCloudBootstrapEnvConfigurations()).append('\n');

        sb.append("\n   Lib Dependencies: ").append(toString(getLibDependencies())).append('\n');

        Map<GdkCloud, String> cloudDependencies = new HashMap<>();
        for (Map.Entry<GdkCloud, DependencyContext> e : getCloudDependencyContexts().entrySet()) {
            cloudDependencies.put(e.getKey(), toString(e.getValue().getDependencies()));
        }
        sb.append("\n   Cloud Dependencies: ").append(cloudDependencies).append('\n');

        sb.append("\n   Lib Project: ").append(getLibProject().getProperties()).append('\n');
        Map<GdkCloud, String> cloudProjects = new HashMap<>();
        for (Map.Entry<GdkCloud, Project> e : getCloudProjects().entrySet()) {
            cloudProjects.put(e.getKey(), e.getValue().getProperties().toString());
        }
        sb.append("\n   Cloud Projects: ").append(cloudProjects).append('\n');

        sb.append("\n   Lib Features: ").append(getLibFeatures()).append('\n');
        sb.append("\n   Cloud Features: ").append(getCloudFeatures()).append('\n');

        sb.append("\n   Lib BuildProperties: ").append(toString(getBuildProperties().getProperties())).append('\n');
        Map<GdkCloud, String> cloudBuildProps = new HashMap<>();
        for (GdkCloud cloud : cloudFeatures.keySet()) {
            cloudBuildProps.put(cloud, toString(getBuildProperties().getProperties(cloud)));
        }
        sb.append("\n   Cloud BuildProperties: ").append(cloudBuildProps).append('\n');

        sb.append("\n   Lib Build Plugins: ").append(toString(super.getBuildPlugins())).append('\n');
        Map<GdkCloud, String> cloudPlugins = new HashMap<>();
        for (Map.Entry<GdkCloud, Set<BuildPlugin>> e : buildPlugins.entrySet()) {
            cloudPlugins.put(e.getKey(), toString(e.getValue()));
        }
        sb.append("\n   Cloud Build Plugins: ").append(cloudPlugins).append('\n');

        sb.append("\n   PostProcessors: ").append(postProcessors).append('\n');

        return sb.toString();
    }

    private String toString(Map<String, Template> templates) {
        StringBuilder sb = new StringBuilder();
        List<String> keys = new ArrayList<>(templates.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            Template t = templates.get(key);
            String className = t.getClass().getSimpleName();
            if (className.equals("")) { // anonymous inner class
                className = t.getClass().getName().substring(t.getClass().getName().lastIndexOf('.') + 1);
            }
            sb.append("\n      ").append(className).append("(").append(key).append(" -> ").append(t.getPath()).append(")");
        }
        return sb.toString();
    }

    private String toString(Collection<Dependency> dependencies) {
        return dependencies.stream()
                .map(d -> d.getGroupId() + ':' + d.getArtifactId())
                .collect(Collectors.toSet())
                .toString();
    }

    private String toString(List<Property> buildProperties) {
        Map<String, String> map = new LinkedHashMap<>();
        for (Property property : buildProperties) {
            map.put(property.getKey(), property.getValue());
        }
        return map.toString();
    }

    private String toString(Set<BuildPlugin> plugins) {
        return plugins.stream()
                .map(p -> p instanceof GradlePlugin ? ((GradlePlugin) p).getId() : ((MavenPlugin) p).getArtifactId())
                .collect(Collectors.toSet())
                .toString();
    }
}
