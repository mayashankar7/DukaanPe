# DukaanPe Discovery Server

Service discovery registry for all backend services.

## Purpose

- Runs Eureka server on `8761`
- Accepts registrations from gateway and domain services
- Provides service lookup during local development

## URL

- Dashboard: `http://localhost:8761`
- Health: `http://localhost:8761/actuator/health`

## Run

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform\dukaanpe-discovery-server"
mvn spring-boot:run
```

## Test

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
mvn -pl dukaanpe-discovery-server test
```

## Service Status (April 2026)

### What Has Been Updated
- Implemented Eureka discovery bootstrapping on port `8761`.
- Implemented health and registry dashboard endpoints used by startup orchestration.
- Added this module to parent build flow with baseline test coverage.

### What Remains
- Hardening startup-timing resilience checks and profile-level runtime tuning during full-stack smoke runs.
- Add production deployment guidance for TLS/auth and multi-node Eureka topology.
- Add CI scenarios for discovery failover and recovery behavior.
