/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.sca.reportconverter.parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.wildfly.security.sca.reportconverter.model.owasp.OwaspReport;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Parser for OWASP Dependency Check JSON reports.
 * Converts JSON format into Java objects using Jackson.
 */
public class OwaspReportParser {
    private final ObjectMapper objectMapper;

    /**
     * Create a new parser with default configuration.
     */
    public OwaspReportParser() {
        this.objectMapper = new ObjectMapper();
        // Configure to ignore unknown properties for forward compatibility
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Handle null values gracefully
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * Parse OWASP JSON report from file.
     *
     * @param inputPath Path to OWASP JSON file
     * @return Parsed OwaspReport object
     * @throws IOException if file cannot be read or parsed
     */
    public OwaspReport parse(Path inputPath) throws IOException {
        if (inputPath == null) {
            throw new IllegalArgumentException("Input path cannot be null");
        }
        if (!inputPath.toFile().exists()) {
            throw new IOException("Input file does not exist: " + inputPath);
        }
        if (!inputPath.toFile().canRead()) {
            throw new IOException("Cannot read input file: " + inputPath);
        }

        return objectMapper.readValue(inputPath.toFile(), OwaspReport.class);
    }

    /**
     * Validate report structure.
     *
     * @param report The parsed report
     * @throws IllegalArgumentException if report is invalid
     */
    public void validate(OwaspReport report) {
        if (report == null) {
            throw new IllegalArgumentException("Report cannot be null");
        }
        if (report.getDependencies() == null) {
            throw new IllegalArgumentException("Report must contain dependencies array");
        }
        if (report.getReportSchema() == null || report.getReportSchema().isEmpty()) {
            throw new IllegalArgumentException("Report must contain reportSchema version");
        }
    }
}

// Made with Bob
