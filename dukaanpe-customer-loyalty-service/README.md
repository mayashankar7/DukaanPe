# DukaanPe Customer Loyalty Service

Core bootstrap slice for Service 10:
- Customer CRUD (`/api/customers`)
- Purchase history (`/api/customers/{id}/purchases`)
- Loyalty earn/redeem (`/api/loyalty/earn`, `/api/loyalty/redeem`)
- Loyalty summary and transaction history

## Service Status (April 2026)

### What Has Been Updated
- Implemented customer CRUD, search/find-by-phone/top-customer filters, purchase history, loyalty earn/redeem, settings, and campaign APIs.
- Implemented campaign eligible-customer preview helpers with a self-describing count DTO response.
- Added integration and edge-case tests for filtering and pagination behavior.

### What Remains
- Hardening advanced loyalty-tier and campaign execution edge workflows.
- Add production campaign orchestration adapters with throttling/retry and delivery controls.
- Add richer customer segmentation and analytics-driven targeting automation.

## Run tests

```powershell
mvn test
```

## Run service

```powershell
mvn spring-boot:run
```

Port: `8088`
H2 console: `http://localhost:8088/h2-console`
