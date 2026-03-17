/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.sca.reportconverter.model.owasp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a dependency in the OWASP Dependency Check report.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OwaspDependency {

    @JsonProperty("isVirtual")
    private boolean isVirtual;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("filePath")
    private String filePath;

    @JsonProperty("md5")
    private String md5;

    @JsonProperty("sha1")
    private String sha1;

    @JsonProperty("sha256")
    private String sha256;

    @JsonProperty("description")
    private String description;

    @JsonProperty("license")
    private String license;

    @JsonProperty("evidenceCollected")
    private EvidenceCollected evidenceCollected;

    @JsonProperty("packages")
    private List<Package> packages;

    @JsonProperty("vulnerabilityIds")
    private List<VulnerabilityId> vulnerabilityIds;

    @JsonProperty("vulnerabilities")
    private List<OwaspVulnerability> vulnerabilities;

    @JsonProperty("suppressedVulnerabilities")
    private List<OwaspVulnerability> suppressedVulnerabilities;

    @JsonProperty("suppressedVulnerabilityIds")
    private List<VulnerabilityId> suppressedVulnerabilityIds;

    public OwaspDependency() {
    }

    public boolean isVirtual() {
        return isVirtual;
    }

    public void setVirtual(boolean virtual) {
        isVirtual = virtual;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public EvidenceCollected getEvidenceCollected() {
        return evidenceCollected;
    }

    public void setEvidenceCollected(EvidenceCollected evidenceCollected) {
        this.evidenceCollected = evidenceCollected;
    }

    public List<Package> getPackages() {
        return packages;
    }

    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }

    public List<VulnerabilityId> getVulnerabilityIds() {
        return vulnerabilityIds;
    }

    public void setVulnerabilityIds(List<VulnerabilityId> vulnerabilityIds) {
        this.vulnerabilityIds = vulnerabilityIds;
    }

    public List<OwaspVulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<OwaspVulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public List<OwaspVulnerability> getSuppressedVulnerabilities() {
        return suppressedVulnerabilities;
    }

    public void setSuppressedVulnerabilities(List<OwaspVulnerability> suppressedVulnerabilities) {
        this.suppressedVulnerabilities = suppressedVulnerabilities;
    }

    public List<VulnerabilityId> getSuppressedVulnerabilityIds() {
        return suppressedVulnerabilityIds;
    }

    public void setSuppressedVulnerabilityIds(List<VulnerabilityId> suppressedVulnerabilityIds) {
        this.suppressedVulnerabilityIds = suppressedVulnerabilityIds;
    }
}

// Made with Bob
