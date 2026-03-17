# Quick Start Guide

Get up and running with the WildFly SCA Scanner Report Converter in 5 minutes.

## 📋 Prerequisites Check

Before starting, verify you have the required tools:

```bash
# Check Java version (need 25+)
java -version
# Should show: openjdk version "25.0.2" or higher

# Check Maven version (need 3.9+)
mvn -version
# Should show: Apache Maven 3.9.x or higher

# Check Git
git --version
```

### Installing Prerequisites

If you need to install the prerequisites:

#### Using SDKMAN (Recommended for Linux/Mac)

```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 25
sdk install java 25.0.2-tem
sdk use java 25.0.2-tem

# Install Maven 3.9
sdk install maven 3.9.13
sdk use maven 3.9.13
```

#### Manual Installation

- **Java**: Download from [OpenJDK](https://openjdk.org/)
- **Maven**: Download from [Apache Maven](https://maven.apache.org/download.cgi)
- **Git**: Download from [Git SCM](https://git-scm.com/downloads)

## 🚀 Step 1: Clone and Build

```bash
# Clone the repository
git clone https://github.com/wildfly-security/wildfly-sca-scanner.git
cd wildfly-sca-scanner

# Build the Java utilities
cd java-utilities
mvn clean install

# This will:
# - Compile all source code
# - Run 40 tests (should all pass ✅)
# - Generate executable JARs
# - Take about 30-60 seconds
```

**Expected Output:**
```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO]
[INFO] WildFly SCA Scanner - Java Utilities ............... SUCCESS
[INFO] WildFly SCA Scanner - Report Converter ............. SUCCESS
[INFO] WildFly SCA Scanner - Suppression Converter ........ SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## 📦 Step 2: Verify Installation

```bash
# Check that the JAR was created
ls -lh report-converter/target/report-converter.jar

# Test the help command
java -jar report-converter/target/report-converter.jar --help
```

**Expected Output:**
```
Usage: report-converter [options]
  Options:
  * --input, -i
      Path to OWASP Dependency Check JSON report
  * --output, -o
      Path for output generic JSON report
  * --version, -v
      WildFly version being scanned
    --skip-suppressed
      Skip suppressed CVEs from output
    --verbose
      Enable verbose logging
    --help, -h
      Display this help message
```

## 🔍 Step 3: Get Sample Data

For this quick start, we'll use a sample OWASP report. You have two options:

### Option A: Use Existing Sample Data

If you have the Results directory with sample data:

```bash
cd ../..  # Back to project root
ls Results/dependency-check-report.json
```

### Option B: Create Minimal Test Data

Create a minimal test report:

```bash
cd ../..  # Back to project root
mkdir -p test-data

cat > test-data/sample-report.json << 'EOF'
{
  "reportSchema": "1.1",
  "scanInfo": {
    "engineVersion": "10.0.4"
  },
  "projectInfo": {
    "name": "WildFly Test"
  },
  "dependencies": [
    {
      "fileName": "example-lib-1.0.0.jar",
      "filePath": "/path/to/example-lib-1.0.0.jar",
      "packages": [
        {
          "id": "pkg:maven/com.example/example-lib@1.0.0"
        }
      ],
      "vulnerabilities": [
        {
          "name": "CVE-2024-1234",
          "severity": "HIGH",
          "description": "Example vulnerability for testing",
          "cvssv3": {
            "baseScore": 7.5,
            "attackVector": "NETWORK",
            "attackComplexity": "LOW",
            "privilegesRequired": "NONE",
            "userInteraction": "NONE",
            "scope": "UNCHANGED",
            "confidentialityImpact": "HIGH",
            "integrityImpact": "NONE",
            "availabilityImpact": "NONE",
            "baseSeverity": "HIGH",
            "vectorString": "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N"
          },
          "references": [
            {
              "source": "NVD",
              "url": "https://nvd.nist.gov/vuln/detail/CVE-2024-1234",
              "name": "CVE-2024-1234"
            }
          ]
        }
      ]
    }
  ]
}
EOF
```

## 🎯 Step 4: Convert Your First Report

Now let's convert the report to the generic format:

```bash
# Using sample data from Results/ (if available)
java -jar java-utilities/report-converter/target/report-converter.jar \
  --input Results/dependency-check-report.json \
  --output Results/wildfly-39.0.1.Final-cve-report.json \
  --version 39.0.1.Final

# OR using test data we just created
java -jar java-utilities/report-converter/target/report-converter.jar \
  --input test-data/sample-report.json \
  --output test-data/generic-report.json \
  --version 39.0.1.Final
```

**Expected Output:**
```
Converting OWASP report to generic format...
Input: Results/dependency-check-report.json
Output: Results/wildfly-39.0.1.Final-cve-report.json
Version: 39.0.1.Final

Conversion completed successfully!
Total vulnerabilities: 15
Active: 12
Suppressed: 3
```

## 📊 Step 5: View the Results

Let's examine the converted report:

```bash
# View the entire report (pretty-printed)
cat Results/wildfly-39.0.1.Final-cve-report.json | jq .

# View just the summary
cat Results/wildfly-39.0.1.Final-cve-report.json | jq .summary

# Count vulnerabilities by severity
cat Results/wildfly-39.0.1.Final-cve-report.json | jq .summary.bySeverity

# List all CVE IDs
cat Results/wildfly-39.0.1.Final-cve-report.json | jq '.vulnerabilities[].id'

# Show only CRITICAL and HIGH severity CVEs
cat Results/wildfly-39.0.1.Final-cve-report.json | \
  jq '.vulnerabilities[] | select(.severity == "CRITICAL" or .severity == "HIGH") | {id, severity, title}'
```

**Example Summary Output:**
```json
{
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
```

## 🎓 Step 6: Try Advanced Features

### Skip Suppressed CVEs

Generate a smaller report without suppressed vulnerabilities:

```bash
java -jar java-utilities/report-converter/target/report-converter.jar \
  --input Results/dependency-check-report.json \
  --output Results/wildfly-active-only.json \
  --version 39.0.1.Final \
  --skip-suppressed

# Compare file sizes
ls -lh Results/wildfly-*.json
```

### Enable Verbose Logging

See detailed processing information:

```bash
java -jar java-utilities/report-converter/target/report-converter.jar \
  --input Results/dependency-check-report.json \
  --output Results/wildfly-verbose.json \
  --version 39.0.1.Final \
  --verbose
```

### Batch Process Multiple Versions

Create a script to process multiple WildFly versions:

```bash
cat > convert-all.sh << 'EOF'
#!/bin/bash
VERSIONS=("36.0.1.Final" "37.0.1.Final" "38.0.1.Final" "39.0.1.Final")

for VERSION in "${VERSIONS[@]}"; do
  echo "Converting report for WildFly $VERSION..."
  java -jar java-utilities/report-converter/target/report-converter.jar \
    --input "reports/dependency-check-${VERSION}.json" \
    --output "reports/wildfly-${VERSION}-cve-report.json" \
    --version "$VERSION" \
    --skip-suppressed
  echo "✅ Done: $VERSION"
  echo ""
done

echo "All conversions complete!"
EOF

chmod +x convert-all.sh
```

## 🧪 Step 7: Run Tests (Optional)

Verify everything works by running the test suite:

```bash
cd java-utilities

# Run all tests
mvn test

# Run tests with coverage report
mvn clean test jacoco:report

# View coverage report in browser
open report-converter/target/site/jacoco/index.html
```

**Expected Output:**
```
[INFO] Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## ✅ Success Checklist

You've successfully completed the quick start if you can:

- [x] Build the project with `mvn clean install`
- [x] Run the converter with `--help` flag
- [x] Convert an OWASP report to generic format
- [x] View the converted report with `jq`
- [x] See vulnerability counts in the summary
- [x] Run tests successfully

## 🎯 Next Steps

Now that you have the basics working, explore:

### 1. **Understand the Output Format**
Read the [Schema Documentation](schemas/SCHEMA_DOCUMENTATION.md) to understand the generic report structure.

### 2. **Integrate with CI/CD**
See the [GitHub Actions Integration Guide](GITHUB_ACTIONS_INTEGRATION.md) to automate scanning.

### 3. **Scan Your Own WildFly**
Run OWASP Dependency Check on your WildFly installation:

```bash
# Download OWASP Dependency Check
wget https://github.com/jeremylong/DependencyCheck/releases/download/v10.0.4/dependency-check-10.0.4-release.zip
unzip dependency-check-10.0.4-release.zip

# Scan your WildFly installation
./dependency-check/bin/dependency-check.sh \
  --project "My WildFly" \
  --scan /path/to/wildfly \
  --format JSON \
  --out my-wildfly-report.json

# Convert to generic format
java -jar java-utilities/report-converter/target/report-converter.jar \
  --input my-wildfly-report.json \
  --output my-wildfly-cve-report.json \
  --version YOUR_VERSION
```

### 4. **Build a Dashboard**
Use the generic JSON reports to build a custom dashboard or integrate with existing tools.

### 5. **Contribute**
Found a bug or have an idea? See the [Contributing Guide](CONTRIBUTING.md).

## 🆘 Troubleshooting

### Build Fails

```bash
# Clear Maven cache and rebuild
mvn clean
rm -rf ~/.m2/repository/org/wildfly/security/sca
mvn clean install -U
```

### Java Version Issues

```bash
# Verify Java version
java -version

# If wrong version, use SDKMAN
sdk list java
sdk use java 25.0.2-tem
```

### Tests Fail

```bash
# Run tests with debug output
mvn test -X

# Run specific test
mvn test -Dtest=OwaspReportParserTest
```

### Conversion Errors

```bash
# Validate input JSON
cat dependency-check-report.json | jq . > /dev/null

# Enable verbose logging
java -jar report-converter.jar \
  --input dependency-check-report.json \
  --output output.json \
  --version 39.0.1.Final \
  --verbose
```

## 📚 Additional Resources

- **[Main README](README.md)** - Project overview
- **[Report Converter README](java-utilities/report-converter/README.md)** - Detailed converter documentation
- **[Schema Documentation](schemas/SCHEMA_DOCUMENTATION.md)** - Output format specification
- **[Test Coverage](java-utilities/report-converter/TEST_COVERAGE.md)** - Testing documentation
- **[GitHub Actions Integration](GITHUB_ACTIONS_INTEGRATION.md)** - CI/CD guide

## 💬 Get Help

- **Issues**: [GitHub Issues](https://github.com/wildfly-security/wildfly-sca-scanner/issues)
- **Chat**: [WildFly Zulip](https://wildfly.zulipchat.com/)
- **Docs**: [Project Documentation](README.md)

## 🎉 Congratulations!

You've successfully set up and used the WildFly SCA Scanner Report Converter. You can now:

- Convert OWASP reports to a standardized format
- Integrate with dashboards and tools
- Automate security scanning in your CI/CD pipeline
- Contribute to the project

Happy scanning! 🔍🛡️

---

**Last Updated**: 2026-03-17
**Estimated Time**: 5-10 minutes
**Difficulty**: Beginner