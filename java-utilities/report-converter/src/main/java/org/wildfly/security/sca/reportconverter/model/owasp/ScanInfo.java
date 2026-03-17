/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.sca.reportconverter.model.owasp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Scan information from OWASP Dependency Check report.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanInfo {

    @JsonProperty("engineVersion")
    private String engineVersion;

    @JsonProperty("dataSource")
    private List<DataSource> dataSource;

    public ScanInfo() {
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    public List<DataSource> getDataSource() {
        return dataSource;
    }

    public void setDataSource(List<DataSource> dataSource) {
        this.dataSource = dataSource;
    }
}

// Made with Bob
