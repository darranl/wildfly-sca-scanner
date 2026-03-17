# Report Converter

A command-line utility that converts OWASP Dependency Check JSON reports to a standardized, tool-agnostic generic JSON format.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
  - [Basic Usage](#basic-usage)
  - [Command-Line Options](#command-line-options)
  - [Examples](#examples)
- [Input Format](#input-format)
- [Output Format](#output-format)
- [Performance](#performance)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

## 🎯 Overview

The Report Converter transforms OWASP Dependency Check reports into a standardized format that can be consumed by:
- Web dashboards
- CI/CD pipelines
- Custom reporting tools
- Multi-tool aggregation systems

This decouples your reporting infrastructure from specific scanning tools, making it easy to switch tools or aggregate results from multiple sources.

## ✨ Features

- **Tool-Agnostic Output** - Standardized JSON format independent of scanning tool
- **CVSS Support** - Handles CVSS v2, v3, and v4 scores with intelligent prioritization
- **Suppression Tracking** - Tracks both active and suppressed vulnerabilities
- **Flexible Filtering** - Optional exclusion of suppressed CVEs to reduce report size
- **Summary Statistics** - Automatic calculation of vulnerability counts by severity
- **Metadata Preservation** - Retains scan date, tool version, and project information
- **Robust Error Handling** - Comprehensive validation and clear error messages
- **Fast Processing** - Converts reports in under 5 seconds

## 📦 Installation

### Prerequisites

- **Java 25** or higher
- **Maven 3.9** or higher

### Build from Source

```bash
# Clone the repository
git clone https://github.com/wildfly-security/wildfly-sca-scanner.git
cd wildfly-sca-scanner/java-utilities

# Build the project
mvn clean install

# The executable JAR is created at:
# report-converter/target/report-converter.jar
```

### Verify Installation

```bash
java -jar report-converter/target/report-converter.jar --help
```

## 🚀 Usage

### Basic Usage

```bash
java -jar report-converter.jar \
  --input dependency-check-report.json \
  --output wildfly-39.0.1.Final-cve-report.json \
  --version 39.0.1.Final
```

### Command-Line Options

| Option | Short | Required | Description |
|--------|-------|----------|-------------|
| `--input` | `-i` | ✅ Yes | Path to OWASP Dependency Check JSON report |
| `--output` | `-o` | ✅ Yes | Path for output generic JSON report |
| `--version` | `-v` | ✅ Yes | WildFly version being scanned (e.g., 39.0.1.Final) |
| `--skip-suppressed` | - | ❌ No | Exclude suppressed CVEs from output (reduces file size) |
| `--verbose` | - | ❌ No | Enable verbose logging for debugging |
| `--help` | `-h` | ❌ No | Display help message and exit |

### Examples

#### 1. Basic Conversion

Convert an OWASP report to generic format:

```bash
java -jar report-converter.jar \
  --input /path/to/dependency-check-report.json \
  --output /path/to/wildfly-cve-report.json \
  --version 39.0.1.Final
```

#### 2. Skip Suppressed CVEs

Reduce output file size by excluding suppressed vulnerabilities:

```bash
java -jar report-converter.jar \
  --input dependency-check-report.json \
  --output wildfly-cve-report.json \
  --version 39.0.1.Final \
  --skip-suppressed
```

This is useful when:
- You only care about active vulnerabilities
- You want smaller files for web dashboards
- You're generating public reports

#### 3. Verbose Logging

Enable detailed logging for troubleshooting:

```bash
java -jar report-converter.jar \
  --input dependency-check-report.json \
  --output wildfly-cve-report.json \
  --version 39.0.1.Final \
  --verbose
```

#### 4. Using Short Options

All options have short forms:

```bash
java -jar report-converter.jar \
  -i dependency-check-report.json \
  -o wildfly-cve-report.json \
  -v 39.0.1.Final
```

#### 5. Batch Processing Multiple Versions

Process multiple WildFly versions in a script:

```bash
#!/bin/bash
VERSIONS=("36.0.1.Final" "37.0.1.Final" "38.0.1.Final" "39.0.1.Final")

for VERSION in "${VERSIONS[@]}"; do
  echo "Converting report for WildFly $VERSION..."
  java -jar report-converter.jar \
    --input "reports/dependency-check-${VERSION}.json" \
    --output "reports/wildfly-${VERSION}-cve-report.json" \
    --version "$VERSION" \
    --skip-suppressed
done
```

#### 6. Integration with OWASP Dependency Check

Complete workflow from scan to conversion:

```bash
# Step 1: Run OWASP Dependency Check
dependency-check \
  --project "WildFly 39.0.1.Final" \
  --scan /path/to/wildfly-39.0.1.Final \
  --format JSON \
  --out dependency-check-report.json \
  --suppression owasp-suppressions.xml

# Step 2: Convert to generic format
java -jar report-converter.jar \
  --input dependency-check-report.json \
  --output wildfly-39.0.1.Final-cve-report.json \
  --version 39.0.1.Final

# Step 3: View summary
cat wildfly-39.0.1.Final-cve-report.json | jq .summary
```

## 📥 Input Format

The tool expects OWASP Dependency Check JSON reports (version 4.0+). Example structure:

```json
{
  "reportSchema": "1.1",
  "scanInfo": {
    "engineVersion": "10.0.4"
  },
  "projectInfo": {
    "name": "WildFly 39.0.1.Final"
  },
  "dependencies": [
    {
      "fileName": "example.jar",
      "vulnerabilities": [
        {
          "name": "CVE-2024-1234",
          "severity": "HIGH",
          "cvssv3": {
            "baseScore": 7.5
          }
        }
      ]
    }
  ]
}
```

## 📤 Output Format

The tool generates a standardized generic JSON format. Example:

```json
{
  "schemaVersion": "1.0",
  "metadata": {
    "scanDate": "2026-03-17T15:30:00Z",
    "wildflyVersion": "39.0.1.Final",
    "scanners": [
      {
        "name": "OWASP Dependency Check",
        "version": "10.0.4"
      }
    ],
    "totalDependencies": 450,
    "vulnerableDependencies": 12
  },
  "vulnerabilities": [
    {
      "id": "CVE-2024-1234",
      "title": "Remote Code Execution in Example Library",
      "severity": "HIGH",
      "cvssScore": 7.5,
      "cvssVector": "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N",
      "description": "A vulnerability was found...",
      "affectedPackages": [
        {
          "name": "example-library",
          "version": "1.2.3",
          "ecosystem": "maven"
        }
      ],
      "references": [
        {
          "url": "https://nvd.nist.gov/vuln/detail/CVE-2024-1234",
          "source": "NVD"
        }
      ],
      "suppressed": false
    }
  ],
  "summary": {
    "total": 15,
    "active": 12,
    "suppressed": 3,
    "bySeverity": {
      "CRITICAL": 2,
      "HIGH": 5,
      "MEDIUM": 4,
      "LOW": 1
    }
  }
}
```

For complete schema documentation, see [schemas/SCHEMA_DOCUMENTATION.md](../../schemas/SCHEMA_DOCUMENTATION.md).

## ⚡ Performance

- **Processing Speed**: < 5 seconds for typical reports (100-500 CVEs)
- **Memory Usage**: ~256MB heap for large reports (1000+ CVEs)
- **File Size**: Generic reports are typically 20-30% smaller than OWASP reports
- **With `--skip-suppressed`**: Can reduce output size by 40-60% depending on suppression rate

### Performance Tips

1. **Use `--skip-suppressed`** for public dashboards to reduce file size
2. **Increase heap size** for very large reports: `java -Xmx512m -jar report-converter.jar ...`
3. **Process in parallel** when converting multiple versions

## 🧪 Testing

The report converter has comprehensive test coverage:

- **40 tests total** (all passing ✅)
- **Unit tests**: Parser and generator logic
- **Integration tests**: End-to-end conversion workflows
- **CLI tests**: Command-line interface behavior

### Run Tests

```bash
cd report-converter
mvn test
```

### Run Tests with Coverage

```bash
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

For detailed test documentation, see [TEST_COVERAGE.md](TEST_COVERAGE.md).

## 🔧 Troubleshooting

### Common Issues

#### 1. "File not found" Error

**Problem**: Input file doesn't exist or path is incorrect

**Solution**:
```bash
# Verify file exists
ls -la dependency-check-report.json

# Use absolute path
java -jar report-converter.jar \
  --input /absolute/path/to/dependency-check-report.json \
  --output output.json \
  --version 39.0.1.Final
```

#### 2. "Malformed JSON" Error

**Problem**: Input file is not valid JSON

**Solution**:
```bash
# Validate JSON
cat dependency-check-report.json | jq . > /dev/null

# Check file encoding
file dependency-check-report.json

# Ensure OWASP Dependency Check completed successfully
```

#### 3. "Missing required argument" Error

**Problem**: Required CLI option not provided

**Solution**:
```bash
# All three options are required
java -jar report-converter.jar \
  --input dependency-check-report.json \
  --output wildfly-cve-report.json \
  --version 39.0.1.Final
```

#### 4. Java Version Issues

**Problem**: Wrong Java version

**Solution**:
```bash
# Check Java version
java -version
# Should show: openjdk version "25.0.2" or higher

# If using sdkman
sdk list java
sdk use java 25.0.2-tem
```

#### 5. Out of Memory Error

**Problem**: Large report exceeds default heap size

**Solution**:
```bash
# Increase heap size
java -Xmx512m -jar report-converter.jar \
  --input large-report.json \
  --output output.json \
  --version 39.0.1.Final
```

#### 6. Permission Denied

**Problem**: Cannot write to output directory

**Solution**:
```bash
# Check directory permissions
ls -la /path/to/output/directory

# Create directory if needed
mkdir -p /path/to/output/directory

# Use a writable location
java -jar report-converter.jar \
  --input dependency-check-report.json \
  --output ~/reports/output.json \
  --version 39.0.1.Final
```

### Debug Mode

Enable verbose logging to diagnose issues:

```bash
java -jar report-converter.jar \
  --input dependency-check-report.json \
  --output output.json \
  --version 39.0.1.Final \
  --verbose
```

### Getting Help

If you encounter issues not covered here:

1. Check the [main project README](../../README.md)
2. Search [existing issues](https://github.com/wildfly-security/wildfly-sca-scanner/issues)
3. Ask on [WildFly Zulip](https://wildfly.zulipchat.com/)
4. Open a [new issue](https://github.com/wildfly-security/wildfly-sca-scanner/issues/new)

## 🤝 Contributing

We welcome contributions! Please see:

- [Contributing Guide](../../CONTRIBUTING.md) - How to contribute
- [Test Coverage](TEST_COVERAGE.md) - Testing guidelines
- [Parent README](../README.md) - Java utilities overview

## 📜 License

This project is licensed under the Apache License 2.0 - see the [LICENSE.txt](../../LICENSE.txt) file for details.

## 🔗 Related Documentation

- [Parent Project README](../../README.md) - Overall project documentation
- [Java Utilities README](../README.md) - All Java utilities
- [Schema Documentation](../../schemas/SCHEMA_DOCUMENTATION.md) - Output format details
- [Test Coverage](TEST_COVERAGE.md) - Testing documentation
- [GitHub Actions Integration](../../GITHUB_ACTIONS_INTEGRATION.md) - CI/CD usage

---

**Module**: report-converter
**Version**: 1.0.0-SNAPSHOT
**Maintainer**: WildFly Security Team
**Last Updated**: 2026-03-17