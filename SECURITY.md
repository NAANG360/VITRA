# Security Policy

## Scope

VITRA is experimental Android software for local observation. It is not a security scanner, identity-verification system, or guarantee that a device or service is safe.

## Reporting a vulnerability

Please do not publish exploit details, private device identifiers, credentials, or sensitive scan captures in a public issue. Contact the repository owner privately through GitHub's security reporting feature, if enabled, or request a private reporting route before sharing details.

Include the affected revision, Android version/device model, impact, and minimal reproduction steps. Redact personal information and secrets.

## Data and operational boundaries

The current app's BLE observations are rendered locally. BLE addresses/names may be randomized or absent; signal strength is approximate. Local interface enumeration is not a host scan. Do not infer authorization, identity, or device safety from an observation.

Never submit keystores, signing passwords, API tokens, personal scan dumps, or other private data to this repository.
