package io.sinapsi.hive.mcp.dependencies;

import io.sinapsi.hive.mcp.dependencies.application.ArtifactCatalog;
import io.sinapsi.hive.mcp.dependencies.application.DependencySecurityAdvisor;
import io.sinapsi.hive.mcp.dependencies.application.SecurityAssessment;
import io.sinapsi.hive.mcp.dependencies.domain.ArtifactCoordinates;
import io.sinapsi.hive.mcp.dependencies.domain.ArtifactVersion;
import io.sinapsi.hive.commons.model.HiveConfig;
import io.sinapsi.hive.commons.project.HiveConfigLoader;
import io.sinapsi.hive.commons.project.MavenPomUpdater;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.nio.file.Path;
import java.util.Map;
import java.util.LinkedHashMap;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import io.sinapsi.hive.mcp.project.HiveProjectContext;
import io.sinapsi.hive.mcp.workflow.HiveWorkflowGate;

@Component
public final class HiveDependencyTools {

    private final ArtifactCatalog catalog;

    private final List<DependencySecurityAdvisor> advisors;

    private final HiveProjectContext projectContext;


    private final MavenPomUpdater pomUpdater;

    private final HiveConfigLoader configs;

    private final DependencyChangeVerifier verifier;

    private final HiveWorkflowGate workflow;

    public HiveDependencyTools(ArtifactCatalog catalog) {
        this(catalog, List.of(), new HiveProjectContext(".", ""), new MavenPomUpdater(), new HiveConfigLoader(),
                path -> DependencyChangeVerifier.VerificationResult.skipped(), new HiveWorkflowGate());
    }

    public HiveDependencyTools(ArtifactCatalog catalog, Path projectRoot) {
        this(catalog, List.of(), new HiveProjectContext(projectRoot.toString(), ""), new MavenPomUpdater(), new HiveConfigLoader(),
                path -> DependencyChangeVerifier.VerificationResult.skipped(), new HiveWorkflowGate());
    }

    public HiveDependencyTools(ArtifactCatalog catalog, Path projectRoot, DependencyChangeVerifier verifier) {
        this(catalog, List.of(), new HiveProjectContext(projectRoot.toString(), ""), new MavenPomUpdater(), new HiveConfigLoader(), verifier,
                new HiveWorkflowGate());
    }

    @Autowired
    public HiveDependencyTools(
            ArtifactCatalog catalog,
            ObjectProvider<DependencySecurityAdvisor> advisors,
            HiveProjectContext projectContext,
            DependencyChangeVerifier verifier,
            HiveWorkflowGate workflow) {
        this(catalog, advisors.orderedStream().toList(), projectContext, new MavenPomUpdater(), new HiveConfigLoader(),
                verifier, workflow);
    }

    private HiveDependencyTools(
            ArtifactCatalog catalog,
            List<DependencySecurityAdvisor> advisors,
            HiveProjectContext projectContext,
            MavenPomUpdater pomUpdater,
            HiveConfigLoader configs,
            DependencyChangeVerifier verifier,
            HiveWorkflowGate workflow) {
        this.catalog = catalog;
        this.advisors = List.copyOf(advisors);
        this.projectContext = projectContext;
        this.pomUpdater = pomUpdater;
        this.configs = configs;
        this.verifier = verifier;
        this.workflow = workflow;
    }

    @McpTool(
            name = "hive_search_dependency",
            description = "Search Maven artifacts through the configured catalog. Use this before adding a dependency when the exact groupId, artifactId, or latest published version is unknown.")
    public List<ArtifactVersion> searchDependency(String query) throws IOException, InterruptedException {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
        }

        return catalog.search(query);
    }

    @McpTool(
            name = "hive_list_dependency_versions",
            description = "List published versions for a Maven coordinate. Use it to compare available versions before asking Sonatype or another security advisor which one is appropriate.")
    public List<ArtifactVersion> listDependencyVersions(String groupId, String artifactId)
            throws IOException, InterruptedException {
        return catalog.versions(new ArtifactCoordinates(groupId, artifactId));
    }

    @McpTool(
            name = "hive_check_dependency",
            description = "Assess a Maven dependency with the configured security advisors, such as Sonatype Guide. Use it after selecting a version and before modifying pom.xml; an empty advisor list means no remote security provider is enabled.")
    public List<SecurityAssessment> checkDependency(String groupId, String artifactId, String version)
            throws IOException, InterruptedException {
        ArtifactVersion dependency = new ArtifactVersion(
                new ArtifactCoordinates(groupId, artifactId), version);
        return advisors.stream().map(advisor -> assess(advisor, dependency)).toList();
    }

    @McpTool(
            name = "hive_add_dependency",
            description = "Add a pinned Maven dependency to the selected project's pom.xml. Use hive_search_dependency and hive_check_dependency first. verify=true runs Maven tests and OSV-Scanner and rolls back on failure; verify=false is explicitly supported when the caller wants an unverified POM edit and returns build/security as NOT_RUN.")
    public Map<String, Object> addDependency(
            String groupId,
            String artifactId,
            String version,
            String scope,
            boolean verify) throws IOException, InterruptedException {
        validateCoordinate(groupId, "groupId");
        validateCoordinate(artifactId, "artifactId");
        validateCoordinate(version, "version");
        String normalizedScope = scope == null || scope.isBlank() ? "compile" : scope.trim().toLowerCase();
        if (!List.of("compile", "provided", "runtime", "test").contains(normalizedScope)) {
            throw new IllegalArgumentException("Unsupported Maven scope: " + scope);
        }

        Path projectRoot = projectContext.current();
        workflow.requireDevelopment(projectRoot);
        HiveConfig config = configs.load(projectRoot);
        Path pom = projectRoot.resolve("pom.xml");
        Path backup = Files.createTempFile("hive-pom-", ".xml");
        boolean hadPom = Files.exists(pom);
        if (hadPom) {
            Files.copy(pom, backup, StandardCopyOption.REPLACE_EXISTING);
        }
        boolean added = pomUpdater.ensureDependency(projectRoot, groupId.trim(), artifactId.trim(), version.trim(),
                "compile".equals(normalizedScope) ? null : normalizedScope);
        DependencyChangeVerifier.VerificationResult verification;
        try {
            verification = verify
                    ? verifier.verify(projectRoot)
                    : DependencyChangeVerifier.VerificationResult.skipped();
        } catch (IOException exception) {
            verification = new DependencyChangeVerifier.VerificationResult(
                    "ERROR", "ERROR", false, exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            verification = new DependencyChangeVerifier.VerificationResult(
                    "INTERRUPTED", "NOT_RUN", false, "Dependency verification interrupted");
        }
        boolean rolledBack = added && verify && !verification.passed();
        if (rolledBack) {
            if (hadPom) {
                Files.copy(backup, pom, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.deleteIfExists(pom);
            }
        }
        Files.deleteIfExists(backup);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectRoot", projectRoot.toString());
        result.put("pom", projectRoot.resolve("pom.xml").toString());
        result.put("dependency", groupId.trim() + ":" + artifactId.trim() + ":" + version.trim());
        result.put("scope", normalizedScope);
        result.put("added", added);
        result.put("verify", verify);
        result.put("build", verification.build());
        result.put("security", verification.security());
        result.put("rolledBack", rolledBack);
        result.put("verificationError", verification.error());
        result.put("basePackage", config.basePackage());
        return result;
    }

    public Map<String, Object> addDependency(
            String groupId, String artifactId, String version, String scope) throws IOException {
        try {
            return addDependency(groupId, artifactId, version, scope, false);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Dependency verification interrupted", exception);
        }
    }

    private void validateCoordinate(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    private SecurityAssessment assess(DependencySecurityAdvisor advisor, ArtifactVersion dependency) {
        try {
            return advisor.assess(dependency);
        } catch (IOException exception) {
            return new SecurityAssessment(
                    advisor.provider(),
                    SecurityAssessment.Status.UNKNOWN,
                    List.of(),
                    "Security provider error: " + exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new SecurityAssessment(
                    advisor.provider(),
                    SecurityAssessment.Status.UNKNOWN,
                    List.of(),
                    "Security provider interrupted");
        }
    }
}
