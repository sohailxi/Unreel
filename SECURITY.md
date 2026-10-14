# Security & Privacy Policy

## 🔒 Privacy Guarantees

Unreel is built on an offline-first, local-storage architecture designed with uncompromising privacy principles:

1. **Zero Content Inspection:** Unreel never inspects, captures, optical-character-recognizes (OCR), or stores on-screen text, comments, captions, creator names, or media streams.
2. **Local-Only Metrics:** All session timestamps, dwell metrics, and reel counts are persisted exclusively on device via local SQLite databases.
3. **No Telemetry Leaks:** Unreel does not send usage events to remote telemetry servers or third-party ad networks.

---

## 🛡️ Supported Versions

Only the latest release and the current active development branch (`main`) receive security patches:

| Version | Supported |
|---|---|
| `main` (trunk) | :white_check_mark: |
| Latest Release | :white_check_mark: |
| Older Versions | :x: |

---

## 📬 Reporting a Vulnerability

If you discover a security vulnerability or potential privacy leak in Unreel, please do **NOT** open a public issue. Instead, follow responsible disclosure practices:

1. **Email the Maintainers:** Send an email to the repository owner via the contact email listed on the GitHub profile (`@sohailxi`).
2. **GitHub Private Vulnerability Reporting:** If enabled on this repository, submit your report via [GitHub Security Advisories](https://github.com/sohailxi/Unreel/security/advisories/new).

Please include:
- A description of the vulnerability and its potential impact.
- Step-by-step reproduction instructions or proof-of-concept code.
- Affected platforms (Android, iOS, or shared architecture).

### Response Timeline
- **Initial Response:** Within 48 hours acknowledging receipt.
- **Triage & Assessment:** Within 5 business days.
- **Fix & Disclosure:** Coordinated release before public disclosure.
