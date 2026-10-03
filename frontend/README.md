# 🖥️ Frontend Application (React + Vite)

## Overview

The **Frontend** is a single-page application (SPA) built with **React 18** and **Vite**. It provides role-based dashboards and interfaces for Customers, Staff, Managers, and Admins to interact with the RentEaseLanka vehicle rental system. The frontend communicates with the Spring Boot backend via Axios HTTP client and uses JWT-based authentication stored in `localStorage`.

---

## Technologies & Libraries

| Technology | Version | Purpose |
|---|---|---|
| **React** | 18.3.1 | UI component library |
| **Vite** | 8.2.2 | Build tool & dev server |
| **React Router DOM** | 7.18.2 | Client-side routing & navigation |
| **Axios** | 1.19.0 | HTTP client for API communication |
| **React Toastify** | 11.1.0 | Toast notification system |
| **React Icons** | 5.7.0 | Icon library |
| **Vanilla CSS** | — | Custom styling (no CSS framework) |

---

## Architecture & Design Patterns

| Pattern | Usage |
|---|---|
| **Context API** | `AuthContext` for global authentication state |
| **Protected Routes** | `ProtectedRoute` component with role-based access control |
| **Layout Pattern** | Separate layouts per role: `CustomerLayout`, `AdminLayout`, `ManagerLayout`, `StaffLayout` |
| **API Service Layer** | Dedicated API modules in `src/api/` for each backend component |
| **Axios Interceptor** | Auto-attaches JWT token to every request via `axiosInstance` |
| **Component Composition** | Reusable components in `src/components/` organized by feature |

---

## Project Structure

```
frontend/
├── src/
│   ├── App.jsx                          # Main app with routing
│   ├── main.jsx                         # React entry point
│   ├── index.css                        # Global styles
│   │
│   ├── api/                             # Backend API service layer
│   │   ├── axiosInstance.js             # Axios config with JWT interceptor
│   │   ├── auth.js                      # Auth API (register, login)
│   │   ├── bookings.js                  # Booking CRUD API
│   │   ├── drivers.js                   # Driver management API
│   │   ├── financial.js                 # Financial reports API
│   │   ├── handovers.js                 # Handover management API
│   │   ├── payments.js                  # Payment system API
│   │   ├── returns.js                   # Return inspection API
│   │   ├── users.js                     # User management API
│   │   └── adminDashboard.js            # Admin dashboard API
│   │
│   ├── context/
│   │   └── AuthContext.jsx              # Authentication context provider
│   │
│   ├── components/
│   │   ├── common/                      # Shared components (ProtectedRoute, etc.)
│   │   ├── layout/                      # Role-based layout components
│   │   ├── category/                    # Vehicle category components
│   │   ├── fleet/                       # Fleet management components
│   │   ├── handover/                    # Handover management components
│   │   ├── maintenance/                 # Maintenance components
│   │   └── payments/                    # Payment components
│   │
│   ├── pages/
│   │   ├── Auth/                        # SignIn, SignUp pages
│   │   │   ├── SignIn.jsx
│   │   │   └── SignUp.jsx
│   │   │
│   │   ├── Customer/                    # Customer-specific pages
│   │   │   ├── MyBookingsPage.jsx
│   │   │   └── NicUploadPage.jsx
│   │   │
│   │   ├── Admin/                       # Admin-specific pages
│   │   │   ├── AdminDashboard.jsx
│   │   │   └── CreateManagerPage.jsx
│   │   │
│   │   ├── Manager/                     # Manager-specific pages
│   │   │   ├── ManagerDashboard.jsx
│   │   │   ├── CreateStaffPage.jsx
│   │   │   └── CreateDriverPage.jsx
│   │   │
│   │   ├── Staff/                       # Staff-specific pages
│   │   │   ├── StaffDashboard.jsx
│   │   │   ├── PendingCustomersPage.jsx
│   │   │   └── HandoverPage.jsx
│   │   │
│   │   ├── HomePage.jsx                 # Public landing page
│   │   ├── VehiclesPage.jsx             # Public vehicle listing
│   │   ├── CustomerVehicleDetail.jsx    # Vehicle detail & booking page
│   │   ├── FleetPage.jsx                # Fleet management (shared)
│   │   ├── BookingsPage.jsx             # Booking management (shared)
│   │   ├── MaintenancePage.jsx          # Maintenance management (shared)
│   │   ├── CategoriesPage.jsx           # Category management (shared)
│   │   ├── PendingVehiclesPage.jsx      # Pending vehicle approval
│   │   ├── FinancialPage.jsx            # Financial reports
│   │   ├── VehicleDetailPage.jsx        # Vehicle detail (admin/staff view)
│   │   ├── Dashboard.jsx                # Generic dashboard component
│   │   └── UnauthorizedPage.jsx         # 403 unauthorized page
│   │
│   └── assets/                          # Static assets (images, etc.)
│
├── package.json                         # Dependencies & scripts
└── Dockerfile                           # Production build container
```

---

## Routing & Role-Based Access

### Public Routes

| Path | Component | Description |
|---|---|---|
| `/` | `HomePage` | Public landing page |
| `/vehicles` | `VehiclesPage` | Browse available vehicles |
| `/vehicles/:id` | `CustomerVehicleDetail` | Vehicle details & booking |
| `/signin` | `SignIn` | Login page |
| `/signup` | `SignUp` | Registration page |
| `/unauthorized` | `UnauthorizedPage` | 403 access denied |

### Customer Routes (`CUSTOMER` role)

| Path | Component | Description |
|---|---|---|
| `/my/bookings` | `MyBookingsPage` | View personal bookings |
| `/my/nic-upload` | `NicUploadPage` | Upload NIC for verification |

### Admin Routes (`ADMIN` role)

| Path | Component | Description |
|---|---|---|
| `/admin` | `AdminDashboard` | Admin dashboard overview |
| `/admin/fleet` | `FleetPage` | Fleet management |
| `/admin/bookings` | `BookingsPage` | Booking management |
| `/admin/pending-vehicles` | `PendingVehiclesPage` | Approve pending vehicles |
| `/admin/categories` | `CategoriesPage` | Vehicle categories |
| `/admin/maintenance` | `MaintenancePage` | Maintenance management |
| `/admin/financial` | `FinancialPage` | Financial reports |
| `/admin/handovers` | `HandoverPage` | Handover management |
| `/admin/managers` | `CreateManagerPage` | Create manager accounts |
| `/admin/vehicle/:id` | `VehicleDetailPage` | Vehicle detail view |

### Manager Routes (`MANAGER` role)

| Path | Component | Description |
|---|---|---|
| `/manager` | `ManagerDashboard` | Manager dashboard |
| `/manager/fleet` | `FleetPage` | Fleet management |
| `/manager/bookings` | `BookingsPage` | Booking management |
| `/manager/pending-vehicles` | `PendingVehiclesPage` | Approve pending vehicles |
| `/manager/categories` | `CategoriesPage` | Vehicle categories |
| `/manager/handovers` | `HandoverPage` | Handover management |
| `/manager/financial` | `FinancialPage` | Financial reports |
| `/manager/staff` | `CreateStaffPage` | Create staff accounts |
| `/manager/drivers` | `CreateDriverPage` | Create driver accounts |
| `/manager/vehicle/:id` | `VehicleDetailPage` | Vehicle detail view |

### Staff Routes (`STAFF` role)

| Path | Component | Description |
|---|---|---|
| `/staff` | `StaffDashboard` | Staff dashboard |
| `/staff/fleet` | `FleetPage` | Fleet overview |
| `/staff/bookings` | `BookingsPage` | Booking management |
| `/staff/handovers` | `HandoverPage` | Vehicle handovers |
| `/staff/pending-customers` | `PendingCustomersPage` | NIC verification queue |
| `/staff/maintenance` | `MaintenancePage` | Maintenance management |
| `/staff/vehicle/:id` | `VehicleDetailPage` | Vehicle detail view |

---

## API Service Layer

The `src/api/` directory contains modular API service files:

| Module | File | Backend Endpoints Consumed |
|---|---|---|
| **Auth** | `auth.js` | `/api/auth/register`, `/api/auth/login` |
| **Bookings** | `bookings.js` | `/api/booking/*` |
| **Drivers** | `drivers.js` | `/api/manager/drivers`, `/api/booking/available-drivers`, `/api/booking/{id}/assign-driver` |
| **Financial** | `financial.js` | `/api/financial/income` |
| **Handovers** | `handovers.js` | `/api/handovers/*` |
| **Payments** | `payments.js` | `/api/payments/*` |
| **Returns** | `returns.js` | `/api/returns/*` |
| **Users** | `users.js` | `/api/customer/*`, `/api/staff/*`, `/api/admin/*`, `/api/manager/*` |
| **Dashboard** | `adminDashboard.js` | `/api/admin/dashboard/*`, `/api/admin/reports/*` |

### Axios Instance Configuration

```javascript
// Auto-attaches JWT token from localStorage
// Base URL: http://localhost:8081
// Interceptor adds: Authorization: Bearer <token>
// Handles 401 responses → redirect to login
```

---

## Authentication Flow

```
1. User logs in via SignIn page
2. POST /api/auth/login → receives { token, user }
3. Token stored in localStorage as "token"
4. User data stored in localStorage as "user"
5. AuthContext provides { user, token, login, logout } to all components
6. ProtectedRoute checks user.role against allowedRoles
7. axiosInstance interceptor auto-attaches token to every API request
8. On 401 response → clear localStorage, redirect to /signin
```

---

## Development

### Prerequisites
- Node.js 18+
- npm or yarn

### Setup & Run

```bash
cd frontend
npm install
npm run dev        # Starts Vite dev server on http://localhost:5173
```

### Build for Production

```bash
npm run build      # Outputs to dist/
npm run preview    # Preview production build
```
