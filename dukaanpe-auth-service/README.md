# DukaanPe Auth Service

Phone OTP authentication and session token lifecycle.

## Purpose

- OTP send and verify flow
- JWT access token + refresh token issuance
- Profile endpoints under `/api/auth/me`
- User role and user administration endpoints

## URL

- Direct: `http://localhost:8081`
- Via gateway: `http://localhost:8080`

## Key APIs

- `POST /api/auth/send-otp`
- `POST /api/auth/verify-otp`
- `POST /api/auth/refresh-token`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `PUT /api/auth/me`

Demo login:

- Phone: `9876543210`
- OTP is dynamic by default.
- Use `123456` only when fixed OTP is enabled (`JWT_ALLOW_FIXED_OTP=true`, typically with `dev` profile).

## Run

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform\dukaanpe-auth-service"
mvn spring-boot:run
```

## Test

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
mvn -pl dukaanpe-auth-service test
```

## Service Status (April 2026)

### What Has Been Updated
- Implemented OTP login, JWT access issuance, refresh-token lifecycle, and profile management APIs.
- Implemented gateway-integrated auth flows for login and profile/security operations.
- Added baseline integration coverage for core authentication paths.
- Added safer non-dev defaults: fixed OTP disabled unless explicitly enabled.

### What Remains
- Hardening startup-race and retry behavior in auth-gateway integration tests.
- Add production secret rotation, token governance, and advanced device/session risk controls.
- Add broader negative-path and load-oriented authentication test scenarios.
