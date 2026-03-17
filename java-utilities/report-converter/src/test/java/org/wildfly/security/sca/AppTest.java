/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.security.sca;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the CLI application (App class).
 * Tests command-line argument parsing, validation, and behavior.
 *
 * Note: These tests verify application behavior through output validation
 * rather than exit codes, since SecurityManager (used to capture System.exit())
 * is deprecated in Java 17+ and removed in Java 21+.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
class AppTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testSuccessfulConversionOutput() throws IOException {
        // Given: Valid input file and arguments
        Path inputPath = getTestResourcePath("minimal-report.json");
        Path outputPath = tempDir.resolve("output.json");

        String[] args = {
            "--input", inputPath.toString(),
            "--output", outputPath.toString(),
            "--version", "39.0.1.Final"
        };

        // When: Run the application (will call System.exit, but we verify output)
        Thread testThread = new Thread(() -> App.main(args));
        testThread.start();

        try {
            testThread.join(5000); // Wait up to 5 seconds
        } catch (InterruptedException e) {
            fail("Test thread was interrupted");
        }

        // Then: Verify success indicators in output
        assertTrue(Files.exists(outputPath), "Output file should be created");
        String output = outContent.toString();
        assertTrue(output.contains("Starting conversion"), "Should show starting message");
        assertTrue(output.contains("Conversion complete") || output.contains("vulnerabilities"),
                "Should show completion or results");
        assertTrue(output.contains("39.0.1.Final"), "Should show WildFly version");
    }

    @Test
    void testMissingInputArgumentShowsError() {
        // Given: Missing --input argument
        String[] args = {
            "--output", tempDir.resolve("output.json").toString(),
            "--version", "39.0.1.Final"
        };

        // When: Run the application
        Thread testThread = new Thread(() -> App.main(args));
        testThread.start();

        try {
            testThread.join(2000);
        } catch (InterruptedException e) {
            fail("Test thread was interrupted");
        }

        // Then: Should show error about missing argument
        String error = errContent.toString();
        assertTrue(error.contains("Invalid arguments") || error.contains("Missing required option"),
                "Should show error about missing argument");
    }

    @Test
    void testMissingOutputArgumentShowsError() {
        // Given: Missing --output argument
        Path inputPath = getTestResourcePath("minimal-report.json");
        String[] args = {
            "--input", inputPath.toString(),
            "--version", "39.0.1.Final"
        };

        // When: Run the application
        Thread testThread = new Thread(() -> App.main(args));
        testThread.start();

        try {
            testThread.join(2000);
        } catch (InterruptedException e) {
            fail("Test thread was interrupted");
        }

        // Then: Should show error
        String error = errContent.toString();
        assertTrue(error.contains("Invalid arguments") || error.contains("Missing required option"),
                "Should show error about missing argument");
    }

    @Test
    void testMissingVersionArgumentShowsError() {
        // Given: Missing --version argument
        Path inputPath = getTestResourcePath("minimal-report.json");
        String[] args = {
            "--input", inputPath.toString(),
            "--output", tempDir.resolve("output.json").toString()
        };

        // When: Run the application
        Thread testThread = new Thread(() -> App.main(args));
        testThread.start();

        try {
            testThread.join(2000);
        } catch (InterruptedException e) {
            fail("Test thread was interrupted");
        }

        // Then: Should show error
        String error = errContent.toString();
        assertTrue(error.contains("Invalid arguments") || error.contains("Missing required option"),
                "Should show error about missing argument");
    }

    @Test
    void testHelpFlagShowsUsage() {
        // Given: --help flag
        String[] args = {"--help"};

        // When: Run the application
        Thread testThread = new Thread(() -> App.main(args));
        testThread.start();

        try {
            testThread.join(2000);
        } catch (InterruptedException e) {
            fail("Test thread was interrupted");
        }

        // Then: Should show help
        String output = outContent.toString();
        assertTrue(output.contains("usage:") || output.contains("Convert OWASP"),
                "Should show help message");
        assertTrue(output.contains("--input") || output.contains("-i"),
                "Should show input option");
        assertTrue(output.contains("--output") || output.contains("-o"),
                "Should show output option");
        assertTrue(output.contains("--version") || output.contains("-v"),
                "Should show version option");
    }

    @Test
    void testVerboseFlagInOutput() throws IOException {
        // Given: Valid arguments with --verbose flag
        Path inputPath = getTestResourcePath("minimal-report.json");
        Path outputPath = tempDir.resolve("output.json");

        String[] args = {
            "--verbose",
            "--input", inputPath.toString(),
            "--output", outputPath.toString(),
            "--version", "39.0.1.Final"
        };

        // When: Run the application
        Thread testThread = new Thread(() -> App.main(args));
        testThread.start();

        try {
            testThread.join(5000);
        } catch (InterruptedException e) {
            fail("Test thread was interrupted");
        }

        // Then: Should complete successfully
        assertTrue(Files.exists(outputPath), "Output file should be created");
    }

    @Test
    void testSkipSuppressedFlagInOutput() throws IOException {
        // Given: Report with suppressed vulnerabilities and --skip-suppressed flag
        Path inputPath = getTestResourcePath("report-with-suppressed.json");
        Path outputPath = tempDir.resolve("output.json");

        String[] args = {
            "--input", inputPath.toString(),
            "--output", outputPath.toString(),
            "--version", "39.0.1.Final",
            "--skip-suppressed"
        };

        // When: Run the application
        Thread testThread = new Thread(() -> App.main(args));
        testThread.start();

        try {
            testThread.join(5000);
        } catch (InterruptedException e) {
            fail("Test thread was interrupted");
        }

        // Then: Should complete successfully and show flag
        assertTrue(Files.exists(outputPath), "Output file should be created");
        String output = outContent.toString();
        assertTrue(output.contains("Skip Suppressed CVEs: true"),
                "Should show skip-suppressed flag is enabled");
    }

    @Test
    void testNonExistentInputFileShowsError() {
        // Given: Non-existent input file
        Path nonExistentPath = tempDir.resolve("non-existent.json");
        String[] args = {
            "--input", nonExistentPath.toString(),
            "--output", tempDir.resolve("output.json").toString(),
            "--version", "39.0.1.Final"
        };

        // When: Run the application
        Thread testThread = new Thread(() -> App.main(args));
        testThread.start();

        try {
            testThread.join(2000);
        } catch (InterruptedException e) {
            fail("Test thread was interrupted");
        }

        // Then: Should show I/O error
        String error = errContent.toString();
        assertTrue(error.contains("I/O error") || error.contains("does not exist"),
                "Should show I/O error message");
    }

    @Test
    void testMalformedInputFileShowsError() {
        // Given: Malformed JSON input file
        Path malformedPath = getTestResourcePath("malformed-report.json");
        String[] args = {
            "--input", malformedPath.toString(),
            "--output", tempDir.resolve("output.json").toString(),
            "--version", "39.0.1.Final"
        };

        // When: Run the application
        Thread testThread = new Thread(() -> App.main(args));
        testThread.start();

        try {
            testThread.join(2000);
        } catch (InterruptedException e) {
            fail("Test thread was interrupted");
        }

        // Then: Should show error
        String error = errContent.toString();
        assertTrue(error.contains("error"), "Should show error message");
    }

    @Test
    void testShortOptionFlags() throws IOException {
        // Given: Valid arguments using short option flags
        Path inputPath = getTestResourcePath("minimal-report.json");
        Path outputPath = tempDir.resolve("output.json");

        String[] args = {
            "-i", inputPath.toString(),
            "-o", outputPath.toString(),
            "-v", "39.0.1.Final"
        };

        // When: Run the application
        Thread testThread = new Thread(() -> App.main(args));
        testThread.start();

        try {
            testThread.join(5000);
        } catch (InterruptedException e) {
            fail("Test thread was interrupted");
        }

        // Then: Should complete successfully
        assertTrue(Files.exists(outputPath), "Output file should be created");
    }

    @Test
    void testOutputShowsVulnerabilityCounts() throws IOException {
        // Given: Valid input with vulnerabilities
        Path inputPath = getTestResourcePath("report-with-suppressed.json");
        Path outputPath = tempDir.resolve("output.json");

        String[] args = {
            "--input", inputPath.toString(),
            "--output", outputPath.toString(),
            "--version", "39.0.1.Final"
        };

        // When: Run the application
        Thread testThread = new Thread(() -> App.main(args));
        testThread.start();

        try {
            testThread.join(5000);
        } catch (InterruptedException e) {
            fail("Test thread was interrupted");
        }

        // Then: Should show vulnerability counts
        String output = outContent.toString();
        assertTrue(output.contains("vulnerabilities"), "Should mention vulnerabilities");
        assertTrue(output.contains("active"), "Should show active count");
        assertTrue(output.contains("suppressed"), "Should show suppressed count");
        assertTrue(output.contains("Severity breakdown"), "Should show severity breakdown");
    }

    @Test
    void testOutputShowsDependencyCount() throws IOException {
        // Given: Valid input
        Path inputPath = getTestResourcePath("minimal-report.json");
        Path outputPath = tempDir.resolve("output.json");

        String[] args = {
            "--input", inputPath.toString(),
            "--output", outputPath.toString(),
            "--version", "39.0.1.Final"
        };

        // When: Run the application
        Thread testThread = new Thread(() -> App.main(args));
        testThread.start();

        try {
            testThread.join(5000);
        } catch (InterruptedException e) {
            fail("Test thread was interrupted");
        }

        // Then: Should show dependency count
        String output = outContent.toString();
        assertTrue(output.contains("Parsed") && output.contains("dependencies"),
                "Should show parsed dependency count");
    }

    /**
     * Helper method to get test resource path.
     */
    private Path getTestResourcePath(String resourceName) {
        return Paths.get("src/test/resources", resourceName);
    }
}

// Made with Bob