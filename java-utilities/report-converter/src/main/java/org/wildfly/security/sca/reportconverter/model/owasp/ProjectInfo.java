package org.wildfly.security.sca.reportconverter.model.owasp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Project information from OWASP Dependency Check report.
 *
 * @author WildFly Security Team
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectInfo {

    @JsonProperty("name")
    private String name;

    @JsonProperty("reportDate")
    private String reportDate;

    @JsonProperty("credits")
    private Map<String, String> credits;

    public ProjectInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public Map<String, String> getCredits() {
        return credits;
    }

    public void setCredits(Map<String, String> credits) {
        this.credits = credits;
    }
}

// Made with Bob
