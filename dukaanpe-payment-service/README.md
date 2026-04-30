# DukaanPe Payment Service

Payment transactions, UPI/QR, reconciliation, and terminal-scoped cash register flows.

## URL

- Direct: `http://localhost:8087`
- Via gateway: `http://localhost:8080`

## Key APIs

- `POST /api/payments/initiate`
- `PUT /api/payments/{transactionId}/complete`
- `GET /api/payments?storeId=1&page=0&size=20&date=2026-03-29`
- `POST /api/payments/upi/generate-link`
- `POST /api/payments/reconciliation`
- `POST /api/payments/cash-register/open`
- `POST /api/payments/cash-register/close`

## Notes

- Idempotency and duplicate reconciliation guards are covered
- Terminal ID normalization regression tests are included

## Run

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform\dukaanpe-payment-service"
mvn spring-boot:run
```

## Test

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
mvn -pl dukaanpe-payment-service test
```

## Service Status (April 2026)

### What Has Been Updated
- Implemented payment transaction lifecycle, UPI/QR endpoints, reconciliation, and terminal-scoped cash register flows.
- Implemented idempotency-key handling, duplicate reconciliation guards, and terminal normalization regressions.
- Added gateway smoke coverage and module-level regression tests.

### What Remains
- Hardening provider adapter boundaries and operational anomaly handling.
- Add real payment gateway adapter integrations with asynchronous callback verification.
- Add production settlement matching, reconciliation exports, and fraud/risk guardrails.
