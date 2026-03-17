/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.sca.reportconverter.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.wildfly.security.sca.reportconverter.model.generic.AffectedPackage;
import org.wildfly.security.sca.reportconverter.model.generic.GenericReport;
import org.wildfly.security.sca.reportconverter.model.generic.Metadata;
import org.wildfly.security.sca.reportconverter.model.generic.Summary;
import org.wildfly.security.sca.reportconverter.model.generic.Vulnerability;
import org.wildfly.security.sca.reportconverter.model.owasp.CvssV2;
import org.wildfly.security.sca.reportconverter.model.owasp.CvssV3;
import org.wildfly.security.sca.reportconverter.model.owasp.CvssV4;
import org.wildfly.security.sca.reportconverter.model.owasp.OwaspDependency;
import org.wildfly.security.sca.reportconverter.model.owasp.OwaspReport;
import org.wildfly.security.sca.reportconverter.model.owasp.OwaspVulnerability;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator for converting OWASP reports to generic format.
 * Handles mapping between OWASP-specific and tool-agnostic structures.
 */
public class GenericReportGenerator {
    private final ObjectMapper objectMapper;

    /**
     * Create a new generator with default configuration.
     */
    public GenericReportGenerator() {
        this.objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Convert OWASP report to generic format.
     *
     * @param owaspReport The parsed OWASP report
     * @param wildflyVersion The WildFly version being scanned
     * @return Generic report object
     */
    public GenericReport convert(OwaspReport owaspReport, String wildflyVersion) {
        if (owaspReport == null) {
            throw new IllegalArgumentException("OWASP report cannot be null");
        }
        if (wildflyVersion == null || wildflyVersion.isEmpty()) {
            throw new IllegalArgumentException("WildFly version cannot be null or empty");
        }

        GenericReport report = new GenericReport();
        report.setSchemaVersion("1.0");
        report.setMetadata(buildMetadata(owaspReport, wildflyVersion));
        report.setVulnerabilities(buildVulnerabilities(owaspReport));
        report.setSummary(calculateSummary(report.getVulnerabilities()));
        return report;
    }

    /**
     * Write generic report to JSON file.
     *
     * @param report The generic report
     * @param outputPath Output file path
     * @throws IOException if file cannot be written
     */
    public void writeToFile(GenericReport report, Path outputPath) throws IOException {
        if (report == null) {
            throw new IllegalArgumentException("Report cannot be null");
        }
        if (outputPath == null) {
            throw new IllegalArgumentException("Output path cannot be null");
        }

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(outputPath.toFile(), report);
    }

    /**
     * Build metadata from OWASP report.
     */
    private Metadata buildMetadata(OwaspReport owaspReport, String wildflyVersion) {
        Metadata metadata = new Metadata();
        metadata.setWildflyVersion(wildflyVersion);

        // Use current timestamp as scan date
        metadata.setScanDate(Instant.now().toString());

        // Build scanner information
        List<org.wildfly.security.sca.reportconverter.model.generic.Scanner> scanners = new ArrayList<>();
        if (owaspReport.getScanInfo() != null && owaspReport.getScanInfo().getEngineVersion() != null) {
            scanners.add(new org.wildfly.security.sca.reportconverter.model.generic.Scanner(
                    "OWASP Dependency Check",
                    owaspReport.getScanInfo().getEngineVersion()));
        }
        metadata.setScanners(scanners);

        // Calculate dependency statistics
        if (owaspReport.getDependencies() != null) {
            metadata.setTotalDependencies(owaspReport.getDependencies().size());

            long vulnerableCount = owaspReport.getDependencies().stream()
                    .filter(dep -> (dep.getVulnerabilities() != null && !dep.getVulnerabilities().isEmpty()) ||
                                   (dep.getSuppressedVulnerabilities() != null && !dep.getSuppressedVulnerabilities().isEmpty()))
                    .count();
            metadata.setVulnerableDependencies((int) vulnerableCount);
        }

        return metadata;
    }

    /**
     * Build vulnerabilities list from OWASP dependencies.
     */
    private List<Vulnerability> buildVulnerabilities(OwaspReport owaspReport) {
        List<Vulnerability> vulnerabilities = new ArrayList<>();

        if (owaspReport.getDependencies() == null) {
            return vulnerabilities;
        }

        for (OwaspDependency dep : owaspReport.getDependencies()) {
            // Process active vulnerabilities
            if (dep.getVulnerabilities() != null) {
                for (OwaspVulnerability owaspVuln : dep.getVulnerabilities()) {
                    vulnerabilities.add(mapVulnerability(owaspVuln, dep, false));
                }
            }

            // Process suppressed vulnerabilities
            if (dep.getSuppressedVulnerabilities() != null) {
                for (OwaspVulnerability owaspVuln : dep.getSuppressedVulnerabilities()) {
                    vulnerabilities.add(mapVulnerability(owaspVuln, dep, true));
                }
            }
        }

        return vulnerabilities;
    }

    /**
     * Map a single OWASP vulnerability to generic format.
     */
    private Vulnerability mapVulnerability(OwaspVulnerability owaspVuln, OwaspDependency dep, boolean suppressed) {
        Vulnerability vuln = new Vulnerability();

        vuln.setId(owaspVuln.getName());
        vuln.setSource(owaspVuln.getSource());
        vuln.setTitle(extractTitle(owaspVuln));
        vuln.setSeverity(owaspVuln.getSeverity());
        vuln.setCvssScore(extractBestCvssScore(owaspVuln));
        vuln.setCvssVector(extractCvssVector(owaspVuln));
        vuln.setDescription(owaspVuln.getDescription());
        vuln.setPublishedDate(extractPublishedDate(owaspVuln));
        vuln.setCwe(owaspVuln.getCwes());
        vuln.setAffectedPackages(buildAffectedPackages(dep));
        vuln.setReferences(mapReferences(owaspVuln.getReferences()));
        vuln.setSuppressed(suppressed);

        if (suppressed) {
            vuln.setSuppressionReason(owaspVuln.getNotes());
        }

        return vuln;
    }

    /**
     * Extract title from vulnerability (first sentence or CVE ID).
     */
    private String extractTitle(OwaspVulnerability vuln) {
        if (vuln.getDescription() != null && !vuln.getDescription().isEmpty()) {
            String desc = vuln.getDescription();
            // Extract first sentence (up to first period, question mark, or exclamation)
            int endIndex = desc.indexOf('.');
            if (endIndex == -1) endIndex = desc.indexOf('?');
            if (endIndex == -1) endIndex = desc.indexOf('!');

            if (endIndex > 0 && endIndex < 100) {
                return desc.substring(0, endIndex + 1).trim();
            } else if (desc.length() <= 100) {
                return desc.trim();
            } else {
                return desc.substring(0, 97).trim() + "...";
            }
        }

        // Fall back to CVE ID
        return vuln.getName();
    }

    /**
     * Extract best available CVSS score (prefer v3, then v2, then v4).
     */
    private Double extractBestCvssScore(OwaspVulnerability vuln) {
        if (vuln.getCvssv3() != null && vuln.getCvssv3().getBaseScore() != null) {
            return vuln.getCvssv3().getBaseScore();
        } else if (vuln.getCvssv2() != null && vuln.getCvssv2().getScore() != null) {
            return vuln.getCvssv2().getScore();
        } else if (vuln.getCvssv4() != null && vuln.getCvssv4().getBaseScore() != null) {
            return vuln.getCvssv4().getBaseScore();
        }
        return null;
    }

    /**
     * Extract CVSS vector string.
     */
    private String extractCvssVector(OwaspVulnerability vuln) {
        if (vuln.getCvssv3() != null && vuln.getCvssv3().getAttackVector() != null) {
            return buildCvssV3Vector(vuln.getCvssv3());
        } else if (vuln.getCvssv2() != null && vuln.getCvssv2().getAccessVector() != null) {
            return buildCvssV2Vector(vuln.getCvssv2());
        } else if (vuln.getCvssv4() != null && vuln.getCvssv4().getAttackVector() != null) {
            return buildCvssV4Vector(vuln.getCvssv4());
        }
        return null;
    }

    /**
     * Build CVSS v3 vector string.
     */
    private String buildCvssV3Vector(CvssV3 cvss) {
        return String.format("CVSS:3.1/AV:%s/AC:%s/PR:%s/UI:%s/S:%s/C:%s/I:%s/A:%s",
                cvss.getAttackVector() != null ? cvss.getAttackVector().substring(0, 1) : "?",
                cvss.getAttackComplexity() != null ? cvss.getAttackComplexity().substring(0, 1) : "?",
                cvss.getPrivilegesRequired() != null ? cvss.getPrivilegesRequired().substring(0, 1) : "?",
                cvss.getUserInteraction() != null ? cvss.getUserInteraction().substring(0, 1) : "?",
                cvss.getScope() != null ? cvss.getScope().substring(0, 1) : "?",
                cvss.getConfidentialityImpact() != null ? cvss.getConfidentialityImpact().substring(0, 1) : "?",
                cvss.getIntegrityImpact() != null ? cvss.getIntegrityImpact().substring(0, 1) : "?",
                cvss.getAvailabilityImpact() != null ? cvss.getAvailabilityImpact().substring(0, 1) : "?");
    }

    /**
     * Build CVSS v2 vector string.
     */
    private String buildCvssV2Vector(CvssV2 cvss) {
        return String.format("AV:%s/AC:%s/Au:%s/C:%s/I:%s/A:%s",
                cvss.getAccessVector() != null ? cvss.getAccessVector().substring(0, 1) : "?",
                cvss.getAccessComplexity() != null ? cvss.getAccessComplexity().substring(0, 1) : "?",
                cvss.getAuthentication() != null ? cvss.getAuthentication().substring(0, 1) : "?",
                cvss.getConfidentialityImpact() != null ? cvss.getConfidentialityImpact().substring(0, 1) : "?",
                cvss.getIntegrityImpact() != null ? cvss.getIntegrityImpact().substring(0, 1) : "?",
                cvss.getAvailabilityImpact() != null ? cvss.getAvailabilityImpact().substring(0, 1) : "?");
    }

    /**
     * Build CVSS v4 vector string.
     * Note: CvssV4 model is simplified, so we use vectorString if available
     */
    private String buildCvssV4Vector(CvssV4 cvss) {
        // If vectorString is available, use it directly
        if (cvss.getVectorString() != null && !cvss.getVectorString().isEmpty()) {
            return cvss.getVectorString();
        }

        // Otherwise build a simplified vector from available fields
        return String.format("CVSS:4.0/AV:%s/AC:%s/PR:%s/UI:%s",
                cvss.getAttackVector() != null ? cvss.getAttackVector().substring(0, 1) : "?",
                cvss.getAttackComplexity() != null ? cvss.getAttackComplexity().substring(0, 1) : "?",
                cvss.getPrivilegesRequired() != null ? cvss.getPrivilegesRequired().substring(0, 1) : "?",
                cvss.getUserInteraction() != null ? cvss.getUserInteraction().substring(0, 1) : "?");
    }

    /**
     * Extract published date from vulnerability.
     * Note: OWASP model doesn't have vulnerabilityIds, so return null for now
     */
    private String extractPublishedDate(OwaspVulnerability vuln) {
        // Published date not available in current OWASP model
        return null;
    }

    /**
     * Build affected packages list from dependency.
     */
    private List<AffectedPackage> buildAffectedPackages(OwaspDependency dep) {
        List<AffectedPackage> packages = new ArrayList<>();

        if (dep.getPackages() != null) {
            for (org.wildfly.security.sca.reportconverter.model.owasp.Package pkg : dep.getPackages()) {
                AffectedPackage affectedPkg = new AffectedPackage();
                affectedPkg.setPackageUrl(pkg.getId());

                // Parse purl to extract components
                if (pkg.getId() != null) {
                    String[] parts = pkg.getId().split("/");
                    if (parts.length >= 2) {
                        String[] nameParts = parts[1].split("@");
                        if (nameParts.length >= 2) {
                            String[] groupArtifact = nameParts[0].split(":");
                            if (groupArtifact.length >= 2) {
                                affectedPkg.setGroupId(groupArtifact[0]);
                                affectedPkg.setArtifactId(groupArtifact[1]);
                            }
                            affectedPkg.setVersion(nameParts[1]);
                        }
                    }
                }

                affectedPkg.setFilePath(dep.getFilePath());
                packages.add(affectedPkg);
            }
        }

        // If no packages found, create one from dependency info
        if (packages.isEmpty()) {
            AffectedPackage affectedPkg = new AffectedPackage();
            affectedPkg.setFilePath(dep.getFilePath());
            affectedPkg.setArtifactId(dep.getFileName());
            packages.add(affectedPkg);
        }

        return packages;
    }

    /**
     * Map OWASP references to generic format, deduplicating by URL.
     */
    private List<org.wildfly.security.sca.reportconverter.model.generic.Reference> mapReferences(
            List<org.wildfly.security.sca.reportconverter.model.owasp.Reference> owaspRefs) {

        if (owaspRefs == null) {
            return new ArrayList<>();
        }

        // Deduplicate by URL while preserving source
        Map<String, org.wildfly.security.sca.reportconverter.model.generic.Reference> refMap = new LinkedHashMap<>();

        for (org.wildfly.security.sca.reportconverter.model.owasp.Reference owaspRef : owaspRefs) {
            if (owaspRef.getUrl() != null && !refMap.containsKey(owaspRef.getUrl())) {
                org.wildfly.security.sca.reportconverter.model.generic.Reference ref =
                    new org.wildfly.security.sca.reportconverter.model.generic.Reference();
                ref.setUrl(owaspRef.getUrl());
                ref.setSource(owaspRef.getSource());
                ref.setName(owaspRef.getName());
                refMap.put(owaspRef.getUrl(), ref);
            }
        }

        return new ArrayList<>(refMap.values());
    }

    /**
     * Calculate summary statistics from vulnerabilities.
     */
    private Summary calculateSummary(List<Vulnerability> vulnerabilities) {
        Summary summary = new Summary();

        for (Vulnerability vuln : vulnerabilities) {
            if (vuln.isSuppressed()) {
                summary.setSuppressed(summary.getSuppressed() + 1);
            } else {
                summary.setActive(summary.getActive() + 1);

                // Count by severity
                if (vuln.getSeverity() != null) {
                    switch (vuln.getSeverity().toUpperCase()) {
                        case "CRITICAL":
                            summary.setCritical(summary.getCritical() + 1);
                            break;
                        case "HIGH":
                            summary.setHigh(summary.getHigh() + 1);
                            break;
                        case "MEDIUM":
                            summary.setMedium(summary.getMedium() + 1);
                            break;
                        case "LOW":
                            summary.setLow(summary.getLow() + 1);
                            break;
                    }
                }
            }
        }

        return summary;
    }
}

// Made with Bob
