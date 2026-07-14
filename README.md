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

The workflows execute in the following sequence with deliberate time offsets to avoid GitHub's on-the-hour congestion:

1. **Tool Downloads**
   - 04:26 UTC - OWASP Dependency Check Download
   - 04:41 UTC - Galleon Download
2. **Database Maintenance**
   - 05:12 UTC - OWASP Database Maintenance
3. **WildFly Provisioning**
   - 05:22 UTC - WildFly Maintenance (standard versions)
   - 05:27 UTC - WildFly Preview Maintenance
   - 05:32 UTC - WildFly EE10 Maintenance
   - 05:47 UTC - WildFly Nightly Maintenance
4. **Security Scanning**
   - 06:13 UTC - WildFly Scan (all variants)

**Note on Timing**: All workflows use minute offsets (e.g., :26, :41, :12) rather than running on the hour (:00) to mitigate GitHub Actions' congestion issues when many workflows across all repositories start simultaneously at the top of each hour.

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
- **Versions**: Last 4 final releases (currently: 37.0.1.Final, 38.0.1.Final, 39.0.1.Final, 40.0.1.Final)
- **Caching**: Each version is cached with key `wildfly-{VERSION}` and only provisioned if cache miss occurs
- **Provisioning**: Uses Galleon to install standard WildFly feature pack

#### WildFly Preview Maintenance (`wildfly-preview-instances.yaml`)
- **Schedule**: Daily at 05:27 UTC
- **Purpose**: Provisions WildFly Preview distributions (experimental features)
- **Versions**: Preview variants for latest version only (currently: 40.0.1.Final)
- **Caching**: Uses key `wildfly-{VERSION}-Preview`
- **Provisioning**: Uses Galleon to install `wildfly-preview` feature pack

#### WildFly EE10 Maintenance (`wildfly-ee10-instances.yaml`)
- **Schedule**: Daily at 05:32 UTC
- **Purpose**: Provisions WildFly EE10 distributions (Jakarta EE 10 compatibility)
- **Versions**: EE10 variants for versions 40+ (currently: 40.0.1.Final)
- **Caching**: Uses key `wildfly-{VERSION}-EE10`
- **Provisioning**: Uses Galleon to install `wildfly-ee10` feature pack

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
- **Scan Matrix**: Covers released versions, preview versions, EE10 variants, and nightly builds
  - Released standard: `37.0.1.Final`, `38.0.1.Final`, `39.0.1.Final`, `40.0.1.Final`
  - Released preview: `38.0.1.Final-Preview`, `39.0.1.Final-Preview`, `40.0.1.Final-Preview`
  - Released EE10: `40.0.1.Final-EE10`
  - Nightly standard: `standard-upstream`, `standard-maintenance`
  - Nightly EE10: `ee10-upstream`
  - Nightly preview: `preview-upstream`, `preview-maintenance`
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

## Managing Provisioned Versions and Scan Targets

The scanner maintains a matrix of WildFly versions and distribution variants that are provisioned and scanned daily. This section explains how to manage this matrix as new WildFly versions are released or distribution variants are added.

### Version and Variant Matrix

The scanning infrastructure provisions and scans different combinations of WildFly versions and distribution variants:

#### Released Versions (wildfly-instances.yaml)
- **Purpose**: Provisions stable, released WildFly versions
- **Version Policy**: Scan the last 4 final releases (currently: 37.0.1.Final, 38.0.1.Final, 39.0.1.Final, 40.0.1.Final)
- **Distribution**: Standard WildFly feature pack (`wildfly#VERSION`)
- **Cache Key Pattern**: `wildfly-{VERSION}`

#### Preview Distributions (wildfly-preview-instances.yaml)
- **Purpose**: Provisions WildFly Preview distributions with experimental features
- **Current Versions**: 38.0.1.Final, 39.0.1.Final, 40.0.1.Final
- **Distribution**: Preview feature pack (`wildfly-preview#VERSION`)
- **Cache Key Pattern**: `wildfly-{VERSION}-Preview`

#### EE10 Distributions (wildfly-ee10-instances.yaml)
- **Purpose**: Provisions WildFly EE10 distributions (Jakarta EE 10 compatibility)
- **Current Versions**: 40.0.1.Final
- **Distribution**: EE10 feature pack (`wildfly-ee10#VERSION`)
- **Cache Key Pattern**: `wildfly-{VERSION}-EE10`
- **Note**: Only available for WildFly 40+; earlier versions use EE10 as the default

#### Nightly Builds (wildfly-nightly.yaml)
- **Purpose**: Provisions latest development builds from CI
- **Branches**: `upstream` (main development), `maintenance` (maintenance branch)
- **Distributions**: 
  - Upstream: standard, EE10, and preview
  - Maintenance: standard and preview only
- **Cache Key Pattern**: `wildfly-{standard|ee10|preview}-{upstream|maintenance}-{RUN_ID}-{ATTEMPT}`

#### Scan Matrix (scan-wildfly.yaml)
The scan workflow must include all provisioned versions and variants:
- Released standard versions: `37.0.1.Final`, `38.0.1.Final`, `39.0.1.Final`, `40.0.1.Final`
- Released preview versions: `40.0.1.Final-Preview` (latest version only)
- Released EE10 versions: `40.0.1.Final-EE10`
- Nightly standard builds: `standard-upstream`, `standard-maintenance`
- Nightly EE10 builds: `ee10-upstream` (upstream only)
- Nightly preview builds: `preview-upstream`, `preview-maintenance`

### Adding a New WildFly Version

When a new WildFly version is released, follow these steps:

1. **Update wildfly-instances.yaml**:
   - Add the new version to the `matrix.version` array
   - Example: `41.0.0.Final`
   - The workflow will automatically provision and cache it

2. **Update wildfly-preview-instances.yaml** (if preview is available):
   - Add the new version to the `matrix.version` array
   - The workflow will provision the preview variant

3. **Update scan-wildfly.yaml**:
   - Add the standard version to the `matrix.version` array
   - Add the preview version with `-Preview` suffix if applicable
   - Example: `41.0.0.Final` and `41.0.0.Final-Preview`

4. **Consider Removing Old Versions**:
   - Evaluate if older versions should be removed to reduce scan time
   - Remove from all three workflow files consistently

### Adding Distribution Variants (EE10, EE11)

Starting with WildFly 40, multiple Jakarta EE variants are available. To add these:

1. **Understand Galleon Feature Packs**:
   - Standard: `wildfly#VERSION`
   - Preview: `wildfly-preview#VERSION`
   - EE10: Requires provisioning.xml with both `org.wildfly:wildfly-ee-10-feature-pack:VERSION` and `org.wildfly:wildfly-galleon-pack:VERSION` (order matters!)
   - Note: EE10 requires a provisioning XML file since Galleon CLI doesn't support multiple feature packs in a single install command

2. **Update Provisioning Workflows**:
   - Create new jobs or matrix dimensions for each variant
   - Use appropriate Galleon feature pack names
   - Assign unique cache keys (e.g., `wildfly-{VERSION}-EE10`)

3. **Update Scan Matrix**:
   - Add entries for each new variant
   - Use descriptive names (e.g., `41.0.0.Final-EE10`, `41.0.0.Final-EE11`)
   - Ensure cache key patterns match provisioning workflows

4. **Example for WildFly 41 with EE10**:
   ```yaml
   # Provision step would use:
   # Standard: ./galleon/bin/galleon.sh install wildfly#41.0.0.Final --dir=wildfly
   # Preview: ./galleon/bin/galleon.sh install wildfly-preview#41.0.0.Final --dir=wildfly
   # EE10: Requires provisioning.xml file
   ```
   
   **EE10 Provisioning Example**:
   ```bash
   cat > provisioning.xml << 'EOF'
   <installation xmlns="urn:jboss:galleon:provisioning:3.0">
       <feature-pack location="org.wildfly:wildfly-ee-10-feature-pack:41.0.0.Final"/>
       <feature-pack location="org.wildfly:wildfly-galleon-pack:41.0.0.Final"/>
   </installation>
   EOF
   ./galleon/bin/galleon.sh provision provisioning.xml --dir=wildfly
   ```
   
   **Note**: EE10 provisioning requires both feature packs in the correct order (wildfly-ee-10-feature-pack first, then wildfly-galleon-pack). Galleon CLI doesn't support multiple feature packs in a single install command, so a provisioning XML file is required.

### Version-Specific Variant Support

Not all WildFly versions support all distribution variants:

- **WildFly 36-37**: Standard only (no preview, no EE variants)
- **WildFly 38-39**: Standard and Preview (no EE variants)
- **WildFly 40+**: Standard, Preview, EE10, EE11 (expected)
- **Nightly Builds**: Follow the same pattern as the target release version

When updating workflows, ensure you only provision variants that exist for each version.

### Workflow Synchronization

**Critical**: The scan matrix in `scan-wildfly.yaml` must exactly match what is provisioned:
- Every entry in the scan matrix must have a corresponding cache entry
- Cache keys must match between provisioning and scanning workflows
- Missing cache entries will cause scan failures (`fail-on-cache-miss: true`)

### Testing Changes

After modifying the version matrix:

1. **Manual Workflow Dispatch**:
   - Trigger provisioning workflows manually via GitHub Actions UI
   - Verify successful provisioning and cache creation
   - Check artifact uploads for any errors

2. **Verify Cache Keys**:
   - Ensure cache keys are unique and consistent
   - Check GitHub Actions cache storage for expected entries

3. **Test Scanning**:
   - Manually trigger scan workflow
   - Verify all matrix entries complete successfully
   - Review scan results artifacts

4. **Monitor Scheduled Runs**:
   - Watch the first scheduled run after changes
   - Verify timing and cache hit rates
   - Check for any failures or warnings

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
