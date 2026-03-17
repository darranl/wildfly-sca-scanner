package org.wildfly.security.sca.reportconverter.model.generic;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Reference link for a vulnerability in the generic report.
 *
 * @author WildFly Security Team
 */
public class Reference {

    @JsonProperty("url")
    private String url;

    @JsonProperty("source")
    private String source;

    @JsonProperty("name")
    private String name;

    public Reference() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
}

// Made with Bob
