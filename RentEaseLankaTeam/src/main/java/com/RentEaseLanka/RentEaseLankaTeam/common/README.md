# 🛠️ Common Module

## Overview

The **Common** module provides shared utilities and cross-cutting concerns used across all components in the RentEaseLanka backend. It includes global exception handling and standardized error response formatting.

---

## Technologies & Libraries

| Technology | Purpose |
|---|---|
| **Spring Boot 4.1** | `@RestControllerAdvice` for global exception handling |
| **Lombok** | DTO boilerplate reduction |

---

## Components

### `GlobalExceptionHandler`

A `@RestControllerAdvice` class that intercepts exceptions thrown by any controller across the application and maps them to consistent HTTP error responses.

**Handled Exception Types:**
- `IllegalArgumentException` → `400 BAD_REQUEST`
- `IllegalStateException` → `409 CONFLICT`
- `RuntimeException` → `500 INTERNAL_SERVER_ERROR`
- `EntityNotFoundException` → `404 NOT_FOUND`
- `MethodArgumentNotValidException` → `400 BAD_REQUEST` (with field errors)
- Generic `Exception` → `500 INTERNAL_SERVER_ERROR`

### `ErrorResponseDTO`

Standardized error response body returned to clients on all error conditions.

| Field | Type | Description |
|---|---|---|
| `status` | `int` | HTTP status code |
| `error` | `String` | Error type |
| `message` | `String` | Error message |
| `timestamp` | `LocalDateTime` | When the error occurred |

**Example Response:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Vehicle not found with ID: 99",
  "timestamp": "2026-09-23T14:30:00"
}
```

---

## File Structure

```
common/
├── dto/
│   └── ErrorResponseDTO.java          # Standardized error response DTO
└── exception/
    └── GlobalExceptionHandler.java    # Global @RestControllerAdvice handler
```
