# DukaanPe Platform

DukaanPe is a microservices-based Kirana/Dukaan digitization platform with Spring Boot backend services and an Angular frontend.

## Service Status (April 2026)

### Done
- Implemented the monorepo structure for discovery, gateway, all domain services, and the Angular frontend.
- Implemented and documented core backend domains: auth, store, product/inventory, billing, udhar, supplier, payment, customer-loyalty, GST, and analytics.
- Added root operational scripts for start/stop/health/smoke/reporting verification.
- Added release gating scripts (`assert-backend-chunks-summary.ps1`, `release-gate.ps1`) and committed full backend chunk-matrix evidence under `test-reports/backend-chunks/20260331-225838/summary.json`.
- Added frontend E2E smoke script wiring (`dukaanpe-frontend/e2e/smoke.spec.ts`, `npm run e2e:smoke`) and shared UI state panel conventions.

### In Progress
- Advanced frontend operator workflow polish for deep edge scenarios across GST/analytics/utility modules.
- Utility services production-depth hardening (real providers, stronger persistence/queue infrastructure, compliance callbacks/audit depth).
- Centralized observability rollout (collector/export pipeline, dashboards, alerting).

### Remaining Work
- Enforce branch protection with required CI checks so release sign-off is not script/manual-only.
- Move release sign-off from manual/script-only execution to enforced branch protection via CI status checks.
- Complete production runtime hardening (secret rotation policy, environment-specific config governance, platform-level observability ownership).

## Current Status Snapshot (Workspace)

As of this workspace state:

- Parent build includes all 15 backend modules in `pom.xml`.
- Service folders exist for discovery, gateway, and all domain services (`8081`-`8093`).
- Frontend app exists in `dukaanpe-frontend` with feature folders for stores/products/billing/customers/payments/udhar and supporting modules.
- Health and smoke helper scripts are present under `scripts/`.
- Local release gate scripts are present under `scripts/`, and CI workflow automation is present in `.github/workflows/ci-platform.yml`.

Use the verification commands below to confirm local runtime health on your machine.

## What Is Done vs In Progress

Status legend used below:

- `Done (code)` = module/service exists and has been implemented in this workspace flow.
- `In progress` = present but still being expanded/hardened.
- `Planned` = scoped but not yet implemented to target depth.
- `Runtime verified` must be confirmed via `scripts/check-services.ps1` and `scripts/smoke-platform.ps1` on your machine.

### Backend Services Matrix

| Service | Status | Notes for Frontend Planning |
|---|---|---|
| `dukaanpe-discovery-server` | `Done (code)` | Infra dependency; start first, no direct UI calls. |
| `dukaanpe-api-gateway` | `Done (code)` | Single frontend entrypoint (`/api/**` via `:8080`). |
| `dukaanpe-auth-service` | `Done (code)` | OTP login and profile APIs for auth shell. |
| `dukaanpe-store-service` | `Done (code)` | Store CRUD/staff/timings for stores area. |
| `dukaanpe-product-inventory-service` | `Done (code)` | Products, inventory, expiry endpoints for catalog/POS. |
| `dukaanpe-billing-pos-service` | `Done (code)` | Billing/POS core flows for checkout screens. |
| `dukaanpe-udhar-khata-service` | `Done (code)` | Credit/khata/reminders/settlement APIs for udhar module. |
| `dukaanpe-supplier-purchase-service` | `Done (code)` | Supplier, PO, GRN, reorder suggestions. |
| `dukaanpe-payment-service` | `Done (code)` | Transactions + UPI/QR + reconciliation + cash register. |
| `dukaanpe-customer-loyalty-service` | `Done (code)` | Customer CRUD, loyalty, campaigns, targeting helpers. |
| `dukaanpe-gst-tax-service` | `Done (code)` | HSN, invoices, returns prep, tax summaries. |
| `dukaanpe-analytics-service` | `Done (code)` | Dashboard/sales/product insight endpoints with seeded data. |
| `dukaanpe-notification-service` | `Done (code)` | Durable queue + retry + callback endpoint + provider abstraction baseline. |
| `dukaanpe-language-service` | `Done (code)` | Provider abstraction + persisted translation audit/glossary version baseline. |
| `dukaanpe-sync-offline-service` | `Done (code)` | Durable event log + dedupe/replay safety + conflict detection baseline. |

### Frontend Modules Matrix

| Frontend Module | Status | Notes |
|---|---|---|
| `auth` | `Done (code)` | OTP login flow and guarded app entry. |
| `dashboard` | `In progress` | Core dashboard exists; continue richer widget parity. |
| `stores` | `Done (code)` | Typed list/actions and table coverage already started. |
| `products` | `Done (code)` | Typed list/filter/pager baseline in place. |
| `billing` | `Done (code)` | Core list/POS coupling in place; continue advanced POS UX. |
| `payments` | `Done (code)` | Typed page wiring pattern applied; continue deep ops screens. |
| `customers` | `Done (code)` | Typed page wiring and filter/pager behavior added. |
| `udhar` | `Done (code)` | Typed query-param pattern and table/pager tests added. |
| `suppliers` | `Done (code)` | Supplier/Purchase/GRN/reorder routes and baseline UX wiring are present. |
| `gst` | `In progress` | Backend ready; expand full compliance UI flows. |
| `analytics` | `In progress` | Backend ready; continue chart-heavy dashboard polish. |
| `notification` | `In progress` | Page/routing baseline exists; continue production-grade operator workflows. |
| `language` | `In progress` | Translation baseline exists; continue audit/export/governance UX depth. |
| `sync` | `In progress` | Queue/conflict baseline exists; continue device/event ops depth. |
| `profile/settings` | `In progress` | Continue account/store settings hardening. |

Operational note: treat this matrix as implementation status, then run the verification commands in this README to confirm runtime health before demo/use.

## Architecture (High Level)

```text
+--------------------+      +----------------------+      +-------------------+
| Angular Frontend   | ---> | API Gateway :8080    | ---> | Domain Services   |
| :4200              |      | Spring Cloud Gateway |      | :8081 - :8093     |
+--------------------+      +----------------------+      +-------------------+
                                      |
                                      v
                            +---------------------+
                            | Eureka Discovery    |
                            | :8761               |
                            +---------------------+
```

## Services and Ports

| Service | Port |
|---|---:|
| `dukaanpe-discovery-server` | 8761 |
| `dukaanpe-api-gateway` | 8080 |
| `dukaanpe-auth-service` | 8081 |
| `dukaanpe-store-service` | 8082 |
| `dukaanpe-product-inventory-service` | 8083 |
| `dukaanpe-billing-pos-service` | 8084 |
| `dukaanpe-udhar-khata-service` | 8085 |
| `dukaanpe-supplier-purchase-service` | 8086 |
| `dukaanpe-payment-service` | 8087 |
| `dukaanpe-customer-loyalty-service` | 8088 |
| `dukaanpe-gst-tax-service` | 8089 |
| `dukaanpe-analytics-service` | 8090 |
| `dukaanpe-notification-service` | 8091 |
| `dukaanpe-language-service` | 8092 |
| `dukaanpe-sync-offline-service` | 8093 |
| `dukaanpe-frontend` | 4200 |

## Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 18+
- Angular CLI 17+

## Demo Login

- Phone: `9876543210`
- OTP: dynamic by default. `123456` works only when `JWT_ALLOW_FIXED_OTP=true` (for example `dev` profile/local demo mode).

## Quick Start (Windows Recommended)

From project root:

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\start-and-check.ps1"
```

This helper:

- starts `scripts/ci/start-platform.ps1` (canonical startup entrypoint)
- repeatedly runs `scripts/check-services.ps1`
- exits `0` when all services are healthy, else exits `1` on timeout

Optional params:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\start-and-check.ps1" -TimeoutSec 1200 -PollIntervalSec 15
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\start-and-check.ps1" -SkipStart -TimeoutSec 120 -PollIntervalSec 5
```

## Manual Run Order (If Starting Service-by-Service)

1. `dukaanpe-discovery-server` (`8761`)
2. `dukaanpe-api-gateway` (`8080`)
3. Domain services (`8081` to `8093`)
4. `dukaanpe-frontend` (`4200`)

For step-by-step first-time guidance, use `docs/SETUP_AND_RUN_FIRST_TIME.md`.

## Gateway API Route Map

- `/api/auth/**` -> `auth-service:8081`
- `/api/stores/**` -> `store-service:8082`
- `/api/products/**`, `/api/inventory/**` -> `product-inventory-service:8083`
- `/api/bills/**`, `/api/pos/**` -> `billing-pos-service:8084`
- `/api/khata/**`, `/api/udhar/**` -> `udhar-khata-service:8085`
- `/api/suppliers/**`, `/api/purchase-orders/**`, `/api/grn/**` -> `supplier-purchase-service:8086`
- `/api/payments/**` -> `payment-service:8087`
- `/api/customers/**`, `/api/loyalty/**` -> `customer-loyalty-service:8088`
- `/api/gst/**`, `/api/tax/**` -> `gst-tax-service:8089`
- `/api/analytics/**` -> `analytics-service:8090`
- `/api/notifications/**` -> `notification-service:8091`
- `/api/language/**` -> `language-service:8092`
- `/api/sync/**` -> `sync-offline-service:8093`

## Canonical Scripts

Use these first for operator workflows. Wrapper scripts remain for compatibility but should not be the primary entrypoint.

| Workflow | Canonical script | Wrapper alias (optional) |
| --- | --- | --- |
| Start | `scripts/ci/start-platform.ps1` | `scripts/start-and-check.ps1` |
| Check | `scripts/check-services.ps1` | - |
| Smoke | `scripts/smoke-platform.ps1` | - |
| Stop | `scripts/ci/stop-platform.ps1` | `scripts/stop-services.ps1` |

## Verification Commands

### CI Baseline (Current State)

- CI workflow automation is present in `.github/workflows/ci-platform.yml`.
- Release checks remain runnable locally via scripts for parity and debugging.
- CI target flow is: backend chunk execution -> frontend quality -> smoke/e2e -> release gate.

To regenerate backend chunk evidence locally before release gate:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\run-backend-test-chunks.ps1" -RetryCount 0
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\assert-backend-chunks-summary.ps1" -ReportRoot ".\test-reports\backend-chunks"
```

`run-backend-test-chunks.ps1` now auto-prunes old runs and keeps the latest 5 timestamped report folders by default.
Manual retention command (optional):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\prune-backend-chunk-reports.ps1" -ReportRoot ".\test-reports\backend-chunks" -KeepLatest 5
```

To run full local release gate (chunk + smoke schema checks):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\release-gate.ps1"
```

Health checks for all services:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\check-services.ps1"
```

Canonical stop flow (ports + compose cleanup):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\ci\stop-platform.ps1"
```

The legacy alias below is still supported and forwards to the canonical script:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\stop-services.ps1"
```

Preview shutdown targets without terminating processes:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\ci\stop-platform.ps1" -DryRun
```

Include frontend (`4200`) in shutdown:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\ci\stop-platform.ps1" -IncludeFrontend
```

Emit shutdown results as JSON:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\ci\stop-platform.ps1" -EmitJson -OutputPath ".\ops-stop-status.json"
```

Emit health checks in the same JSON schema shape used by smoke status:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\check-services.ps1" -EmitJson -OutputPath ".\ops-status.check.json"
```

Gateway + cross-service smoke flow:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\smoke-platform.ps1"
```

This command is the authoritative runtime verification source and writes a structured report to `ops-status.json`.
Only `ops-status.json` and `ops-status.check.json` are canonical; other ops logs are disposable.

Read the latest smoke report in a compact operator-friendly format:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\read-smoke-status.ps1"
```

Smoke flow with custom options:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\smoke-platform.ps1" -GatewayBaseUrl "http://localhost:8080" -TimeoutSec 25
```

Custom output file example:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\smoke-platform.ps1" -OutputPath ".\test-reports\ops-status.latest.json"
```

Generate one canonical platform report (health + smoke merged):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\platform-health-report.ps1" -OutputPath ".\ops-status.json"
powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\read-smoke-status.ps1" -Path ".\ops-status.json"
```

## Containerized Local Orchestration

All modules now include Dockerfiles and the root `docker-compose.yml` wires the full stack.

Use `.env.example` as the base for local overrides:

```powershell
Copy-Item ".\.env.example" ".\.env" -Force
```

Validate and run compose:

```powershell
docker compose config
docker compose up -d --build
docker compose ps
```

Shutdown compose stack:

```powershell
docker compose down
```

## CI Pipeline

- Repository CI workflow is committed at `.github/workflows/ci-platform.yml`.
- Pipeline covers backend chunk tests, frontend build/tests, smoke + E2E, and final `scripts/release-gate.ps1`.
- Enforce these checks via branch protection using `docs/BRANCH_PROTECTION_REQUIRED_CHECKS.md`.

## CI Helper Scripts

- `scripts/ci/start-platform.ps1` starts compose services with secure defaults, waits for gateway readiness, and can optionally start frontend.
- `scripts/ci/stop-platform.ps1` stops compose stack and any frontend process started by the CI helper.
- These helpers reduce local-vs-CI drift for smoke and E2E startup behavior.

## API Contracts

- Services publish OpenAPI JSON at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`.
- Use `scripts/export-openapi.ps1` to export contract snapshots to `docs/openapi/`.

## Config and Secret Hardening

- `dukaanpe-auth-service` now supports env-backed JWT and datasource settings.
- `dukaanpe-api-gateway` routes and Eureka URL now support env overrides.
- `.env.example` documents secret/config keys for local and CI usage.

Important: do not commit production-grade secrets into repository files. Keep real JWT secrets in CI/CD and runtime environment variables.

## Observability Scaffold

- Collector baseline config: `observability/otel-collector-config.yaml`.
- Prometheus baseline scrape config: `observability/prometheus.yml`.
- Per-service alert templates: `observability/alerts/services/*.alerts.template.yml`.
- Auditable ownership matrix: `docs/observability-service-owner-matrix.md`.
- Release ownership and alert checklist: `docs/OBSERVABILITY_ALERT_CHECKLIST.md`.
- Start optional observability containers with compose profile: `docker compose --profile observability up -d`.

## H2 Consoles

H2 console is disabled in default config and intended for `dev` profile only.

Enable with `SPRING_PROFILES_ACTIVE=dev` when local debugging requires it.

Examples:

- `http://localhost:8081/h2-console`
- `http://localhost:8082/h2-console`
- `http://localhost:8083/h2-console`

## Documentation Index

- Service endpoint catalog: `docs/service-catalog.md`
- First-time setup/run guide: `docs/SETUP_AND_RUN_FIRST_TIME.md`
- Frontend milestones: `docs/frontend-milestones.md`

## Technology Stack

- Spring Boot 3.2.x
- Spring Cloud 2023.0.x
- Spring Data JPA + H2
- OpenFeign
- Angular 17
- Tailwind CSS
- Chart.js

## Roadmap Notes

- Continue frontend expansion against finalized endpoint contracts.
- Add tighter cross-service smoke assertions per business flow.
- Wire release scripts into committed CI workflow and branch protection checks.

