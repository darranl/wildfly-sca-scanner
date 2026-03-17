/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.sca.reportconverter.model.owasp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Individual evidence item (we don't need to parse this in detail for conversion).
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Evidence {

    @JsonProperty("type")
    private String type;

    @JsonProperty("confidence")
    private String confidence;

    @JsonProperty("source")
    private String source;

    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;

    public Evidence() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

// Made with Bob
