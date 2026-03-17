package org.wildfly.security.sca.reportconverter.model.owasp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CVSS v2 scoring information.
 *
 * @author WildFly Security Team
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CvssV2 {

    @JsonProperty("score")
    private Double score;

    @JsonProperty("accessVector")
    private String accessVector;

    @JsonProperty("accessComplexity")
    private String accessComplexity;

    @JsonProperty("authenticationr")
    private String authentication;

    @JsonProperty("confidentialityImpact")
    private String confidentialityImpact;

    @JsonProperty("integrityImpact")
    private String integrityImpact;

    @JsonProperty("availabilityImpact")
    private String availabilityImpact;

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("version")
    private String version;

    @JsonProperty("exploitabilityScore")
    private String exploitabilityScore;

    @JsonProperty("impactScore")
    private String impactScore;

    @JsonProperty("userInteractionRequired")
    private String userInteractionRequired;

    public CvssV2() {
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getAccessVector() {
        return accessVector;
    }

    public void setAccessVector(String accessVector) {
        this.accessVector = accessVector;
    }

    public String getAccessComplexity() {
        return accessComplexity;
    }

    public void setAccessComplexity(String accessComplexity) {
        this.accessComplexity = accessComplexity;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public String getConfidentialityImpact() {
        return confidentialityImpact;
    }

    public void setConfidentialityImpact(String confidentialityImpact) {
        this.confidentialityImpact = confidentialityImpact;
    }

    public String getIntegrityImpact() {
        return integrityImpact;
    }

    public void setIntegrityImpact(String integrityImpact) {
        this.integrityImpact = integrityImpact;
    }

    public String getAvailabilityImpact() {
        return availabilityImpact;
    }

    public void setAvailabilityImpact(String availabilityImpact) {
        this.availabilityImpact = availabilityImpact;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getExploitabilityScore() {
        return exploitabilityScore;
    }

    public void setExploitabilityScore(String exploitabilityScore) {
        this.exploitabilityScore = exploitabilityScore;
    }

    public String getImpactScore() {
        return impactScore;
    }

    public void setImpactScore(String impactScore) {
        this.impactScore = impactScore;
    }

    public String getUserInteractionRequired() {
        return userInteractionRequired;
    }

    public void setUserInteractionRequired(String userInteractionRequired) {
        this.userInteractionRequired = userInteractionRequired;
    }
}

// Made with Bob
