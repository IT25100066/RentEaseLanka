# 🤝 Handover Management Component

## Overview

The **Handover Management** component manages the physical vehicle handover process when a rental begins. It tracks the condition of the vehicle at the time of handover including starting mileage, damage inspection, fuel level, and payment verification. A completed handover is a **prerequisite** for activating a booking and marking a vehicle as RENTED.

---

## Technologies & Libraries

| Technology | Purpose |
|---|---|
| **Spring Boot 4.1** | Application framework |
| **Spring Data JPA** | ORM / database interaction |
| **Spring Security** | `@PreAuthorize` role-based endpoint protection |
| **MS SQL Server** | Relational database |
| **Lombok** | Boilerplate code reduction |
| **Jakarta Validation** | `@Valid`, `@PositiveOrZero` constraints |

---

## Architecture & Design Patterns

| Pattern | Usage |
|---|---|
| **Layered Architecture** | Controller → Service (Interface + Impl) → Repository |
| **DTO Pattern** | Separate request/response DTOs for each operation |
| **Builder Pattern** | `HandoverRecord.builder()` for entity construction |
| **Status Enum** | `HandoverStatus` for lifecycle management |
| **Decoupled References** | Uses `bookingId`, `customerId` as foreign keys (not JPA relations) for loose coupling |

---

## Data Model

### `HandoverRecord` Entity (table: `handover_records`)

| Field | Type | Description |
|---|---|---|
| `id` | `Long` (PK, auto) | Unique identifier |
| `vehicle` | `Vehicle` (FK, ManyToOne) | Vehicle being handed over |
| `bookingId` | `Long` | Reference to booking (decoupled) |
| `customerId` | `Long` | Reference to customer (decoupled) |
| `customerName` | `String` | Customer's display name |
| `staffId` | `String` | Staff member performing handover |
| `status` | `HandoverStatus` (enum) | Current handover state |
| `startingMileage` | `Double` | Vehicle odometer at handover |
| `hasDamage` | `boolean` | Whether pre-existing damage found |
| `paymentVerified` | `boolean` | Whether customer payment verified |
| `damageDescription` | `String` | Details of any damage found |
| `fuelLevel` | `String` | Fuel level at handover |
| `notes` | `String` | Additional notes |
| `handoverDateTime` | `LocalDateTime` | Handover timestamp |
| `completionDateTime` | `LocalDateTime` | When handover was completed |
| `createdAt` | `LocalDateTime` | Record creation timestamp |
| `updatedAt` | `LocalDateTime` | Last update timestamp |

### `HandoverStatus` Enum

```
PENDING ──▶ IN_PROGRESS ──▶ COMPLETED
   │             │
   └─── CANCELLED ◄──┘
```

| Status | Description |
|---|---|
| `PENDING` | Handover initiated, awaiting inspection |
| `IN_PROGRESS` | Mileage/damage recording underway |
| `COMPLETED` | All checks done, vehicle handed to customer |
| `CANCELLED` | Handover cancelled |

---

## API Endpoints

**Base Path:** `/api/handovers`  
**Class-Level Auth:** `@PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('MANAGER')")`

### CRUD Operations

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/handovers` | Initiate a new vehicle handover |
| `GET` | `/api/handovers?status=PENDING` | Get all handovers (optional status filter) |
| `GET` | `/api/handovers/{id}` | Get handover by ID |
| `GET` | `/api/handovers/vehicle/{vehicleId}` | Get handovers for a specific vehicle |
| `PUT` | `/api/handovers/{id}` | Update editable handover fields |
| `DELETE` | `/api/handovers/{id}` | Delete handover (only if PENDING/IN_PROGRESS/CANCELLED) |

### Business Operations

| Method | Endpoint | Description |
|---|---|---|
| `PUT` | `/api/handovers/{id}/record-mileage` | Record the vehicle's starting mileage |
| `PUT` | `/api/handovers/{id}/record-damage` | Record damage or confirm no damage |
| `PUT` | `/api/handovers/{id}/complete` | Complete the handover process |
| `PUT` | `/api/handovers/{id}/cancel` | Cancel the handover |

---

## Request/Response DTOs

### `HandoverCreateRequestDTO`

```json
{
  "vehicleId": 1,
  "bookingId": 10,
  "customerId": 5,
  "customerName": "John Doe",
  "staffId": "STAFF001"
}
```

### `MileageRecordDTO`

```json
{
  "startingMileage": 45230.5
}
```

### `DamageInspectionDTO`

```json
{
  "hasDamage": true,
  "damageDescription": "Scratch on rear bumper"
}
```

### `HandoverResponseDTO`

```json
{
  "id": 1,
  "vehicleId": 1,
  "bookingId": 10,
  "customerId": 5,
  "customerName": "John Doe",
  "staffId": "STAFF001",
  "status": "COMPLETED",
  "startingMileage": 45230.5,
  "hasDamage": false,
  "paymentVerified": true,
  "damageDescription": null,
  "fuelLevel": "FULL",
  "notes": "Vehicle in good condition",
  "handoverDateTime": "2026-09-20T10:00:00",
  "completionDateTime": "2026-09-20T10:30:00"
}
```

---

## Operational Workflow

### Handover Process (Step-by-Step)

```
┌──────────────────────────────────────────────────────────────────────┐
│                      HANDOVER WORKFLOW                               │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  1. STAFF initiates handover                                         │
│     POST /api/handovers                                              │
│     └─▶ Status: PENDING                                              │
│     └─▶ Links to booking, vehicle, customer                         │
│                                                                      │
│  2. STAFF records starting mileage                                   │
│     PUT /api/handovers/{id}/record-mileage                           │
│     └─▶ Status: IN_PROGRESS                                         │
│     └─▶ Stores odometer reading                                     │
│                                                                      │
│  3. STAFF records damage inspection                                  │
│     PUT /api/handovers/{id}/record-damage                            │
│     └─▶ hasDamage: true/false                                       │
│     └─▶ damageDescription if damage found                           │
│                                                                      │
│  4. STAFF completes handover                                         │
│     PUT /api/handovers/{id}/complete                                 │
│     └─▶ Status: COMPLETED                                           │
│     └─▶ Vehicle status: RENTED                                      │
│     └─▶ completionDateTime recorded                                 │
│                                                                      │
│  ❌ Cancel at any non-complete stage                                  │
│     PUT /api/handovers/{id}/cancel                                   │
│     └─▶ Status: CANCELLED                                           │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

### Integration with Booking

```
Booking.makeActive() requires:
  └─▶ handoverRepository.findByBookingId(bookingId)
      └─▶ At least one HandoverRecord with status == COMPLETED

Without a completed handover, booking activation is BLOCKED with:
"Cannot rent vehicle or activate Booking: Vehicle handover has not been completed."
```

---

## File Structure

```
Handover_Management/
├── Controller/
│   └── HandoverController.java         # REST endpoints for handover operations
├── Model/
│   ├── HandoverRecord.java             # JPA entity for handover records
│   ├── HandoverStatus.java             # Status enum
│   └── DTO/
│       ├── HandoverCreateRequestDTO.java  # Create handover request
│       ├── HandoverUpdateRequestDTO.java  # Update handover request
│       ├── HandoverResponseDTO.java       # Handover response DTO
│       ├── MileageRecordDTO.java          # Mileage recording request
│       └── DamageInspectionDTO.java       # Damage inspection request
├── Repository/
│   └── HandoverRepository.java         # JPA repository with findByBookingId
└── Service/
    ├── HandoverService.java            # Service interface
    └── HandoverServiceImpl.java        # Service implementation
```
