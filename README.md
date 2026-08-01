# Event Ticket Platform - Backend Demo

A Spring Boot backend for creating events, selling tickets, generating QR codes, and validating attendees at the gate.

An organizer creates a concert with multiple ticket tiers. Attendees discover published events, purchase a ticket, and receive a QR code. At the venue, staff scan the QR code or enter a ticket ID manually. The backend checks whether the ticket has already been used and records a validation result.

This repo is the server side of that flow. It includes the REST API, JWT-based security with Keycloak, PostgreSQL persistence, QR code generation, role-based access control, and Swagger documentation.

## Table of Contents

- What this demo proves
- How to run it
- The demo flow (step by step)
- Architecture
- The three hard problems and how they're solved
- File-by-file walkthrough
- API reference
- Tests
- What's NOT real (and what would change for production)
- Honest limitations of the concept
- Troubleshooting

## What this demo proves

The system shows these backend workflows working end to end:

1. Organizers can create, update, list, and delete their own events. Other organizers cannot access those private event-management endpoints.
2. Public users can browse only published events without authentication.
3. Authenticated attendees can purchase tickets from available ticket types.
4. Every purchased ticket gets a generated QR code.
5. Staff can validate a ticket by QR code or manual ticket ID.
6. A ticket validates successfully only once. Later validation attempts are recorded as invalid.
7. Ticket inventory is protected during purchase with a pessimistic database lock.

## How to run it

### Prerequisites

- JDK 21 or newer installed and on `PATH`. Check with:

```powershell
java -version
```

- Docker Desktop, for PostgreSQL and Keycloak.

No local Maven install is required. The Maven wrapper is included.

### Start PostgreSQL and Keycloak

From the project root:

```powershell
docker compose up -d
```

This starts:

| Service | URL / Port | Purpose |
|---|---:|---|
| PostgreSQL | `localhost:5432` | Main application database |
| Keycloak | `http://localhost:9090` | JWT issuer and user/role management |
| Adminer | `http://localhost:8888` | Web UI for inspecting PostgreSQL |

PostgreSQL credentials:

```text
database: postgres
username: postgres
password: changemeinprod!
```

### Configure Keycloak

Open:

```text
http://localhost:9090
```

Log in with:

```text
username: admin
password: admin
```

Create:

- Realm: `event-ticket-platform`
- Client: `event-ticket-platform-app`
- Users for organizer, attendee, and staff demos
- Realm roles:
  - `ROLE_ORGANIZER`
  - `ROLE_USER`
  - `ROLE_STAFF`

Assign roles to users as needed. The backend reads roles from the JWT `realm_access.roles` claim.

### Run the backend on Windows

```powershell
.\mvnw.cmd spring-boot:run
```

The backend starts on:

```text
http://localhost:8081
```

### Run the backend on Mac/Linux

```bash
./mvnw spring-boot:run
```

### Open Swagger

Once the app has started, open:

```text
http://localhost:8081/swagger-ui/index.html
```

Swagger is public, but most API calls require a Bearer token from Keycloak.

### Stop the server

Press `Ctrl+C` in the terminal running Spring Boot.

Stop infrastructure:

```powershell
docker compose down
```

### Run the tests

```powershell
.\mvnw.cmd test
```

The current test suite contains a Spring Boot context-load test.

## The demo flow (step by step)

### Step 1 - Create users and roles in Keycloak

Create at least three users:

- Organizer user with `ROLE_ORGANIZER`
- Attendee user with `ROLE_USER`
- Staff user with `ROLE_STAFF`

What actually happens on the backend:

- Spring Security validates JWTs using the configured issuer:

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/event-ticket-platform
```

- `JwtAuthenticationConverter` extracts roles from `realm_access.roles`.
- Routes are allowed or denied based on those Spring Security authorities.

### Step 2 - Organizer creates an event

Call:

```http
POST /api/v1/events
Authorization: Bearer <organizer-token>
Content-Type: application/json
```

Example body:

```json
{
  "name": "Indie Night 2026",
  "start": "2026-09-10T18:00:00",
  "end": "2026-09-10T22:00:00",
  "venue": "City Arena",
  "salesStart": "2026-08-01T09:00:00",
  "salesEnd": "2026-09-09T23:59:00",
  "status": "PUBLISHED",
  "ticketTypes": [
    {
      "name": "General",
      "price": 499.0,
      "description": "Standard entry",
      "totalAvailable": 100
    },
    {
      "name": "VIP",
      "price": 1499.0,
      "description": "Front row entry",
      "totalAvailable": 20
    }
  ]
}
```

The backend:

- Reads the organizer ID from the JWT subject.
- Loads the organizer user from the database.
- Creates an `Event`.
- Creates child `TicketType` rows.
- Saves the full aggregate in one transaction.

### Step 3 - Attendees browse published events

Call:

```http
GET /api/v1/published-events?page=0&size=10
```

This endpoint is public. It returns only events with status `PUBLISHED`.

Search is also supported:

```http
GET /api/v1/published-events?q=arena&page=0&size=10
```

The backend uses a PostgreSQL full-text search query across event name and venue.

### Step 4 - Attendee purchases a ticket

Call:

```http
POST /api/v1/events/{eventId}/ticket-types/{ticketTypeId}/tickets
Authorization: Bearer <attendee-token>
```

The backend:

- Reads the purchaser ID from the JWT subject.
- Loads the selected ticket type with a `PESSIMISTIC_WRITE` lock.
- Counts already purchased tickets for that ticket type.
- Rejects the purchase if capacity is exhausted.
- Creates a `Ticket` with status `PURCHASED`.
- Generates a QR code for the ticket.
- Stores the QR code as a base64 PNG in the `qr_codes` table.

### Step 5 - Attendee downloads ticket QR

Call:

```http
GET /api/v1/tickets/{ticketId}/qr-codes
Authorization: Bearer <attendee-token>
```

Response:

```text
Content-Type: image/png
```

Only the purchaser can retrieve their QR image.

### Step 6 - Staff validates the ticket

For QR scan validation:

```http
POST /api/v1/ticket-validations
Authorization: Bearer <staff-token>
Content-Type: application/json
```

```json
{
  "id": "qr-code-uuid-here",
  "method": "QR_SCAN"
}
```

For manual validation:

```json
{
  "id": "ticket-uuid-here",
  "method": "MANUAL"
}
```

First successful validation returns:

```json
{
  "ticketId": "ticket-uuid-here",
  "status": "VALID"
}
```

If the same ticket is validated again, the service records the attempt and returns:

```json
{
  "ticketId": "ticket-uuid-here",
  "status": "INVALID"
}
```

## Architecture

```text
+-----------------------------------------------------------------------+
|                              KEYCLOAK                                 |
|  Users, roles, JWTs                                                   |
|  Roles: ROLE_ORGANIZER, ROLE_USER, ROLE_STAFF                         |
+---------------------------------------+-------------------------------+
                                        |
                                        | Bearer JWT
                                        v
+-----------------------------------------------------------------------+
|                       SPRING BOOT BACKEND                             |
|                                                                       |
|  SecurityConfig                                                       |
|    - Validates JWT                                                    |
|    - Enforces role-based route access                                 |
|                                                                       |
|  Controllers                                                          |
|    - EventController                                                  |
|    - PublishedEventController                                         |
|    - TicketTypeController                                             |
|    - TicketController                                                 |
|    - TicketValidationController                                       |
|                                                                       |
|  Services                                                             |
|    - EventServiceImpl: organizer event management + public search     |
|    - TicketTypeServiceImpl: ticket purchase + inventory lock          |
|    - QrCodeServiceImpl: QR PNG generation                             |
|    - TicketValidationServiceImpl: gate validation rules                |
|                                                                       |
|  Persistence                                                          |
|    - Spring Data JPA repositories                                     |
|    - PostgreSQL                                                       |
+---------------------------------------+-------------------------------+
                                        |
                                        | SQL
                                        v
+-----------------------------------------------------------------------+
|                              POSTGRES                                 |
|  users, events, ticket_types, tickets, qr_codes, ticket_validations    |
+-----------------------------------------------------------------------+
```

## The three hard problems and how they're solved

### Problem 1: Role-based access

Organizers should manage events, attendees should buy tickets, staff should validate tickets, and public users should only browse published events.

Solution: Keycloak JWTs plus Spring Security.

`SecurityConfig` configures the app as an OAuth2 resource server. Each request with a Bearer token is validated against the Keycloak realm issuer. `JwtAuthenticationConverter` reads the token's `realm_access.roles` claim and converts roles into Spring Security authorities.

Important route rules:

- `GET /api/v1/published-events/**` is public.
- `/api/v1/events` requires `ROLE_ORGANIZER`.
- `/api/v1/ticket-validations` requires `ROLE_STAFF`.
- All other routes require authentication.

### Problem 2: Overselling tickets

Two people can click purchase at nearly the same time. If both requests read the same remaining capacity before either write completes, the system can sell more tickets than exist.

Solution: database-level pessimistic locking during purchase.

`TicketTypeRepository.findByIdWithLock()` uses:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

The purchase flow locks the selected `TicketType`, counts purchased tickets, compares the count with `totalAvailable`, and only then creates the ticket. Concurrent purchases for the same ticket type line up behind the lock instead of racing through the availability check.

### Problem 3: Fake or reused entry attempts

A gate scanner must distinguish a valid ticket from an unknown QR code and must reject a second attempt to enter with the same ticket.

Solution: QR IDs are server-generated, stored, and checked against validation history.

`QrCodeServiceImpl` generates a random UUID for each ticket QR code and stores the QR image in the database. During QR validation, `TicketValidationServiceImpl` loads only active QR codes. It then checks existing validations for the ticket:

- No previous `VALID` validation means the new validation is `VALID`.
- If a previous `VALID` validation exists, the new validation is `INVALID`.

Every attempt is recorded in `ticket_validations`, which gives staff an audit trail of accepted and rejected entry attempts.

## File-by-file walkthrough

```text
tickets/
|-- pom.xml                                      Maven build, Spring Boot 4.0.6, Java 21
|-- mvnw, mvnw.cmd                               Maven wrapper
|-- docker-compose.yml                           PostgreSQL, Keycloak, Adminer
|-- README.md                                    this file
`-- src/main/
    |-- resources/
    |   |-- application.properties               app port, Postgres config, JWT issuer
    |   `-- META-INF/orm.xml                     JPA ORM metadata
    `-- java/com/project/tickets/
        |-- TicketsApplication.java              Spring Boot main class
        |
        |-- config/
        |   |-- SecurityConfig.java              route authorization, JWT resource server
        |   |-- JwtAuthenticationConverter.java  converts Keycloak roles to authorities
        |   |-- JpaConfiguration.java            JPA configuration
        |   `-- QrCodeConfig.java                ZXing QR writer bean
        |
        |-- controller/
        |   |-- EventController.java             organizer event CRUD
        |   |-- PublishedEventController.java    public published event listing/search/details
        |   |-- TicketTypeController.java        ticket purchase endpoint
        |   |-- TicketController.java            attendee tickets and QR download
        |   |-- TicketValidationController.java  staff validation endpoint
        |   `-- GlobalExceptionHandler.java      API error responses
        |
        |-- domain/
        |   |-- entities/                        JPA entities and enums
        |   |-- dtos/                            request/response DTOs
        |   |-- CreateEventRequest.java          service-layer create event model
        |   |-- UpdateEventRequest.java          service-layer update event model
        |   |-- CreateTicketTypeRequest.java     service-layer ticket type create model
        |   `-- UpdateTicketTypeRequest.java     service-layer ticket type update model
        |
        |-- filters/
        |   `-- UserProvisioningFilter.java      creates local users from JWT claims
        |
        |-- mappers/
        |   |-- EventMapper.java                 MapStruct event mappings
        |   |-- TicketMapper.java                MapStruct ticket mappings
        |   `-- TicketValidationMapper.java      MapStruct validation mappings
        |
        |-- repositories/
        |   |-- EventRepository.java             organizer queries + published search
        |   |-- TicketTypeRepository.java        ticket type lookup with pessimistic lock
        |   |-- TicketRepository.java            ticket lookup/count queries
        |   |-- QrCodeRepository.java            QR lookup queries
        |   |-- TicketValidationRepository.java  validation persistence
        |   `-- UserRepository.java              local user persistence
        |
        |-- services/
        |   |-- EventService.java
        |   |-- TicketTypeService.java
        |   |-- TicketService.java
        |   |-- QrCodeService.java
        |   |-- TicketValidationService.java
        |   `-- impl/
        |       |-- EventServiceImpl.java
        |       |-- TicketTypeServiceImpl.java
        |       |-- TicketServiceImpl.java
        |       |-- QrCodeServiceImpl.java
        |       `-- TicketValidationServiceImpl.java
        |
        |-- exceptions/                          custom domain exceptions
        `-- util/
            `-- JwtUtil.java                     JWT helper methods

src/test/java/com/project/tickets/
`-- TicketsApplicationTests.java                 Spring context-load test
```

## API reference

| Method | Path | Auth | What it does |
|---|---|---|---|
| `GET` | `/swagger-ui/index.html` | Public | Swagger UI |
| `GET` | `/v3/api-docs` | Public | OpenAPI JSON |
| `POST` | `/api/v1/events` | `ROLE_ORGANIZER` | Create an event with ticket types |
| `GET` | `/api/v1/events` | Authenticated organizer | List current organizer's events |
| `GET` | `/api/v1/events/{eventId}` | Authenticated organizer | Get one organizer-owned event |
| `PUT` | `/api/v1/events/{eventId}` | Authenticated organizer | Update event fields and ticket types |
| `DELETE` | `/api/v1/events/{eventId}` | Authenticated organizer | Delete an organizer-owned event |
| `GET` | `/api/v1/published-events` | Public | List or search published events |
| `GET` | `/api/v1/published-events/{eventId}` | Public | Get public event details |
| `POST` | `/api/v1/events/{eventId}/ticket-types/{ticketTypeId}/tickets` | Authenticated | Purchase a ticket |
| `GET` | `/api/v1/tickets` | Authenticated | List current user's tickets |
| `GET` | `/api/v1/tickets/{ticketId}` | Authenticated | Get current user's ticket details |
| `GET` | `/api/v1/tickets/{ticketId}/qr-codes` | Authenticated | Download ticket QR PNG |
| `POST` | `/api/v1/ticket-validations` | `ROLE_STAFF` | Validate ticket by QR code or manual ID |

### Request format for ticket validation

```http
POST /api/v1/ticket-validations
Content-Type: application/json
Authorization: Bearer <staff-token>
```

QR scan:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "method": "QR_SCAN"
}
```

Manual:

```json
{
  "id": "8c1d28e1-36f0-4c72-8f45-7a0f8e7d69f8",
  "method": "MANUAL"
}
```

Response:

```json
{
  "ticketId": "8c1d28e1-36f0-4c72-8f45-7a0f8e7d69f8",
  "status": "VALID"
}
```

Possible validation statuses:

| Status | Meaning |
|---|---|
| `VALID` | First accepted validation for this ticket |
| `INVALID` | Ticket was already validated before |
| `EXPIRED` | Enum exists for future expiry logic |

## Tests

Run all tests:

```powershell
.\mvnw.cmd test
```

Current included test:

- `TicketsApplicationTests.contextLoads` verifies that the Spring application context starts.

Useful tests to add next:

- Purchase cannot exceed `totalAvailable` when multiple requests run concurrently.
- First ticket validation returns `VALID`, second validation returns `INVALID`.
- Public endpoints work without JWT.
- Organizer cannot read or update another organizer's event.
- QR code download is restricted to the ticket purchaser.

## What's NOT real (and what would change for production)

This is a teaching/demo backend. To make it production-grade, these areas need hardening:

| What's in the demo | What it would be in production |
|---|---|
| Docker Compose PostgreSQL | Managed PostgreSQL with backups, migrations, monitoring |
| `spring.jpa.hibernate.ddl-auto=update` | Flyway or Liquibase migrations |
| Keycloak dev mode | Managed Keycloak/IdP with realm export under version control |
| Hardcoded DB password | Secrets manager or environment variables |
| No payment gateway | Real payment provider integration before ticket issuance |
| QR stored as base64 text | Object storage or compact binary storage, depending on needs |
| Simple validation history check | Stronger constraints and event-specific gate/staff authorization |
| Broad authenticated access for some routes | More explicit role and ownership checks |
| Console SQL logging enabled | Structured production logs with SQL logging disabled by default |
| Minimal automated tests | Unit, integration, security, and concurrency test coverage |

The main backend shape is useful for a portfolio project: REST controllers, service layer, DTO mapping, JPA persistence, Keycloak JWT security, PostgreSQL search, QR generation, and a realistic validation workflow.

## Honest limitations of the concept

The backend does not process real payments. A ticket purchase creates a ticket immediately after capacity is checked. In a real system, ticket creation should happen only after payment authorization or payment capture succeeds.

The `eventId` in the purchase URL is not used by the service when looking up the `ticketTypeId`. In production, the backend should verify that the ticket type belongs to the event in the path.

User provisioning is intended to create local users from JWT claims. Review `UserProvisioningFilter` before relying on it, because the current implementation builds a `User` object when missing and should persist it with `userRepository.save(user)`.

Validation prevents repeated entry for the same ticket, but it does not currently check whether the event is happening now, whether the staff member belongs to that event, or whether the ticket is cancelled.

Published event search uses PostgreSQL full-text search. It is good for a demo, but production search may need ranking, typo tolerance, filtering by date/location, and pagination tuning.

## Troubleshooting

### `java: command not found`

Install JDK 21 or newer and make sure it is on `PATH`.

On Windows, one option is:

```powershell
winget install EclipseAdoptium.Temurin.21.JDK
```

### `mvnw.cmd : The term 'mvnw.cmd' is not recognized`

In PowerShell, run it with `.\`:

```powershell
.\mvnw.cmd spring-boot:run
```

### Backend starts on the wrong port

This project is configured for:

```properties
server.port=8081
```

Open:

```text
http://localhost:8081/swagger-ui/index.html
```

### PostgreSQL connection refused

Start Docker services:

```powershell
docker compose up -d
```

Then confirm PostgreSQL is listening on `localhost:5432`.

### Keycloak issuer errors

Make sure the realm exists:

```text
event-ticket-platform
```

And make sure this URL opens in your browser:

```text
http://localhost:9090/realms/event-ticket-platform/.well-known/openid-configuration
```

### 403 Forbidden on protected routes

Check that the Keycloak user has the correct realm role:

- Event creation needs `ROLE_ORGANIZER`.
- Ticket validation needs `ROLE_STAFF`.

The backend expects role names to already include the `ROLE_` prefix.

### User not found while creating events or purchasing tickets

The service layer expects the JWT subject UUID to already exist in the local `users` table. If automatic provisioning is not persisting users in your build, insert the user row manually or update `UserProvisioningFilter` to save the new `User`.
