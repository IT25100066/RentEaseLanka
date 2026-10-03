# 📈 Admin Dashboard & Reporting Component

## Overview

The **Admin Dashboard & Reporting** component provides a centralized management dashboard with fleet inventory summaries, vehicle performance metrics, critical system notifications, and maintenance reporting. It aggregates data from the Fleet, Maintenance, and potentially Booking/Payment modules to give administrators a comprehensive view of the business.

---

## Technologies & Libraries

| Technology | Purpose |
|---|---|
| **Spring Boot 4.1** | Application framework |
| **Spring Data JPA** | Aggregate queries across modules |
| **MS SQL Server** | Relational database |
| **Lombok** | `@Data`, `@Builder` for DTOs |

---

## Architecture & Design Patterns

| Pattern | Usage |
|---|---|
| **Layered Architecture** | Controller → Service (Interface + Impl) |
| **DTO Pattern** | 4 specialized DTOs for different dashboard views |
| **Aggregation Service** | Collects and computes data from multiple repositories |
| **Read-Only Module** | No write operations — purely reporting |
| **Date-Range Filtering** | Optional date filtering for maintenance reports |

---

## Data Models (Response DTOs)

### `DashboardSummaryDTO`

| Field | Type | Description |
|---|---|---|
| `totalVehicles` | `long` | Total vehicles in fleet |
| `availableVehicles` | `long` | Vehicles with AVAILABLE status |
| `rentedVehicles` | `long` | Vehicles currently rented |
| `underMaintenanceVehicles` | `long` | Vehicles under maintenance |
| `outOfServiceVehicles` | `long` | Out of service vehicles |
| `fleetUtilizationRate` | `double` | % of fleet currently rented |
| `totalMaintenanceRecords` | `long` | Total maintenance records |
| `scheduledMaintenance` | `long` | SCHEDULED maintenance count |
| `inProgressMaintenance` | `long` | IN_PROGRESS maintenance count |
| `completedMaintenance` | `long` | COMPLETED maintenance count |
| `cancelledMaintenance` | `long` | CANCELLED maintenance count |
| `totalMaintenanceCost` | `BigDecimal` | Sum of all maintenance costs |
| `bookingIntegrationNotice` | `String` | Booking integration status |
| `revenueIntegrationNotice` | `String` | Revenue integration status |

### `VehiclePerformanceDTO`

| Field | Type | Description |
|---|---|---|
| `vehicleId` | `Long` | Vehicle ID |
| `licensePlate` | `String` | Registration number |
| `model` | `String` | Vehicle model |
| `year` | `int` | Manufacture year |
| `currentOdometer` | `double` | Current mileage |
| `status` | `String` | Current vehicle status |
| `dailyRentalRate` | `BigDecimal` | Daily rate |
| `categoryName` | `String` | Category name |
| `totalMaintenanceRecords` | `long` | Maintenance count for this vehicle |
| `completedMaintenanceCount` | `long` | Completed maintenance count |
| `totalMaintenanceCost` | `BigDecimal` | Total maintenance cost |

### `NotificationDTO`

| Field | Type | Description |
|---|---|---|
| `type` | `String` | Notification type (WARNING, INFO, etc.) |
| `title` | `String` | Notification title |
| `message` | `String` | Notification message |
| `timestamp` | `LocalDateTime` | When notification was generated |

### `MaintenanceReportDTO`

| Field | Type | Description |
|---|---|---|
| `startDate` | `LocalDate` | Report start date |
| `endDate` | `LocalDate` | Report end date |
| `totalRecords` | `long` | Total maintenance records |
| `scheduledCount` | `long` | Scheduled count |
| `inProgressCount` | `long` | In-progress count |
| `completedCount` | `long` | Completed count |
| `cancelledCount` | `long` | Cancelled count |
| `totalCost` | `BigDecimal` | Total maintenance cost |
| `records` | `List<MaintenanceRecordSummary>` | Individual records |

---

## API Endpoints

**Base Path:** `/api/admin`

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/admin/dashboard/summary` | Fleet & maintenance summary overview | ✅ ADMIN |
| `GET` | `/api/admin/dashboard/vehicles` | All vehicles with performance metrics | ✅ ADMIN |
| `GET` | `/api/admin/dashboard/vehicles/{id}` | Single vehicle performance details | ✅ ADMIN |
| `GET` | `/api/admin/dashboard/notifications` | Critical system notifications | ✅ ADMIN |
| `GET` | `/api/admin/reports/maintenance?startDate=...&endDate=...` | Maintenance report (date-filtered) | ✅ ADMIN |

---

## Business Logic

### Dashboard Summary

```
1. Count vehicles by status (AVAILABLE, RENTED, UNDER_MAINTENANCE, OUT_OF_SERVICE)
2. Calculate fleet utilization: (rented / total) × 100
3. Count maintenance records by status
4. Sum total maintenance cost
```

### Critical Notifications

```
Generated dynamically based on current system state:
- Vehicles out of service for extended periods
- Overdue maintenance records
- Fleet utilization above/below thresholds
- Integration status messages
```

### Maintenance Report

```
1. Accept optional startDate and endDate parameters
2. If dates provided: filter maintenance records within range
3. Aggregate counts by status (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)
4. Sum maintenance costs for the period
5. Return detailed MaintenanceReportDTO
```

---

## File Structure

```
AdminDashboard_Reporting/
├── Controller/
│   └── AdminDashboardController.java       # Dashboard & report endpoints
├── DTO/
│   ├── DashboardSummaryDTO.java            # Fleet overview response
│   ├── VehiclePerformanceDTO.java          # Vehicle metrics response
│   ├── NotificationDTO.java               # Notification alert response
│   └── MaintenanceReportDTO.java           # Maintenance report response
└── Service/
    ├── AdminDashboardService.java          # Service interface
    └── AdminDashboardServiceImpl.java      # Service implementation (15K+ lines)
```
