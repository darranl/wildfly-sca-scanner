/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.sca.reportconverter.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.wildfly.security.sca.reportconverter.model.owasp.OwaspReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OwaspReportParser.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
class OwaspReportParserTest {

    private OwaspReportParser parser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        parser = new OwaspReportParser();
    }

    @Test
    void testParseValidMinimalReport() throws IOException {
        Path reportPath = getTestResourcePath("minimal-report.json");

        OwaspReport report = parser.parse(reportPath);

        assertNotNull(report, "Report should not be null");
        assertEquals("1.1", report.getReportSchema(), "Report schema should be 1.1");
        assertNotNull(report.getScanInfo(), "Scan info should not be null");
        assertEquals("12.0.0", report.getScanInfo().getEngineVersion(), "Engine version should match");
        assertNotNull(report.getDependencies(), "Dependencies should not be null");
        assertEquals(1, report.getDependencies().size(), "Should have 1 dependency");

        // Verify vulnerability was parsed
        assertEquals(1, report.getDependencies().get(0).getVulnerabilities().size(),
                "Should have 1 vulnerability");
        assertEquals("CVE-2024-12345",
                report.getDependencies().get(0).getVulnerabilities().get(0).getName(),
                "CVE ID should match");
    }

    @Test
    void testParseReportWithSuppressed() throws IOException {
        Path reportPath = getTestResourcePath("report-with-suppressed.json");

        OwaspReport report = parser.parse(reportPath);

        assertNotNull(report, "Report should not be null");
        assertEquals(1, report.getDependencies().size(), "Should have 1 dependency");

        // Verify active vulnerabilities
        assertEquals(2, report.getDependencies().get(0).getVulnerabilities().size(),
                "Should have 2 active vulnerabilities");

        // Verify suppressed vulnerabilities
        assertEquals(1, report.getDependencies().get(0).getSuppressedVulnerabilities().size(),
                "Should have 1 suppressed vulnerability");
        assertEquals("CVE-2024-33333",
                report.getDependencies().get(0).getSuppressedVulnerabilities().get(0).getName(),
                "Suppressed CVE ID should match");
    }

    @Test
    void testParseReportNoVulnerabilities() throws IOException {
        Path reportPath = getTestResourcePath("report-no-vulnerabilities.json");

        OwaspReport report = parser.parse(reportPath);

        assertNotNull(report, "Report should not be null");
        assertEquals(2, report.getDependencies().size(), "Should have 2 dependencies");

        // Verify no vulnerabilities
        for (var dep : report.getDependencies()) {
            assertTrue(dep.getVulnerabilities() == null || dep.getVulnerabilities().isEmpty(),
                    "Should have no active vulnerabilities");
            assertTrue(dep.getSuppressedVulnerabilities() == null || dep.getSuppressedVulnerabilities().isEmpty(),
                    "Should have no suppressed vulnerabilities");
        }
    }

    @Test
    void testParseReportMultipleCvss() throws IOException {
        Path reportPath = getTestResourcePath("report-multiple-cvss.json");

        OwaspReport report = parser.parse(reportPath);

        assertNotNull(report, "Report should not be null");
        assertEquals(1, report.getDependencies().size(), "Should have 1 dependency");
        assertEquals(4, report.getDependencies().get(0).getVulnerabilities().size(),
                "Should have 4 vulnerabilities with different CVSS versions");

        var vulns = report.getDependencies().get(0).getVulnerabilities();

        // CVSSv2 only
        assertNotNull(vulns.get(0).getCvssv2(), "First vuln should have CVSSv2");
        assertNull(vulns.get(0).getCvssv3(), "First vuln should not have CVSSv3");

        // CVSSv3 only
        assertNull(vulns.get(1).getCvssv2(), "Second vuln should not have CVSSv2");
        assertNotNull(vulns.get(1).getCvssv3(), "Second vuln should have CVSSv3");

        // Both CVSSv2 and CVSSv3
        assertNotNull(vulns.get(2).getCvssv2(), "Third vuln should have CVSSv2");
        assertNotNull(vulns.get(2).getCvssv3(), "Third vuln should have CVSSv3");

        // CVSSv4 only
        assertNotNull(vulns.get(3).getCvssv4(), "Fourth vuln should have CVSSv4");
    }

    @Test
    void testParseNullPath() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parser.parse(null),
                "Should throw IllegalArgumentException for null path");

        assertTrue(exception.getMessage().contains("cannot be null"),
                "Exception message should mention null");
    }

    @Test
    void testParseNonExistentFile() {
        Path nonExistentPath = tempDir.resolve("non-existent-file.json");

        IOException exception = assertThrows(IOException.class,
                () -> parser.parse(nonExistentPath),
                "Should throw IOException for non-existent file");

        assertTrue(exception.getMessage().contains("does not exist"),
                "Exception message should mention file does not exist");
    }

    @Test
    void testParseUnreadableFile() throws IOException {
        // Create a file and make it unreadable
        Path unreadablePath = tempDir.resolve("unreadable.json");
        Files.writeString(unreadablePath, "{}");
        unreadablePath.toFile().setReadable(false);

        // Skip test if we can't make file unreadable (e.g., running as root)
        if (unreadablePath.toFile().canRead()) {
            return;
        }

        IOException exception = assertThrows(IOException.class,
                () -> parser.parse(unreadablePath),
                "Should throw IOException for unreadable file");

        assertTrue(exception.getMessage().contains("Cannot read"),
                "Exception message should mention cannot read");

        // Cleanup: restore permissions
        unreadablePath.toFile().setReadable(true);
    }

    @Test
    void testParseMalformedJson() {
        Path malformedPath = getTestResourcePath("malformed-report.json");

        assertThrows(IOException.class,
                () -> parser.parse(malformedPath),
                "Should throw IOException for malformed JSON");
    }

    @Test
    void testValidateNullReport() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parser.validate(null),
                "Should throw IllegalArgumentException for null report");

        assertTrue(exception.getMessage().contains("cannot be null"),
                "Exception message should mention null");
    }

    @Test
    void testValidateReportWithNullDependencies() {
        OwaspReport report = new OwaspReport();
        report.setReportSchema("1.1");
        report.setDependencies(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parser.validate(report),
                "Should throw IllegalArgumentException for null dependencies");

        assertTrue(exception.getMessage().contains("dependencies"),
                "Exception message should mention dependencies");
    }

    @Test
    void testValidateReportWithNullSchema() {
        OwaspReport report = new OwaspReport();
        report.setReportSchema(null);
        report.setDependencies(java.util.Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parser.validate(report),
                "Should throw IllegalArgumentException for null schema");

        assertTrue(exception.getMessage().contains("reportSchema"),
                "Exception message should mention reportSchema");
    }

    @Test
    void testValidateReportWithEmptySchema() {
        OwaspReport report = new OwaspReport();
        report.setReportSchema("");
        report.setDependencies(java.util.Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parser.validate(report),
                "Should throw IllegalArgumentException for empty schema");

        assertTrue(exception.getMessage().contains("reportSchema"),
                "Exception message should mention reportSchema");
    }

    @Test
    void testValidateValidReport() throws IOException {
        Path reportPath = getTestResourcePath("minimal-report.json");
        OwaspReport report = parser.parse(reportPath);

        // Should not throw any exception
        assertDoesNotThrow(() -> parser.validate(report),
                "Valid report should pass validation");
    }

    @Test
    void testParserHandlesUnknownProperties() throws IOException {
        // Create a report with unknown properties
        String jsonWithUnknownProps = """
                {
                  "reportSchema": "1.1",
                  "unknownField": "should be ignored",
                  "scanInfo": {
                    "engineVersion": "12.0.0",
                    "unknownScanField": "also ignored"
                  },
                  "projectInfo": {
                    "name": "Test",
                    "reportDate": "2026-03-17T07:00:00Z"
                  },
                  "dependencies": []
                }
                """;

        Path testPath = tempDir.resolve("unknown-props.json");
        Files.writeString(testPath, jsonWithUnknownProps);

        // Should parse successfully, ignoring unknown properties
        OwaspReport report = assertDoesNotThrow(() -> parser.parse(testPath),
                "Parser should handle unknown properties gracefully");

        assertNotNull(report, "Report should be parsed");
        assertEquals("1.1", report.getReportSchema(), "Known fields should be parsed correctly");
    }

    /**
     * Helper method to get test resource path.
     */
    private Path getTestResourcePath(String resourceName) {
        return Paths.get("src/test/resources", resourceName);
    }
}

// Made with Bob