# DukaanPe Analytics Service

Service 12 provides dashboard aggregations and insights on top of seeded daily time-series.

## Features

- Dashboard aggregates (`grossSales`, `netSales`, `orders`, `AOV`) with date-wise series
- Sales insights (period-over-period growth, best sales day)
- Product insights (top products by revenue)
- Deterministic seed endpoint for test/demo data

## Base URLs

- Direct service: `http://localhost:8090`
- Via gateway: `http://localhost:8080`

## APIs

- `POST /api/analytics/seed`
- `GET /api/analytics/dashboard?storeId=501&fromDate=2026-03-20&toDate=2026-03-26`
- `GET /api/analytics/sales-insights?storeId=501&fromDate=2026-03-20&toDate=2026-03-26`
- `GET /api/analytics/product-insights?storeId=501&fromDate=2026-03-20&toDate=2026-03-26&limit=5`

## Sample Payloads

### Seed Request

```json
{
  "storeId": 501,
  "fromDate": "2026-03-20",
  "toDate": "2026-03-26"
}
```

### Dashboard Response (trimmed)

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "storeId": 501,
    "days": 7,
    "totalOrders": 812,
    "grossSales": 123456.78,
    "netSales": 117578.84,
    "totalTax": 5877.94,
    "averageOrderValue": 144.80,
    "timeSeries": [
      {
        "date": "2026-03-20",
        "grossSales": 17123.45,
        "netSales": 16308.05,
        "taxAmount": 815.40,
        "ordersCount": 110
      }
    ]
  }
}
```

### Product Insights Response (trimmed)

```json
{
  "success": true,
  "data": {
    "storeId": 501,
    "limit": 3,
    "topProducts": [
      {
        "productId": 1001,
        "productName": "Aashirvaad Atta 10kg",
        "totalQuantitySold": 112.44,
        "totalRevenue": 56220.00
      }
    ]
  }
}
```

## Service Status (April 2026)

### What Has Been Updated
- Implemented dashboard aggregation, sales insights, product insights, and seeded time-series endpoints.
- Implemented smoke/API checks for key analytics response paths.
- Added frontend typed-client integration baseline for analytics views.

### What Remains
- Hardening operator UX parity with richer summary cards, filters, and interaction tests.
- Add anomaly detection, forecasting, and scheduled reporting workflows.
- Add production observability for aggregation freshness and long-running analytics jobs.

## Run Tests

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
mvn -pl dukaanpe-analytics-service test
```
