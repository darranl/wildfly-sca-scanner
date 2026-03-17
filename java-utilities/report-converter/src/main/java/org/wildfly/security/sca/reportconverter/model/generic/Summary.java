/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.sca.reportconverter.model.generic;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Summary statistics for the generic report.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class Summary {

    @JsonProperty("critical")
    private int critical;

    @JsonProperty("high")
    private int high;

    @JsonProperty("medium")
    private int medium;

    @JsonProperty("low")
    private int low;

    @JsonProperty("suppressed")
    private int suppressed;

    @JsonProperty("active")
    private int active;

    public Summary() {
    }

    public int getCritical() {
        return critical;
    }

    public void setCritical(int critical) {
        this.critical = critical;
    }

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public int getMedium() {
        return medium;
    }

    public void setMedium(int medium) {
        this.medium = medium;
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public int getSuppressed() {
        return suppressed;
    }

    public void setSuppressed(int suppressed) {
        this.suppressed = suppressed;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }
}

// Made with Bob
