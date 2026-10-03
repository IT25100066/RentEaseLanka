# 💳 Payment Management System Component

## Overview

The **Payment Management System** handles the full financial transaction lifecycle for RentEaseLanka including invoice generation, payment processing, security deposit holds/releases, damage recovery charges, refunds, customer payment method management, and automated no-show processing. It uses a **simulated payment gateway** to process transactions.

---

## Technologies & Libraries

| Technology | Purpose |
|---|---|
| **Spring Boot 4.1** | Application framework |
| **Spring Data JPA** | ORM / database interaction |
| **MS SQL Server** | Relational database |
| **Lombok** | Boilerplate code reduction |
| **Jakarta Validation** | `@Valid`, `@NotNull` constraints |
| **Java BigDecimal** | Precise monetary calculations |

---

## Architecture & Design Patterns

| Pattern | Usage |
|---|---|
| **Layered Architecture** | Controller → Service → Repository |
| **DTO Pattern** | 13 separate DTOs for different operations |
| **Payment Simulator** | `PaymentSimulatorService` simulates gateway processing |
| **Idempotency** | `txnReference` ensures duplicate payments are detected |
| **Transaction Logging** | `PaymentTransactionLog` records every payment event |
| **Multi-Entity** | Invoice, Payment, PaymentMethod, DamageRecord, SimulatorConfig, TransactionLog |

---

## Data Models

### `Invoice` Entity (table: `invoices`)

| Field | Type | Description |
|---|---|---|
| `id` | `Long` (PK, auto) | Unique identifier |
| `vehicleReservationId` | `Long` | Linked booking ID |
| `customerId` | `Long` | Customer ID |
| `rentalDays` | `Integer` | Number of rental days |
| `dailyRate` | `BigDecimal` | Rate per day |
| `subtotal` | `BigDecimal` | Pre-tax total |
| `taxAmount` | `BigDecimal` | Tax amount |
| `totalAmount` | `BigDecimal` | Final total (subtotal + tax) |
| `paidAmount` | `BigDecimal` | Amount paid so far |
| `isPartialAllowed` | `Boolean` | Whether partial payment allowed |
| `minimumDepositAmount` | `BigDecimal` | Minimum required deposit |
| `status` | `InvoiceStatus` | `UNPAID`, `PARTIALLY_PAID`, `PAID`, `REQUEST_REJECTED`, `NO_SHOW` |
| `pickupDeadline` | `LocalDateTime` | Deadline for pickup (no-show trigger) |

### `Payment` Entity (table: `payments`)

| Field | Type | Description |
|---|---|---|
| `id` | `Long` (PK, auto) | Unique identifier |
| `invoiceId` | `Long` | Linked invoice ID |
| `paymentMethodId` | `Long` | Payment method used |
| `amount` | `BigDecimal` | Transaction amount |
| `status` | `PaymentStatus` | Transaction status |
| `paymentType` | `PaymentType` | Type of payment |
| `txnReference` | `String` (unique) | Idempotency key |
| `createdAt` | `LocalDateTime` | Transaction timestamp |

### Enums

**`InvoiceStatus`**: `UNPAID`, `PARTIALLY_PAID`, `PAID`, `REQUEST_REJECTED`, `NO_SHOW`

**`PaymentStatus`**: `INITIATED`, `PROCESSING`, `SUCCESS`, `FAILED`, `REFUNDED`, `PARTIALLY_REFUNDED`, `HELD`, `RELEASED`

**`PaymentType`**: `RENTAL_FEE`, `SECURITY_DEPOSIT_HOLD`, `DAMAGE_RECOVERY_CHARGE`

**`RentalTerm`**: `SHORT_TERM` (≤2 days), `MEDIUM_TERM` (3–29 days), `LONG_TERM` (≥30 days)

---

## API Endpoints

**Base Path:** `/api/payments`

### Invoice Management

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/payments/invoices` | Create a new invoice |
| `GET` | `/api/payments/invoices/{invoiceId}` | Get invoice details |
| `GET` | `/api/payments/invoices/booking/{bookingId}` | Get invoice by booking ID |
| `GET` | `/api/payments/customer/{customerId}/invoices` | Get all invoices for a customer |

### Payment Processing

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/payments/process` | Process a payment (rental fee) |
| `POST` | `/api/payments/{paymentId}/refund` | Issue a refund |

### Security Deposit Management

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/payments/holds/place?invoiceId=...&paymentMethodId=...&holdAmount=...&idempotencyKey=...` | Place a security deposit hold |
| `POST` | `/api/payments/holds/{paymentId}/release?reason=...` | Release a held security deposit |
| `GET` | `/api/payments/holds/booking/{bookingId}` | Get active deposit for a booking |

### Damage & Recovery

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/payments/damage/charge` | Charge damage from security deposit |

### No-Show Processing

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/payments/cron/no-shows` | Process overdue no-show invoices |

### Payment Methods

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/payments/customer/{customerId}/methods` | Get customer's saved payment methods |
| `POST` | `/api/payments/customer/{customerId}/methods` | Add a new payment method |

### Payment Simulator

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/payments/simulator/rules` | Configure simulator rules (for testing) |

---

## Business Logic

### Invoice Auto-Generation

```
When a booking is created (BookingServiceImpl.createBooking):
  1. Calculate rental days and daily rate
  2. Create invoice with:
     - vehicleReservationId = bookingId
     - customerId = user.id
     - totalAmount = calculated total
     - status = UNPAID
     - pickupDeadline = pickupDateTime + 2 hours
  3. Invoice auto-generates minimum deposit based on rental term
```

### Payment Processing Flow

```
1. Validate invoice exists and is not PAID/NO_SHOW
2. Simulate payment via PaymentSimulatorService
3. If simulation succeeds:
   a. Create Payment record with status = SUCCESS
   b. Update invoice.paidAmount += payment.amount
   c. If paidAmount >= totalAmount: invoice.status = PAID
   d. Else: invoice.status = PARTIALLY_PAID
4. If simulation fails: Payment status = FAILED
5. Log transaction in PaymentTransactionLog
```

### Security Deposit Workflow

```
1. PLACE: Staff places hold on customer's payment method
   └─▶ Payment: type=SECURITY_DEPOSIT_HOLD, status=HELD

2a. RELEASE (no damage):
    └─▶ Payment: status=RELEASED
    └─▶ Full deposit returned

2b. DAMAGE CHARGE:
    └─▶ DamageRecord created
    └─▶ If damage ≤ deposit: deduct from hold, release remainder
    └─▶ If damage > deposit: use full deposit, create additional charge
    └─▶ Payment: status=RELEASED
```

### Security Deposit Settlement at Return

```
VehicleReturnService.inspectAndCompleteReturn() calls:
  paymentService.settleSecurityDepositForReturn(bookingId, hasDamage, damageAmount, description)

  If no damage → full deposit released
  If damage → damage charged from deposit, remainder released
  If no active hold exists → skip settlement
```

### No-Show Processing

```
POST /api/payments/cron/no-shows
1. Find all UNPAID/PARTIALLY_PAID invoices past pickupDeadline
2. For each overdue invoice:
   a. Mark invoice status = NO_SHOW
   b. Cancel associated booking
   c. Release vehicle back to AVAILABLE
3. Return list of processed NoShowResultDTOs
```

---

## Operational Workflow

```
┌───────────────────────────────────────────────────────────────────────┐
│                    PAYMENT LIFECYCLE                                  │
├───────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  1. Booking created ──▶ Invoice auto-generated (UNPAID)              │
│                                                                       │
│  2. Customer adds payment method                                     │
│     POST /customer/{id}/methods                                      │
│                                                                       │
│  3. Customer/Staff processes payment                                 │
│     POST /process                                                    │
│     └─▶ Invoice: PARTIALLY_PAID or PAID                              │
│                                                                       │
│  4. Staff places security deposit hold                               │
│     POST /holds/place                                                │
│     └─▶ Payment: HELD                                                │
│                                                                       │
│  5. [At Return] Deposit settlement                                   │
│     ├─▶ No damage → RELEASED (full refund)                           │
│     └─▶ Damage found → DAMAGE_RECOVERY_CHARGE + RELEASED             │
│                                                                       │
│  6. [If no-show] Cron trigger                                        │
│     POST /cron/no-shows                                              │
│     └─▶ Invoice: NO_SHOW, Booking: CANCELLED                        │
│                                                                       │
│  7. [Optional] Refund                                                │
│     POST /{paymentId}/refund                                         │
│     └─▶ Payment: REFUNDED or PARTIALLY_REFUNDED                     │
│                                                                       │
└───────────────────────────────────────────────────────────────────────┘
```

---

## File Structure

```
Payment_Management_System/
├── controller/
│   └── PaymentController.java          # REST endpoints for all payment operations
├── dto/
│   ├── AddPaymentMethodRequest.java    # Add payment method request
│   ├── ChargeDamageRequest.java        # Damage charge request
│   ├── CreateInvoiceRequest.java       # Invoice creation request
│   ├── CreateSimulationRuleRequest.java # Simulator config request
│   ├── InvoiceDTO.java                 # Invoice summary response
│   ├── InvoiceDetailsDTO.java          # Detailed invoice response
│   ├── NoShowResultDTO.java            # No-show processing result
│   ├── PaymentMethodDTO.java           # Payment method response
│   ├── PaymentReceiptDTO.java          # Payment receipt response
│   ├── ProcessPaymentRequest.java      # Payment processing request
│   ├── RefundRequest.java              # Refund request
│   ├── RefundStatusDTO.java            # Refund status response
│   └── SimulationRuleDTO.java          # Simulator rule response
├── entity/
│   ├── DamageRecord.java               # Damage recovery record entity
│   ├── Invoice.java                    # Invoice entity
│   ├── Payment.java                    # Payment transaction entity
│   ├── PaymentMethod.java              # Customer payment method entity
│   ├── PaymentTransactionLog.java      # Transaction audit log entity
│   └── SimulatorConfig.java            # Payment simulator config entity
├── enums/
│   ├── EventType.java                  # Transaction log event types
│   ├── InvoiceStatus.java              # Invoice status enum
│   ├── PaymentStatus.java              # Payment status enum
│   ├── PaymentType.java                # Payment type enum
│   └── RentalTerm.java                 # Rental duration classification
├── repository/
│   ├── DamageRecordRepository.java
│   ├── InvoiceRepository.java
│   ├── PaymentMethodRepository.java
│   ├── PaymentRepository.java
│   ├── PaymentTransactionLogRepository.java
│   └── SimulatorConfigRepository.java
└── service/
    ├── PaymentService.java             # Main payment service (33K+ lines)
    └── PaymentSimulatorService.java    # Simulated payment gateway
```
