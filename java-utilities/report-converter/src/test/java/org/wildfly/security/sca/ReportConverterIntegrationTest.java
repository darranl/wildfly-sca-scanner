/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.sca;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.wildfly.security.sca.reportconverter.generator.GenericReportGenerator;
import org.wildfly.security.sca.reportconverter.model.generic.GenericReport;
import org.wildfly.security.sca.reportconverter.model.owasp.OwaspReport;
import org.wildfly.security.sca.reportconverter.parser.OwaspReportParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete report conversion workflow.
 * Tests the end-to-end process: parse OWASP → convert → validate generic format.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
class ReportConverterIntegrationTest {

    private OwaspReportParser parser;
    private GenericReportGenerator generator;
    private ObjectMapper objectMapper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        parser = new OwaspReportParser();
        generator = new GenericReportGenerator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testEndToEndConversionMinimalReport() throws IOException {
        // Given: A minimal OWASP report
        Path inputPath = getTestResourcePath("minimal-report.json");
        Path outputPath = tempDir.resolve("generic-minimal.json");
        String wildflyVersion = "39.0.1.Final";

        // When: Parse, convert, and write
        OwaspReport owaspReport = parser.parse(inputPath);
        parser.validate(owaspReport);
        GenericReport genericReport = generator.convert(owaspReport, wildflyVersion);
        generator.writeToFile(genericReport, outputPath);

        // Then: Verify output file exists and is valid
        assertTrue(Files.exists(outputPath), "Output file should exist");
        assertTrue(Files.size(outputPath) > 0, "Output file should not be empty");

        // Verify JSON structure
        JsonNode root = objectMapper.readTree(outputPath.toFile());
        assertTrue(root.has("schemaVersion"), "Should have schemaVersion");
        assertTrue(root.has("metadata"), "Should have metadata");
        assertTrue(root.has("vulnerabilities"), "Should have vulnerabilities");
        assertTrue(root.has("summary"), "Should have summary");

        // Verify metadata
        JsonNode metadata = root.get("metadata");
        assertEquals(wildflyVersion, metadata.get("wildflyVersion").asText(),
                "WildFly version should match");
        assertTrue(metadata.has("scanDate"), "Should have scan date");
        assertEquals(1, metadata.get("totalDependencies").asInt(),
                "Should have 1 dependency");

        // Verify vulnerabilities array
        JsonNode vulnerabilities = root.get("vulnerabilities");
        assertTrue(vulnerabilities.isArray(), "Vulnerabilities should be an array");
        assertEquals(1, vulnerabilities.size(), "Should have 1 vulnerability");

        // Verify vulnerability structure
        JsonNode vuln = vulnerabilities.get(0);
        assertTrue(vuln.has("id"), "Vulnerability should have id");
        assertTrue(vuln.has("severity"), "Vulnerability should have severity");
        assertTrue(vuln.has("cvssScore"), "Vulnerability should have cvssScore");
        assertTrue(vuln.has("affectedPackages"), "Vulnerability should have affectedPackages");

        // Verify summary
        JsonNode summary = root.get("summary");
        assertEquals(1, summary.get("active").asInt(), "Should have 1 active");
        assertEquals(0, summary.get("suppressed").asInt(), "Should have 0 suppressed");
    }

    @Test
    void testEndToEndConversionWithSuppressed() throws IOException {
        // Given: A report with suppressed vulnerabilities
        Path inputPath = getTestResourcePath("report-with-suppressed.json");
        Path outputPath = tempDir.resolve("generic-suppressed.json");
        String wildflyVersion = "38.0.1.Final";

        // When: Convert including suppressed vulnerabilities
        OwaspReport owaspReport = parser.parse(inputPath);
        GenericReport genericReport = generator.convert(owaspReport, wildflyVersion, false);
        generator.writeToFile(genericReport, outputPath);

        // Then: Verify both active and suppressed are present
        JsonNode root = objectMapper.readTree(outputPath.toFile());
        JsonNode vulnerabilities = root.get("vulnerabilities");
        assertEquals(3, vulnerabilities.size(), "Should have 3 total vulnerabilities");

        // Count suppressed vs active
        int suppressedCount = 0;
        int activeCount = 0;
        for (JsonNode vuln : vulnerabilities) {
            if (vuln.get("suppressed").asBoolean()) {
                suppressedCount++;
                assertTrue(vuln.has("suppressionReason"),
                        "Suppressed vulnerability should have reason");
            } else {
                activeCount++;
            }
        }

        assertEquals(2, activeCount, "Should have 2 active vulnerabilities");
        assertEquals(1, suppressedCount, "Should have 1 suppressed vulnerability");

        // Verify summary matches
        JsonNode summary = root.get("summary");
        assertEquals(2, summary.get("active").asInt(), "Summary should show 2 active");
        assertEquals(1, summary.get("suppressed").asInt(), "Summary should show 1 suppressed");
    }

    @Test
    void testEndToEndConversionSkipSuppressed() throws IOException {
        // Given: A report with suppressed vulnerabilities
        Path inputPath = getTestResourcePath("report-with-suppressed.json");
        Path outputPath = tempDir.resolve("generic-skip-suppressed.json");
        String wildflyVersion = "38.0.1.Final";

        // When: Convert skipping suppressed vulnerabilities
        OwaspReport owaspReport = parser.parse(inputPath);
        GenericReport genericReport = generator.convert(owaspReport, wildflyVersion, true);
        generator.writeToFile(genericReport, outputPath);

        // Then: Verify only active vulnerabilities are present
        JsonNode root = objectMapper.readTree(outputPath.toFile());
        JsonNode vulnerabilities = root.get("vulnerabilities");
        assertEquals(2, vulnerabilities.size(), "Should have 2 vulnerabilities (suppressed skipped)");

        // Verify none are suppressed
        for (JsonNode vuln : vulnerabilities) {
            assertFalse(vuln.get("suppressed").asBoolean(),
                    "No vulnerabilities should be marked as suppressed");
        }

        // Verify summary
        JsonNode summary = root.get("summary");
        assertEquals(2, summary.get("active").asInt(), "Summary should show 2 active");
        assertEquals(0, summary.get("suppressed").asInt(), "Summary should show 0 suppressed");
    }

    @Test
    void testEndToEndConversionNoVulnerabilities() throws IOException {
        // Given: A clean report with no vulnerabilities
        Path inputPath = getTestResourcePath("report-no-vulnerabilities.json");
        Path outputPath = tempDir.resolve("generic-clean.json");
        String wildflyVersion = "39.0.1.Final";

        // When: Convert
        OwaspReport owaspReport = parser.parse(inputPath);
        GenericReport genericReport = generator.convert(owaspReport, wildflyVersion);
        generator.writeToFile(genericReport, outputPath);

        // Then: Verify empty vulnerabilities array
        JsonNode root = objectMapper.readTree(outputPath.toFile());
        JsonNode vulnerabilities = root.get("vulnerabilities");
        assertTrue(vulnerabilities.isArray(), "Vulnerabilities should be an array");
        assertEquals(0, vulnerabilities.size(), "Should have 0 vulnerabilities");

        // Verify metadata shows no vulnerable dependencies
        JsonNode metadata = root.get("metadata");
        assertEquals(2, metadata.get("totalDependencies").asInt(),
                "Should have 2 total dependencies");
        assertEquals(0, metadata.get("vulnerableDependencies").asInt(),
                "Should have 0 vulnerable dependencies");

        // Verify summary is all zeros
        JsonNode summary = root.get("summary");
        assertEquals(0, summary.get("active").asInt(), "Should have 0 active");
        assertEquals(0, summary.get("suppressed").asInt(), "Should have 0 suppressed");
        assertEquals(0, summary.get("critical").asInt(), "Should have 0 critical");
        assertEquals(0, summary.get("high").asInt(), "Should have 0 high");
        assertEquals(0, summary.get("medium").asInt(), "Should have 0 medium");
        assertEquals(0, summary.get("low").asInt(), "Should have 0 low");
    }

    @Test
    void testEndToEndConversionMultipleCvss() throws IOException {
        // Given: A report with multiple CVSS versions
        Path inputPath = getTestResourcePath("report-multiple-cvss.json");
        Path outputPath = tempDir.resolve("generic-multi-cvss.json");
        String wildflyVersion = "39.0.1.Final";

        // When: Convert
        OwaspReport owaspReport = parser.parse(inputPath);
        GenericReport genericReport = generator.convert(owaspReport, wildflyVersion);
        generator.writeToFile(genericReport, outputPath);

        // Then: Verify CVSS scores and vectors are present
        JsonNode root = objectMapper.readTree(outputPath.toFile());
        JsonNode vulnerabilities = root.get("vulnerabilities");
        assertEquals(4, vulnerabilities.size(), "Should have 4 vulnerabilities");

        // Verify each has CVSS score and vector
        for (JsonNode vuln : vulnerabilities) {
            assertTrue(vuln.has("cvssScore"), "Should have CVSS score");
            assertFalse(vuln.get("cvssScore").isNull(), "CVSS score should not be null");
            assertTrue(vuln.get("cvssScore").asDouble() > 0, "CVSS score should be positive");

            assertTrue(vuln.has("cvssVector"), "Should have CVSS vector");
            assertFalse(vuln.get("cvssVector").isNull(), "CVSS vector should not be null");
        }

        // Verify CVSSv3 is preferred when multiple versions available
        JsonNode vulnWithBoth = vulnerabilities.get(2); // CVE-2024-33333 has both v2 and v3
        assertEquals(8.1, vulnWithBoth.get("cvssScore").asDouble(), 0.01,
                "Should use CVSSv3 score (8.1) over CVSSv2 (6.8)");
        assertTrue(vulnWithBoth.get("cvssVector").asText().startsWith("CVSS:3.1/"),
                "Should use CVSSv3 vector format");
    }

    @Test
    void testSeverityDistribution() throws IOException {
        // Given: A report with multiple severity levels
        Path inputPath = getTestResourcePath("report-with-suppressed.json");
        Path outputPath = tempDir.resolve("generic-severity.json");
        String wildflyVersion = "39.0.1.Final";

        // When: Convert
        OwaspReport owaspReport = parser.parse(inputPath);
        GenericReport genericReport = generator.convert(owaspReport, wildflyVersion, false);
        generator.writeToFile(genericReport, outputPath);

        // Then: Verify severity counts in summary
        JsonNode root = objectMapper.readTree(outputPath.toFile());
        JsonNode summary = root.get("summary");

        // Active vulnerabilities: 1 CRITICAL, 1 MEDIUM
        // Suppressed: 1 LOW (not counted in severity breakdown)
        assertEquals(1, summary.get("critical").asInt(), "Should have 1 critical");
        assertEquals(0, summary.get("high").asInt(), "Should have 0 high");
        assertEquals(1, summary.get("medium").asInt(), "Should have 1 medium");
        assertEquals(0, summary.get("low").asInt(), "Should have 0 low (suppressed not counted)");
    }

    @Test
    void testAffectedPackagesStructure() throws IOException {
        // Given: A report with package information
        Path inputPath = getTestResourcePath("minimal-report.json");
        Path outputPath = tempDir.resolve("generic-packages.json");
        String wildflyVersion = "39.0.1.Final";

        // When: Convert
        OwaspReport owaspReport = parser.parse(inputPath);
        GenericReport genericReport = generator.convert(owaspReport, wildflyVersion);
        generator.writeToFile(genericReport, outputPath);

        // Then: Verify affected packages structure
        JsonNode root = objectMapper.readTree(outputPath.toFile());
        JsonNode vulnerabilities = root.get("vulnerabilities");
        JsonNode vuln = vulnerabilities.get(0);
        JsonNode affectedPackages = vuln.get("affectedPackages");

        assertTrue(affectedPackages.isArray(), "Affected packages should be an array");
        assertTrue(affectedPackages.size() > 0, "Should have at least one affected package");

        JsonNode pkg = affectedPackages.get(0);
        assertTrue(pkg.has("packageUrl"), "Package should have packageUrl");
        assertTrue(pkg.has("groupId"), "Package should have groupId");
        assertTrue(pkg.has("artifactId"), "Package should have artifactId");
        assertTrue(pkg.has("version"), "Package should have version");
    }

    @Test
    void testReferencesStructure() throws IOException {
        // Given: A report with references
        Path inputPath = getTestResourcePath("minimal-report.json");
        Path outputPath = tempDir.resolve("generic-references.json");
        String wildflyVersion = "39.0.1.Final";

        // When: Convert
        OwaspReport owaspReport = parser.parse(inputPath);
        GenericReport genericReport = generator.convert(owaspReport, wildflyVersion);
        generator.writeToFile(genericReport, outputPath);

        // Then: Verify references structure
        JsonNode root = objectMapper.readTree(outputPath.toFile());
        JsonNode vulnerabilities = root.get("vulnerabilities");
        JsonNode vuln = vulnerabilities.get(0);
        JsonNode references = vuln.get("references");

        assertTrue(references.isArray(), "References should be an array");
        assertTrue(references.size() > 0, "Should have at least one reference");

        JsonNode ref = references.get(0);
        assertTrue(ref.has("url"), "Reference should have url");
        assertTrue(ref.has("source"), "Reference should have source");
    }

    @Test
    void testOutputIsValidJson() throws IOException {
        // Given: Any valid OWASP report
        Path inputPath = getTestResourcePath("minimal-report.json");
        Path outputPath = tempDir.resolve("generic-valid.json");

        // When: Convert
        OwaspReport owaspReport = parser.parse(inputPath);
        GenericReport genericReport = generator.convert(owaspReport, "39.0.1.Final");
        generator.writeToFile(genericReport, outputPath);

        // Then: Verify output is valid, parseable JSON
        assertDoesNotThrow(() -> {
            JsonNode root = objectMapper.readTree(outputPath.toFile());
            assertNotNull(root, "Should parse as valid JSON");
        }, "Output should be valid JSON");
    }

    @Test
    void testOutputIsPrettyPrinted() throws IOException {
        // Given: Any valid OWASP report
        Path inputPath = getTestResourcePath("minimal-report.json");
        Path outputPath = tempDir.resolve("generic-pretty.json");

        // When: Convert
        OwaspReport owaspReport = parser.parse(inputPath);
        GenericReport genericReport = generator.convert(owaspReport, "39.0.1.Final");
        generator.writeToFile(genericReport, outputPath);

        // Then: Verify output is pretty-printed (contains newlines and indentation)
        String content = Files.readString(outputPath);
        assertTrue(content.contains("\n"), "Should contain newlines (pretty-printed)");
        assertTrue(content.contains("  "), "Should contain indentation (pretty-printed)");
    }

    /**
     * Helper method to get test resource path.
     */
    private Path getTestResourcePath(String resourceName) {
        return Paths.get("src/test/resources", resourceName);
    }
}

// Made with Bob