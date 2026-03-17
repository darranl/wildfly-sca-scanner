# GitHub Actions Integration Guide

This guide shows how to integrate the WildFly SCA Scanner Report Converter into your GitHub Actions CI/CD workflows.

## 📋 Table of Contents

- [Overview](#overview)
- [Basic Integration](#basic-integration)
- [Complete Workflow Example](#complete-workflow-example)
- [Advanced Patterns](#advanced-patterns)
- [Publishing to GitHub Pages](#publishing-to-github-pages)
- [Caching Strategies](#caching-strategies)
- [Security Best Practices](#security-best-practices)
- [Troubleshooting](#troubleshooting)

## 🎯 Overview

The Report Converter integrates seamlessly with GitHub Actions to:

- **Automate** security scanning on every commit or schedule
- **Convert** OWASP reports to generic format automatically
- **Publish** results to GitHub Pages for public visibility
- **Archive** reports as workflow artifacts
- **Notify** teams of new vulnerabilities

## 🚀 Basic Integration

### Minimal Workflow

Add this to `.github/workflows/security-scan.yml`:

```yaml
name: Security Scan

on:
  push:
    branches: [ main ]
  schedule:
    - cron: '13 6 * * *'  # Daily at 6:13 AM UTC

jobs:
  scan:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '25'

      - name: Build Report Converter
        run: |
          cd java-utilities
          mvn clean install -DskipTests

      - name: Run OWASP Dependency Check
        run: |
          # Download and run OWASP Dependency Check
          wget -q https://github.com/jeremylong/DependencyCheck/releases/download/v10.0.4/dependency-check-10.0.4-release.zip
          unzip -q dependency-check-10.0.4-release.zip

          ./dependency-check/bin/dependency-check.sh \
            --project "WildFly 39.0.1.Final" \
            --scan /path/to/wildfly \
            --format JSON \
            --out dependency-check-report.json

      - name: Convert to Generic Format
        run: |
          java -jar java-utilities/report-converter/target/report-converter.jar \
            --input dependency-check-report.json \
            --output wildfly-cve-report.json \
            --version 39.0.1.Final \
            --skip-suppressed

      - name: Upload Report
        uses: actions/upload-artifact@v4
        with:
          name: cve-report
          path: wildfly-cve-report.json
```

## 📦 Complete Workflow Example

This is the actual workflow used by the WildFly SCA Scanner project:

```yaml
name: WildFly Security Scan

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  schedule:
    # Run nightly at 6:13 AM UTC
    - cron: '13 6 * * *'
  workflow_dispatch:  # Allow manual triggers

env:
  JAVA_VERSION: '25'
  MAVEN_OPTS: '-Xmx1024m'

jobs:
  build-converter:
    name: Build Report Converter
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: ${{ env.JAVA_VERSION }}
          cache: 'maven'

      - name: Build and test
        run: |
          cd java-utilities
          mvn clean install

      - name: Upload converter JAR
        uses: actions/upload-artifact@v4
        with:
          name: report-converter
          path: java-utilities/report-converter/target/report-converter.jar
          retention-days: 7

  scan-wildfly:
    name: Scan WildFly ${{ matrix.version }}
    needs: build-converter
    runs-on: ubuntu-latest

    strategy:
      matrix:
        version:
          - '36.0.1.Final'
          - '37.0.1.Final'
          - '38.0.1.Final'
          - '39.0.1.Final'
      fail-fast: false

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: ${{ env.JAVA_VERSION }}

      - name: Download report converter
        uses: actions/download-artifact@v4
        with:
          name: report-converter
          path: converter

      - name: Provision WildFly ${{ matrix.version }}
        run: |
          # Download Galleon CLI
          wget -q https://github.com/wildfly/galleon/releases/download/6.0.2.Final/galleon-6.0.2.Final.zip
          unzip -q galleon-6.0.2.Final.zip

          # Provision WildFly
          ./galleon-6.0.2.Final/bin/galleon.sh install \
            wildfly:${{ matrix.version }} \
            --dir=wildfly-${{ matrix.version }}

      - name: Cache OWASP NVD data
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository/org/owasp/dependency-check-data
          key: owasp-nvd-${{ runner.os }}-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            owasp-nvd-${{ runner.os }}-

      - name: Run OWASP Dependency Check
        run: |
          # Download OWASP Dependency Check
          wget -q https://github.com/jeremylong/DependencyCheck/releases/download/v10.0.4/dependency-check-10.0.4-release.zip
          unzip -q dependency-check-10.0.4-release.zip

          # Run scan with suppressions
          ./dependency-check/bin/dependency-check.sh \
            --project "WildFly ${{ matrix.version }}" \
            --scan wildfly-${{ matrix.version }} \
            --format JSON \
            --format HTML \
            --out reports \
            --suppression owasp-suppressions/owasp-suppressions.xml \
            --nvdApiKey ${{ secrets.NVD_API_KEY }}

      - name: Convert to generic format
        run: |
          java -jar converter/report-converter.jar \
            --input reports/dependency-check-report.json \
            --output reports/wildfly-${{ matrix.version }}-cve-report.json \
            --version ${{ matrix.version }} \
            --skip-suppressed

      - name: Generate summary
        run: |
          echo "## Security Scan Results - WildFly ${{ matrix.version }}" >> $GITHUB_STEP_SUMMARY
          echo "" >> $GITHUB_STEP_SUMMARY

          # Extract summary from generic report
          TOTAL=$(jq -r '.summary.total' reports/wildfly-${{ matrix.version }}-cve-report.json)
          ACTIVE=$(jq -r '.summary.active' reports/wildfly-${{ matrix.version }}-cve-report.json)
          CRITICAL=$(jq -r '.summary.bySeverity.CRITICAL' reports/wildfly-${{ matrix.version }}-cve-report.json)
          HIGH=$(jq -r '.summary.bySeverity.HIGH' reports/wildfly-${{ matrix.version }}-cve-report.json)
          MEDIUM=$(jq -r '.summary.bySeverity.MEDIUM' reports/wildfly-${{ matrix.version }}-cve-report.json)
          LOW=$(jq -r '.summary.bySeverity.LOW' reports/wildfly-${{ matrix.version }}-cve-report.json)

          echo "| Metric | Count |" >> $GITHUB_STEP_SUMMARY
          echo "|--------|-------|" >> $GITHUB_STEP_SUMMARY
          echo "| Total Vulnerabilities | $TOTAL |" >> $GITHUB_STEP_SUMMARY
          echo "| Active | $ACTIVE |" >> $GITHUB_STEP_SUMMARY
          echo "| Critical | $CRITICAL |" >> $GITHUB_STEP_SUMMARY
          echo "| High | $HIGH |" >> $GITHUB_STEP_SUMMARY
          echo "| Medium | $MEDIUM |" >> $GITHUB_STEP_SUMMARY
          echo "| Low | $LOW |" >> $GITHUB_STEP_SUMMARY

      - name: Upload reports
        uses: actions/upload-artifact@v4
        with:
          name: reports-${{ matrix.version }}
          path: |
            reports/dependency-check-report.html
            reports/dependency-check-report.json
            reports/wildfly-${{ matrix.version }}-cve-report.json
          retention-days: 30

      - name: Check for critical vulnerabilities
        run: |
          CRITICAL=$(jq -r '.summary.bySeverity.CRITICAL' reports/wildfly-${{ matrix.version }}-cve-report.json)
          if [ "$CRITICAL" -gt 0 ]; then
            echo "::warning::Found $CRITICAL critical vulnerabilities in WildFly ${{ matrix.version }}"
          fi

  publish-dashboard:
    name: Publish to GitHub Pages
    needs: scan-wildfly
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'

    permissions:
      contents: write
      pages: write
      id-token: write

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Download all reports
        uses: actions/download-artifact@v4
        with:
          path: artifacts

      - name: Prepare dashboard data
        run: |
          mkdir -p dashboard/data

          # Copy generic reports to dashboard
          find artifacts -name "wildfly-*-cve-report.json" -exec cp {} dashboard/data/ \;

          # Generate index of available reports
          ls dashboard/data/*.json | jq -R -s -c 'split("\n")[:-1]' > dashboard/data/reports.json

      - name: Deploy to GitHub Pages
        uses: peaceiris/actions-gh-pages@v4
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./dashboard
          cname: wildfly-cve.example.com  # Optional: custom domain
```

## 🔧 Advanced Patterns

### Pattern 1: Multi-Version Matrix Scan

Scan multiple WildFly versions in parallel:

```yaml
strategy:
  matrix:
    version:
      - '36.0.1.Final'
      - '37.0.1.Final'
      - '38.0.1.Final'
      - '39.0.1.Final'
    include:
      - version: '39.0.1.Final'
        is-latest: true
  fail-fast: false
  max-parallel: 4
```

### Pattern 2: Conditional Execution

Only run on specific conditions:

```yaml
- name: Convert report
  if: success() && github.event_name == 'schedule'
  run: |
    java -jar report-converter.jar \
      --input dependency-check-report.json \
      --output wildfly-cve-report.json \
      --version ${{ matrix.version }}
```

### Pattern 3: Slack Notifications

Send results to Slack:

```yaml
- name: Notify Slack
  if: always()
  uses: slackapi/slack-github-action@v1
  with:
    payload: |
      {
        "text": "Security scan completed for WildFly ${{ matrix.version }}",
        "blocks": [
          {
            "type": "section",
            "text": {
              "type": "mrkdwn",
              "text": "*WildFly ${{ matrix.version }} Security Scan*\n• Total: ${{ env.TOTAL_VULNS }}\n• Critical: ${{ env.CRITICAL_VULNS }}\n• High: ${{ env.HIGH_VULNS }}"
            }
          }
        ]
      }
  env:
    SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

### Pattern 4: Compare with Previous Scan

Track changes over time:

```yaml
- name: Download previous report
  uses: dawidd6/action-download-artifact@v3
  with:
    workflow: security-scan.yml
    name: reports-${{ matrix.version }}
    path: previous-reports
    if_no_artifact_found: ignore

- name: Compare reports
  run: |
    if [ -f "previous-reports/wildfly-${{ matrix.version }}-cve-report.json" ]; then
      PREV_TOTAL=$(jq -r '.summary.total' previous-reports/wildfly-${{ matrix.version }}-cve-report.json)
      CURR_TOTAL=$(jq -r '.summary.total' reports/wildfly-${{ matrix.version }}-cve-report.json)
      DIFF=$((CURR_TOTAL - PREV_TOTAL))

      if [ $DIFF -gt 0 ]; then
        echo "::warning::$DIFF new vulnerabilities found!"
      elif [ $DIFF -lt 0 ]; then
        echo "::notice::$((DIFF * -1)) vulnerabilities resolved!"
      fi
    fi
```

## 📄 Publishing to GitHub Pages

### Step 1: Enable GitHub Pages

1. Go to repository Settings → Pages
2. Select "GitHub Actions" as source
3. Save

### Step 2: Add Deployment Workflow

```yaml
- name: Setup Pages
  uses: actions/configure-pages@v4

- name: Upload artifact
  uses: actions/upload-pages-artifact@v3
  with:
    path: './dashboard'

- name: Deploy to GitHub Pages
  id: deployment
  uses: actions/deploy-pages@v4
```

### Step 3: Create Dashboard HTML

Create `dashboard/index.html`:

```html
<!DOCTYPE html>
<html>
<head>
    <title>WildFly CVE Dashboard</title>
    <script>
        async function loadReports() {
            const response = await fetch('data/reports.json');
            const reports = await response.json();

            for (const report of reports) {
                const data = await fetch(report).then(r => r.json());
                displayReport(data);
            }
        }

        function displayReport(data) {
            // Display logic here
            console.log(data.summary);
        }

        window.onload = loadReports;
    </script>
</head>
<body>
    <h1>WildFly Security Dashboard</h1>
    <div id="reports"></div>
</body>
</html>
```

## 💾 Caching Strategies

### Cache Maven Dependencies

```yaml
- name: Cache Maven packages
  uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: maven-${{ runner.os }}-${{ hashFiles('**/pom.xml') }}
    restore-keys: |
      maven-${{ runner.os }}-
```

### Cache OWASP NVD Data

```yaml
- name: Cache NVD data
  uses: actions/cache@v4
  with:
    path: ~/.m2/repository/org/owasp/dependency-check-data
    key: owasp-nvd-${{ runner.os }}-${{ github.run_id }}
    restore-keys: |
      owasp-nvd-${{ runner.os }}-
```

### Cache WildFly Distributions

```yaml
- name: Cache WildFly
  uses: actions/cache@v4
  with:
    path: wildfly-${{ matrix.version }}
    key: wildfly-${{ matrix.version }}
```

## 🔒 Security Best Practices

### 1. Use Secrets for API Keys

```yaml
env:
  NVD_API_KEY: ${{ secrets.NVD_API_KEY }}
```

Add secrets in: Settings → Secrets and variables → Actions

### 2. Limit Permissions

```yaml
permissions:
  contents: read
  actions: read
  security-events: write
```

### 3. Pin Action Versions

```yaml
# ✅ Good - pinned to specific version
uses: actions/checkout@v4.1.1

# ❌ Bad - uses latest
uses: actions/checkout@main
```

### 4. Validate Inputs

```yaml
- name: Validate version format
  run: |
    if [[ ! "${{ matrix.version }}" =~ ^[0-9]+\.[0-9]+\.[0-9]+\.Final$ ]]; then
      echo "Invalid version format"
      exit 1
    fi
```

## 🔧 Troubleshooting

### Issue: Build Fails with "Java version mismatch"

**Solution**: Ensure Java 25 is specified:

```yaml
- uses: actions/setup-java@v4
  with:
    distribution: 'temurin'
    java-version: '25'
```

### Issue: OWASP Dependency Check Times Out

**Solution**: Increase timeout and use NVD API key:

```yaml
- name: Run OWASP Dependency Check
  timeout-minutes: 60
  run: |
    ./dependency-check/bin/dependency-check.sh \
      --nvdApiKey ${{ secrets.NVD_API_KEY }} \
      ...
```

### Issue: Artifacts Not Found

**Solution**: Check artifact names match:

```yaml
# Upload
- uses: actions/upload-artifact@v4
  with:
    name: report-converter  # Must match download

# Download
- uses: actions/download-artifact@v4
  with:
    name: report-converter  # Must match upload
```

### Issue: Out of Disk Space

**Solution**: Clean up before scanning:

```yaml
- name: Free disk space
  run: |
    sudo rm -rf /usr/share/dotnet
    sudo rm -rf /opt/ghc
    sudo rm -rf /usr/local/share/boost
    df -h
```

### Issue: Maven Build Fails

**Solution**: Use Maven wrapper or specify version:

```yaml
- name: Set up Maven
  uses: stCarolas/setup-maven@v5
  with:
    maven-version: 3.9.13
```

## 📊 Monitoring and Alerts

### GitHub Actions Status Badge

Add to README.md:

```markdown
![Security Scan](https://github.com/wildfly-security/wildfly-sca-scanner/workflows/Security%20Scan/badge.svg)
```

### Email Notifications

Configure in repository settings or use action:

```yaml
- name: Send email
  if: failure()
  uses: dawidd6/action-send-mail@v3
  with:
    server_address: smtp.gmail.com
    server_port: 465
    username: ${{ secrets.EMAIL_USERNAME }}
    password: ${{ secrets.EMAIL_PASSWORD }}
    subject: Security scan failed
    body: Check ${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}
    to: security-team@example.com
```

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [OWASP Dependency Check](https://jeremylong.github.io/DependencyCheck/)
- [Report Converter README](java-utilities/report-converter/README.md)
- [Schema Documentation](schemas/SCHEMA_DOCUMENTATION.md)

## 💡 Tips and Best Practices

1. **Use matrix builds** for scanning multiple versions in parallel
2. **Cache dependencies** to speed up builds (Maven, NVD data)
3. **Set timeouts** to prevent hanging workflows
4. **Use artifacts** to preserve reports between jobs
5. **Add summaries** to make results visible in GitHub UI
6. **Schedule nightly scans** to catch new vulnerabilities
7. **Use secrets** for API keys and credentials
8. **Pin action versions** for reproducibility
9. **Enable branch protection** to require passing scans
10. **Monitor costs** if using private repositories

---

**Last Updated**: 2026-03-17
**Maintainer**: WildFly Security Team