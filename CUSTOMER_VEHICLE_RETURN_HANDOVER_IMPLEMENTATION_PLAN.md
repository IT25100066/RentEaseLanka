# Customer Vehicle Return and Deposit Refund Implementation Plan

## 1. Objective

Implement the vehicle return workflow where:

1. The customer returns the vehicle to the company.
2. A staff member checks the vehicle condition and return details.
3. Staff records whether new damage exists.
4. If damage exists, staff records an approved deduction amount.
5. The damage amount is deducted from the customer's deposit.
6. The remaining deposit is refunded through the payment simulator.
7. The booking cannot be completed until the return inspection, final payment, damage settlement, and refund process are complete.

## 2. Existing System Context

The current application already contains:

- `BookingStatus`: `PENDING -> CONFIRMED -> ACTIVE -> COMPLETED`.
- `HandoverRecord` and `HandoverService` for vehicle pickup handover.
- `HandoverServiceImpl.recordDamage(...)` for recording damage descriptions.
- `BookingServiceImpl.makeCompleted(...)` for ending a rental.
- `PaymentTransaction` records for deposits, final payments, damage deductions, and refunds.
- Payment endpoints under `/api/payment`.
- Staff handover interface in `frontend/src/pages/Staff/HandoverPage.jsx`.
- Customer bookings interface in `frontend/src/pages/Customer/MyBookingsPage.jsx`.

The existing pickup handover process must not be reused blindly for vehicle return. Return inspection needs its own state, timestamps, mileage, damage amount, staff identity, and refund result.

## 3. Requirements

- **REQ-001:** A staff member can start a return inspection for an active booking.
- **REQ-002:** Staff can record the returned odometer reading and vehicle condition.
- **REQ-003:** Staff must explicitly confirm whether new damage exists.
- **REQ-004:** Staff must enter a valid damage amount when damage is found.
- **REQ-005:** Damage deduction cannot exceed the customer's remaining deposit.
- **REQ-006:** The remaining deposit must be refunded through the payment simulator.
- **REQ-007:** A booking cannot be completed before return inspection is finalized.
- **REQ-008:** A booking cannot be completed before final rental payment succeeds.
- **REQ-009:** A refund must not be issued more than once.
- **REQ-010:** Failed refunds remain retryable and visible to staff.
- **REQ-011:** Only authorized staff, manager, or admin users can finalize return inspections and approve deductions.
- **REQ-012:** Customer and staff screens show deposit, deduction, and refund amounts clearly.
- **REQ-013:** When a payment succeeds, its simulator-generated payment reference is automatically returned by the backend and displayed in the staff payment-verification interface as a read-only value.
- **REQ-014:** Staff must never manually type or edit a successful payment reference; the interface must show a verified state only after the backend confirms the transaction.

## 4. Proposed Return Lifecycle

```text
ACTIVE BOOKING
      |
      v
RETURN INSPECTION INITIATED
      |
      +--> Record return mileage
      |
      +--> Record fuel/condition details
      |
      +--> Confirm damage status
              |
              +--> NO DAMAGE
              |      |
              |      v
              |   REFUND FULL DEPOSIT
              |
              +--> DAMAGE FOUND
                     |
                     v
                Approve deduction amount
                     |
                     v
                Deduct from deposit
                     |
                     v
                Refund remaining deposit
      |
      v
FINAL PAYMENT VERIFIED
      |
      v
BOOKING COMPLETED
```

A return inspection must be finalized before `BookingStatus.COMPLETED` is allowed.

## 5. Backend Design

### 5.1 Return inspection model

Create a dedicated return inspection entity under:

```text
RentEaseLankaTeam/src/main/java/com/RentEaseLanka/RentEaseLankaTeam/Return_Management/
```

Recommended structure:

```text
Return_Management/
├── Controller/
│   └── ReturnInspectionController.java
├── Model/
│   ├── ReturnInspection.java
│   ├── ReturnInspectionStatus.java
│   ├── DamageAssessmentStatus.java
│   └── DTO/
│       ├── ReturnInspectionCreateDTO.java
│       ├── ReturnMileageDTO.java
│       ├── ReturnDamageDTO.java
│       └── ReturnInspectionResponseDTO.java
├── Repository/
│   └── ReturnInspectionRepository.java
└── Service/
    ├── ReturnInspectionService.java
    └── ReturnInspectionServiceImpl.java
```

Recommended `ReturnInspection` fields:

```text
id
bookingId
vehicleId
customerId
staffId
status: INITIATED, INSPECTING, REFUND_PENDING, COMPLETED, FAILED
startingOdometer
returnOdometer
fuelLevel
hasDamage
damageDescription
damageAmount
damageApprovedBy
damageApprovedAt
remainingDeposit
refundAmount
refundTransactionReference
refundStatus
inspectionStartedAt
inspectionCompletedAt
createdAt
updatedAt
```

Use `BigDecimal` for `damageAmount`, `remainingDeposit`, and `refundAmount`. Do not use `double` for monetary values.

### 5.2 Return inspection status

Add an explicit status instead of relying only on nullable fields:

```java
public enum ReturnInspectionStatus {
    INITIATED,
    INSPECTING,
    REFUND_PENDING,
    COMPLETED,
    FAILED
}
```

This prevents a booking from being completed when the inspection is only partially filled.

### 5.3 Link to booking and vehicle

The return inspection must reference:

- The active booking.
- The vehicle assigned to the booking.
- The customer who owns the booking.
- The staff member performing the inspection.

Validate all references server-side. Do not trust vehicle ID or customer ID supplied by the frontend when the booking already identifies them.

## 6. Return Inspection APIs

All return inspection write operations require one of:

```text
STAFF, MANAGER, ADMIN
```

### 6.1 Start return inspection

```http
POST /api/returns/bookings/{bookingId}/start
```

Rules:

- Booking must be `ACTIVE`.
- Booking must not already have a completed return inspection.
- The vehicle must belong to the booking.
- Only one active inspection may exist for a booking.
- Staff identity comes from the authenticated user where possible.

Response includes inspection ID, booking ID, vehicle details, and deposit balance.

### 6.2 Record return mileage

```http
PUT /api/returns/{inspectionId}/mileage
```

Request:

```json
{
  "returnOdometer": 45210.5
}
```

Rules:

- Return odometer must be greater than or equal to the pickup odometer.
- Reject negative or missing values.
- Store the final odometer on the vehicle only after the return inspection is successfully finalized.

### 6.3 Record vehicle condition

```http
PUT /api/returns/{inspectionId}/condition
```

Request:

```json
{
  "fuelLevel": "FULL",
  "notes": "Vehicle returned with all accessories."
}
```

### 6.4 Record damage assessment

```http
PUT /api/returns/{inspectionId}/damage
```

No-damage request:

```json
{
  "hasDamage": false,
  "damageDescription": "No new damage found.",
  "damageAmount": 0
}
```

Damage request:

```json
{
  "hasDamage": true,
  "damageDescription": "New scratch on the front bumper.",
  "damageAmount": 12500.00,
  "evidenceReference": "return-inspection-21-photo-1"
}
```

Validation:

- `hasDamage` is required.
- Description is required when damage is true.
- Amount is required when damage is true.
- Amount must be zero when no damage is confirmed.
- Amount must not exceed the remaining deposit.
- Damage amount must be approved by the submitting staff member or an authorized reviewer according to the selected approval policy.

### 6.5 Finalize return and refund deposit

```http
PUT /api/returns/{inspectionId}/complete
```

The service must:

1. Verify the inspection belongs to an active booking.
2. Verify return mileage exists.
3. Verify condition inspection exists.
4. Verify damage decision exists.
5. Verify final rental payment succeeded.
6. Calculate the remaining deposit.
7. Create a damage deduction transaction when applicable.
8. Transfer the remaining deposit through the payment simulator.
9. Update the vehicle odometer and return vehicle status to `AVAILABLE`.
10. Mark the return inspection `COMPLETED`.
11. Mark the booking `COMPLETED`.

Recommended transaction order:

```text
Validate all inspection data
  -> Validate final payment
  -> Persist damage deduction
  -> Request refund using idempotency key
  -> Persist refund result
  -> Update vehicle state
  -> Complete booking
```

If the refund fails, do not mark the inspection or booking completed. Set the inspection to `REFUND_PENDING` and provide a retry operation.

### 6.6 Retry refund

```http
POST /api/returns/{inspectionId}/retry-refund
```

Rules:

- Only `REFUND_PENDING` or `FAILED` inspections may be retried.
- Reuse the same refund idempotency key.
- Return the original successful transaction if the refund already succeeded.
- Never create a second successful refund for the same inspection.

### 6.7 View return inspection

```http
GET /api/returns/{inspectionId}
GET /api/returns/bookings/{bookingId}
```

The response should include:

```text
bookingId
vehicle details
customer details
return mileage
fuel level
damage status
damage description
damage amount
deposit amount
remaining deposit
refund amount
refund status
final payment status
inspection status
staff details
timestamps
```


### 6.8 Automatic payment reference display

Every successful simulator payment response must include the generated transaction reference. The backend must obtain this value from the persisted `PaymentTransaction` record, not from a value typed by the staff member or customer.

Required payment-verification response fields:

```json
{
  "paymentVerified": true,
  "paymentReference": "SIM-COLLECT-ABC123",
  "paymentPurpose": "DEPOSIT",
  "amount": 50000.00,
  "currency": "LKR",
  "status": "SUCCESS"
}
```

Rules:

- Return `paymentReference` only after the transaction has a persisted `SUCCESS` status.
- Return `null` or an explicit unavailable state for failed, pending, or missing payments.
- Keep the reference immutable after a successful transaction.
- Reuse the same reference in the staff interface, customer booking history, audit logs, and payment reports.
- If `PUT /api/handovers/{id}/verify-payment` remains, it must look up the booking's successful deposit transaction and populate the response automatically.
- Request notes may be accepted, but a client request must never override the generated payment reference.
## 7. Payment Integration

Use the existing `PaymentTransaction` model and `PaymentService`.

### 7.1 Deposit calculation

```text
depositAmount = successful DEPOSIT transactions
existingDamageDeductions = successful DAMAGE_DEDUCTION transactions
remainingDeposit = depositAmount - existingDamageDeductions
```

### 7.2 Damage deduction

When damage is approved:

- Create one successful `DAMAGE_DEDUCTION` transaction.
- Link it to the booking and customer.
- Store the inspection ID in a suitable transaction metadata field or add `referenceId`/`referenceType` fields to `PaymentTransaction`.
- Prevent duplicate deduction using an inspection-specific idempotency key:

```text
damage-deduction-return-{inspectionId}
```

### 7.3 Deposit refund

Refund amount:

```text
refundAmount = depositAmount - totalApprovedDamageDeductions
```

Create a `DEPOSIT_REFUND` transaction with:

- Booking ID.
- Customer ID.
- Amount.
- Currency.
- Simulator reference.
- Status.
- Inspection-specific idempotency key:

```text
refund-return-{inspectionId}
```

A refund of zero should be recorded as `NOT_REQUIRED` or treated as a completed settlement without calling the simulator. Do not issue a negative refund.

## 8. Booking Completion Guard

Update `BookingServiceImpl.makeCompleted(...)` so it rejects completion unless:

- Booking status is `ACTIVE`.
- Successful final rental payment exists.
- A completed return inspection exists.
- Refund status is `SUCCESS`, `NOT_REQUIRED`, or an explicitly approved settlement state.

The existing generic `/api/booking/{id}/complete` endpoint must not bypass return inspection.

Recommended error messages:

```text
Cannot complete booking: return inspection has not been started.
Cannot complete booking: return mileage has not been recorded.
Cannot complete booking: damage assessment has not been finalized.
Cannot complete booking: final rental payment has not been received.
Cannot complete booking: deposit refund is still pending.
```

The booking completion service should be the final authority even if the frontend disables the button.

## 9. Vehicle State Changes

The return workflow is the authority for the final vehicle return transition.

After successful settlement:

- Save the final return odometer.
- Transition the vehicle from `RENTED` to `AVAILABLE`.
- Preserve the return inspection history.
- Do not make the vehicle available when refund processing failed unless the business explicitly allows operational completion with a pending refund.

If the product chooses to allow the vehicle to return to service while the refund is pending, separate operational completion from financial settlement using two statuses. Do not hide a pending refund under `BOOKING COMPLETED`.

## 10. Frontend Implementation

### 10.1 API client

Create:

```text
frontend/src/api/returns.js
```

Functions:

```javascript
startReturnInspection(bookingId)
recordReturnMileage(inspectionId, data)
recordReturnCondition(inspectionId, data)
recordReturnDamage(inspectionId, data)
completeReturnInspection(inspectionId)
retryReturnRefund(inspectionId)
getReturnInspection(inspectionId)
getBookingReturnInspection(bookingId)
```

### 10.2 Staff return screen

Create:

```text
frontend/src/pages/Staff/VehicleReturnPage.jsx
frontend/src/pages/Staff/VehicleReturnPage.css
```

The screen should provide:

- Active booking search.
- Start return inspection action.
- Customer and vehicle details.
- Pickup and return odometer values.
- Fuel and condition fields.
- Damage yes/no control.
- Damage description field.
- Damage amount field visible only when damage is true.
- Deposit amount display.
- Remaining deposit display.
- Refund amount preview.
- Final payment status.
- Complete return and refund button.
- Retry refund button when refund is pending or failed.
- Clear loading, validation, success, and failure states.

#### Staff payment-verification interface UX

The payment-verification section must follow a clear status-first design:

- Show a prominent text status: `Deposit verified`, `Payment pending`, `Payment failed`, or `Payment unavailable`.
- Automatically fetch the payment summary when the handover/return inspection opens or when staff selects a booking.
- Display a successful payment reference in a read-only field or compact copyable row labeled `Payment reference`.
- Never show an empty editable reference input for a successful transaction.
- Display amount, currency, payment purpose, transaction time, and reference together for quick verification.
- Provide a copy button with a tooltip and confirmation such as `Reference copied`.
- Show a loading skeleton or spinner while payment status is loading; do not show a false unpaid state during loading.
- When payment is missing, show the next action: `Customer must pay the deposit before verification can continue`.
- When the simulator fails, show the failure reason and a retry/reload action without clearing saved inspection data.
- Keep `Complete return` and `Complete handover` disabled until the backend confirms the required payment.
- Use text and icons in addition to color so status remains accessible.
- Keep payment summary visually separate from damage assessment so the deposit is not confused with the deduction.

Recommended layout:

```text
Payment verification
------------------------------------------------
Status             [Deposit verified]
Purpose            Deposit
Amount             LKR 50,000.00
Payment reference  SIM-COLLECT-ABC123 [Copy]
Processed at       14 Sep 2026, 10:42 AM
------------------------------------------------
```

Use the same pattern for final payment, damage deduction, and refund references. Long references must wrap or scroll safely on mobile without expanding the layout.

### 10.3 Customer booking page

Update:

```text
frontend/src/pages/Customer/MyBookingsPage.jsx
```

Show for active or completed bookings:

- Return inspection status.
- Damage amount deducted.
- Deposit refunded amount.
- Refund transaction reference.
- Refund pending or failed message.

Customers should not be able to approve their own damage deduction.

### 10.4 Staff handover navigation

Add a return action to the existing staff handover or booking management screen:

```text
Active booking -> Start Vehicle Return
```

Do not present return inspection as a pickup handover action. The UI should distinguish:

- Pickup handover: company gives the vehicle to the customer.
- Return handover: customer returns the vehicle to the company.

## 11. Authorization and Security

- Return inspection creation and finalization require `STAFF`, `MANAGER`, or `ADMIN`.
- Customer identity must be derived from the booking, not accepted as a trusted request field.
- Vehicle identity must be derived from the booking.
- Only authorized staff may enter or approve damage amounts.
- Customers may view their own return and refund status only.
- Payment simulator references and transaction IDs may be displayed, but no sensitive payment credentials may be stored or returned.
- Every refund and deduction must have an authenticated actor and timestamp.
- Use idempotency keys for damage deduction, refund, and retry operations.
- Payment references are server-generated, immutable, and never accepted as trusted client input.

## 12. Failure and Recovery Rules

### Damage amount exceeds deposit

Reject the request and leave the inspection open for correction.

### Final payment is missing

Do not complete the return settlement or booking. Show the final payment action to the customer or authorized staff-assisted payment workflow.

### Refund simulator fails

- Mark refund as `REFUND_PENDING` or `FAILED`.
- Keep inspection open for retry.
- Do not mark booking completed.
- Do not call the simulator repeatedly without idempotency.

### Staff submits the same damage request twice

Return the existing deduction transaction for the same inspection idempotency key.

### Staff submits completion twice

Return the existing completed inspection and refund result. Do not duplicate vehicle transitions or refunds.

### Payment reference unavailable

- Do not mark payment as verified.
- Show `Payment reference unavailable` with the transaction status and a retry/reload action.
- Keep completion actions disabled until a persisted successful transaction and reference are available.

### Customer cancels before return

Follow the existing cancellation policy. If a deposit refund is required for cancellation, use a separate cancellation refund purpose and do not treat it as a vehicle-return refund.

## 13. Test Plan

### Unit tests

- Active booking can start one return inspection.
- Pending, cancelled, or completed bookings cannot start a return inspection.
- Return mileage cannot be less than pickup mileage.
- Missing condition data prevents completion.
- Missing damage decision prevents completion.
- Damage amount is required when damage exists.
- Damage amount must be zero when no damage exists.
- Damage amount cannot exceed the remaining deposit.
- Refund equals deposit minus approved deductions.
- Zero refund does not call the simulator.
- Final payment is required before completion.
- Refund failure produces `REFUND_PENDING`.
- Refund retry is idempotent.
- Duplicate completion does not create duplicate refunds.
- Successful payments display their generated reference automatically in the verification interface.
- Failed or pending payments never display a misleading successful reference.
- Staff cannot edit or replace a generated payment reference.

### Controller tests

- Staff can start and complete a return inspection.
- Manager and admin access follows policy.
- Customer cannot finalize a return inspection.
- Customer can view only their own return information.
- Unauthorized users receive `401` or `403`.
- Invalid damage amount returns `400`.
- Completion before return inspection returns a clear conflict/error response.

### Integration flow

```text
Active booking
  -> Start return inspection
  -> Record return mileage
  -> Record condition
  -> Record no damage
  -> Verify final payment
  -> Refund full deposit
  -> Vehicle becomes AVAILABLE
  -> Booking becomes COMPLETED
```

Damage flow:

```text
Active booking
  -> Start return inspection
  -> Record return mileage
  -> Record damage and amount
  -> Deduct damage from deposit
  -> Refund remaining deposit
  -> Vehicle becomes AVAILABLE
  -> Booking becomes COMPLETED
```

Failure flow:

```text
Refund simulator fails
  -> Inspection becomes REFUND_PENDING
  -> Booking remains incomplete
  -> Staff retries refund
  -> Successful refund
  -> Vehicle and booking are finalized
```

## 14. Implementation Order

1. Create return inspection entity, statuses, DTOs, repository, and service.
2. Add start-inspection, mileage, condition, damage, and read endpoints.
3. Add payment transaction reference fields for return inspection linkage if required.
4. Implement damage deduction validation through `PaymentService`.
5. Implement refund calculation and simulator transfer.
6. Add refund retry and idempotency handling.
7. Add the booking completion guard.
8. Add vehicle odometer and status update after successful settlement.
9. Create the staff vehicle return screen.
10. Add customer refund and deduction status display.
11. Add controller, service, repository, and integration tests.
12. Update README API documentation and run Docker-based verification.

## 15. Acceptance Criteria

The feature is complete when:

- Staff can start a return inspection for an active booking.
- Staff can record final mileage and vehicle condition.
- Staff must explicitly record damage or no damage.
- Damage amount is validated against the remaining deposit.
- The damage deduction is persisted as a payment transaction.
- The remaining deposit is calculated correctly.
- The remaining deposit is transferred through the simulator exactly once.
- A failed refund remains visible and retryable.
- Booking completion is rejected until the return process is complete.
- Final rental payment is required before completion.
- Vehicle mileage and availability are updated only after successful settlement.
- Customers can view deduction and refund results.
- Authorization rules prevent customers from approving damage deductions.
- All monetary calculations use `BigDecimal`.
- Tests cover successful, duplicate, invalid, unauthorized, and failed-refund flows.

## 16. Definition of Done

- Return inspection is a separate, auditable workflow from pickup handover.
- Backend service rules cannot be bypassed by frontend changes.
- Every damage deduction and refund has a transaction reference and idempotency key.
- Booking, vehicle, inspection, and payment changes are transactionally consistent.
- Refund failures can be retried without duplicate transfers.
- The customer and staff interfaces clearly show the required amount, deduction, remaining balance, and refund status.
- Successful deposit, final-payment, deduction, and refund references are automatically displayed as read-only, copyable values.
- The payment-verification UI communicates loading, success, failure, and retry states clearly on desktop and mobile.
- README documentation contains the new return inspection and refund endpoints.
