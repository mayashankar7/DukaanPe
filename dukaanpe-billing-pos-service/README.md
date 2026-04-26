# DukaanPe Billing POS Service

Billing, bill history filters, return flows, and cross-service hooks.

## URL

- Direct: `http://localhost:8084`
- Via gateway: `http://localhost:8080`

## Key APIs

- `POST /api/bills`
- `GET /api/bills?storeId=1&page=0&size=20`
- `GET /api/bills/search?storeId=1&q=BILL&page=0&size=20`
- `PUT /api/bills/{id}/cancel`
- `GET /api/bills/today-summary?storeId=1`

## Notes

- Billing search/date pagination edge tests are in place
- Credit-mode bills can hook into udhar endpoints

## Run

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform\dukaanpe-billing-pos-service"
mvn spring-boot:run
```

## Test

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
mvn -pl dukaanpe-billing-pos-service test
```

## Service Status (April 2026)

### What Has Been Updated
- Implemented core billing APIs, search/date pagination filters, and cancellation workflows.
- Implemented POS calculations and credit-mode hook integration into udhar service.
- Added edge-case controller tests for bill query/filter behavior.

### What Remains
- Hardening deep POS interaction paths and cross-service fallback behavior for partial failures.
- Add richer receipt/invoice export variants and printer-adapter production tuning.
- Add advanced reconciliation/audit traceability across billing and payment boundaries.
