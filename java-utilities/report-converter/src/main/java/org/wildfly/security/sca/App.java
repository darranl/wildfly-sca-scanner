/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.sca;

import org.apache.commons.cli.*;
import org.wildfly.security.sca.reportconverter.generator.GenericReportGenerator;
import org.wildfly.security.sca.reportconverter.model.generic.GenericReport;
import org.wildfly.security.sca.reportconverter.model.owasp.OwaspReport;
import org.wildfly.security.sca.reportconverter.parser.OwaspReportParser;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main CLI application for the OWASP to Generic report converter.
 * Provides command-line interface for converting OWASP Dependency Check reports
 * to a tool-agnostic generic format.
 */
public class App {

    public static void main(String[] args) {
        Options options = buildOptions();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                printHelp(options);
                System.exit(0);
            }

            // Validate required arguments
            validateRequiredArgs(cmd);

            // Configure logging level
            if (cmd.hasOption("verbose")) {
                System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
            }

            // Execute conversion
            Path inputPath = Paths.get(cmd.getOptionValue("input"));
            Path outputPath = Paths.get(cmd.getOptionValue("output"));
            String version = cmd.getOptionValue("version");

            System.out.println("Starting conversion...");
            System.out.println("Input: " + inputPath);
            System.out.println("Output: " + outputPath);
            System.out.println("WildFly Version: " + version);

            // Parse OWASP report
            OwaspReportParser reportParser = new OwaspReportParser();
            OwaspReport owaspReport = reportParser.parse(inputPath);
            reportParser.validate(owaspReport);

            System.out.println("Parsed " + owaspReport.getDependencies().size() + " dependencies");

            // Generate generic report
            GenericReportGenerator generator = new GenericReportGenerator();
            GenericReport genericReport = generator.convert(owaspReport, version);
            generator.writeToFile(genericReport, outputPath);

            System.out.println("Conversion complete!");
            System.out.println("Found " + genericReport.getVulnerabilities().size() +
                             " vulnerabilities (" + genericReport.getSummary().getActive() +
                             " active, " + genericReport.getSummary().getSuppressed() + " suppressed)");
            System.out.println("Severity breakdown:");
            System.out.println("  Critical: " + genericReport.getSummary().getCritical());
            System.out.println("  High: " + genericReport.getSummary().getHigh());
            System.out.println("  Medium: " + genericReport.getSummary().getMedium());
            System.out.println("  Low: " + genericReport.getSummary().getLow());

            System.exit(0);

        } catch (ParseException e) {
            System.err.println("Invalid arguments: " + e.getMessage());
            printHelp(options);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            if (args.length > 0 && (args[0].equals("--verbose") || args[0].equals("-verbose"))) {
                e.printStackTrace();
            }
            System.exit(2);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            if (args.length > 0 && (args[0].equals("--verbose") || args[0].equals("-verbose"))) {
                e.printStackTrace();
            }
            System.exit(3);
        }
    }

    /**
     * Build command-line options.
     */
    private static Options buildOptions() {
        Options options = new Options();

        options.addOption(Option.builder("i")
                .longOpt("input")
                .hasArg()
                .required()
                .desc("Path to OWASP Dependency Check JSON report")
                .build());

        options.addOption(Option.builder("o")
                .longOpt("output")
                .hasArg()
                .required()
                .desc("Path for output generic JSON report")
                .build());

        options.addOption(Option.builder("v")
                .longOpt("version")
                .hasArg()
                .required()
                .desc("WildFly version (e.g., 39.0.1.Final)")
                .build());

        options.addOption(Option.builder()
                .longOpt("verbose")
                .desc("Enable verbose logging")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Display this help message")
                .build());

        return options;
    }

    /**
     * Validate that all required arguments are present.
     */
    private static void validateRequiredArgs(CommandLine cmd) throws ParseException {
        if (!cmd.hasOption("input")) {
            throw new ParseException("Missing required option: --input");
        }
        if (!cmd.hasOption("output")) {
            throw new ParseException("Missing required option: --output");
        }
        if (!cmd.hasOption("version")) {
            throw new ParseException("Missing required option: --version");
        }
    }

    /**
     * Print help message.
     */
    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(
                "report-converter",
                "Convert OWASP Dependency Check reports to generic format\n\n",
                options,
                "\nExample: java -jar report-converter.jar -i report.json -o output.json -v 39.0.1.Final\n\n" +
                "Exit Codes:\n" +
                "  0 - Success\n" +
                "  1 - Invalid arguments\n" +
                "  2 - I/O error (file not found, permission denied, etc.)\n" +
                "  3 - Unexpected error (parsing, conversion, etc.)\n",
                true);
    }
}

// Made with Bob
