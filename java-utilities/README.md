# WildFly SCA Scanner - Java Utilities

Java utilities for the WildFly SCA Scanner project, providing tools for converting and managing security vulnerability reports and suppressions.

## Overview

This project contains two Maven modules:

1. **report-converter** - Converts OWASP Dependency Check reports to a generic JSON format
2. **suppression-converter** - Bidirectional converter between JSON and OWASP XML suppression formats

## Project Structure

```
java-utilities/
├── pom.xml                        # Parent POM
├── README.md                      # This file
├── report-converter/              # Report conversion utility
│   ├── pom.xml
│   └── src/
│       ├── main/java/
│       └── test/java/
└── suppression-converter/         # Suppression conversion utility
    ├── pom.xml
    └── src/
        ├── main/java/
        └── test/java/
```

## Requirements

- **Java**: 25 or higher
- **Maven**: 3.9 or higher
- **Build Tool**: Maven (managed via sdkman recommended)

## Dependencies

All modules use the following dependencies (versions managed in parent POM):

- **JUnit 5** (5.11.0) - Unit testing framework
- **Jackson** (2.18.2) - JSON processing
- **Apache Commons CLI** (1.9.0) - Command-line argument parsing
- **SLF4J** (2.0.16) - Logging facade

## Building

### Build All Modules

```bash
cd java-utilities
mvn clean install
```

### Build Without Tests

```bash
mvn clean install -DskipTests
```

### Build Specific Module

```bash
cd report-converter
mvn clean install
```

### Run Tests Only

```bash
mvn test
```

## Maven Coordinates

### Parent POM

```xml
<groupId>org.wildfly.security.sca</groupId>
<artifactId>java-utilities</artifactId>
<version>1.0.0-SNAPSHOT</version>
<packaging>pom</packaging>
```

### Report Converter Module

```xml
<groupId>org.wildfly.security.sca</groupId>
<artifactId>report-converter</artifactId>
<version>1.0.0-SNAPSHOT</version>
```

### Suppression Converter Module

```xml
<groupId>org.wildfly.security.sca</groupId>
<artifactId>suppression-converter</artifactId>
<version>1.0.0-SNAPSHOT</version>
```

## Module Descriptions

### Report Converter

Converts OWASP Dependency Check JSON reports to a generic, tool-agnostic JSON format suitable for:
- Web dashboards
- CI/CD integration
- Custom reporting tools
- Multi-tool aggregation

**Main Class**: `org.wildfly.security.sca.App`

**Features**:
- Parses OWASP Dependency Check JSON reports
- Generates standardized generic JSON format
- Handles CVSS v2, v3, and v4 scores
- Tracks both active and suppressed vulnerabilities
- **Optional suppression filtering** - Skip suppressed CVEs to reduce report size
- Calculates summary statistics

**Basic Usage**:
```bash
java -jar report-converter/target/report-converter.jar \
  --input dependency-check-report.json \
  --output wildfly-36.0.1.Final-cve-report.json \
  --version 36.0.1.Final
```

**Skip Suppressed CVEs** (reduces report size):
```bash
java -jar report-converter/target/report-converter.jar \
  --input dependency-check-report.json \
  --output wildfly-36.0.1.Final-cve-report.json \
  --version 36.0.1.Final \
  --skip-suppressed
```

**Options**:
- `--input, -i` - Path to OWASP JSON report (required)
- `--output, -o` - Path for output generic JSON (required)
- `--version, -v` - WildFly version being scanned (required)
- `--skip-suppressed` - Skip suppressed CVEs from output (optional)
- `--verbose` - Enable verbose logging (optional)
- `--help, -h` - Display help message (optional)

### Suppression Converter

Bidirectional converter between JSON and OWASP XML suppression formats with version filtering support.

**Main Class**: `org.wildfly.security.sca.suppressionconverter.Main`

**Usage** (once implemented):

Convert JSON to XML (with version filtering):
```bash
java -jar suppression-converter/target/suppression-converter-1.0.0-SNAPSHOT.jar \
  --input suppressions.json \
  --output owasp-suppressions.xml \
  --version 36.0.1.Final
```

Convert XML to JSON (bulk migration):
```bash
java -jar suppression-converter/target/suppression-converter-1.0.0-SNAPSHOT.jar \
  --input owasp-suppressions.xml \
  --output suppressions.json \
  --infer-versions
```

## Development

### Project Setup

The project was created using Maven archetypes and configured with:
- Java 25 compiler target
- JUnit 5 for testing
- Dependency management in parent POM
- Executable JAR configuration

### Adding Dependencies

Dependencies are managed in the parent POM's `<dependencyManagement>` section. To add a dependency to a module:

1. Add version to parent POM properties (if not already present)
2. Add dependency to parent POM's `<dependencyManagement>`
3. Reference dependency in module POM (without version)

Example:
```xml
<!-- In module pom.xml -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <!-- Version inherited from parent -->
</dependency>
```

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific module
cd report-converter
mvn test

# Run specific test class
mvn test -Dtest=OwaspReportParserTest

# Run with debug output
mvn test -X
```

### Test Coverage with JaCoCo

The project uses [JaCoCo](https://www.jacoco.org/) for test coverage analysis. Coverage reports are automatically generated during the test phase.

```bash
# Run tests with coverage report
mvn clean test jacoco:report

# View coverage report in browser
# For report-converter:
open report-converter/target/site/jacoco/index.html

# For suppression-converter:
open suppression-converter/target/site/jacoco/index.html
```

**Coverage Reports Include**:
- Line coverage percentage
- Branch coverage percentage
- Complexity metrics
- Detailed per-class and per-method coverage

**Current Coverage** (report-converter):
- **40 tests** - All passing ✅
- **Core logic coverage**: 80%+ for parser and generator
- **Critical paths**: 100% coverage for error handling

For detailed test documentation, see [report-converter/TEST_COVERAGE.md](report-converter/TEST_COVERAGE.md).

### Code Quality

```bash
# Show dependency tree
mvn dependency:tree

# Analyze dependencies
mvn dependency:analyze

# Check for dependency updates
mvn versions:display-dependency-updates

# Check for plugin updates
mvn versions:display-plugin-updates
```

## Useful Maven Commands

```bash
# Clean build artifacts
mvn clean

# Package without installing
mvn package

# Install to local repository
mvn install

# Build in parallel (4 threads)
mvn clean install -T 4

# Build offline (after first build)
mvn clean install -o

# Show effective POM
mvn help:effective-pom

# Validate POM
mvn validate
```

## Troubleshooting

### Build Fails with Dependency Errors

```bash
# Clear local repository cache
mvn dependency:purge-local-repository

# Force update snapshots
mvn clean install -U
```

### Java Version Issues

Ensure Java 25 is active:
```bash
java -version
# Should show: openjdk version "25.0.2"

# If using sdkman:
sdk list java
sdk use java 25.0.2-tem
```

### Maven Not Found

If using sdkman:
```bash
sdk list maven
sdk install maven
sdk use maven 3.9.13
```

## Integration with WildFly SCA Scanner

These utilities are designed to integrate with the WildFly SCA Scanner GitHub Actions workflow:

1. **Report Converter** - Converts OWASP scan results to generic format for dashboard
2. **Suppression Converter** - Generates version-specific suppressions for each WildFly version scanned

## Contributing

See the main project's [CONTRIBUTING.md](../CONTRIBUTING.md) for contribution guidelines.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE.txt](../LICENSE.txt) file for details.

## Related Documentation

- [Project Context](../../notes/CONTEXT.md) - High-level project overview
- [Implementation Plan](../../notes/IMPLEMENTATION_PLAN.md) - Detailed implementation guide
- [Maven Setup Plan](../../notes/MAVEN_SETUP_PLAN.md) - Maven project setup details
- [Maven Commands Reference](../../notes/MAVEN_COMMANDS_REFERENCE.md) - Comprehensive Maven command guide

## Status

**Current Status**: Project structure created, ready for implementation

**Next Steps**:
1. Implement report converter functionality
2. Implement suppression converter functionality
3. Add comprehensive unit tests
4. Integrate with GitHub Actions workflow

---

**Created**: 2026-03-17
**Version**: 1.0.0-SNAPSHOT
**Maintainer**: WildFly Security Team