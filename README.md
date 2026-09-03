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

**Dashboard**
- Total balance, income, expenses, and monthly savings, each with a period-over-period % change
- Date range filter: Today, This Week, This Month, Last Month, This Year, or a custom range
- Spending-by-category donut chart and an income/expense/savings cash-flow chart (daily or monthly buckets depending on the range)
- Recent transactions
- Full app shell: dark sidebar, light/dark mode toggle (persisted per browser), a live in-app notification bell

**Budgets**
- Monthly, per-category budgets (expense categories only) with used/remaining amounts and usage %
- Visual alerts at 80% used and over-budget, backed by a real notification the moment a transaction crosses either threshold
- Dashboard's Budget Overview shows live progress for the current month

**Savings Goals**
- Create goals with a target amount, optional target date, and description
- "Add money" deposits, kept as an auditable contribution history per goal
- Progress bar with percentage and remaining amount; auto-completes when the target is reached
- Filter by status: Active, Completed, Cancelled

**Bills & Reminders**
- One-time or recurring bills (daily/weekly/monthly/yearly) with an optional expense category
- PENDING bills automatically flip to OVERDUE once their due date passes
- Mark as paid — recurring bills auto-generate their next occurrence when paid
- Upcoming (unpaid) bills shown on the Dashboard with a one-click "mark paid"

**Reports & Analytics**
- Monthly/yearly/custom financial report: total income, total expenses, net savings, savings rate, highest expense category, average monthly expense — each vs. the equivalent prior period
- Expense and income category breakdowns (pie chart), an income-vs-expense chart for the selected period, and a fixed trailing-6-month cash flow + savings trend
- Export the current report as CSV (summary + full transaction list) or PDF (formatted summary + category tables)

**Notifications**
- In-app bell with a live unread badge (polled every 30s), mark-as-read, mark-all-as-read, and delete
- Raised automatically: budget warning (80% used) / exceeded, on the transaction that crosses the threshold; goal completed, when a contribution or edit reaches the target; recurring transaction added, each time the scheduler generates one; bill due reminder, 3 days before and on the due date

Every resource is scoped to the authenticated user — one user can never read or modify another user's data.

**Planned:** an admin panel and Docker Compose for one-command startup — each has a placeholder page in the sidebar already. See [Roadmap](#roadmap).

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
| `BILL_REMINDERS_CRON` | Cron schedule for sending bill due-date reminder notifications | `0 10 0 * * *` (daily at 00:10) |
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

**Dashboard** — `/api/dashboard` *(authenticated)*
`GET /?range=TODAY|THIS_WEEK|THIS_MONTH|LAST_MONTH|THIS_YEAR|CUSTOM&startDate=&endDate=` — `startDate`/`endDate` required only when `range=CUSTOM`.

**Budgets** — `/api/budgets` *(authenticated)*
`GET /?month=&year=` (both default to the current month) · `POST /` · `PUT /{id}` · `DELETE /{id}`

**Goals** — `/api/goals` *(authenticated)*
`GET /` (optional `?status=ACTIVE|COMPLETED|CANCELLED`) · `GET /{id}` · `POST /` · `PUT /{id}` · `DELETE /{id}` · `POST /{id}/contributions` · `GET /{id}/contributions`

**Bills** — `/api/bills` *(authenticated)*
`GET /` (optional `?status=PENDING|OVERDUE|PAID`) · `GET /upcoming?limit=` · `GET /{id}` · `POST /` · `PUT /{id}` · `DELETE /{id}` · `POST /{id}/pay`

**Reports** — `/api/reports` *(authenticated)*
`GET /summary?range=TODAY|THIS_WEEK|THIS_MONTH|LAST_MONTH|THIS_YEAR|CUSTOM&startDate=&endDate=` · `GET /export/csv` (same params, returns a CSV file) · `GET /export/pdf` (same params, returns a PDF file)

**Notifications** — `/api/notifications` *(authenticated)*
`GET /` (optional `?unreadOnly=true`) · `GET /unread-count` · `PUT /{id}/read` · `PUT /read-all` · `DELETE /{id}`

## Roadmap

- [x] Project setup
- [x] Database schema (all 14 tables)
- [x] Authentication
- [x] Account management
- [x] Category management
- [x] Transaction management
- [x] Dashboard
- [x] Budget management
- [x] Savings goals
- [x] Bills and reminders
- [x] Reports and analytics
- [x] Notifications
- [ ] Admin panel
- [ ] Testing and security hardening
- [ ] Dockerization
- [ ] Deployment preparation
