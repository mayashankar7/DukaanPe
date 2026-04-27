# DukaanPe Udhar Khata Service

Credit ledger, reminders, settlements, and defaulter tracking.

## URL

- Direct: `http://localhost:8085`
- Via gateway: `http://localhost:8080`

## Key APIs

- `POST /api/khata/customers`
- `GET /api/khata/customers?storeId=1&page=0&size=20`
- `POST /api/udhar/credit`
- `POST /api/udhar/payment`
- `GET /api/udhar/entries?customerId=1&page=0&size=20`
- `GET /api/udhar/overdue?storeId=1&page=0&size=20`
- `POST /api/udhar/reminders`
- `POST /api/udhar/settlements`

## Notes

- Includes monthly settlement and reminder flow coverage
- Supports filter hardening with date windows and pagination

## Run

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform\dukaanpe-udhar-khata-service"
mvn spring-boot:run
```

## Test

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
mvn -pl dukaanpe-udhar-khata-service test
```

## Service Status (April 2026)

### What Has Been Updated
- Implemented khata customer management, credit/payment entries, reminders, settlements, and report endpoints.
- Implemented date-range and pagination filter hardening for entries, overdue, and reporting APIs.
- Added billing credit-mode integration hook support.

### What Remains
- Hardening overdue scoring and end-to-end collections journey assertions in smoke validation.
- Add production notification provider integrations for reminder delivery channels.
- Add advanced credit risk scoring and collections automation workflows.
