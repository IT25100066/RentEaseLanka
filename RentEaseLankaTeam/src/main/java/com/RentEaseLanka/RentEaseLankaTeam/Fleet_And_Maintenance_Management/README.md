# 🚗 Fleet & Maintenance Management Component

## Overview

The **Fleet & Maintenance Management** component is a dual-purpose module that manages the vehicle fleet inventory and the vehicle maintenance lifecycle. It is divided into two sub-modules:

1. **Fleet Management** — Vehicle CRUD, image handling, category management, and vehicle status lifecycle.
2. **Maintenance Management** — Scheduling, tracking, and completing vehicle maintenance records.

Both sub-modules use the **State Pattern** to enforce valid status transitions.

---

## Technologies & Libraries

| Technology | Purpose |
|---|---|
| **Spring Boot 4.1** | Application framework |
| **Spring Data JPA** | ORM / database interaction |
| **Spring Security** | `@PreAuthorize` role-based endpoint protection |
| **Redis** | Caching layer for fleet queries (via `RedisConfig`) |
| **MS SQL Server** | Relational database |
| **Lombok** | Boilerplate code reduction |
| **Jakarta Validation** | `@Min`, `@PositiveOrZero`, `@DecimalMin`, `@NotNull` constraints |

---

## Architecture & Design Patterns

| Pattern | Usage |
|---|---|
| **State Pattern** | `VehicleState` interface with 7 concrete states for vehicle lifecycle |
| **State Pattern** | `VehicleMaintenanceState` interface with 4 concrete states for maintenance |
| **Layered Architecture** | Controller → Service (Interface + Impl) → Repository |
| **Service Interface Pattern** | `FleetService`/`MaintenanceService` interfaces with implementations |
| **DTO Pattern** | `VehicleCreateRequestDTO`, `VehicleResponseDTO`, `MaintenanceRecordDTO` |
| **Redis Caching** | `RedisConfig` configures caching for vehicle data |

---

## Data Models

### `Vehicle` Entity

| Field | Type | Description |
|---|---|---|
| `id` | `Long` (PK, auto) | Unique identifier |
| `licensePlate` | `String` (unique) | Vehicle registration number |
| `model` | `String` | Vehicle model name |
| `year` | `int` | Manufacture year (≥ 1990) |
| `currentOdometer` | `double` | Current mileage reading |
| `status` | `VehicleStatus` (enum) | Current lifecycle state |
| `dailyRentalRate` | `BigDecimal` | Price per day (> 0) |
| `category` | `VehicleCategory` (FK) | Vehicle category reference |
| `images` | `List<VehicleImage>` | Associated images |
| `maintenanceRecords` | `List<MaintenanceRecord>` | Linked maintenance history |

### `VehicleStatus` Enum — State Machine

```
                    ┌──────────────┐
                    │   PENDING    │ ◄── New vehicles start here
                    └──────┬───────┘
                           │ makeAvailable()
                           ▼
                    ┌──────────────┐
          ┌────────│  AVAILABLE   │◄──────────────────────────┐
          │        └──────┬───────┘                           │
          │               │ reserve()                         │ returnVehicle()
          │               ▼                                   │
          │        ┌──────────────┐                    ┌──────┴───────┐
          │        │  RESERVED    │───rent()──────────▶│   RENTED     │
          │        └──────────────┘                    └──────────────┘
          │
          │ scheduleMaintenance()
          ▼
   ┌──────────────────┐
   │ UNDER_MAINTENANCE │───makeAvailable()──▶ AVAILABLE
   └──────────────────┘
          │
          │ outOfService()
          ▼
   ┌──────────────────┐
   │  OUT_OF_SERVICE   │ (terminal / recoverable by admin)
   └──────────────────┘
```

### `VehicleCategory` Entity

| Field | Type | Description |
|---|---|---|
| `id` | `Long` (PK, auto) | Unique identifier |
| `name` | `String` (unique) | Category name (e.g., SUV, Sedan) |
| `description` | `String` | Category description |

### `VehicleImage` Entity

| Field | Type | Description |
|---|---|---|
| `id` | `Long` (PK, auto) | Unique identifier |
| `imageUrl` | `String` | File path/URL to image |
| `vehicle` | `Vehicle` (FK) | Parent vehicle reference |

### `MaintenanceRecord` Entity

| Field | Type | Description |
|---|---|---|
| `id` | `Long` (PK, auto) | Unique identifier |
| `vehicle` | `Vehicle` (FK) | Vehicle being maintained |
| `type` | `MaintenanceType` (enum) | `OIL_CHANGE`, `TIRE_ROTATION`, `BRAKE_CHECK`, `ENGINE_SERVICE`, `GENERAL_INSPECTION` |
| `status` | `MaintenanceStatus` (enum) | `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| `description` | `String` | Work description |
| `cost` | `BigDecimal` | Maintenance cost |
| `odometerAtService` | `Double` | Odometer reading at service time |
| `scheduledDate` | `LocalDateTime` | Scheduled maintenance date |
| `completionDate` | `LocalDateTime` | Actual completion date |

### Maintenance State Machine

```
SCHEDULED ──startMaintenance()──▶ IN_PROGRESS ──completeMaintenance()──▶ COMPLETED
    │                                  │
    └──cancelMaintenance()──▶ CANCELLED ◄──cancelMaintenance()──┘
```

---

## API Endpoints

### Fleet Management

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/vehicle/create` | Create vehicle with images (multipart) | ✅ MANAGER |
| `GET` | `/api/vehicles` | Get all vehicles | ❌ Public |
| `GET` | `/api/vehicle/{id}` | Get vehicle by ID | ❌ Public |
| `POST` | `/api/vehicle/update` | Update vehicle details | ✅ Authenticated |
| `GET` | `/api/pendingVehicles` | List vehicles with PENDING status | ✅ Authenticated |
| `POST` | `/api/manager/vehicle/accept` | Approve pending vehicles (batch) | ✅ MANAGER / ADMIN |
| `GET` | `/api/vehicle?filterBy=...` | Filter vehicles by category/status | ❌ Public |
| `DELETE` | `/api/vehicle/{id}/remove` | Set vehicle to OUT_OF_SERVICE | ✅ MANAGER / ADMIN |
| `DELETE` | `/api/vehicle/{id}/delete` | Permanently delete vehicle | ✅ MANAGER / ADMIN |

### Vehicle Categories

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/category/create` | Create a new category | ✅ MANAGER |
| `GET` | `/api/category/all` | Get all categories | ❌ Public |
| `GET` | `/api/category/{id}` | Get category by ID | ❌ Public |
| `PUT` | `/api/category/{id}` | Update category | ✅ MANAGER |
| `DELETE` | `/api/category/{id}` | Delete category | ✅ MANAGER |

### Maintenance Management (`/api/maintenance`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/maintenance` | Schedule new maintenance | ✅ STAFF / MANAGER / ADMIN |
| `GET` | `/api/maintenance/vehicle/{id}` | Get all records for a vehicle | ✅ STAFF / MANAGER / ADMIN |
| `GET` | `/api/maintenance/{id}` | Get maintenance record by ID | ✅ STAFF / MANAGER / ADMIN |
| `PUT` | `/api/maintenance/{id}/startMaintenance` | Start scheduled maintenance | ✅ STAFF / MANAGER / ADMIN |
| `PUT` | `/api/maintenance/{id}/completed` | Complete maintenance | ✅ STAFF / MANAGER / ADMIN |
| `DELETE` | `/api/maintenance/{id}` | Cancel maintenance | ✅ STAFF / MANAGER / ADMIN |

---

## Operational Workflows

### Vehicle Onboarding Workflow

```
1. MANAGER creates vehicle with images ──▶ Status: PENDING
2. MANAGER/ADMIN approves pending vehicle ──▶ Status: AVAILABLE
3. Vehicle appears in public listings
```

### Maintenance Workflow

```
1. STAFF schedules maintenance for a vehicle
   └─▶ MaintenanceRecord: SCHEDULED
   └─▶ Vehicle: UNDER_MAINTENANCE

2. STAFF starts the maintenance work
   └─▶ MaintenanceRecord: IN_PROGRESS

3. STAFF completes maintenance (records cost, updated odometer)
   └─▶ MaintenanceRecord: COMPLETED
   └─▶ Vehicle: AVAILABLE

OR: Cancel at SCHEDULED/IN_PROGRESS
   └─▶ MaintenanceRecord: CANCELLED
   └─▶ Vehicle: AVAILABLE
```

---

## State Pattern Implementation

### Vehicle States (7 classes)

Each state class implements the `VehicleState` interface:

```java
public interface VehicleState {
    void reserve(Vehicle vehicle);
    void rent(Vehicle vehicle);
    void returnVehicle(Vehicle vehicle);
    void scheduleMaintenance(Vehicle vehicle);
    void makeAvailable(Vehicle vehicle);
    void outOfService(Vehicle vehicle);
    void pendingState(Vehicle vehicle);
}
```

| State Class | Allowed Transitions |
|---|---|
| `PendingState` | → Available |
| `AvailableState` | → Reserved, UnderMaintenance, OutOfService |
| `ReservedState` | → Rented, Available, OutOfService |
| `RentedState` | → Available (return), OutOfService |
| `MaintenanceState` | → Available, OutOfService |
| `OutOfServiceState` | → Available (recovery) |

### Maintenance States (4 classes)

Each implements `VehicleMaintenanceState`:

| State Class | Allowed Transitions |
|---|---|
| `ScheduledStatus` | → InProgress, Cancelled |
| `InProgressStatus` | → Completed, Cancelled |
| `CompletedStatus` | *(terminal)* |
| `CanceledStatus` | *(terminal)* |

---

## File Structure

```
Fleet_And_Maintenance_Management/
├── Config/
│   └── RedisConfig.java                    # Redis cache configuration
├── FleetManagement/
│   ├── Controller/
│   │   ├── FleetController.java            # Vehicle CRUD & lifecycle endpoints
│   │   └── VehicleCategoryController.java  # Category CRUD endpoints
│   ├── Model/
│   │   ├── Vehicle.java                    # JPA entity with state pattern
│   │   ├── VehicleCategory.java            # Category entity
│   │   ├── VehicleImage.java               # Image entity
│   │   ├── VehicleStatus.java              # Status enum
│   │   ├── DTOs/                           # Request/Response DTOs
│   │   ├── Mappers/                        # Entity ↔ DTO mappers
│   │   └── Vehicle_State/                  # State pattern implementations
│   │       ├── VehicleState.java           # State interface
│   │       ├── AvailableState.java
│   │       ├── ReservedState.java
│   │       ├── RentedState.java
│   │       ├── PendingState.java
│   │       ├── MaintenanceState.java
│   │       └── OutOfServiceState.java
│   ├── Repository/
│   │   ├── VehicleRepository.java
│   │   ├── VehicleCategoryRepository.java
│   │   └── VehicleImageRepository.java
│   ├── Security/
│   │   ├── CorsConfig.java                 # CORS configuration
│   │   └── WebMvcConfig.java               # Static resource serving
│   └── Service/
│       ├── FleetService.java               # Fleet service interface
│       ├── FleetServiceImpl.java           # Fleet service implementation
│       ├── VehicleCategoryService.java     # Category service interface
│       ├── VehicleCategoryServiceImpl.java # Category service implementation
│       └── FileStorageService/             # Vehicle image file storage
└── MaintenenceManagement/
    ├── Controller/
    │   └── MaintenanceController.java      # Maintenance CRUD & lifecycle endpoints
    ├── Model/
    │   ├── MaintenanceRecord.java          # JPA entity with state pattern
    │   ├── MaintenanceStatus.java          # Status enum
    │   ├── MaintenanceType.java            # Type enum
    │   ├── DTO/                            # Request/Response DTOs
    │   └── MaintainanceStatus/             # State pattern implementations
    │       ├── VehicleMaintenanceState.java # State interface
    │       ├── ScheduledStatus.java
    │       ├── InProgressStatus.java
    │       ├── CompletedStatus.java
    │       └── CanceledStatus.java
    ├── Repository/
    │   └── MaintenanceRepository.java
    └── Service/
        ├── MaintenanceService.java         # Maintenance service interface
        └── MaintenanceServiceImpl.java     # Maintenance service implementation
```
