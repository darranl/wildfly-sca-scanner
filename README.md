# WildFly SCA Scanner

## Purpose

This project provides automated Software Composition Analysis (SCA) scanning for WildFly application server distributions using OWASP Dependency Check. The scanner identifies known security vulnerabilities (CVEs) in WildFly's dependencies by analyzing the packaged libraries against the National Vulnerability Database (NVD) and Sonatype OSS Index.

The automation runs daily via GitHub Actions to:
- Monitor multiple WildFly versions (both released versions and nightly builds) for security vulnerabilities
- Generate detailed HTML and JSON reports of identified CVEs
- Maintain a suppression file for false positives and known/accepted vulnerabilities
- Track vulnerability trends across WildFly releases

## GitHub Actions Workflow Design

The scanning infrastructure is implemented as a series of coordinated GitHub Actions workflows that run on a scheduled basis throughout the early morning (UTC). The workflows are designed to maximize cache reuse and minimize redundant work.

### Workflow Execution Schedule

The workflows execute in the following sequence:

1. **Tool Downloads** (04:26 & 04:41 UTC)
2. **Database Maintenance** (05:12 UTC)
3. **WildFly Provisioning** (05:22, 05:27, 05:47 UTC)
4. **Security Scanning** (06:13 UTC)

### 1. Tool Downloads

Two workflows download and cache the required tools:

#### OWASP Dependency Check Download (`dependency-check-download.yaml`)
- **Schedule**: Daily at 04:26 UTC
- **Purpose**: Downloads and caches the OWASP Dependency Check CLI
- **Version Control**: Uses repository variable `DEPENDENCY_CHECK_VERSION` to control which version is downloaded
- **Caching**: Stores the Dependency Check installation in GitHub Actions cache with key `dependency-check-{VERSION}`
- **Script**: `scripts/update-dependency-check.sh` handles download and extraction

#### Galleon Download (`galleon-download.yaml`)
- **Schedule**: Daily at 04:41 UTC
- **Purpose**: Downloads and caches the Galleon provisioning tool used to install WildFly
- **Version Control**: Uses repository variable `GALLEON_VERSION`
- **Caching**: Stores Galleon installation with key `galleon-{VERSION}`
- **Script**: `scripts/update-galleon.sh` handles download and setup

### 2. Database Maintenance

#### OWASP Database Maintenance (`database-maintenance.yaml`)
- **Schedule**: Daily at 05:12 UTC
- **Purpose**: Updates the OWASP vulnerability database with the latest CVE information
- **Database Sources**: 
  - NVD (National Vulnerability Database) via API key
  - Sonatype OSS Index via username/password
- **Caching Strategy**: 
  - Restores the most recent database cache (key pattern: `owasp-database-*`)
  - Runs update to fetch new CVE data
  - Saves updated database with unique run-specific key: `owasp-database-{RUN_ID}-{ATTEMPT}`
- **Secrets Used**:
  - `NVD_API_KEY`: API key for NVD access
  - `OSS_INDEX_USERNAME` and `OSS_INDEX_PASSWORD`: Credentials for Sonatype OSS Index

### 3. WildFly Provisioning

Three workflows provision different WildFly distributions and cache them for scanning:

#### WildFly Maintenance (`wildfly-instances.yaml`)
- **Schedule**: Daily at 05:22 UTC
- **Purpose**: Provisions released WildFly versions using Galleon
- **Versions**: Matrix includes specific released versions (e.g., 36.0.1.Final, 37.0.1.Final, 38.0.1.Final, 39.0.1.Final)
- **Caching**: Each version is cached with key `wildfly-{VERSION}` and only provisioned if cache miss occurs
- **Provisioning**: Uses Galleon to install standard WildFly feature pack

#### WildFly Preview Maintenance (`wildfly-preview-instances.yaml`)
- **Schedule**: Daily at 05:27 UTC
- **Purpose**: Provisions WildFly Preview distributions (experimental features)
- **Versions**: Currently provisions 39.0.1.Final Preview
- **Caching**: Uses key `wildfly-{VERSION}-Preview`
- **Provisioning**: Uses Galleon to install `wildfly-preview` feature pack

#### WildFly Nightly Maintenance (`wildfly-nightly.yaml`)
- **Schedule**: Daily at 05:47 UTC
- **Purpose**: Downloads and provisions nightly builds from WildFly CI
- **Versions**: Matrix includes `upstream` (main branch) and `maintenance` (maintenance branch)
- **CI Integration**: 
  - Maps matrix version to CI job names via repository variables (`UPSTREAM_CI`, `MAINTENANCE_CI`)
  - Downloads latest successful build from `ci.wildfly.org`
  - Uses `wildfly-nightly-download` action to fetch Maven repository tarball
- **Dual Provisioning**: Creates both standard and preview distributions for each nightly
  - Standard: cached as `wildfly-standard-{VERSION}-{RUN_ID}-{ATTEMPT}`
  - Preview: cached as `wildfly-preview-{VERSION}-{RUN_ID}-{ATTEMPT}`
- **Maintenance Control**: The `MAINTENANCE_CI` variable can be set to `'OFF'` to disable maintenance branch scanning

### 4. Security Scanning

#### WildFly Scan (`scan-wildfly.yaml`)
- **Schedule**: Daily at 06:13 UTC
- **Purpose**: Performs SCA scans on all provisioned WildFly distributions
- **Scan Matrix**: Covers released versions, preview versions, and nightly builds
  - Released: `36.0.1.Final`, `37.0.1.Final`, `38.0.1.Final`, `39.0.1.Final`
  - Preview: `39.0.1.Final-Preview`
  - Nightly: `standard-upstream`, `preview-upstream`, `standard-maintenance`, `preview-maintenance`
- **Execution Control**: `max-parallel: 1` ensures scans run sequentially to manage resource usage and database contention
- **Cache Dependencies**:
  - Restores WildFly installation (fail on cache miss)
  - Restores Dependency Check CLI (fail on cache miss)
  - Restores latest OWASP database
- **Scan Execution**:
  - Runs Dependency Check with `--noupdate` (uses pre-updated database)
  - Scans the `wildfly` directory
  - Applies suppressions from `owasp-suppressions/owasp-suppressions.xml`
  - Generates both HTML and JSON reports
  - Uses NVD API key and OSS Index credentials
- **Outputs**:
  - Success: Uploads `dependency-check-report.*` artifacts as `scan-results-{VERSION}`
  - Failure: Uploads log files as `log-files-{VERSION}` for debugging
- **Maintenance Control**: Maintenance branch scans can be disabled by setting `MAINTENANCE_CI` variable to `'OFF'`

### Caching Strategy

The workflows implement a sophisticated caching strategy to optimize performance:

1. **Tool Caches** (stable, long-lived):
   - `dependency-check-{VERSION}`: Dependency Check CLI
   - `galleon-{VERSION}`: Galleon provisioning tool
   - These persist until version changes

2. **WildFly Instance Caches** (version-specific):
   - Released versions: `wildfly-{VERSION}` (stable)
   - Preview versions: `wildfly-{VERSION}-Preview` (stable)
   - Nightly builds: `wildfly-{standard|preview}-{upstream|maintenance}-{RUN_ID}-{ATTEMPT}` (daily rotation)

3. **Database Cache** (rolling):
   - Latest database: restored using prefix `owasp-database-`
   - After update: saved with unique key `owasp-database-{RUN_ID}-{ATTEMPT}-{JOB_INDEX}`
   - This ensures database updates propagate across matrix jobs while avoiding conflicts

### Configuration

The workflows are configured via GitHub repository settings:

#### Variables
- `DEPENDENCY_CHECK_VERSION`: Version of OWASP Dependency Check to use
- `GALLEON_VERSION`: Version of Galleon provisioning tool
- `UPSTREAM_CI`: TeamCity job name for upstream nightly builds
- `MAINTENANCE_CI`: TeamCity job name for maintenance branch builds (or `'OFF'` to disable)

#### Secrets
- `NVD_API_KEY`: API key for National Vulnerability Database access
- `OSS_INDEX_USERNAME`: Username for Sonatype OSS Index
- `OSS_INDEX_PASSWORD`: Password for Sonatype OSS Index

### Suppressions

The `owasp-suppressions/owasp-suppressions.xml` file contains suppression rules for:
- False positives (incorrect CVE matches)
- Accepted risks (known vulnerabilities with accepted/mitigated risk)
- CVEs under investigation or planned for future fixes

Suppressions should include comments explaining the rationale and any tracking issue references.

### Manual Execution

All workflows can be triggered manually via the GitHub Actions UI using the `workflow_dispatch` event. This is useful for:
- Testing changes to workflow definitions
- Running ad-hoc scans after dependency updates
- Forcing cache refresh after tool version changes

## Artifacts

Scan results are available as workflow artifacts:
- **HTML Reports**: Human-readable vulnerability reports with details and recommendations
- **JSON Reports**: Machine-readable data for integration with other tools
- **Log Files**: Detailed execution logs (only on failure)

Artifacts are retained according to GitHub's retention policy (default 90 days).

## Contributing

When modifying workflows:
1. Test changes using manual `workflow_dispatch` triggers
2. Monitor cache hit rates to ensure caching strategy remains effective
3. Update this README if workflow behavior or design changes
4. Keep suppression file comments up-to-date with issue references

## License

This project follows the same license as WildFly.
