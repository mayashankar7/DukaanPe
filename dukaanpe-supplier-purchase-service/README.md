# DukaanPe Supplier Purchase Service

Supplier master, purchase orders, GRN workflow, and inventory push hooks.

## URL

- Direct: `http://localhost:8086`
- Via gateway: `http://localhost:8080`

## Key APIs

- `POST /api/suppliers`
- `GET /api/suppliers?storeId=1`
- `POST /api/purchase-orders`
- `GET /api/purchase-orders?storeId=1&page=0&size=20`
- `GET /api/purchase-orders/auto-suggest?storeId=1`
- `POST /api/grn`
- `PUT /api/grn/{id}/verify`
- `PUT /api/grn/{id}/approve`

## Notes

- Approve flow can push stock adjustments to inventory service
- Contains contract/integration tests for hook payload behavior

## Run

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform\dukaanpe-supplier-purchase-service"
mvn spring-boot:run
```

## Test

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
mvn -pl dukaanpe-supplier-purchase-service test
```

## Service Status (April 2026)

### What Has Been Updated
- Implemented supplier CRUD, purchase orders, GRN create/verify/approve workflow, and auto-reorder suggestions.
- Implemented inventory stock-adjustment push hook on approved GRN.
- Added contract and integration tests for GRN and hook payload/URL behavior.

### What Remains
- Hardening downstream inventory-call retry and fallback behavior under degraded network conditions.
- Add production event-driven reorder integrations and supplier performance analytics.
- Add extended procurement audit controls and approval policy workflows.
