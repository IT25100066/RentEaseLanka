# 📊 Financial Management Component

## Overview

The **Financial Management** component provides income reporting and financial analytics for RentEaseLanka. It aggregates revenue from completed bookings and deducts maintenance costs to calculate net revenue for a given date range. This is a read-only reporting module used by Admins and Managers.

---

## Technologies & Libraries

| Technology | Purpose |
|---|---|
| **Spring Boot 4.1** | Application framework |
| **Spring Data JPA** | Custom JPQL aggregate queries |
| **Spring Security** | `@PreAuthorize` role-based access |
| **MS SQL Server** | Relational database |
| **Java BigDecimal** | Precise monetary calculations |

---

## Architecture & Design Patterns

| Pattern | Usage |
|---|---|
| **Layered Architecture** | Controller → Service (Interface + Impl) |
| **DTO Pattern** | `FinancialSummaryDTO` for report output |
| **Cross-Module Aggregation** | Queries BookingRepository + MaintenanceRepository |
| **Date-Range Filtering** | `from` and `to` parameters with `@DateTimeFormat` |

---

## Data Model

### `FinancialSummaryDTO` (Response)

| Field | Type | Description |
|---|---|---|
| `from` | `LocalDate` | Report start date |
| `to` | `LocalDate` | Report end date |
| `status` | `BookingStatus` | Booking status filter (default: COMPLETED) |
| `bookingCount` | `long` | Number of matching bookings |
| `grossRevenue` | `BigDecimal` | Total booking revenue |
| `maintenanceCost` | `BigDecimal` | Total maintenance expenses |
| `netRevenue` | `BigDecimal` | Gross revenue - maintenance cost |

---

## API Endpoints

**Base Path:** `/api/financial`  
**Class-Level Auth:** `@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")`

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/financial/income?from=YYYY-MM-DD&to=YYYY-MM-DD&status=COMPLETED` | Get income summary for date range | ✅ ADMIN / MANAGER |

### Query Parameters

| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `from` | `LocalDate` | ✅ | — | Report start date |
| `to` | `LocalDate` | ✅ | — | Report end date (inclusive) |
| `status` | `BookingStatus` | ❌ | `COMPLETED` | Booking status to filter by |

### Example Request

```
GET /api/financial/income?from=2026-09-01&to=2026-09-30
```

### Example Response

```json
{
  "from": "2026-09-01",
  "to": "2026-09-30",
  "status": "COMPLETED",
  "bookingCount": 45,
  "grossRevenue": 1350000.00,
  "maintenanceCost": 120000.00,
  "netRevenue": 1230000.00
}
```

---

## Business Logic

### Revenue Calculation

```
1. Validate: from and to dates are required, from ≤ to
2. Resolve status: default to COMPLETED if not specified
3. Convert dates to LocalDateTime range: [from 00:00, to+1 00:00)
4. Query BookingRepository:
   - countRevenueBookings(status, periodStart, periodEnd)
   - sumRevenueBookings(status, periodStart, periodEnd)
   - Uses COALESCE(completedAt, returnDateTime) as the date reference
5. Query MaintenanceRepository:
   - getCompletedCost(periodStart, periodEnd)
   - Sums cost of COMPLETED maintenance records in the period
6. netRevenue = grossRevenue - maintenanceCost
```

### Custom JPQL Queries

**Booking Revenue Count:**
```sql
SELECT COUNT(b) FROM Booking b
WHERE b.status = :status
  AND COALESCE(b.completedAt, b.returnDateTime) >= :from
  AND COALESCE(b.completedAt, b.returnDateTime) < :to
```

**Booking Revenue Sum:**
```sql
SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b
WHERE b.status = :status
  AND COALESCE(b.completedAt, b.returnDateTime) >= :from
  AND COALESCE(b.completedAt, b.returnDateTime) < :to
```

---

## File Structure

```
Financial_Management/
├── Controller/
│   └── FinancialController.java         # GET /income endpoint
├── Model/
│   └── DTO/
│       └── FinancialSummaryDTO.java     # Response DTO
└── Service/
    ├── FinancialService.java            # Service interface
    └── FinancialServiceImpl.java        # Service implementation
```
