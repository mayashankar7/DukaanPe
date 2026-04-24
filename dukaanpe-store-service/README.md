# DukaanPe Store Service

Store profile, staffing, timings, licenses, and subscription management.

## URL

- Direct: `http://localhost:8082`
- Via gateway: `http://localhost:8080`

## Key APIs

- `POST /api/stores`
- `GET /api/stores`
- `GET /api/stores/{id}`
- `PUT /api/stores/{id}`
- `GET /api/stores/{id}/staff`
- `PUT /api/stores/{id}/timings`

## Notes

- Includes seed stores for demo owner users
- Supports owner filter and pageable listing flows

## Run

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform\dukaanpe-store-service"
mvn spring-boot:run
```

## Test

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
mvn -pl dukaanpe-store-service test
```

## Service Status (April 2026)

### What Has Been Updated
- Implemented store CRUD, timings, staff, subscription, and license management endpoints.
- Implemented pagination/filter behavior with controller integration tests.
- Added gateway routing and smoke-path coverage for core store workflows.

### What Remains
- Hardening UX-contract alignment and strict edge validation for complex update scenarios.
- Add production role-based policy controls and enriched audit trails.
- Add high-volume sorting/index and scalability tests for larger store/staff datasets.
