package cloud.graal.gcn.dependencies

import io.micronaut.build.catalogs.internal.LenientVersionCatalogParser
import io.micronaut.build.catalogs.internal.Library
import io.micronaut.build.catalogs.internal.RichVersion
import io.micronaut.build.catalogs.internal.Status
import io.micronaut.build.catalogs.internal.VersionCatalogTomlModel
import io.micronaut.build.catalogs.internal.VersionModel
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.result.ResolutionResult
import org.gradle.api.artifacts.result.UnresolvedDependencyResult
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class VersionCatalogUpdate extends DefaultTask {

    @InputDirectory
    abstract DirectoryProperty getCatalogsDirectory()

    @OutputDirectory
    abstract DirectoryProperty getOutputDirectory()

    @Input
    abstract ListProperty<String> getRejectedQualifiers()

    @Input
    abstract SetProperty<String> getIgnoredModules()

    @Input
    abstract Property<Boolean> getAllowMajorUpdates()

    @Input
    abstract Property<Boolean> getAllowMinorUpdates()

    private static final Pattern ORACLE_VERSION_PATTERN = Pattern.compile(".+-oracle-\\d+")

    private static final Pattern VERSION_PATTERN = Pattern.compile("(version\\s*=\\s*[\"'])(.+?)([\"'])")

    @TaskAction
    void updateCatalogs() throws IOException, InterruptedException {
        Set<File> catalogs = getCatalogsDirectory().getAsFileTree()
                .matching { pattern -> pattern.include("*.versions.toml") }
                .getFiles()
        File outputDir = getOutputDirectory().getAsFile().get()
        File logFile = getOutputDirectory().file("updates.log").get().getAsFile()
        if (outputDir.isDirectory() || outputDir.mkdirs()) {
            if (catalogs.isEmpty()) {
                getLogger().info("Didn't find any version catalog to process")
            }
            for (File catalog : catalogs) {
                getLogger().info("Processing {}", catalog)
                updateCatalog(catalog, new File(outputDir, catalog.getName()), logFile)
            }
        } else {
            throw new GradleException("Unable to create output directory " + outputDir)
        }
    }

    static boolean supportsUpdate(RichVersion richVersion) {
        return richVersion != null
                && richVersion.getRequire() != null
                && richVersion.getStrictly() == null
                && richVersion.getPrefer() == null
                && !richVersion.isRejectAll()
                && richVersion.getRejectedVersions() == null
    }

    void updateCatalog(File inputCatalog, File outputCatalog, File logFile) throws IOException, InterruptedException {
        try (PrintWriter log = newPrintWriter(logFile)) {
            log.println("Processing catalog file " + inputCatalog)
            LenientVersionCatalogParser parser = new LenientVersionCatalogParser()
            try (FileInputStream is = new FileInputStream(inputCatalog)) {
                parser.parse(is)
            }
            List<String> lines = Files.readAllLines(inputCatalog.toPath(), StandardCharsets.UTF_8)
            boolean allowMajorUpdate = getAllowMajorUpdates().get()
            boolean allowMinorUpdate = getAllowMajorUpdates().get()
            VersionCatalogTomlModel model = parser.getModel()
            DependencyHandler dependencies = getProject().getDependencies()
            ConfigurationContainer configurations = getProject().getConfigurations()
            Configuration detachedConfiguration = configurations.detachedConfiguration()
            detachedConfiguration.setCanBeConsumed(false)
            detachedConfiguration.setCanBeResolved(true)
            detachedConfiguration.setTransitive(false)
            detachedConfiguration.getResolutionStrategy()
                    .cacheDynamicVersionsFor(0, TimeUnit.MINUTES)
            List<Pattern> rejectedQualifiers = getRejectedQualifiers().get()
                    .stream()
                    .map { qualifier -> Pattern.compile("(?i).*[.-]" + qualifier + "[.\\d-+]*") }
                    .toList()
            detachedConfiguration.getResolutionStrategy().getComponentSelection().all { rules ->
                ModuleComponentIdentifier candidateModule = rules.getCandidate()
                String candidateVersion = candidateModule.getVersion()
                def micronautIgnoreGroups = Set.of(
                        "io.micronaut.build",
                        "io.micronaut.build.internal.bom",
                        "io.micronaut.gradle",
                        "io.micronaut.platform",
                        "io.micronaut.starter",
                        "io.micronaut.maven",
                        "io.micronaut.testresources"
                )

                if (candidateModule.getGroup().contains("io.micronaut") && !micronautIgnoreGroups.contains(candidateModule.getGroup()) && !VersionCatalogUpdate.ORACLE_VERSION_PATTERN.matcher(candidateVersion).find()) {
                    rules.reject("Rejecting micronaut module not build at oracle")
                    log.println("Rejecting " + candidateModule.getModuleIdentifier() + " version " + candidateVersion)
                }
                Set<String> modulesToAllowAlpha = Set.of("google-cloud-logging-logback", "opentelemetry-semconv")
                rejectedQualifiers.forEach { qualifier ->
                    if (qualifier.matcher(candidateVersion).find()) {
                        if (!modulesToAllowAlpha.contains(candidateModule.getModule()) || !candidateVersion.contains("alpha")) {
                            rules.reject("Rejecting qualifier " + qualifier)
                            log.println("Rejecting " + candidateModule.getModuleIdentifier() + " version " + candidateVersion + " because of qualifier '" + qualifier + "'")
                        }
                    }
                }
                if (!allowMajorUpdate || !allowMinorUpdate) {
                    model.findLibrary(
                            candidateModule.getGroup(), candidateModule.getModule()
                    ).ifPresent { library ->
                        VersionModel version = library.getVersion()
                        if (version.getReference() != null) {
                            version = model.findVersion(version.getReference()).orElse(null)
                        }
                        if (version != null) {
                            String required = version.getVersion().getRequire()
                            if (required != null) {
                                if (!allowMajorUpdate) {
                                    String major = majorVersionOf(required)
                                    String candidateMajor = majorVersionOf(candidateVersion)
                                    if (major != candidateMajor) {
                                        rules.reject("Rejecting major version " + candidateMajor)
                                        log.println("Rejecting " + candidateModule.getModuleIdentifier() + " version " + candidateVersion + " because it's not the same major version")
                                    }
                                }
                                if (!allowMinorUpdate) {
                                    String minor = minorVersionOf(required)
                                    String candidateMinor = minorVersionOf(candidateVersion)
                                    if (minor != candidateMinor) {
                                        rules.reject("Rejecting minor version " + candidateMinor)
                                        log.println("Rejecting " + candidateModule.getModuleIdentifier() + " version " + candidateVersion + " because it's not the same minor version")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Set<String> ignoredModules = getIgnoredModules().get()
            model.librariesTable
                    .findAll { library -> !ignoredModules.contains(library.module) }
                    .findAll { library -> library.version.reference != null || !requiredVersionOf(library).isEmpty() }
                    .each { library -> detachedConfiguration.dependencies.add(VersionCatalogUpdate.requirePom(dependencies, library)) }

            ResolutionResult resolutionResult = detachedConfiguration.getIncoming()
                    .getResolutionResult()
            resolutionResult
                    .allComponents { result ->
                        ModuleVersionIdentifier mid = result.getModuleVersion()
                        String latest = mid.getVersion()
                        Status targetStatus = Status.detectStatus(latest)
                        log.println("Latest release of " + mid.getModule() + " is " + latest + " (status " + targetStatus + ")")
                        model.findLibrary(mid.getGroup(), mid.getName()).ifPresent { library ->
                            VersionModel version = library.getVersion()
                            String reference = version.getReference()
                            if (reference != null) {
                                model.findVersion(reference).ifPresent { referencedVersion ->
                                    RichVersion richVersion = referencedVersion.getVersion()
                                    if (supportsUpdate(richVersion)) {
                                        String require = richVersion.getRequire()
                                        Status sourceStatus = Status.detectStatus(require)
                                        if (!Objects.equals(require, latest) && targetStatus.isAsStableOrMoreStableThan(sourceStatus)) {
                                            log.println("Updating required version from " + require + " to " + latest)
                                            String lookup = "(" + reference + "\\s*=\\s*[\"'])(.+?)([\"'])"
                                            int lineNb = referencedVersion.getPosition().line() - 1
                                            String line = lines.get(lineNb)
                                            Matcher m = Pattern.compile(lookup).matcher(line)
                                            if (m.find()) {
                                                lines.set(lineNb, m.replaceAll('$1' + latest + '$3'))
                                            } else {
                                                log.println("Line " + lineNb + " contains unsupported notation, automatic updating failed")
                                            }
                                        }
                                    } else {
                                        log.println("Version '" + reference + "' uses a notation which is not supported for automatic upgrades yet.")
                                    }
                                }
                            } else {
                                int lineNb = library.getPosition().line() - 1
                                String line = lines.get(lineNb)
                                Matcher m = VersionCatalogUpdate.VERSION_PATTERN.matcher(line)
                                if (m.find()) {
                                    lines.set(lineNb, m.replaceAll('$1' + latest + '$3'))
                                } else {
                                    def lookup = "(\\s*=\\s*[\"'])(" + library.getGroup() + "):("+ library.getName()+"):(.+?)([\"'])"
                                    m = Pattern.compile(lookup).matcher(line)
                                    if (m.find()) {
                                        lines.set(lineNb, m.replaceAll('$1$2:$3:' + latest + '$5'))
                                    } else {
                                        log.println("Line " + lineNb + " contains unsupported notation, automatic updating failed")
                                    }
                                }
                            }
                        }
                    }
            getLogger().lifecycle("Writing updated catalog at " + outputCatalog)
            try (PrintWriter writer = newPrintWriter(outputCatalog)) {
                lines.forEach(writer::println)
            }
            def errors = resolutionResult.allDependencies
                    .findAll { it instanceof UnresolvedDependencyResult }
                    .collect { r ->
                        log.println("Unresolved dependency ${r.attempted.displayName}")
                        log.println("   reason ${r.attemptedReason}")
                        log.println("   failure")
                        r.failure.printStackTrace(log)
                        "\n    - ${r.attempted.displayName} -> ${r.failure.message}"
                    }
            if (!errors.isEmpty()) {
                throw new GradleException("Some modules couldn't be updated because of the following reasons:" + errors)
            }
        }
    }

    private static String requiredVersionOf(Library library) {
        RichVersion version = library.getVersion().getVersion()
        if (version != null) {
            String require = version.getRequire()
            if (require != null) {
                return require
            }
        }
        return ""
    }

    static Dependency requirePom(DependencyHandler dependencies, Library library) {
        ExternalModuleDependency dependency = (ExternalModuleDependency) dependencies.create(library.getGroup() + ":" + library.getName() + ":+")
        dependency.artifact { artifact -> artifact.setType("pom") }
        return dependency
    }

    static PrintWriter newPrintWriter(File file) throws FileNotFoundException {
        return new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))
    }

    static String majorVersionOf(String version) {
        return version.contains(".") ? version.split("\\.")[0] : version
    }

    static String minorVersionOf(String version) {
        return version.contains(".") ? version.split("\\.")[1] : version
    }
}
