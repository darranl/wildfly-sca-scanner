/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.sca.reportconverter.model.owasp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Vulnerable software information from OWASP report.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VulnerableSoftware {

    @JsonProperty("software")
    private Software software;

    public VulnerableSoftware() {
    }

    public Software getSoftware() {
        return software;
    }

    public void setSoftware(Software software) {
        this.software = software;
    }

    /**
     * Inner class representing software details.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Software {

        @JsonProperty("id")
        private String id;

        @JsonProperty("vulnerabilityIdMatched")
        private String vulnerabilityIdMatched;

        @JsonProperty("versionStartIncluding")
        private String versionStartIncluding;

        @JsonProperty("versionEndExcluding")
        private String versionEndExcluding;

        @JsonProperty("versionEndIncluding")
        private String versionEndIncluding;

        public Software() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getVulnerabilityIdMatched() {
            return vulnerabilityIdMatched;
        }

        public void setVulnerabilityIdMatched(String vulnerabilityIdMatched) {
            this.vulnerabilityIdMatched = vulnerabilityIdMatched;
        }

        public String getVersionStartIncluding() {
            return versionStartIncluding;
        }

        public void setVersionStartIncluding(String versionStartIncluding) {
            this.versionStartIncluding = versionStartIncluding;
        }

        public String getVersionEndExcluding() {
            return versionEndExcluding;
        }

        public void setVersionEndExcluding(String versionEndExcluding) {
            this.versionEndExcluding = versionEndExcluding;
        }

        public String getVersionEndIncluding() {
            return versionEndIncluding;
        }

        public void setVersionEndIncluding(String versionEndIncluding) {
            this.versionEndIncluding = versionEndIncluding;
        }
    }
}

// Made with Bob
