# DukaanPe API Gateway

Central ingress for frontend and smoke checks.

## Purpose

- Routes `/api/**` traffic to backend services
- Applies global filters (logging, rate limiting)
- Exposes actuator endpoints for health checks

## URL

- Base: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`

## Key Routed Domains

- `/api/auth/**` -> auth service
- `/api/stores/**` -> store service
- `/api/products/**` and `/api/inventory/**` -> inventory service
- `/api/bills/**` -> billing service
- `/api/khata/**` and `/api/udhar/**` -> udhar service
- `/api/suppliers/**`, `/api/purchase-orders/**`, `/api/grn/**` -> supplier service
- `/api/payments/**`, `/api/customers/**`, `/api/gst/**`, `/api/analytics/**`
- `/api/notifications/**`, `/api/language/**`, `/api/sync/**`

## Run

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform\dukaanpe-api-gateway"
mvn spring-boot:run
```

## Test

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
mvn -pl dukaanpe-api-gateway test
```

## Service Status (April 2026)

### What Has Been Updated
- Implemented gateway routes for all backend service domains under `/api/**`.
- Implemented global logging/rate-limit filters and actuator health exposure.
- Added gateway smoke checks and route assertions to infra regression flow.

### What Remains
- Hardening integration timing behavior with retry-safe assertions during clean stack startup.
- Add production-grade policies for auth propagation, circuit breaking, and adaptive rate limiting.
- Add structured gateway telemetry export for centralized observability.
