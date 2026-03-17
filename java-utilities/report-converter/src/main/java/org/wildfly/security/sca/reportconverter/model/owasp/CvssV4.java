package org.wildfly.security.sca.reportconverter.model.owasp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CVSS v4 scoring information.
 *
 * @author WildFly Security Team
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CvssV4 {

    @JsonProperty("baseScore")
    private Double baseScore;

    @JsonProperty("baseSeverity")
    private String baseSeverity;

    @JsonProperty("vectorString")
    private String vectorString;

    @JsonProperty("version")
    private String version;

    // Additional CVSS v4 fields (simplified for now)
    @JsonProperty("attackVector")
    private String attackVector;

    @JsonProperty("attackComplexity")
    private String attackComplexity;

    @JsonProperty("privilegesRequired")
    private String privilegesRequired;

    @JsonProperty("userInteraction")
    private String userInteraction;

    public CvssV4() {
    }

    public Double getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(Double baseScore) {
        this.baseScore = baseScore;
    }

    public String getBaseSeverity() {
        return baseSeverity;
    }

    public void setBaseSeverity(String baseSeverity) {
        this.baseSeverity = baseSeverity;
    }

    public String getVectorString() {
        return vectorString;
    }

    public void setVectorString(String vectorString) {
        this.vectorString = vectorString;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
}

// Made with Bob
