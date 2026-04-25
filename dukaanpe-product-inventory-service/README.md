# DukaanPe Product Inventory Service

Product catalog, category management, inventory levels, stock transactions, and expiry tracking.

## URL

- Direct: `http://localhost:8083`
- Via gateway: `http://localhost:8080`

## Key APIs

- `POST /api/products`
- `GET /api/products?storeId=1&page=0&size=20`
- `GET /api/products/search?storeId=1&q=milk&page=0&size=20`
- `GET /api/inventory?storeId=1&page=0&size=20`
- `POST /api/inventory/adjust`
- `GET /api/inventory/transactions?productId=1&page=0&size=20`

## Notes

- Includes seeded grocery categories/products and inventory records.
- Supports strict filter combinations (`categoryId`, `q`, pagination) and dedicated endpoint tests.

## Run

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform\dukaanpe-product-inventory-service"
mvn spring-boot:run
```

## Test

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
mvn -pl dukaanpe-product-inventory-service test
```

## Service Status (April 2026)

### What Has Been Updated
- Implemented category/product CRUD, inventory updates, stock adjustments, and transaction history endpoints.
- Implemented strict search/pagination filter handling with edge-focused controller tests.
- Added integration hooks for billing and supplier inventory interactions.

### What Remains
- Hardening advanced filter combinations and operational edge behavior found during integrated smoke runs.
- Add bulk import/export and high-volume stock reconciliation workflows.
- Add production-safe inventory event streaming adapters beyond synchronous service hooks.
