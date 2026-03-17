/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.sca.reportconverter.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.wildfly.security.sca.reportconverter.model.generic.GenericReport;
import org.wildfly.security.sca.reportconverter.model.generic.Vulnerability;
import org.wildfly.security.sca.reportconverter.model.owasp.*;
import org.wildfly.security.sca.reportconverter.parser.OwaspReportParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GenericReportGenerator.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
class GenericReportGeneratorTest {

    private GenericReportGenerator generator;
    private OwaspReportParser parser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        generator = new GenericReportGenerator();
        parser = new OwaspReportParser();
    }

    @Test
    void testConvertMinimalReport() throws IOException {
        Path reportPath = getTestResourcePath("minimal-report.json");
        OwaspReport owaspReport = parser.parse(reportPath);

        GenericReport genericReport = generator.convert(owaspReport, "39.0.1.Final");

        assertNotNull(genericReport, "Generic report should not be null");
        assertEquals("1.0", genericReport.getSchemaVersion(), "Schema version should be 1.0");

        // Verify metadata
        assertNotNull(genericReport.getMetadata(), "Metadata should not be null");
        assertEquals("39.0.1.Final", genericReport.getMetadata().getWildflyVersion(),
                "WildFly version should match");
        assertNotNull(genericReport.getMetadata().getScanDate(), "Scan date should be set");
        assertEquals(1, genericReport.getMetadata().getTotalDependencies(),
                "Should have 1 dependency");
        assertEquals(1, genericReport.getMetadata().getVulnerableDependencies(),
                "Should have 1 vulnerable dependency");

        // Verify vulnerabilities
        assertEquals(1, genericReport.getVulnerabilities().size(),
                "Should have 1 vulnerability");
        Vulnerability vuln = genericReport.getVulnerabilities().get(0);
        assertEquals("CVE-2024-12345", vuln.getId(), "CVE ID should match");
        assertEquals("HIGH", vuln.getSeverity(), "Severity should be HIGH");
        assertEquals(7.5, vuln.getCvssScore(), "CVSS score should match");
        assertFalse(vuln.isSuppressed(), "Should not be suppressed");

        // Verify summary
        assertNotNull(genericReport.getSummary(), "Summary should not be null");
        assertEquals(1, genericReport.getSummary().getActive(), "Should have 1 active vulnerability");
        assertEquals(0, genericReport.getSummary().getSuppressed(), "Should have 0 suppressed");
        assertEquals(0, genericReport.getSummary().getCritical(), "Should have 0 critical");
        assertEquals(1, genericReport.getSummary().getHigh(), "Should have 1 high");
        assertEquals(0, genericReport.getSummary().getMedium(), "Should have 0 medium");
        assertEquals(0, genericReport.getSummary().getLow(), "Should have 0 low");
    }

    @Test
    void testConvertWithSuppressedVulnerabilities() throws IOException {
        Path reportPath = getTestResourcePath("report-with-suppressed.json");
        OwaspReport owaspReport = parser.parse(reportPath);

        GenericReport genericReport = generator.convert(owaspReport, "39.0.1.Final", false);

        // Should include both active and suppressed
        assertEquals(3, genericReport.getVulnerabilities().size(),
                "Should have 3 total vulnerabilities");

        long activeCount = genericReport.getVulnerabilities().stream()
                .filter(v -> !v.isSuppressed())
                .count();
        long suppressedCount = genericReport.getVulnerabilities().stream()
                .filter(Vulnerability::isSuppressed)
                .count();

        assertEquals(2, activeCount, "Should have 2 active vulnerabilities");
        assertEquals(1, suppressedCount, "Should have 1 suppressed vulnerability");

        // Verify summary
        assertEquals(2, genericReport.getSummary().getActive(), "Summary should show 2 active");
        assertEquals(1, genericReport.getSummary().getSuppressed(), "Summary should show 1 suppressed");
        assertEquals(1, genericReport.getSummary().getCritical(), "Should have 1 critical");
        assertEquals(0, genericReport.getSummary().getHigh(), "Should have 0 high");
        assertEquals(1, genericReport.getSummary().getMedium(), "Should have 1 medium");
        assertEquals(0, genericReport.getSummary().getLow(), "Should have 0 low (suppressed not counted)");

        // Verify suppression reason is captured
        Vulnerability suppressed = genericReport.getVulnerabilities().stream()
                .filter(Vulnerability::isSuppressed)
                .findFirst()
                .orElseThrow();
        assertNotNull(suppressed.getSuppressionReason(), "Suppression reason should be set");
        assertTrue(suppressed.getSuppressionReason().contains("Not applicable"),
                "Suppression reason should contain expected text");
    }

    @Test
    void testConvertSkipSuppressedVulnerabilities() throws IOException {
        Path reportPath = getTestResourcePath("report-with-suppressed.json");
        OwaspReport owaspReport = parser.parse(reportPath);

        GenericReport genericReport = generator.convert(owaspReport, "39.0.1.Final", true);

        // Should only include active vulnerabilities
        assertEquals(2, genericReport.getVulnerabilities().size(),
                "Should have 2 vulnerabilities (suppressed skipped)");

        // All should be active
        assertTrue(genericReport.getVulnerabilities().stream().noneMatch(Vulnerability::isSuppressed),
                "All vulnerabilities should be active");

        // Verify summary
        assertEquals(2, genericReport.getSummary().getActive(), "Summary should show 2 active");
        assertEquals(0, genericReport.getSummary().getSuppressed(), "Summary should show 0 suppressed");
    }

    @Test
    void testConvertNoVulnerabilities() throws IOException {
        Path reportPath = getTestResourcePath("report-no-vulnerabilities.json");
        OwaspReport owaspReport = parser.parse(reportPath);

        GenericReport genericReport = generator.convert(owaspReport, "39.0.1.Final");

        assertEquals(0, genericReport.getVulnerabilities().size(),
                "Should have 0 vulnerabilities");
        assertEquals(2, genericReport.getMetadata().getTotalDependencies(),
                "Should have 2 dependencies");
        assertEquals(0, genericReport.getMetadata().getVulnerableDependencies(),
                "Should have 0 vulnerable dependencies");

        // Verify summary is all zeros
        assertEquals(0, genericReport.getSummary().getActive(), "Should have 0 active");
        assertEquals(0, genericReport.getSummary().getSuppressed(), "Should have 0 suppressed");
        assertEquals(0, genericReport.getSummary().getCritical(), "Should have 0 critical");
        assertEquals(0, genericReport.getSummary().getHigh(), "Should have 0 high");
        assertEquals(0, genericReport.getSummary().getMedium(), "Should have 0 medium");
        assertEquals(0, genericReport.getSummary().getLow(), "Should have 0 low");
    }

    @Test
    void testCvssScorePriority() throws IOException {
        Path reportPath = getTestResourcePath("report-multiple-cvss.json");
        OwaspReport owaspReport = parser.parse(reportPath);

        GenericReport genericReport = generator.convert(owaspReport, "39.0.1.Final");

        List<Vulnerability> vulns = genericReport.getVulnerabilities();
        assertEquals(4, vulns.size(), "Should have 4 vulnerabilities");

        // CVE-2020-11111: CVSSv2 only (score 7.5)
        assertEquals(7.5, vulns.get(0).getCvssScore(), "Should use CVSSv2 score");

        // CVE-2023-22222: CVSSv3 only (score 9.1)
        assertEquals(9.1, vulns.get(1).getCvssScore(), "Should use CVSSv3 score");

        // CVE-2024-33333: Both v2 (6.8) and v3 (8.1) - should prefer v3
        assertEquals(8.1, vulns.get(2).getCvssScore(), "Should prefer CVSSv3 over v2");

        // CVE-2025-44444: CVSSv4 only (score 6.9)
        assertEquals(6.9, vulns.get(3).getCvssScore(), "Should use CVSSv4 score");
    }

    @Test
    void testCvssVectorGeneration() throws IOException {
        Path reportPath = getTestResourcePath("report-multiple-cvss.json");
        OwaspReport owaspReport = parser.parse(reportPath);

        GenericReport genericReport = generator.convert(owaspReport, "39.0.1.Final");

        List<Vulnerability> vulns = genericReport.getVulnerabilities();

        // CVSSv2 vector
        assertNotNull(vulns.get(0).getCvssVector(), "CVSSv2 vector should be generated");
        assertTrue(vulns.get(0).getCvssVector().startsWith("AV:"),
                "CVSSv2 vector should start with AV:");

        // CVSSv3 vector
        assertNotNull(vulns.get(1).getCvssVector(), "CVSSv3 vector should be generated");
        assertTrue(vulns.get(1).getCvssVector().startsWith("CVSS:3.1/"),
                "CVSSv3 vector should start with CVSS:3.1/");

        // Should prefer CVSSv3 vector when both available
        assertNotNull(vulns.get(2).getCvssVector(), "Vector should be generated");
        assertTrue(vulns.get(2).getCvssVector().startsWith("CVSS:3.1/"),
                "Should prefer CVSSv3 vector format");

        // CVSSv4 vector
        assertNotNull(vulns.get(3).getCvssVector(), "CVSSv4 vector should be generated");
        assertTrue(vulns.get(3).getCvssVector().contains("CVSS:4.0"),
                "CVSSv4 vector should contain CVSS:4.0");
    }

    @Test
    void testTitleExtraction() throws IOException {
        Path reportPath = getTestResourcePath("minimal-report.json");
        OwaspReport owaspReport = parser.parse(reportPath);

        GenericReport genericReport = generator.convert(owaspReport, "39.0.1.Final");

        Vulnerability vuln = genericReport.getVulnerabilities().get(0);
        assertNotNull(vuln.getTitle(), "Title should be extracted");
        assertTrue(vuln.getTitle().endsWith("."), "Title should end with period (first sentence)");
        assertTrue(vuln.getTitle().length() <= 100, "Title should be truncated to 100 chars");
    }

    @Test
    void testAffectedPackagesExtraction() throws IOException {
        Path reportPath = getTestResourcePath("minimal-report.json");
        OwaspReport owaspReport = parser.parse(reportPath);

        GenericReport genericReport = generator.convert(owaspReport, "39.0.1.Final");

        Vulnerability vuln = genericReport.getVulnerabilities().get(0);
        assertNotNull(vuln.getAffectedPackages(), "Affected packages should not be null");
        assertFalse(vuln.getAffectedPackages().isEmpty(), "Should have affected packages");

        var pkg = vuln.getAffectedPackages().get(0);
        assertNotNull(pkg.getPackageUrl(), "Package URL should be set");
        assertTrue(pkg.getPackageUrl().startsWith("pkg:maven/"),
                "Package URL should be in purl format");
        assertEquals("com.example", pkg.getGroupId(), "Group ID should be extracted");
        assertEquals("test-library", pkg.getArtifactId(), "Artifact ID should be extracted");
        assertEquals("1.0.0", pkg.getVersion(), "Version should be extracted");
    }

    @Test
    void testReferencesDeduplication() throws IOException {
        // Create a report with duplicate references
        OwaspReport owaspReport = createReportWithDuplicateReferences();

        GenericReport genericReport = generator.convert(owaspReport, "39.0.1.Final");

        Vulnerability vuln = genericReport.getVulnerabilities().get(0);
        assertNotNull(vuln.getReferences(), "References should not be null");

        // Should deduplicate by URL
        assertEquals(2, vuln.getReferences().size(),
                "Should have 2 unique references (duplicates removed)");
    }

    @Test
    void testConvertNullReport() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generator.convert(null, "39.0.1.Final"),
                "Should throw IllegalArgumentException for null report");

        assertTrue(exception.getMessage().contains("cannot be null"),
                "Exception message should mention null");
    }

    @Test
    void testConvertNullVersion() throws IOException {
        Path reportPath = getTestResourcePath("minimal-report.json");
        OwaspReport owaspReport = parser.parse(reportPath);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generator.convert(owaspReport, null),
                "Should throw IllegalArgumentException for null version");

        assertTrue(exception.getMessage().contains("version"),
                "Exception message should mention version");
    }

    @Test
    void testConvertEmptyVersion() throws IOException {
        Path reportPath = getTestResourcePath("minimal-report.json");
        OwaspReport owaspReport = parser.parse(reportPath);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generator.convert(owaspReport, ""),
                "Should throw IllegalArgumentException for empty version");

        assertTrue(exception.getMessage().contains("version"),
                "Exception message should mention version");
    }

    @Test
    void testWriteToFile() throws IOException {
        Path reportPath = getTestResourcePath("minimal-report.json");
        OwaspReport owaspReport = parser.parse(reportPath);
        GenericReport genericReport = generator.convert(owaspReport, "39.0.1.Final");

        Path outputPath = tempDir.resolve("output.json");
        generator.writeToFile(genericReport, outputPath);

        assertTrue(Files.exists(outputPath), "Output file should be created");
        assertTrue(Files.size(outputPath) > 0, "Output file should not be empty");

        // Verify it's valid JSON by reading it back
        String content = Files.readString(outputPath);
        assertTrue(content.contains("schemaVersion"), "Should contain schemaVersion");
        assertTrue(content.contains("39.0.1.Final"), "Should contain WildFly version");
    }

    @Test
    void testWriteToFileNullReport() {
        Path outputPath = tempDir.resolve("output.json");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generator.writeToFile(null, outputPath),
                "Should throw IllegalArgumentException for null report");

        assertTrue(exception.getMessage().contains("cannot be null"),
                "Exception message should mention null");
    }

    @Test
    void testWriteToFileNullPath() throws IOException {
        Path reportPath = getTestResourcePath("minimal-report.json");
        OwaspReport owaspReport = parser.parse(reportPath);
        GenericReport genericReport = generator.convert(owaspReport, "39.0.1.Final");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generator.writeToFile(genericReport, null),
                "Should throw IllegalArgumentException for null path");

        assertTrue(exception.getMessage().contains("cannot be null"),
                "Exception message should mention null");
    }

    @Test
    void testSeverityCountsAllLevels() {
        // Create a report with all severity levels
        OwaspReport owaspReport = createReportWithAllSeverities();

        GenericReport genericReport = generator.convert(owaspReport, "39.0.1.Final");

        assertEquals(1, genericReport.getSummary().getCritical(), "Should have 1 critical");
        assertEquals(1, genericReport.getSummary().getHigh(), "Should have 1 high");
        assertEquals(1, genericReport.getSummary().getMedium(), "Should have 1 medium");
        assertEquals(1, genericReport.getSummary().getLow(), "Should have 1 low");
        assertEquals(4, genericReport.getSummary().getActive(), "Should have 4 active total");
    }

    /**
     * Helper method to get test resource path.
     */
    private Path getTestResourcePath(String resourceName) {
        return Paths.get("src/test/resources", resourceName);
    }

    /**
     * Helper method to create a report with duplicate references.
     */
    private OwaspReport createReportWithDuplicateReferences() {
        OwaspReport report = new OwaspReport();
        report.setReportSchema("1.1");

        ScanInfo scanInfo = new ScanInfo();
        scanInfo.setEngineVersion("12.0.0");
        report.setScanInfo(scanInfo);

        OwaspDependency dep = new OwaspDependency();
        dep.setFileName("test.jar");

        OwaspVulnerability vuln = new OwaspVulnerability();
        vuln.setName("CVE-2024-99999");
        vuln.setSeverity("HIGH");
        vuln.setDescription("Test vulnerability");

        // Add duplicate references
        Reference ref1 = new Reference();
        ref1.setUrl("https://example.com/vuln");
        ref1.setSource("NVD");

        Reference ref2 = new Reference();
        ref2.setUrl("https://example.com/vuln"); // Duplicate URL
        ref2.setSource("MITRE");

        Reference ref3 = new Reference();
        ref3.setUrl("https://example.com/other");
        ref3.setSource("NVD");

        vuln.setReferences(Arrays.asList(ref1, ref2, ref3));
        dep.setVulnerabilities(Collections.singletonList(vuln));
        report.setDependencies(Collections.singletonList(dep));

        return report;
    }

    /**
     * Helper method to create a report with all severity levels.
     */
    private OwaspReport createReportWithAllSeverities() {
        OwaspReport report = new OwaspReport();
        report.setReportSchema("1.1");

        ScanInfo scanInfo = new ScanInfo();
        scanInfo.setEngineVersion("12.0.0");
        report.setScanInfo(scanInfo);

        OwaspDependency dep = new OwaspDependency();
        dep.setFileName("test.jar");

        List<OwaspVulnerability> vulns = new ArrayList<>();

        // Critical
        OwaspVulnerability critical = new OwaspVulnerability();
        critical.setName("CVE-2024-00001");
        critical.setSeverity("CRITICAL");
        critical.setDescription("Critical vulnerability");
        vulns.add(critical);

        // High
        OwaspVulnerability high = new OwaspVulnerability();
        high.setName("CVE-2024-00002");
        high.setSeverity("HIGH");
        high.setDescription("High vulnerability");
        vulns.add(high);

        // Medium
        OwaspVulnerability medium = new OwaspVulnerability();
        medium.setName("CVE-2024-00003");
        medium.setSeverity("MEDIUM");
        medium.setDescription("Medium vulnerability");
        vulns.add(medium);

        // Low
        OwaspVulnerability low = new OwaspVulnerability();
        low.setName("CVE-2024-00004");
        low.setSeverity("LOW");
        low.setDescription("Low vulnerability");
        vulns.add(low);

        dep.setVulnerabilities(vulns);
        report.setDependencies(Collections.singletonList(dep));

        return report;
    }
}

// Made with Bob