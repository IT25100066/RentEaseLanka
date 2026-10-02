# 📋 Booking Management Component

## Overview

The **Booking Management** component is the core business logic module of RentEaseLanka. It manages the entire vehicle rental booking lifecycle — from creation through confirmation, activation, and completion. It enforces a **strict state machine** for booking transitions and integrates with Handover, Payment, and Return Management to ensure business rules are satisfied before state changes.

---

## Technologies & Libraries

| Technology | Purpose |
|---|---|
| **Spring Boot 4.1** | Application framework |
| **Spring Data JPA** | ORM / database interaction |
| **Spring Security** | `@PreAuthorize` role-based endpoint protection |
| **MS SQL Server** | Relational database |
| **MapStruct 1.5.5** | Entity-to-DTO mapping at compile time |
| **Lombok** | Boilerplate code reduction |
| **Java BigDecimal** | Precise monetary calculations |

---

## Architecture & Design Patterns

| Pattern | Usage |
|---|---|
| **Layered Architecture** | Controller → Service (Interface + Impl) → Repository |
| **State Machine Pattern** | `BookingStatus` enum with `transitionTo()` enforcing valid state transitions |
| **DTO Pattern** | `BookingRequestDTO` (input), `BookingDetailsDTO` (output) |
| **Mapper Pattern** | `BookingDetailsMapper` (MapStruct) for entity↔DTO conversion |
| **Service Interface Pattern** | `BookingService` interface with `BookingServiceImpl` implementation |
| **Template Method** | Each `make*()` method in `Booking` entity delegates to the state machine |
| **Cross-Module Validation** | Booking completion checks Payment, Handover, and Return inspection |

---

## Data Model

### `Booking` Entity (table: `booking`)

| Field | Type | Description |
|---|---|---|
| `BookingId` | `Long` (PK, auto) | Unique identifier |
| `user` | `User` (FK, ManyToOne) | Customer who made the booking |
| `vehicle` | `Vehicle` (FK, ManyToOne) | Rented vehicle |
| `pickupDateTime` | `LocalDateTime` | Scheduled pickup time |
| `returnDateTime` | `LocalDateTime` | Scheduled return time |
| `pickupLocation` | `String` | Pickup address |
| `returnLocation` | `String` | Return address |
| `totalAmount` | `BigDecimal` | Calculated total cost |
| `status` | `BookingStatus` (enum) | Current booking state |
| `withDriver` | `boolean` | Whether a driver is requested |
| `createdAt` | `LocalDateTime` | Record creation timestamp |
| `updateAt` | `LocalDateTime` | Last update timestamp |
| `completedAt` | `LocalDateTime` | Completion timestamp |
| `driver` | `User` (FK, ManyToOne) | Assigned driver (nullable) |

### `BookingStatus` Enum — State Machine

```
                    ┌──────────────┐
                    │   PENDING    │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              ▼                         ▼
      ┌──────────────┐          ┌──────────────┐
      │  CONFIRMED   │          │  CANCELLED   │
      └──────┬───────┘          │  (terminal)  │
             │                  └──────────────┘
    ┌────────┼────────┐
    ▼        │        ▼
┌────────┐   │   ┌──────────────┐
│ ACTIVE │   │   │  CANCELLED   │
└───┬────┘   │   └──────────────┘
    │        ▼
    │   ┌──────────────┐
    ├──▶│  COMPLETED   │
    │   │  (terminal)  │
    │   └──────────────┘
    │
    └──▶ CANCELLED
```

**Valid Transitions:**
| From | To |
|---|---|
| `PENDING` | `CONFIRMED`, `CANCELLED` |
| `CONFIRMED` | `ACTIVE`, `COMPLETED`, `CANCELLED` |
| `ACTIVE` | `COMPLETED`, `CANCELLED` |
| `COMPLETED` | *(terminal — no transitions)* |
| `CANCELLED` | *(terminal — no transitions)* |

---

## API Endpoints

**Base Path:** `/api/booking`

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/booking/create` | Create a new booking | ✅ CUSTOMER (verified only) |
| `POST` | `/api/booking/calculate-price` | Calculate total price for a booking | ❌ Public |
| `POST` | `/api/booking/{id}/confirm` | Confirm a pending booking | ✅ STAFF / MANAGER / ADMIN |
| `POST` | `/api/booking/{id}/activate` | Activate a confirmed booking | ✅ STAFF / MANAGER / ADMIN |
| `POST` | `/api/booking/{id}/complete` | Complete an active booking | ✅ STAFF / MANAGER / ADMIN |
| `POST` | `/api/booking/{id}/cancel` | Cancel a booking | ✅ CUSTOMER / STAFF / MANAGER / ADMIN |
| `GET` | `/api/booking/all` | Get all bookings | ✅ STAFF / MANAGER / ADMIN |
| `GET` | `/api/booking/{id}` | Get booking details by ID | ✅ ADMIN / MANAGER |
| `GET` | `/api/booking/customer` | Get current customer's bookings | ✅ CUSTOMER |
| `GET` | `/api/booking/customer/{customerId}` | Get bookings for a specific customer | ✅ STAFF |

---

## Business Logic & Pricing

### Price Calculation

```
Total Price = dailyRentalRate × numberOfDays
            + (if withDriver: 3000 LKR/day × numberOfDays)

- Minimum 1 day rental
- Days = abs(ChronoUnit.DAYS.between(pickup, return))
- Driver fee is a flat 3,000 LKR per day
```

### Booking Creation Flow

```
1. Validate user is VERIFIED (otherwise 403)
2. Validate dates (pickup must be before return, not in past)
3. Check vehicle availability:
   a. Vehicle not OUT_OF_SERVICE or UNDER_MAINTENANCE
   b. No overlapping non-cancelled bookings for same vehicle
4. Calculate total price (with/without driver fee)
5. Reserve the vehicle (VehicleStatus → RESERVED)
6. Save booking (status = PENDING)
7. Auto-generate invoice via PaymentService
8. Return BookingDetailsDTO
```

### Booking Activation Prerequisites

```
- Booking must NOT be CANCELLED or COMPLETED
- ✅ Handover must be COMPLETED (vehicle inspection done)
- Vehicle status is changed to RENTED by the Handover workflow
```

### Booking Completion Prerequisites (All must be met)

```
- Booking must be in ACTIVE state
- ✅ Invoice must be PAID (full rental amount)
- ✅ Return inspection must be recorded
- ✅ Security deposit must be released or used (no HELD deposits)
- Vehicle status transitions back to AVAILABLE
```

---

## Cross-Module Dependencies

| Module | Dependency |
|---|---|
| **Fleet Management** | `VehicleRepository` — check availability, reserve/return vehicle |
| **Handover Management** | `HandoverRepository` — verify handover completion before activation |
| **Payment Management** | `PaymentService` — auto-create invoice; verify payment/deposit for completion |
| **Return Management** | `VehicleReturnInspectionRepository` — verify return inspection exists |
| **User Management** | `UserRepository` — verify user exists and is verified |

---

## File Structure

```
Booking_Management/
├── Controller/
│   └── BookingController.java        # REST endpoints for all booking operations
├── Model/
│   ├── Booking.java                  # JPA entity with state machine methods
│   ├── BookingStatus.java            # Enum with valid transition rules
│   ├── DTOs/
│   │   ├── BookingDetailsDTO.java    # Output DTO with customer/vehicle/driver info
│   │   └── BookingRequestDTO.java    # Input DTO for booking creation
│   └── Mapper/
│       └── BookingDetailsMapper.java # MapStruct mapper: Booking → BookingDetailsDTO
├── Repository/
│   └── BookingRepository.java        # JPA repo with custom revenue queries
└── Service/
    ├── BookingService.java           # Service interface
    └── BookingServiceImpl.java       # Full implementation with business logic
```

---

## Complete Booking Workflow (End-to-End)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        COMPLETE BOOKING LIFECYCLE                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. CUSTOMER creates booking ──▶ Status: PENDING                        │
│     └─▶ Vehicle: RESERVED                                               │
│     └─▶ Invoice: auto-generated (UNPAID)                                │
│                                                                         │
│  2. STAFF confirms booking ──▶ Status: CONFIRMED                        │
│                                                                         │
│  3. STAFF initiates handover                                            │
│     └─▶ Records starting mileage                                       │
│     └─▶ Records damage inspection                                       │
│     └─▶ Completes handover ──▶ Vehicle: RENTED                          │
│                                                                         │
│  4. STAFF activates booking ──▶ Status: ACTIVE                          │
│     └─▶ Requires: Handover COMPLETED ✅                                  │
│                                                                         │
│  5. Customer pays rental invoice ──▶ Invoice: PAID                      │
│                                                                         │
│  6. STAFF performs return inspection                                     │
│     └─▶ Records ending mileage + damage                                 │
│     └─▶ Settles security deposit                                        │
│                                                                         │
│  7. STAFF completes booking ──▶ Status: COMPLETED                       │
│     └─▶ Requires: Invoice PAID ✅, Return inspection ✅, Deposit settled ✅│
│     └─▶ Vehicle: AVAILABLE                                              │
│                                                                         │
│  ❌ CANCEL at any non-terminal state ──▶ Status: CANCELLED               │
│     └─▶ Vehicle: AVAILABLE                                              │
└─────────────────────────────────────────────────────────────────────────┘
```
