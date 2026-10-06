# 🔙 Return Management Component

## Overview

The **Return Management** component handles the vehicle return and post-rental inspection process. When a customer returns a rented vehicle, staff performs an inspection recording the ending mileage, damage assessment, and optional notes. This component integrates with Payment Management to settle security deposits and with Booking Management to auto-complete bookings when all conditions are met.

---

## Technologies & Libraries

| Technology | Purpose |
|---|---|
| **Spring Boot 4.1** | Application framework |
| **Spring Data JPA** | ORM / database interaction |
| **Spring Security** | `@PreAuthorize` role-based endpoint protection |
| **MS SQL Server** | Relational database |
| **Lombok** | Boilerplate code reduction |
| **Jakarta Validation** | `@Valid` constraints on request DTOs |

---

## Architecture & Design Patterns

| Pattern | Usage |
|---|---|
| **Layered Architecture** | Controller → Service → Repository |
| **Transactional Orchestration** | Single `@Transactional` method handles inspection + deposit settlement + booking completion |
| **Immutable Record** | `VehicleReturnInspection` is a one-time immutable inspection record |
| **Cross-Module Integration** | Coordinates with Payment, Booking, and Fleet modules |
| **Guard Clause Pattern** | Extensive validation before processing |

---

## Data Model

### `VehicleReturnInspection` Entity (table: `vehicle_return_inspections`)

| Field | Type | Description |
|---|---|---|
| `id` | `Long` (PK, auto) | Unique identifier |
| `bookingId` | `Long` (unique) | One inspection per booking |
| `vehicleId` | `Long` | Inspected vehicle ID |
| `staffId` | `String` | Staff performing inspection |
| `endingMileage` | `Double` | Odometer reading at return |
| `hasDamage` | `boolean` | Whether damage was found |
| `damageDescription` | `String` | Description of damage (if any) |
| `notes` | `String` | Additional notes |
| `inspectedAt` | `LocalDateTime` | Inspection timestamp |

---

## API Endpoints

**Base Path:** `/api/returns`

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/returns/booking/{bookingId}/inspect` | Perform return inspection | ✅ STAFF / MANAGER / ADMIN |
| `GET` | `/api/returns/booking/{bookingId}` | Get return inspection for a booking | ✅ STAFF / MANAGER / ADMIN |

---

## Request/Response DTOs

### `ReturnInspectionRequest`

```json
{
  "staffId": "STAFF001",
  "endingMileage": 48500.0,
  "hasDamage": true,
  "damageDescription": "Dent on left door panel",
  "damageAmount": 15000.00,
  "notes": "Customer acknowledged damage"
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `staffId` | `String` | ✅ | Staff performing inspection |
| `endingMileage` | `double` | ✅ | Final odometer reading |
| `hasDamage` | `boolean` | ✅ | Whether damage was found |
| `damageDescription` | `String` | If hasDamage=true | Damage details |
| `damageAmount` | `BigDecimal` | If hasDamage=true | Monetary damage value |
| `notes` | `String` | ❌ | Optional notes |

### `ReturnInspectionResponse`

```json
{
  "id": 1,
  "bookingId": 10,
  "vehicleId": 5,
  "staffId": "STAFF001",
  "endingMileage": 48500.0,
  "hasDamage": true,
  "damageDescription": "Dent on left door panel",
  "notes": "Customer acknowledged damage",
  "inspectedAt": "2026-09-23T14:30:00"
}
```

---

## Business Logic

### Validation Rules

```
1. Only ONE inspection per booking (duplicate rejected)
2. Booking must be ACTIVE or CONFIRMED
3. If CONFIRMED → auto-transitions to ACTIVE first
4. Booking must have an assigned vehicle
5. Ending mileage ≥ current vehicle odometer
6. If hasDamage=true → damageDescription is REQUIRED
7. If hasDamage=true → damageAmount must be positive
```

### Return & Inspection Flow

```
inspectAndCompleteReturn(bookingId, request):

1. VALIDATE: No existing inspection for this booking
2. VALIDATE: Booking exists and is ACTIVE/CONFIRMED
3. If CONFIRMED → auto-transition to ACTIVE
4. VALIDATE: Vehicle exists, ending mileage ≥ current odometer
5. VALIDATE: Damage description required if hasDamage=true
6. VALIDATE: Damage amount required and positive if hasDamage=true

7. SAVE inspection record

8. SETTLE SECURITY DEPOSIT:
   └─▶ paymentService.settleSecurityDepositForReturn(
         bookingId, hasDamage, damageAmount, damageDescription)
   ├─▶ No damage → full deposit released
   └─▶ Damage → charge from deposit, release remainder

9. CHECK INVOICE STATUS:
   └─▶ If invoice is PAID:
       └─▶ bookingService.makeCompleted(bookingId)
           └─▶ Vehicle: AVAILABLE
           └─▶ Booking: COMPLETED
   └─▶ If invoice NOT PAID:
       └─▶ Log: "Awaiting final rental payment"
       └─▶ Booking stays ACTIVE until payment received
```

---

## Operational Workflow

```
┌───────────────────────────────────────────────────────────────────────┐
│                   VEHICLE RETURN WORKFLOW                             │
├───────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  Customer returns vehicle to rental location                         │
│                                                                       │
│  ┌─────────────┐                                                     │
│  │   STAFF     │                                                     │
│  │  inspects   │                                                     │
│  │  vehicle    │                                                     │
│  └──────┬──────┘                                                     │
│         │                                                             │
│         ▼                                                             │
│  POST /api/returns/booking/{id}/inspect                              │
│  ┌──────────────────────────────────────────┐                        │
│  │ Record:                                   │                        │
│  │  • Ending mileage (odometer)              │                        │
│  │  • Damage found? (yes/no)                 │                        │
│  │  • Damage description & amount            │                        │
│  │  • Staff ID & notes                       │                        │
│  └──────────────┬───────────────────────────┘                        │
│                 │                                                     │
│         ┌───────┴───────┐                                            │
│         ▼               ▼                                            │
│    No Damage       Damage Found                                      │
│    ┌────────┐     ┌───────────────┐                                  │
│    │Release │     │Charge damage  │                                  │
│    │full    │     │from deposit,  │                                  │
│    │deposit │     │release rest   │                                  │
│    └───┬────┘     └──────┬────────┘                                  │
│        │                 │                                            │
│        └────────┬────────┘                                           │
│                 │                                                     │
│                 ▼                                                     │
│         Invoice PAID?                                                │
│         ┌───────┴───────┐                                            │
│         ▼               ▼                                            │
│       YES              NO                                            │
│    ┌────────┐     ┌──────────────┐                                   │
│    │Complete│     │Await payment │                                   │
│    │booking │     │before        │                                   │
│    │Vehicle │     │completing    │                                   │
│    │→ AVAIL │     └──────────────┘                                   │
│    └────────┘                                                        │
│                                                                       │
└───────────────────────────────────────────────────────────────────────┘
```

---

## Cross-Module Dependencies

| Module | Dependency |
|---|---|
| **Booking Management** | `BookingRepository`, `BookingService` — validate status, auto-complete booking |
| **Fleet Management** | `Vehicle` entity — validate odometer, update status |
| **Payment Management** | `PaymentService` — settle security deposit, check invoice status |

---

## File Structure

```
Return_Management/
├── Controller/
│   └── VehicleReturnController.java        # REST endpoints for return inspection
├── DTO/
│   ├── ReturnInspectionRequest.java        # Inspection input DTO
│   └── ReturnInspectionResponse.java       # Inspection output DTO
├── Model/
│   └── VehicleReturnInspection.java        # JPA entity (immutable record)
├── Repository/
│   └── VehicleReturnInspectionRepository.java  # JPA repo with findByBookingId
└── Service/
    └── VehicleReturnService.java           # Orchestration service
```
