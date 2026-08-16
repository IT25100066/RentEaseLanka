# Fleet and Maintenance Management Module

This module is a core component of the RentEaseLanka backend application. It is responsible for handling all operations related to vehicle fleet management and vehicle maintenance scheduling and tracking.

## 🚗 Fleet Management Features

The Fleet Management component handles the lifecycle of vehicles and their categories within the system.

### Vehicle Operations
- **Add a New Vehicle:** Create a new vehicle entry in the fleet, including uploading vehicle images (`POST /api/vehicle/create`).
- **View All Vehicles:** Retrieve a comprehensive list of all vehicles along with their image URLs (`GET /api/vehicles`).
- **View Vehicle Details:** Retrieve the detailed information of a specific vehicle using its unique ID (`GET /api/vehicle/{id}`).
- **Update Vehicle Information:** Modify existing details of a vehicle (`POST /api/vehicle/update`).
- **Filter Vehicles:** Search and filter vehicles based on specific criteria such as filter type, category, and status (`GET /api/vehicle`).
- **Manage Pending Vehicles:** View a list of vehicles that are in a 'pending' state (`GET /api/pendingVehicles`).
- **Make Vehicles Available:** Update the status of one or multiple vehicles to 'available' for rent (`POST /api/vehicle/makeAvailable`).
- **Remove/Out of Service:** Mark a vehicle as "Out of Service" or remove it from the active fleet (`DELETE /api/vehicle/{id}/remove`).

### Vehicle Category Operations
- **Create Vehicle Category:** Define new vehicle categories (`POST /api/category/create`).
- **View All Categories:** Retrieve a list of all existing vehicle categories (`GET /api/category`).
- **Update Category Pricing:** Update the pricing details associated with a specific vehicle category (`POST /api/category/update`).

---

## 🛠️ Maintenance Management Features

The Maintenance Management component is designed to keep track of vehicle servicing, repairs, and general upkeep to ensure the fleet remains in optimal condition.

### Maintenance Operations
- **Schedule Maintenance:** Schedule a new maintenance task for a specific vehicle (`POST /api/maintenance`).
- **View Maintenance Records by Vehicle:** Retrieve all historical and upcoming maintenance records for a particular vehicle (`GET /api/maintenance/vehicle/{id}`).
- **View Specific Maintenance Record:** Retrieve detailed information about a specific maintenance task using its ID (`GET /api/maintenance/{id}`).
- **Start Maintenance:** Update the status of a scheduled maintenance task to "In Progress" (`PUT /api/maintenance/{id}/startMaintenance`).
- **Complete Maintenance:** Mark a maintenance task as completed and automatically make the vehicle available again (`PUT /api/maintenance/{id}/completed`).
- **Cancel Maintenance:** Cancel a scheduled maintenance task that is no longer required (`DELETE /api/maintenance/{id}`).

---

## 🚀 API Endpoints Summary

### Fleet API
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/vehicle/create` | Add a new vehicle (Supports Multipart form data) |
| `GET`  | `/api/vehicles` | Get all vehicles |
| `GET`  | `/api/vehicle/{id}` | Get vehicle by ID |
| `POST` | `/api/vehicle/update` | Update vehicle details |
| `GET`  | `/api/pendingVehicles` | Get all pending vehicles |
| `POST` | `/api/vehicle/makeAvailable` | Set vehicles as available (Accepts list of IDs) |
| `GET`  | `/api/vehicle` | Filter vehicles by type, category, or status |
| `DELETE` | `/api/vehicle/{id}/remove` | Set vehicle out of service |

### Vehicle Category API
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/category/create` | Create a new category |
| `GET`  | `/api/category` | Get all categories |
| `POST` | `/api/category/update` | Update category pricing |

### Maintenance API
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/maintenance` | Schedule maintenance |
| `GET`  | `/api/maintenance/vehicle/{id}` | Get records for a specific vehicle |
| `GET`  | `/api/maintenance/{id}` | Get a specific maintenance record |
| `PUT`  | `/api/maintenance/{id}/startMaintenance` | Start a maintenance task |
| `PUT`  | `/api/maintenance/{id}/completed` | Complete a maintenance task |
| `DELETE` | `/api/maintenance/{id}` | Cancel a maintenance task |
