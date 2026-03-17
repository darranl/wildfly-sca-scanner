package org.wildfly.security.sca.reportconverter.model.owasp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Scan information from OWASP Dependency Check report.
 *
 * @author WildFly Security Team
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
