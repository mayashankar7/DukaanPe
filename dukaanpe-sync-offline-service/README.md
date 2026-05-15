# DukaanPe Sync Offline Service

Service 15 for offline-first sync event exchange.

## Features

- Push sync events from local/offline clients
- Pull events since a timestamp
- Persisted sync event log with dedupe-window checks and conflict candidate marking

## Base URLs

- Direct service: `http://localhost:8093`
- Via gateway: `http://localhost:8080`

## APIs

- `POST /api/sync/push`
- `GET /api/sync/pull?storeId=701&since=2026-03-26T11:00:00`

## Sample Payloads

### Push Request

```json
{
  "storeId": 701,
  "entityType": "BILL",
  "entityId": "BILL-1001",
  "operation": "UPSERT",
  "payload": "{\"amount\":1200}"
}
```

### Push Response

```json
{
  "success": true,
  "message": "Sync event accepted",
  "data": {
    "id": 1,
    "storeId": 701,
    "entityType": "BILL",
    "entityId": "BILL-1001",
    "operation": "UPSERT",
    "payload": "{\"amount\":1200}",
    "createdAt": "2026-03-26T11:12:00"
  },
  "timestamp": "2026-03-26T11:12:00",
  "statusCode": 200
}
```

### Pull Response (trimmed)

```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "storeId": 701,
      "entityType": "BILL",
      "entityId": "BILL-1001",
      "operation": "UPSERT"
    }
  ]
}
```

## Service Status (April 2026)

### What Has Been Updated
- Implemented core push/pull sync APIs with persisted event records.
- Added dedupe-window replay safety and conflict-candidate detection baseline.
- Implemented gateway routing and platform health integration.
- Added baseline operator UI wiring for queue/status visibility.

### What Remains
- Hardening conflict-handling UX and richer audit/export interactions.
- Expanding test coverage for device and queue workflows.
- Add production conflict-resolution policy workflows and reconciler tooling.
- Add multi-device sync observability/alerting and scale-oriented backpressure controls.

## Run Tests

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
mvn -pl dukaanpe-sync-offline-service test
```
