# Smart Money Manager

A full-stack personal finance and expense tracking web application — accounts, categories, transactions, budgets, savings goals, bills, and reports, built with a Spring Boot REST API and a React frontend.

## Features

**Authentication**
- Register, log in, log out
- Email verification, forgot/reset password, change password
- JWT access + refresh tokens, role-based authorization (`ROLE_USER` / `ROLE_ADMIN`)
- Profile view and update

**Accounts**
- Multiple account types: Cash, Bank Account, Mobile Banking, Credit Card, Savings Account, Custom
- Create, edit, delete accounts
- Transfer money between two of your own accounts

**Categories**
- 16 default income/expense categories, seeded automatically, read-only
- Create, edit, delete your own custom categories

**Transactions**
- Add, edit, delete income/expense transactions; every change keeps the account balance in sync
- Search, filter (date range, category, type, account, amount range), sort, and paginate
- Recurring transactions (daily/weekly/monthly/yearly), generated automatically on schedule — with catch-up if the app was offline when one or more occurrences were due
- Account-to-account transfers are recorded as a real linked pair of transactions (tagged "Transfer"), not just a balance move

Every resource is scoped to the authenticated user — one user can never read or modify another user's data.

**Planned:** a dashboard with charts, budgets, savings goals, bills & reminders, reports, notifications, an admin panel, and Docker Compose for one-command startup. See [Roadmap](#roadmap).

## Tech Stack

**Frontend:** React (Vite), Axios, React Router, Recharts
**Backend:** Java 17, Spring Boot, Spring Security, JWT, Spring Data JPA, Hibernate, Maven
**Database:** MySQL
**DevOps:** Docker, Docker Compose (planned)

## Project Structure

```
Smart Money Manager/
├── backend/    Spring Boot REST API (controller / service / repository / entity / dto / mapper / security / config / exception)
├── frontend/   React SPA (components / pages / layouts / services / hooks / context / routes / utils)
├── docs/       Database schema and design notes
└── README.md
```

## Prerequisites

- Java 17+
- Node.js 18+
- MySQL 8+ (a local instance, or run one in Docker)

## Getting Started

### 1. Database

Create a MySQL database (defaults to `smart_money_manager`); no manual schema setup needed — Hibernate creates all tables on first run in the `dev` profile. See [`docs/PHASE_2_DATABASE_SCHEMA.md`](docs/PHASE_2_DATABASE_SCHEMA.md) for the full ER diagram and design notes.

### 2. Backend

```
cd backend
./mvnw spring-boot:run
```

Runs on `http://localhost:8080`. Active profile defaults to `dev` (`SPRING_PROFILES_ACTIVE`); connection settings are in `backend/src/main/resources/application-dev.properties`, all overridable via environment variables (see below). A dev-only JWT secret is preconfigured so it runs out of the box — override it via `JWT_SECRET` for anything beyond local development.

### 3. Frontend

```
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173`. Copy `.env.example` to `.env` to configure `VITE_API_BASE_URL` (defaults to `http://localhost:8080/api`).

### Email in development

Without a real SMTP server configured, verification and password-reset emails aren't actually delivered — the links are logged to the backend console instead, so registration/reset flows still work end-to-end locally. Point `MAIL_HOST`/`MAIL_PORT` at a tool like [MailHog](https://github.com/mailhog/MailHog) to receive them as real emails.

## Environment Variables

| Variable | Description | Default |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Backend profile (`dev` / `prod`) | `dev` |
| `SERVER_PORT` | Backend port | `8080` |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | MySQL connection | `localhost` / `3306` / `smart_money_manager` |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL credentials | `root` / `root` (dev only) |
| `JWT_SECRET` | JWT signing key | dev-only fallback; **required** in `prod` |
| `JWT_EXPIRATION_MS` | Access token lifetime (ms) | `86400000` (24h) |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh token lifetime (ms) | `604800000` (7d) |
| `FRONTEND_URL` | Used for CORS and links in emails | `http://localhost:5173` |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP config | unset (links are logged instead) |
| `RECURRING_TRANSACTIONS_CRON` | Cron schedule for generating due recurring transactions | `0 5 0 * * *` (daily at 00:05) |
| `VITE_API_BASE_URL` (frontend `.env`) | Backend API base URL | `http://localhost:8080/api` |

## API Reference

All responses use a standard envelope: `{ success, message, data, errors, timestamp }`.

**Auth** — `/api/auth`
`POST /register` · `POST /login` · `POST /logout` · `GET /verify-email` · `POST /resend-verification` · `POST /forgot-password` · `POST /reset-password` · `POST /refresh`

**Users** — `/api/users` *(authenticated)*
`GET /me` · `PUT /me` · `PUT /me/password`

**Accounts** — `/api/accounts` *(authenticated)*
`GET /` · `GET /{id}` · `POST /` · `PUT /{id}` · `DELETE /{id}` · `POST /transfer`

**Categories** — `/api/categories` *(authenticated)*
`GET /` (optional `?type=INCOME|EXPENSE`) · `POST /` · `PUT /{id}` · `DELETE /{id}`

**Transactions** — `/api/transactions` *(authenticated)*
`GET /` (paginated; `?type=&accountId=&categoryId=&dateFrom=&dateTo=&amountFrom=&amountTo=&search=&sortBy=&sortDir=&page=&size=`) · `GET /{id}` · `POST /` · `PUT /{id}` · `DELETE /{id}`

**Recurring transactions** — `/api/recurring-transactions` *(authenticated)*
`GET /` · `POST /` · `PUT /{id}` · `DELETE /{id}`

## Roadmap

- [x] Project setup
- [x] Database schema (all 14 tables)
- [x] Authentication
- [x] Account management
- [x] Category management
- [x] Transaction management
- [ ] Dashboard
- [ ] Budget management
- [ ] Savings goals
- [ ] Bills and reminders
- [ ] Reports and analytics
- [ ] Notifications
- [ ] Admin panel
- [ ] Testing and security hardening
- [ ] Dockerization
- [ ] Deployment preparation
