# DukaanPe Language Service

Service 14 for language support and translation helpers.

## Features

- Translate text across supported languages
- Return supported language codes
- Translation provider abstraction with deterministic local fallback
- Persisted translation audit records with provider and glossary-version metadata

## Base URLs

- Direct service: `http://localhost:8092`
- Via gateway: `http://localhost:8080`

## APIs

- `POST /api/language/translate`
- `GET /api/language/supported`

## Sample Payloads

### Translate Request

```json
{
  "text": "hello",
  "sourceLanguage": "en",
  "targetLanguage": "hi"
}
```

### Translate Response

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "sourceLanguage": "en",
    "targetLanguage": "hi",
    "originalText": "hello",
    "translatedText": "namaste"
  },
  "timestamp": "2026-03-26T11:11:00",
  "statusCode": 200
}
```

### Supported Languages Response

```json
{
  "success": true,
  "message": "Success",
  "data": ["en", "hi", "ta"],
  "timestamp": "2026-03-26T11:11:00",
  "statusCode": 200
}
```

## Run Tests

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
mvn -pl dukaanpe-language-service test
```

## Service Status (April 2026)

### What Has Been Updated
- Implemented supported-language and translation endpoints with deterministic local fallback.
- Implemented gateway routing and health-script integration for language APIs.
- Added baseline operator UI wiring for translation operations.
- Added persisted translation audit trail and glossary-version capture.

### What Remains
- Hardening filter/audit behavior and operator UX parity.
- Expanding component tests for export/audit interactions.
- Add production translation-provider adapters and enterprise glossary governance workflows.
- Add cross-service multilingual content rollout policy (versioning, approvals, rollback).
