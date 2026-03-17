/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.sca.reportconverter.model.generic;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Scanner information in the generic report.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class Scanner {

    @JsonProperty("name")
    private String name;

    @JsonProperty("version")
    private String version;

    public Scanner() {
    }

    public Scanner(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

// Made with Bob
