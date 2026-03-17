# Generic Report Schema Documentation

This document provides detailed documentation for the WildFly SCA Scanner generic report format.

## 📋 Table of Contents

- [Overview](#overview)
- [Schema Version](#schema-version)
- [Top-Level Structure](#top-level-structure)
- [Metadata Section](#metadata-section)
- [Vulnerabilities Section](#vulnerabilities-section)
- [Summary Section](#summary-section)
- [CVSS Handling](#cvss-handling)
- [Examples](#examples)
- [Validation](#validation)
- [Version History](#version-history)

## 🎯 Overview

The generic report format is a tool-agnostic JSON schema designed to represent security vulnerability information in a standardized way. This format:

- **Decouples** reporting from specific scanning tools
- **Enables** easy tool switching or multi-tool aggregation
- **Simplifies** dashboard and integration development
- **Provides** consistent structure across different scanners

**Schema Location**: [`generic-report-schema.json`](generic-report-schema.json)

## 📌 Schema Version

Current schema version: **1.0**

The schema follows semantic versioning:
- **Major version** changes indicate breaking changes
- **Minor version** changes add new optional fields
- Reports include `schemaVersion` field for compatibility checking

## 🏗️ Top-Level Structure

Every generic report contains four required top-level fields:

```json
{
  "schemaVersion": "1.0",
  "metadata": { ... },
  "vulnerabilities": [ ... ],
  "summary": { ... }
}
```

### Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `schemaVersion` | string | ✅ Yes | Schema version (format: "major.minor") |
| `metadata` | object | ✅ Yes | Scan metadata and context |
| `vulnerabilities` | array | ✅ Yes | List of vulnerability objects |
| `summary` | object | ✅ Yes | Aggregated statistics |

## 📊 Metadata Section

The metadata section provides context about the scan:

```json
{
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
  }
}
```

### Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `scanDate` | string | ✅ Yes | ISO 8601 timestamp of when scan was performed |
| `wildflyVersion` | string | ✅ Yes | WildFly version that was scanned |
| `scanners` | array | ✅ Yes | List of scanning tools used |
| `totalDependencies` | integer | ❌ No | Total number of dependencies analyzed |
| `vulnerableDependencies` | integer | ❌ No | Number of dependencies with vulnerabilities |

### Scanner Object

Each scanner in the `scanners` array contains:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | ✅ Yes | Scanner tool name (e.g., "OWASP Dependency Check") |
| `version` | string | ✅ Yes | Scanner tool version (e.g., "10.0.4") |

**Note**: The `scanners` array supports multiple tools for future multi-tool aggregation.

## 🔍 Vulnerabilities Section

The vulnerabilities section is an array of vulnerability objects:

```json
{
  "vulnerabilities": [
    {
      "id": "CVE-2024-1234",
      "title": "Remote Code Execution in Example Library",
      "severity": "HIGH",
      "cvssScore": 7.5,
      "cvssVector": "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N",
      "description": "A vulnerability was found in example-library that allows...",
      "affectedPackages": [
        {
          "name": "example-library",
          "version": "1.2.3",
          "ecosystem": "maven",
          "filePath": "/path/to/example-library-1.2.3.jar"
        }
      ],
      "references": [
        {
          "url": "https://nvd.nist.gov/vuln/detail/CVE-2024-1234",
          "source": "NVD"
        },
        {
          "url": "https://github.com/advisories/GHSA-xxxx-yyyy-zzzz",
          "source": "GitHub Advisory"
        }
      ],
      "suppressed": false,
      "suppressionReason": null
    }
  ]
}
```

### Vulnerability Object Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | string | ✅ Yes | CVE identifier (e.g., "CVE-2024-1234") |
| `title` | string | ✅ Yes | Short vulnerability title/summary |
| `severity` | string | ✅ Yes | Severity level: CRITICAL, HIGH, MEDIUM, LOW, or UNKNOWN |
| `cvssScore` | number | ❌ No | CVSS base score (0.0-10.0) |
| `cvssVector` | string | ❌ No | CVSS vector string |
| `description` | string | ✅ Yes | Detailed vulnerability description |
| `affectedPackages` | array | ✅ Yes | List of affected package objects |
| `references` | array | ✅ Yes | List of reference objects (URLs) |
| `suppressed` | boolean | ✅ Yes | Whether this CVE is suppressed |
| `suppressionReason` | string | ❌ No | Reason for suppression (if suppressed) |

### Severity Values

The `severity` field uses standardized values:

| Value | Description | CVSS Score Range |
|-------|-------------|------------------|
| `CRITICAL` | Critical severity | 9.0 - 10.0 |
| `HIGH` | High severity | 7.0 - 8.9 |
| `MEDIUM` | Medium severity | 4.0 - 6.9 |
| `LOW` | Low severity | 0.1 - 3.9 |
| `UNKNOWN` | Severity not determined | N/A |

### Affected Package Object

Each package in the `affectedPackages` array contains:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | ✅ Yes | Package/artifact name |
| `version` | string | ✅ Yes | Package version |
| `ecosystem` | string | ❌ No | Package ecosystem (maven, npm, pypi, etc.) |
| `filePath` | string | ❌ No | Path to the file in the scanned distribution |

### Reference Object

Each reference in the `references` array contains:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `url` | string | ✅ Yes | URL to vulnerability information |
| `source` | string | ❌ No | Source of the reference (NVD, GitHub, etc.) |

**Note**: References are automatically deduplicated by URL during conversion.

## 📈 Summary Section

The summary section provides aggregated statistics:

```json
{
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

### Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `total` | integer | ✅ Yes | Total number of vulnerabilities |
| `active` | integer | ✅ Yes | Number of active (non-suppressed) vulnerabilities |
| `suppressed` | integer | ✅ Yes | Number of suppressed vulnerabilities |
| `bySeverity` | object | ✅ Yes | Count of vulnerabilities by severity level |

### By Severity Object

The `bySeverity` object contains counts for each severity level:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `CRITICAL` | integer | ✅ Yes | Count of CRITICAL severity vulnerabilities |
| `HIGH` | integer | ✅ Yes | Count of HIGH severity vulnerabilities |
| `MEDIUM` | integer | ✅ Yes | Count of MEDIUM severity vulnerabilities |
| `LOW` | integer | ✅ Yes | Count of LOW severity vulnerabilities |

**Note**: Counts include both active and suppressed vulnerabilities unless `--skip-suppressed` was used during conversion.

## 🎯 CVSS Handling

The report converter handles multiple CVSS versions with intelligent prioritization:

### CVSS Version Priority

When multiple CVSS scores are available, the converter uses this priority:

1. **CVSS v3.x** (preferred) - Most widely adopted
2. **CVSS v2.0** (fallback) - Legacy systems
3. **CVSS v4.0** (future) - Newest version

### CVSS Score Extraction

```json
{
  "cvssScore": 7.5,
  "cvssVector": "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N"
}
```

- **cvssScore**: The base score (0.0-10.0)
- **cvssVector**: The complete vector string including version

### CVSS Vector Formats

**CVSS v3.x**:
```
CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N
```

**CVSS v2.0**:
```
AV:N/AC:L/Au:N/C:P/I:N/A:N
```

**CVSS v4.0**:
```
CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:H/VI:N/VA:N/SC:N/SI:N/SA:N
```

### Missing CVSS Data

If no CVSS data is available:
- `cvssScore` is omitted (not included in JSON)
- `cvssVector` is omitted (not included in JSON)
- `severity` is still required (may be "UNKNOWN")

## 📝 Examples

### Example 1: Minimal Report

A report with no vulnerabilities:

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
    "vulnerableDependencies": 0
  },
  "vulnerabilities": [],
  "summary": {
    "total": 0,
    "active": 0,
    "suppressed": 0,
    "bySeverity": {
      "CRITICAL": 0,
      "HIGH": 0,
      "MEDIUM": 0,
      "LOW": 0
    }
  }
}
```

### Example 2: Report with Suppressed CVE

A report showing both active and suppressed vulnerabilities:

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
    "vulnerableDependencies": 2
  },
  "vulnerabilities": [
    {
      "id": "CVE-2024-1234",
      "title": "Active Vulnerability",
      "severity": "HIGH",
      "cvssScore": 7.5,
      "cvssVector": "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N",
      "description": "This is an active vulnerability.",
      "affectedPackages": [
        {
          "name": "vulnerable-lib",
          "version": "1.0.0",
          "ecosystem": "maven"
        }
      ],
      "references": [
        {
          "url": "https://nvd.nist.gov/vuln/detail/CVE-2024-1234",
          "source": "NVD"
        }
      ],
      "suppressed": false,
      "suppressionReason": null
    },
    {
      "id": "CVE-2024-5678",
      "title": "Suppressed Vulnerability",
      "severity": "MEDIUM",
      "cvssScore": 5.3,
      "cvssVector": "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:L/I:N/A:N",
      "description": "This vulnerability is suppressed.",
      "affectedPackages": [
        {
          "name": "suppressed-lib",
          "version": "2.0.0",
          "ecosystem": "maven"
        }
      ],
      "references": [
        {
          "url": "https://nvd.nist.gov/vuln/detail/CVE-2024-5678",
          "source": "NVD"
        }
      ],
      "suppressed": true,
      "suppressionReason": "False positive - not applicable to our usage"
    }
  ],
  "summary": {
    "total": 2,
    "active": 1,
    "suppressed": 1,
    "bySeverity": {
      "CRITICAL": 0,
      "HIGH": 1,
      "MEDIUM": 1,
      "LOW": 0
    }
  }
}
```

### Example 3: Multi-Package Vulnerability

A single CVE affecting multiple packages:

```json
{
  "id": "CVE-2024-9999",
  "title": "Vulnerability Affecting Multiple Packages",
  "severity": "CRITICAL",
  "cvssScore": 9.8,
  "cvssVector": "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H",
  "description": "Critical vulnerability in multiple versions.",
  "affectedPackages": [
    {
      "name": "example-lib",
      "version": "1.0.0",
      "ecosystem": "maven",
      "filePath": "/modules/system/layers/base/com/example/lib/main/example-lib-1.0.0.jar"
    },
    {
      "name": "example-lib",
      "version": "1.1.0",
      "ecosystem": "maven",
      "filePath": "/modules/system/layers/base/com/example/lib/main/example-lib-1.1.0.jar"
    }
  ],
  "references": [
    {
      "url": "https://nvd.nist.gov/vuln/detail/CVE-2024-9999",
      "source": "NVD"
    },
    {
      "url": "https://github.com/advisories/GHSA-xxxx-yyyy-zzzz",
      "source": "GitHub Advisory"
    },
    {
      "url": "https://example.com/security/CVE-2024-9999",
      "source": "Vendor Advisory"
    }
  ],
  "suppressed": false,
  "suppressionReason": null
}
```

## ✅ Validation

### JSON Schema Validation

Validate reports against the schema:

```bash
# Using ajv-cli
npm install -g ajv-cli
ajv validate -s generic-report-schema.json -d wildfly-cve-report.json

# Using Python jsonschema
pip install jsonschema
python -c "
import json
import jsonschema

with open('generic-report-schema.json') as f:
    schema = json.load(f)
with open('wildfly-cve-report.json') as f:
    report = json.load(f)

jsonschema.validate(report, schema)
print('Valid!')
"
```

### Common Validation Errors

#### 1. Missing Required Field

```json
{
  "error": "Missing required property: 'schemaVersion'"
}
```

**Solution**: Ensure all required top-level fields are present.

#### 2. Invalid Schema Version Format

```json
{
  "error": "schemaVersion does not match pattern '^\\d+\\.\\d+$'"
}
```

**Solution**: Use format "major.minor" (e.g., "1.0", not "v1.0" or "1").

#### 3. Invalid Severity Value

```json
{
  "error": "severity must be one of: CRITICAL, HIGH, MEDIUM, LOW, UNKNOWN"
}
```

**Solution**: Use only the defined severity values (case-sensitive).

#### 4. Invalid CVSS Score Range

```json
{
  "error": "cvssScore must be between 0.0 and 10.0"
}
```

**Solution**: Ensure CVSS scores are in the valid range.

## 📚 Version History

### Version 1.0 (2026-03-17)

**Initial Release**

- Defined core schema structure
- Support for CVSS v2, v3, and v4
- Suppression tracking
- Multi-package vulnerability support
- Reference deduplication
- Summary statistics

### Future Versions

**Planned for 1.1**:
- Optional `fixedVersions` field for remediation guidance
- Optional `exploitability` field for exploit status
- Optional `patchAvailable` boolean field

**Planned for 2.0**:
- Support for multiple scanner aggregation
- Enhanced metadata for tool-specific data
- Backward compatibility with 1.x reports

## 🔗 Related Documentation

- [Report Converter README](../java-utilities/report-converter/README.md) - Tool documentation
- [Root Project README](../README.md) - Overall project documentation
- [Quick Start Guide](../QUICK_START.md) - Getting started tutorial
- [GitHub Actions Integration](../GITHUB_ACTIONS_INTEGRATION.md) - CI/CD usage

## 📞 Support

For questions or issues with the schema:

1. Check the [FAQ](../README.md#faq) in the main README
2. Search [existing issues](https://github.com/wildfly-security/wildfly-sca-scanner/issues)
3. Ask on [WildFly Zulip](https://wildfly.zulipchat.com/)
4. Open a [new issue](https://github.com/wildfly-security/wildfly-sca-scanner/issues/new)

---

**Schema Version**: 1.0
**Last Updated**: 2026-03-17
**Maintainer**: WildFly Security Team