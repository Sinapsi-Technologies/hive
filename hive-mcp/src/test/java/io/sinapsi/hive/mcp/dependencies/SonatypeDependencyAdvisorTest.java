package io.sinapsi.hive.mcp.dependencies;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sinapsi.hive.mcp.dependencies.application.SecurityAssessment;
import io.sinapsi.hive.mcp.dependencies.domain.ArtifactCoordinates;
import io.sinapsi.hive.mcp.dependencies.domain.ArtifactVersion;
import io.sinapsi.hive.mcp.dependencies.infrastructure.SonatypeDependencyAdvisor;
import io.sinapsi.hive.mcp.dependencies.infrastructure.SonatypeSecurityProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SonatypeDependencyAdvisorTest {

    @Test
    void mapsSonatypeVulnerabilitiesAndFailsAtConfiguredSeverity() throws Exception {
        SonatypeSecurityProperties properties = new SonatypeSecurityProperties();
        properties.setEnabled(true);
        properties.setFailSeverity("HIGH");

        SonatypeDependencyAdvisor advisor = new SonatypeDependencyAdvisor(
                dependency -> """
                        {"vulnerabilities":[
                          {"identifier":"CVE-2026-0001","severity":"HIGH","fixedVersion":"2.0.1","url":"https://example.test/CVE-2026-0001"}
                        ]}
                        """,
                new ObjectMapper(),
                properties);

        SecurityAssessment assessment = advisor.assess(
                new ArtifactVersion(new ArtifactCoordinates("com.example", "demo"), "2.0.0"));

        assertEquals(SecurityAssessment.Status.FAIL, assessment.status());
        assertEquals("CVE-2026-0001", assessment.advisories().getFirst().identifier());
        assertEquals("2.0.1", assessment.advisories().getFirst().fixedVersion());
    }
}
