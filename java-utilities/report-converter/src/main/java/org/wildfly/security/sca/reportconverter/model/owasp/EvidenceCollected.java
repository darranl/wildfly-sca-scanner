package org.wildfly.security.sca.reportconverter.model.owasp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Evidence collected during OWASP scan (we don't need to parse this in detail).
 *
 * @author WildFly Security Team
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EvidenceCollected {

    @JsonProperty("vendorEvidence")
    private List<Evidence> vendorEvidence;

    @JsonProperty("productEvidence")
    private List<Evidence> productEvidence;

    @JsonProperty("versionEvidence")
    private List<Evidence> versionEvidence;

    public EvidenceCollected() {
    }

    public List<Evidence> getVendorEvidence() {
        return vendorEvidence;
    }

    public void setVendorEvidence(List<Evidence> vendorEvidence) {
        this.vendorEvidence = vendorEvidence;
    }

    public List<Evidence> getProductEvidence() {
        return productEvidence;
    }

    public void setProductEvidence(List<Evidence> productEvidence) {
        this.productEvidence = productEvidence;
    }

    public List<Evidence> getVersionEvidence() {
        return versionEvidence;
    }

    public void setVersionEvidence(List<Evidence> versionEvidence) {
        this.versionEvidence = versionEvidence;
    }
}

// Made with Bob
