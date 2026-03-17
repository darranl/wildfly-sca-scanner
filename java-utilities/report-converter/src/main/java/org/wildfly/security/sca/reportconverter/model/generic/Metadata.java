/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.sca.reportconverter.model.generic;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Metadata for the generic CVE report.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class Metadata {

    @JsonProperty("scanDate")
    private String scanDate;

    @JsonProperty("wildflyVersion")
    private String wildflyVersion;

    @JsonProperty("scanners")
    private List<Scanner> scanners;

    @JsonProperty("totalDependencies")
    private Integer totalDependencies;

    @JsonProperty("vulnerableDependencies")
    private Integer vulnerableDependencies;

    public Metadata() {
    }

    public String getScanDate() {
        return scanDate;
    }

    public void setScanDate(String scanDate) {
        this.scanDate = scanDate;
    }

    public String getWildflyVersion() {
        return wildflyVersion;
    }

    public void setWildflyVersion(String wildflyVersion) {
        this.wildflyVersion = wildflyVersion;
    }

    public List<Scanner> getScanners() {
        return scanners;
    }

    public void setScanners(List<Scanner> scanners) {
        this.scanners = scanners;
    }

    public Integer getTotalDependencies() {
        return totalDependencies;
    }

    public void setTotalDependencies(Integer totalDependencies) {
        this.totalDependencies = totalDependencies;
    }

    public Integer getVulnerableDependencies() {
        return vulnerableDependencies;
    }

    public void setVulnerableDependencies(Integer vulnerableDependencies) {
        this.vulnerableDependencies = vulnerableDependencies;
    }
}

// Made with Bob
