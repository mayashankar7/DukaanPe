# DukaanPe GST Tax Service

Service 11 for GST compliance workflows:

- HSN master CRUD
- GST invoice generation
- GST return preparation
- Tax summary and rate-wise breakup

## Base URLs

- Direct service: `http://localhost:8089`
- Via gateway: `http://localhost:8080`

## Endpoints

- `POST /api/gst/hsn`
- `GET /api/gst/hsn/{id}`
- `GET /api/gst/hsn?page=0&size=20&hsnCode=&description=`
- `PUT /api/gst/hsn/{id}`
- `DELETE /api/gst/hsn/{id}`

- `POST /api/gst/invoices/generate`
- `GET /api/gst/invoices/{id}`
- `GET /api/gst/invoices?storeId=301&fromDate=2026-03-01&toDate=2026-03-31&page=0&size=20`

- `POST /api/gst/returns/prepare`
- `GET /api/gst/returns/{id}`

- `GET /api/tax/summary?storeId=301&fromDate=2026-03-01&toDate=2026-03-31`

## Sample Payloads

### 1) Create HSN

Request:

```json
{
  "hsnCode": "1001",
  "description": "Wheat and meslin",
  "gstRate": 5.00,
  "cessRate": 0.00
}
```

Response:

```json
{
  "success": true,
  "message": "HSN created",
  "data": {
    "id": 1,
    "hsnCode": "1001",
    "description": "Wheat and meslin",
    "gstRate": 5.00,
    "cessRate": 0.00,
    "active": true,
    "createdAt": "2026-03-26T10:00:00",
    "updatedAt": "2026-03-26T10:00:00"
  },
  "timestamp": "2026-03-26T10:00:00",
  "statusCode": 200
}
```

### 2) Generate GST Invoice

Request:

```json
{
  "storeId": 301,
  "invoiceNumber": "GST-INV-001",
  "invoiceDate": "2026-03-26",
  "customerName": "GST Customer",
  "customerGstin": "29ABCDE1234F2Z5",
  "placeOfSupply": "KA",
  "intraState": true,
  "items": [
    {
      "hsnCode": "1001",
      "itemDescription": "Taxable Line",
      "quantity": 1.000,
      "taxableValue": 1000.00
    }
  ]
}
```

Response:

```json
{
  "success": true,
  "message": "GST invoice generated",
  "data": {
    "id": 11,
    "storeId": 301,
    "invoiceNumber": "GST-INV-001",
    "invoiceDate": "2026-03-26",
    "intraState": true,
    "taxableAmount": 1000.00,
    "cgstAmount": 90.00,
    "sgstAmount": 90.00,
    "igstAmount": 0.00,
    "cessAmount": 0.00,
    "totalTaxAmount": 180.00,
    "invoiceTotal": 1180.00,
    "items": [
      {
        "hsnCode": "1001",
        "taxableValue": 1000.00,
        "gstRate": 18.00,
        "cessRate": 0.00,
        "cgstAmount": 90.00,
        "sgstAmount": 90.00,
        "igstAmount": 0.00,
        "cessAmount": 0.00,
        "totalTax": 180.00,
        "lineTotal": 1180.00
      }
    ]
  },
  "timestamp": "2026-03-26T10:02:00",
  "statusCode": 200
}
```

### 3) Prepare GST Return

Request:

```json
{
  "storeId": 301,
  "returnType": "GSTR1",
  "periodStart": "2026-03-01",
  "periodEnd": "2026-03-31"
}
```

Response:

```json
{
  "success": true,
  "message": "GST return prepared",
  "data": {
    "id": 7,
    "storeId": 301,
    "returnType": "GSTR1",
    "periodStart": "2026-03-01",
    "periodEnd": "2026-03-31",
    "totalInvoices": 24,
    "taxableAmount": 125000.00,
    "totalTaxAmount": 18450.00,
    "totalTaxLiability": 18450.00,
    "generatedAt": "2026-03-31T22:30:00"
  },
  "timestamp": "2026-03-31T22:30:00",
  "statusCode": 200
}
```

### 4) Tax Summary

Response:

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "storeId": 301,
    "fromDate": "2026-03-01",
    "toDate": "2026-03-31",
    "invoiceCount": 24,
    "taxableAmount": 125000.00,
    "cgstAmount": 7025.00,
    "sgstAmount": 7025.00,
    "igstAmount": 4100.00,
    "cessAmount": 300.00,
    "totalTaxAmount": 18450.00,
    "totalInvoiceAmount": 143450.00,
    "taxRateBreakup": [
      {
        "gstRate": 5.00,
        "taxableAmount": 30000.00,
        "totalTaxAmount": 1500.00
      },
      {
        "gstRate": 18.00,
        "taxableAmount": 95000.00,
        "totalTaxAmount": 16950.00
      }
    ]
  },
  "timestamp": "2026-03-31T23:00:00",
  "statusCode": 200
}
```

## Service Status (April 2026)

### What Has Been Updated
- Implemented HSN master CRUD, GST invoice generation, return preparation, and tax summary endpoints.
- Implemented edge-case coverage for inter-state IGST, cess-heavy lines, and multi-rate invoice summaries.
- Added service-level payload examples and module test coverage.

### What Remains
- Hardening complex statutory edge handling and export-format consistency.
- Add production filing adapters for GST portal upload, signed artifacts, and filing-status reconciliation.
- Add enhanced compliance audit traceability for enterprise reporting.

## Run Tests

```powershell
Set-Location "C:\Users\mayas\Downloads\demoSpring\dukaanpe-platform"
mvn -pl dukaanpe-gst-tax-service test
```
