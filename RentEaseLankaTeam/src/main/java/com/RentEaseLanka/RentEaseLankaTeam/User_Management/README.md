# 🔐 User Management Component

## Overview

The **User Management** component is the foundation of RentEaseLanka's security and identity system. It handles user registration, authentication via JWT tokens, role-based access control (RBAC), NIC (National Identity Card) document upload/verification, and hierarchical user creation (Admin → Manager → Staff → Driver).

---

## Technologies & Libraries

| Technology | Purpose |
|---|---|
| **Spring Boot 4.1** | Application framework |
| **Spring Security** | Authentication & authorization framework |
| **JWT (jjwt 0.11.5)** | Stateless token-based authentication |
| **Spring Data JPA** | ORM / database interaction |
| **MS SQL Server** | Relational database |
| **Lombok** | Boilerplate code reduction |
| **BCrypt** | Password hashing (via `PasswordEncoder`) |

---

## Architecture & Design Patterns

| Pattern | Usage |
|---|---|
| **Layered Architecture** | Controller → Service → Repository |
| **DTO Pattern** | `RegisterRequest`, `AuthenticationRequest`, `CreateUserRequest`, `AuthenticationResponse` |
| **Builder Pattern** | `User.builder()` for constructing user entities |
| **Filter Chain Pattern** | `JwtAuthenticationFilter` intercepts every request for token validation |
| **Stateless Session** | `SessionCreationPolicy.STATELESS` — no server-side session |
| **Data Seeder** | `DataSeeder` auto-creates the default ADMIN account on first launch |

---

## Data Model

### `User` Entity (table: `_user`)

| Field | Type | Description |
|---|---|---|
| `id` | `Long` (PK, auto) | Unique identifier |
| `firstName` | `String` | User's first name |
| `lastName` | `String` | User's last name |
| `email` | `String` (unique) | Login credential / username |
| `password` | `String` | BCrypt-hashed password |
| `role` | `Role` (enum) | `ADMIN`, `CUSTOMER`, `MANAGER`, `STAFF`, `DRIVER` |
| `verificationStatus` | `VerificationStatus` (enum) | `PENDING`, `VERIFIED`, `REJECTED` |
| `nicImageUrl` | `String` | Path to uploaded NIC image file |

The `User` class implements Spring Security's `UserDetails` interface, granting `ROLE_<role>` authority.

### Enums

- **`Role`**: `ADMIN`, `CUSTOMER`, `MANAGER`, `STAFF`, `DRIVER`
- **`VerificationStatus`**: `PENDING`, `VERIFIED`, `REJECTED`

---

## API Endpoints

### Authentication (`/api/auth`) — Public

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new customer account | ❌ |
| `POST` | `/api/auth/login` | Authenticate and receive JWT token | ❌ |

**Register Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Authentication Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": { "id": 1, "firstName": "John", "role": "CUSTOMER", ... }
}
```

### Customer (`/api/customer`) — CUSTOMER role

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/customer/nic-upload` | Upload NIC image for verification (multipart) | ✅ CUSTOMER |

### Staff (`/api/staff`) — STAFF / MANAGER / ADMIN roles

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/staff/pending-customers` | List customers with PENDING verification | ✅ STAFF+ |
| `POST` | `/api/staff/verify-customer/{id}` | Approve a customer's NIC verification | ✅ STAFF+ |
| `POST` | `/api/staff/reject-customer/{id}` | Reject a customer's NIC verification | ✅ STAFF+ |
| `GET` | `/api/staff/file/{fileName}` | Serve NIC image file securely | ✅ STAFF+ |

### Manager (`/api/manager`) — MANAGER / ADMIN roles

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/manager/staff` | Create a new Staff user account | ✅ MANAGER+ |

### Admin (`/api/admin`) — ADMIN role only

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/admin/managers` | Create a new Manager user account | ✅ ADMIN |

---

## Operational Workflows

### 1. Customer Registration & Verification Workflow

```
┌──────────────┐     ┌──────────────┐     ┌───────────────┐     ┌──────────────┐
│   Customer   │────▶│  POST        │────▶│  User saved   │────▶│  JWT token   │
│   registers  │     │  /register   │     │  role=CUSTOMER│     │  returned    │
│              │     │              │     │  status=PENDING│     │              │
└──────────────┘     └──────────────┘     └───────────────┘     └──────────────┘
                                                  │
                                                  ▼
                                          ┌───────────────┐
                                          │  Customer     │
                                          │  uploads NIC  │
                                          │  /nic-upload  │
                                          └───────┬───────┘
                                                  │
                                                  ▼
                                          ┌───────────────┐
                                          │  Staff reviews │
                                          │  NIC document  │
                                          │  /verify or    │
                                          │  /reject       │
                                          └───────┬───────┘
                                                  │
                                    ┌─────────────┴─────────────┐
                                    ▼                           ▼
                            ┌──────────────┐          ┌──────────────┐
                            │   VERIFIED   │          │   REJECTED   │
                            │  Can book    │          │  Cannot book │
                            └──────────────┘          └──────────────┘
```

### 2. JWT Authentication Flow

```
1. Client sends POST /api/auth/login with {email, password}
2. AuthenticationManager validates credentials
3. JwtService generates HS256-signed token with claims:
   - sub: user email
   - role: user role
   - userId: user ID
   - verificationStatus: verification state
   - exp: current time + 24h (default)
4. Token returned in response body
5. Client sends token as "Authorization: Bearer <token>" on subsequent requests
6. JwtAuthenticationFilter intercepts every request:
   - Extracts token from Authorization header
   - Validates signature and expiration
   - Loads UserDetails from database
   - Sets SecurityContext authentication
```

### 3. User Hierarchy Creation

```
ADMIN ──creates──▶ MANAGER (auto-verified)
MANAGER ──creates──▶ STAFF (auto-verified)
MANAGER ──creates──▶ DRIVER (auto-verified, via Driver_Management)
```

---

## Security Configuration

- **CORS**: Allows `localhost:5173`, `localhost:3000`, `localhost:5174`
- **CSRF**: Disabled (stateless JWT)
- **Session**: `STATELESS` — no HTTP sessions
- **Password Encoding**: BCrypt
- **Method Security**: `@PreAuthorize` with SpEL expressions
- **Public Endpoints**: `/api/auth/*`, `/api/vehicles/**`, `/api/category/**`, `/uploads/**`, `/api/booking/calculate-price`
- **Path Traversal Protection**: NIC file serving rejects `..`, `/`, `\` in filenames

---

## File Structure

```
User_Management/
├── Config/
│   ├── ApplicationConfig.java        # AuthenticationProvider, PasswordEncoder beans
│   ├── DataSeeder.java               # Auto-seeds default admin account
│   ├── DotenvEnvironmentPostProcessor.java  # Loads .env variables
│   ├── JwtAuthenticationFilter.java  # OncePerRequestFilter for JWT validation
│   ├── JwtService.java              # Token generation, validation, claim extraction
│   └── SecurityConfig.java          # SecurityFilterChain, CORS, endpoint rules
├── Controller/
│   ├── AdminController.java          # Admin-only user creation endpoints
│   ├── AuthController.java           # Public register/login endpoints
│   ├── CustomerController.java       # Customer NIC upload endpoint
│   ├── ManagerController.java        # Manager staff creation endpoint
│   └── StaffController.java          # Staff verification + NIC file serving
├── DTOs/
│   ├── AuthenticationRequest.java    # Login request DTO
│   ├── AuthenticationResponse.java   # JWT + user response DTO
│   ├── CreateUserRequest.java        # Staff/Manager creation DTO
│   └── RegisterRequest.java          # Customer registration DTO
├── Model/
│   ├── Role.java                     # Role enum
│   ├── User.java                     # JPA entity implementing UserDetails
│   └── VerificationStatus.java       # Verification status enum
├── Repository/
│   └── UserRepository.java           # JPA repository with findByEmail, findByRole
└── Service/
    ├── AuthService.java              # Registration & authentication logic
    ├── FileStorageService.java       # NIC file storage on disk
    └── UserService.java              # User CRUD, NIC upload, verification
```
