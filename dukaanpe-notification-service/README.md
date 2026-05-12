# DukaanPe Notification Service

Service 13 for basic notification workflows.

## Features

- Send notifications (`SMS`, `EMAIL`, etc.)
- List notifications by store
- Persisted delivery state with retry scheduling, queue limits, and provider callback updates

## Base URLs

- Direct service: `http://localhost:8091`
- Via gateway: `http://localhost:8080`

## APIs

- `POST /api/notifications/send`
- `GET /api/notifications?storeId=601`

## Sample Payloads

### Send Notification Request

```json
{
  "storeId": 601,
  "channel": "SMS",
  "recipient": "9876500001",
  "title": "Payment Received",
  "message": "You received Rs 500"
}
```

### Send Notification Response

```json
{
  "success": true,
  "message": "Notification queued",
  "data": {
    "id": 1,
    "storeId": 601,
    "channel": "SMS",
    "recipient": "9876500001",
    "title": "Payment Received",
    "message": "You received Rs 500",
    "status": "SENT",
    "sentAt": "2026-03-26T11:10:00"
  },
  "timestamp": "2026-03-26T11:10:00",
  "statusCode": 200
}
```

### List Response (trimmed)

```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "storeId": 601,
      "channel": "SMS",
      "recipient": "9876500001",
      "status": "SENT"
    }
  ]
}
```

## Run Tests

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
mvn -pl dukaanpe-notification-service test
```

## Service Status (April 2026)

### What Has Been Updated
- Implemented notification send/list flows with persisted queue state and callback updates.
- Implemented retry/backoff behavior and queue-cap safeguards for local runtime stability.
- Implemented gateway routing and platform health integration for this service.
- Added baseline operator UI wiring for notification operations.

### What Remains
- Hardening provider abstraction and richer audit/export operator interactions.
- Expanding component and integration test coverage for notification workflows.
- Add production WhatsApp/SMS/email adapters (real vendor APIs, secrets, delivery SLAs).
- Add compliance-grade template/version governance, alerting, and retention policies.
