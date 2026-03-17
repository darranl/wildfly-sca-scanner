package org.wildfly.security.sca.reportconverter.model.generic;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Root object for the generic CVE report format.
 * This is the tool-agnostic output format.
 *
 * @author WildFly Security Team
 */
public class GenericReport {

    @JsonProperty("schemaVersion")
    private String schemaVersion;

    @JsonProperty("metadata")
    private Metadata metadata;

    @JsonProperty("vulnerabilities")
    private List<Vulnerability> vulnerabilities;

    @JsonProperty("summary")
    private Summary summary;

    public GenericReport() {
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }
}

// Made with Bob
