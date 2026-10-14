# GitHub Repository Configuration & Management Guide

This guide details how Unreel's GitHub repository is configured, protected, and managed following modern mobile engineering standards.

---

## 1. Branch Protection Rules (`main`)

To ensure stability, test integrity, and linear history, protect the `main` branch under **Settings > Branches > Add branch protection rule**:

### Branch name pattern: `main`

#### 1. Protect matching branches
- [x] **Require a pull request before merging**
  - [x] **Require approvals:** `1`
  - [x] **Dismiss stale pull request approvals when new commits are pushed**
  - [x] **Require review from Code Owners**
  - [x] **Require approval of the most recent reviewable push**

#### 2. Require status checks to pass before merging
- [x] **Require status checks to pass before merging**
  - [x] **Require branches to be up to date before merging** (Strict PR checks)
  - **Required Status Checks:**
    - `All CI Checks Passed` (aggregator job from `.github/workflows/ci.yml`)
    - `Validate Conventional Commit Title` (from `.github/workflows/pr-lint.yml`)

#### 3. Merge Restrictions & Hygiene
- [x] **Require conversation resolution before merging**
- [x] **Require signed commits** (strongly recommended)
- [x] **Require linear history** (prevents messy merge commits)
- [x] **Do not allow bypassing the above settings** (enforce for administrators)

---

## 2. Pull Request & Merge Strategy

Configure under **Settings > General > Pull Requests**:

- **Allow Squash Merging:** :white_check_mark: Enabled
  - **Default commit message:** `Pull request title and description`
  - Ensures clean commit history conforming to Conventional Commits.
- **Allow Rebase Merging:** :white_check_mark: Enabled (optional for fast-forward merges)
- **Allow Merge Commits:** :x: Disabled (prevents multi-parent merge commits)
- **Automatically delete head branches:** :white_check_mark: Enabled (keeps repository tidy)

---

## 3. GitHub Actions Workflows

| Workflow | File | Triggers | Description |
|---|---|---|---|
| **CI** | [ci.yml](file:///.github/workflows/ci.yml) | `push` (main), `pull_request` (main), `workflow_dispatch` | Validates wrapper, runs Android Lint, runs all JVM & Android unit tests, runs all KMP iOS tests on macOS runner, builds Android APK and iOS Xcode simulator app, and exposes aggregate gate `All CI Checks Passed`. |
| **PR Title Check** | [pr-lint.yml](file:///.github/workflows/pr-lint.yml) | `pull_request_target` (opened, edited, synchronize) | Enforces Conventional Commits syntax in PR titles (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `ci:`, etc.). |
| **Dependency Review** | [dependency-review.yml](file:///.github/workflows/dependency-review.yml) | `pull_request` (build file changes) | Detects known vulnerabilities (CVEs) and license compliance issues before dependencies are merged. |

---

## 4. Dependabot Automated Maintenance

Configured in [.github/dependabot.yml](file:///.github/dependabot.yml):
- Weekly dependency scans every Monday morning.
- Automatic grouping for `kotlin-ecosystem`, `androidx`, and `testing` libraries to prevent PR spam.
- Commit messages tagged as `chore(deps):` or `ci(deps):`.
- Cap of 5 concurrent open PRs.

---

## 5. Local Developer Git Hook (Optional)

To enforce Conventional Commits locally before pushing, developers can configure git to use the repository's hooks:

```bash
# Point git hooks path to repository hook scripts
git config core.hooksPath .githooks
```

---

## 6. Release Management & SemVer

Releases are tagged using Semantic Versioning (`vMAJOR.MINOR.PATCH`):
- `v0.1.0` - Initial prototype / Phase 0 spikes complete
- `v0.2.0` - Core counting and rule engine verified
- `v1.0.0` - Production-ready release on Google Play & App Store
