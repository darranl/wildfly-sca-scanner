package org.wildfly.security.sca.reportconverter.model.owasp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CVSS v3 scoring information.
 *
 * @author WildFly Security Team
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CvssV3 {

    @JsonProperty("baseScore")
    private Double baseScore;

    @JsonProperty("attackVector")
    private String attackVector;

    @JsonProperty("attackComplexity")
    private String attackComplexity;

    @JsonProperty("privilegesRequired")
    private String privilegesRequired;

    @JsonProperty("userInteraction")
    private String userInteraction;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("confidentialityImpact")
    private String confidentialityImpact;

    @JsonProperty("integrityImpact")
    private String integrityImpact;

    @JsonProperty("availabilityImpact")
    private String availabilityImpact;

    @JsonProperty("baseSeverity")
    private String baseSeverity;

    @JsonProperty("version")
    private String version;

    @JsonProperty("exploitabilityScore")
    private String exploitabilityScore;

    @JsonProperty("impactScore")
    private String impactScore;

    public CvssV3() {
    }

    public Double getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(Double baseScore) {
        this.baseScore = baseScore;
    }

    public String getAttackVector() {
        return attackVector;
    }

    public void setAttackVector(String attackVector) {
        this.attackVector = attackVector;
    }

    public String getAttackComplexity() {
        return attackComplexity;
    }

    public void setAttackComplexity(String attackComplexity) {
        this.attackComplexity = attackComplexity;
    }

    public String getPrivilegesRequired() {
        return privilegesRequired;
    }

    public void setPrivilegesRequired(String privilegesRequired) {
        this.privilegesRequired = privilegesRequired;
    }

    public String getUserInteraction() {
        return userInteraction;
    }

    public void setUserInteraction(String userInteraction) {
        this.userInteraction = userInteraction;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
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

    public String getBaseSeverity() {
        return baseSeverity;
    }

    public void setBaseSeverity(String baseSeverity) {
        this.baseSeverity = baseSeverity;
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
}

// Made with Bob
