# 🚘 Driver Management Component

## Overview

The **Driver Management** component handles the creation, availability checking, and assignment of drivers to bookings. Drivers are users with the `DRIVER` role who can be optionally attached to bookings that request chauffeur service. The system prevents double-booking of drivers by checking for schedule overlaps.

---

## Technologies & Libraries

| Technology | Purpose |
|---|---|
| **Spring Boot 4.1** | Application framework |
| **Spring Data JPA** | ORM / database interaction |
| **Spring Security** | `@PreAuthorize` role-based endpoint protection |
| **MS SQL Server** | Relational database |
| **Lombok** | Boilerplate code reduction |
| **MapStruct** | Reuses `BookingDetailsMapper` from Booking module |

---

## Architecture & Design Patterns

| Pattern | Usage |
|---|---|
| **Layered Architecture** | Controller → Service (Interface + Impl) → Repository |
| **DTO Pattern** | `UserSummaryDTO`, `AssignDriverRequest` |
| **Inheritance** | `Driver` extends `User` (adds `licenseNumber`, `isAvailable`) |
| **Cross-Module Integration** | Reuses Booking and User repositories/mappers |
| **Overlap Detection** | Custom JPQL query to find overlapping driver assignments |

---

## Data Model

### `Driver` (extends `User`)

| Field | Type | Description |
|---|---|---|
| *(all User fields)* | — | Inherited from `User` entity |
| `licenseNumber` | `String` | Driver's license number |
| `isAvailable` | `boolean` | Current availability flag |

> **Note:** Drivers are stored in the same `_user` table with `role = DRIVER`. The `Driver` class is a POJO extension, not a separate JPA entity.

### `UserSummaryDTO`

| Field | Type | Description |
|---|---|---|
| `id` | `Long` | User ID |
| `firstName` | `String` | First name |
| `lastName` | `String` | Last name |
| `email` | `String` | Email address |
| `role` | `String` | User role name |

### `AssignDriverRequest`

| Field | Type | Description |
|---|---|---|
| `driverId` | `Long` | ID of the driver to assign |

---

## API Endpoints

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/manager/drivers` | Create a new driver account | ✅ MANAGER |
| `GET` | `/api/booking/available-drivers?pickupDateTime=...&returnDateTime=...` | List drivers available for given dates | ✅ STAFF |
| `POST` | `/api/booking/{id}/assign-driver` | Assign a driver to a booking | ✅ STAFF |

---

## Business Logic

### Driver Creation

```
1. Validate email uniqueness
2. Create User entity with role = DRIVER
3. Password is BCrypt-hashed
4. verificationStatus = VERIFIED (auto-verified)
5. Return UserSummaryDTO
```

### Available Drivers Query

```
1. Find all users with role = DRIVER
2. For each driver, check for overlapping bookings:
   - Query: findOverlappingDriverBookings(driverId, excludeBookingId, pickup, return, excludeStatus)
   - Excludes cancelled bookings from overlap check
3. Return only drivers with no scheduling conflicts
```

### Driver Assignment

```
1. Validate booking exists
2. Validate driver exists and has DRIVER role
3. Validate booking requires a driver (withDriver = true)
4. Validate booking is not CANCELLED or COMPLETED
5. Check no overlapping assignments for the driver's schedule
6. Set booking.driver = driver
7. Save and return updated BookingDetailsDTO
```

---

## Operational Workflow

```
┌──────────────┐     ┌────────────────────┐
│   MANAGER    │────▶│ Create Driver      │
│              │     │ POST /manager/     │
│              │     │ drivers            │
└──────────────┘     └────────────────────┘

┌──────────────┐     ┌────────────────────┐     ┌───────────────────┐
│   STAFF      │────▶│ Check Available    │────▶│ List of available │
│              │     │ GET /available-    │     │ UserSummaryDTOs   │
│              │     │ drivers            │     │                   │
└──────────────┘     └────────────────────┘     └───────────────────┘
                              │
                              ▼
                     ┌────────────────────┐
                     │ Assign Driver      │
                     │ POST /{id}/       │
                     │ assign-driver      │
                     │                    │
                     │ Validates:         │
                     │ • Is a driver      │
                     │ • Booking needs    │
                     │   driver           │
                     │ • No schedule      │
                     │   conflicts        │
                     └────────────────────┘
```

---

## File Structure

```
Driver_Management/
├── Controller/
│   └── DriverController.java           # REST endpoints for driver operations
├── Model/
│   ├── AssignDriverRequest.java         # Request DTO for driver assignment
│   ├── Driver.java                      # Driver POJO (extends User)
│   └── UserSummaryDTO.java             # Response DTO for driver details
├── Repository/
│   └── DriverBookingRepository.java     # Custom JPQL for overlap detection
└── Service/
    ├── DriverService.java               # Service interface
    └── DriverServiceImpl.java           # Service implementation
```
