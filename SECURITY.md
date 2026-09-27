# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability in AI Mobile Studio, please report it via responsible disclosure:

1. Email the maintainer team or open a private advisory on GitHub.
2. Provide a clear proof-of-concept and environment details.
3. Do not disclose vulnerabilities publicly until a patch has been released.

### PRoot Security Boundaries
- The PRoot userspace layer is a software-based syscall interception mechanism, not a virtualization sandbox.
- Users are advised to review all agent scripts and dependencies before granting approval in `ASK_SENSITIVE` mode.
