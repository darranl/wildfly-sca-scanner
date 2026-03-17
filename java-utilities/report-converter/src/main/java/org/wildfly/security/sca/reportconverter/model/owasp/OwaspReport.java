package org.wildfly.security.sca.reportconverter.model.owasp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Root object representing an OWASP Dependency Check JSON report.
 *
 * @author WildFly Security Team
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OwaspReport {

    @JsonProperty("reportSchema")
    private String reportSchema;

    @JsonProperty("scanInfo")
    private ScanInfo scanInfo;

    @JsonProperty("projectInfo")
    private ProjectInfo projectInfo;

    @JsonProperty("dependencies")
    private List<OwaspDependency> dependencies;

    public OwaspReport() {
    }

    public String getReportSchema() {
        return reportSchema;
    }

    public void setReportSchema(String reportSchema) {
        this.reportSchema = reportSchema;
    }

    public ScanInfo getScanInfo() {
        return scanInfo;
    }

    public void setScanInfo(ScanInfo scanInfo) {
        this.scanInfo = scanInfo;
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }

    public void setProjectInfo(ProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
    }

    public List<OwaspDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<OwaspDependency> dependencies) {
        this.dependencies = dependencies;
    }
}

// Made with Bob
