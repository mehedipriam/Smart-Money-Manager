# Smart Money Manager

A production-ready, full-stack personal finance management and expense tracking web application.

## Tech Stack

**Frontend:** React.js (Vite), Axios, React Router, Recharts
**Backend:** Java 17, Spring Boot, Spring Security, JWT, Spring Data JPA, Hibernate, Maven
**Database:** MySQL
**DevOps:** Docker, Docker Compose

## Project Structure

```
Budget Planner/
├── backend/    Spring Boot REST API (controller / service / repository / entity / dto / security / config)
├── frontend/   React SPA (components / pages / layouts / services / hooks / context / routes)
└── README.md
```

## Development Roadmap

This project is being built phase by phase:

1. **Project Setup** ✅
2. **Full Database Schema Creation** ✅
3. **Authentication** ✅
4. **Account Management** ✅
5. **Category Management** ✅
6. Transaction Management
7. Dashboard
8. Budget Management
9. Savings Goals
10. Bills and Reminders
11. Reports and Analytics
12. Notifications
13. Admin Panel
14. Testing and Security
15. Dockerization
16. Deployment Preparation

## Running Locally (current state — Phase 1)

### Backend

```
cd backend
./mvnw spring-boot:run
```

Runs on `http://localhost:8080`, active profile defaults to `dev` (`SPRING_PROFILES_ACTIVE`).
Requires a local MySQL instance once the schema exists (Phase 2) — connection settings are in
`backend/src/main/resources/application-dev.properties`, all overridable via environment variables
(`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`).

### Frontend

```
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173`. Copy `.env.example` to `.env` to configure `VITE_API_BASE_URL`
(defaults to `http://localhost:8080/api`).

### Database (Phase 2 — complete)

The full schema (all 14 tables) is defined as JPA entities under
`backend/src/main/java/com/smartmoneymanager/backend/entity/`, with a mirrored
reference DDL script at `backend/src/main/resources/db/full_schema.sql` and a
full ER diagram + design notes at `docs/PHASE_2_DATABASE_SCHEMA.md`.

With a MySQL instance reachable using the `DB_*` environment variables above,
starting the backend (`./mvnw spring-boot:run`, `dev` profile) creates every
table automatically via Hibernate (`spring.jpa.hibernate.ddl-auto=update`).
This was verified end-to-end against a real MySQL 8.4 instance — `SHOW TABLES`
returns all 14 expected tables with their foreign keys and indexes in place.

From Phase 3 onward the schema is treated as fixed; further phases only add
repository/service/DTO/mapper/controller code on top of it.

### Authentication (Phase 3 — complete)

JWT-based auth (BCrypt password hashing, stateless access + refresh tokens,
role-based authorization for `ROLE_USER`/`ROLE_ADMIN`), with email verification,
forgot/reset password, change password, and profile management. Endpoints live
under `/api/auth/**` (public) and `/api/users/**` (authenticated); every
response uses a standard `{ success, message, data, errors }` envelope. The
frontend has matching login/register/verify/forgot/reset pages, a protected
profile page, an Axios client that silently refreshes an expired access token,
and toast notifications.

Roles are seeded automatically on startup. In dev, without a real SMTP server,
verification/reset links are logged to the backend console instead of failing
the request — set `MAIL_HOST`/`MAIL_PORT`/etc. (or point at a tool like
MailHog) to receive them as real emails.

### Account Management (Phase 4 — complete)

Create/edit/delete financial accounts (Cash, Bank Account, Mobile Banking,
Credit Card, Savings Account, Custom) and transfer money between two of your
own accounts. Every account and transfer endpoint is ownership-scoped —
`/api/accounts/**` — so one user can never read or modify another user's
accounts (verified with a dedicated cross-user test). Balances only move
through create/transfer, never a direct edit, so they can't drift from
history.

**Known scope limitation, called out deliberately rather than stubbed:**
a transfer currently only moves the two account balances. Once Category
Management (Phase 5) and Transaction Management (Phase 6) exist, it will
also write the two linked transaction rows (EXPENSE on the source account,
INCOME on the destination, tagged "Transfer") described in
`docs/PHASE_2_DATABASE_SCHEMA.md`, so transfers show up in transaction
history and reports.

The frontend adds an Accounts page (account cards, create/edit/delete/transfer
modals) and simple top navigation between Accounts and Profile.

### Category Management (Phase 5 — complete)

16 default income/expense categories (matching the spec's list) are seeded
automatically on startup, visible to every user, and can never be edited or
deleted (`400` if attempted, IDOR-safe `404` for another user's custom
category). Users can create/edit/delete their own custom categories; a
category's type (income/expense) is fixed at creation so it can't drift out
of sync with anything tagged with it later. Deleting a category still in use
(once transactions/budgets/bills reference it, from Phase 6 onward) fails
with a `409` instead of an orphaned foreign key.

The frontend adds a Categories page (expense/income columns, default
categories shown with a badge and no edit/delete controls) and a
"Categories" nav link.

---

Full installation steps, API documentation, and deployment instructions will be
added as their corresponding phases (11, 16) are completed.
