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

**Admin Panel** *(`ROLE_ADMIN` only)*
- System statistics: total/active/disabled/verified users, new users this month, and total counts of transactions, accounts, budgets, goals, and bills
- Searchable, paginated user list (by name/email, filterable by active/disabled); view a single user's profile/account status
- Enable/disable a user account (blocks/restores login); an admin cannot disable their own account
- Admins never see another user's individual accounts, transactions, budgets, goals, or bills — only profile and account-status fields

Every resource is scoped to the authenticated user — one user can never read or modify another user's data.

## Tech Stack

**Frontend:** React (Vite), Axios, React Router, Recharts
**Backend:** Java 17, Spring Boot, Spring Security, JWT, Spring Data JPA, Hibernate, Maven
**Database:** MySQL
**DevOps:** Docker, Docker Compose

## Project Structure

```
Smart Money Manager/
├── backend/                     Spring Boot REST API (controller / service / repository / entity / dto / mapper / security / config / exception)
├── frontend/                    React SPA (components / pages / layouts / services / hooks / context / routes / utils)
├── docs/                        Database schema and design notes
├── docker-compose.yml           Base stack: mysql + backend + frontend (see "Docker")
├── docker-compose.prod.yml      Production overlay: hardened ports + Caddy/HTTPS (see "Deployment")
├── Caddyfile                    Reverse proxy + automatic HTTPS config, used only by the prod overlay
├── .env.example                 Template for the root .env that both compose files read
└── README.md
```

## Prerequisites

Either:
- Docker + Docker Compose (the whole stack in one command — see [Docker](#docker) below), **or**
- Java 17+, Node.js 18+, and MySQL 8+ for running each service yourself (see [Getting Started](#getting-started))

## Docker

Runs MySQL, the backend, and the frontend together with one command — no local Java/Node/MySQL install needed.

```
cp .env.example .env    # then edit JWT_SECRET, DB passwords, etc.
docker compose up --build
```

- Frontend: `http://localhost:3000` (or your `FRONTEND_PORT`)
- Backend API: `http://localhost:8080/api` (or your `BACKEND_PORT`)
- MySQL: `localhost:3306` (or your `MYSQL_PORT`), for connecting a client like MySQL Workbench

The frontend's nginx container reverse-proxies `/api/*` to the backend container, so the browser only ever talks to one origin — CORS never comes into play for normal use. `backend` waits for MySQL's healthcheck (not just "container started") before connecting, and `frontend` waits for `backend` to start.

`SPRING_PROFILES_ACTIVE` defaults to `dev` in `.env.example`, which auto-creates the schema (`ddl-auto=update`) on the fresh database Compose just created — matching a true first run, no manual schema setup. Switch it to `prod` (`ddl-auto=validate`) only once that schema already exists and has been verified, per [Phase 2's schema doc](docs/PHASE_2_DATABASE_SCHEMA.md); Compose itself never runs migrations, it just injects environment variables into whichever profile is active — see `backend/src/main/resources/application-dev.properties` / `application-prod.properties`.

**Commands:**

| Command | Effect |
|---|---|
| `docker compose up --build` | Build images (if needed) and start all three services, attached |
| `docker compose up --build -d` | Same, detached (background) |
| `docker compose down` | Stop and remove the containers (the `mysql_data` volume — and your data — survives) |
| `docker compose down -v` | Stop and remove containers **and** the MySQL volume — full reset, all data lost |
| `docker compose logs -f` | Follow logs from all three services |
| `docker compose logs -f backend` | Follow logs from just one service (`backend` / `frontend` / `mysql`) |
| `docker compose up --build <service>` | Rebuild and restart just one service |
| `docker compose ps` | Show each service's status, including healthcheck state |

## Deployment

Builds on the [Docker](#docker) setup above via a production overlay, `docker-compose.prod.yml`, applied together with the base file rather than replacing it — it never introduces a separate, non-Docker deployment path.

### 1. Server prerequisites

- A server (VPS or similar) with Docker + Docker Compose installed, and ports 80/443 open.
- A domain's DNS A record pointed at the server's IP — needed for Caddy (the reverse proxy the overlay adds) to obtain a real Let's Encrypt certificate. Testing the overlay locally instead? Set `DOMAIN=localhost` and Caddy issues itself a locally-trusted certificate instead — no real DNS needed.

### 2. Configure

```
git clone <repo-url>
cd "Smart Money Manager"
cp .env.example .env
```

Edit `.env` with real values:
- `JWT_SECRET` — a long random value, e.g. `openssl rand -hex 64`
- `MYSQL_ROOT_PASSWORD` / `MYSQL_PASSWORD` — strong, unique passwords
- `DOMAIN` — your domain, e.g. `smartmoneymanager.example.com`
- `FRONTEND_URL` — `https://` + that same domain (used for CORS and the links inside verification/reset emails)
- `MAIL_*` — real SMTP credentials, so those emails actually get delivered
- Leave `SPRING_PROFILES_ACTIVE=dev` for now — see the next step

### 3. First boot: create and verify the schema, then lock it

```
docker compose -f docker-compose.yml -f docker-compose.prod.yml up --build -d
docker compose exec mysql mysql -u root -p"$MYSQL_ROOT_PASSWORD" smart_money_manager -e "SHOW TABLES;"
```

The `dev` profile's `ddl-auto=update` creates all 14 tables against the fresh database on this first run. Confirm all 14 are listed (see [`docs/PHASE_2_DATABASE_SCHEMA.md`](docs/PHASE_2_DATABASE_SCHEMA.md)), then switch `SPRING_PROFILES_ACTIVE=prod` in `.env` and restart just the backend:

```
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d backend
```

`application-prod.properties` uses `ddl-auto=validate` — from now on the backend refuses to start if the schema doesn't match, rather than silently altering it.

### 4. Verify

Visit `https://<DOMAIN>` — Caddy should present a valid certificate within seconds. `docker compose -f docker-compose.yml -f docker-compose.prod.yml ps` should show all four services (`mysql`, `backend`, `frontend`, `caddy`) up, with `mysql`/`backend`/`frontend` healthy.

### What the overlay changes

| | Base (`docker-compose.yml`) | + `docker-compose.prod.yml` |
|---|---|---|
| Internet-facing | `mysql`, `backend`, `frontend` each publish a port | only `caddy` (80/443) |
| `mysql` / `backend` | reachable at `localhost:<port>` for local tools | internal-only, over `smm-network` |
| HTTPS | none | automatic, via Caddy + Let's Encrypt |
| Restart policy | `unless-stopped` | `always` |

### Updating

```
git pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml up --build -d
```

### Backups

```
docker compose exec mysql mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" smart_money_manager > backup-$(date +%F).sql
```

Restore with `docker compose exec -T mysql mysql -u root -p"$MYSQL_ROOT_PASSWORD" smart_money_manager < backup-2026-01-01.sql`. The `mysql_data` volume itself also persists across restarts and redeploys on its own — only `docker compose down -v` removes it.

### Deploying frontend and backend separately

The two are fully decoupled — Docker Compose is the recommended path here, not the only possible one. The backend is a single self-contained container (or plain jar: `./mvnw clean package` → `backend/target/*.jar`) deployable anywhere that can reach a MySQL instance and has the [environment variables](#environment-variables) below set. The frontend is a static Vite build (`npm run build` → `frontend/dist/`) deployable to any static host or CDN, with `VITE_API_BASE_URL` pointed at wherever the backend ends up — add that frontend origin to the backend's `FRONTEND_URL` if the two aren't served from the same origin, so CORS still allows it.

### Security checklist before going live

- [ ] `JWT_SECRET`, `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD` are strong, unique, and not left as the `.env.example` placeholders
- [ ] `.env` is never committed (already `.gitignore`d) or shared outside the deployment
- [ ] `SPRING_PROFILES_ACTIVE=prod` once the schema is verified — never run `dev`'s auto-schema-update against real data
- [ ] Running with **both** compose files (`-f docker-compose.yml -f docker-compose.prod.yml`) — the base file alone still publishes `mysql`/`backend` ports directly to the host
- [ ] Real SMTP credentials are set, or verification/reset links only ever reach the backend's logs, not real users

## Getting Started

The manual, no-Docker path — run this instead of (not in addition to) the [Docker](#docker) section above.

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

### Backend tests

```
cd backend
./mvnw test
```

Runs against an in-memory H2 database (`test` profile, `src/test/resources/application-test.properties`) — no real MySQL or SMTP server needed. Covers:
- **Authentication boundary**: weak-password/duplicate-email rejection, login blocked before email verification or on a wrong password, protected routes reject a missing/garbage/expired-by-disablement JWT.
- **Ownership (IDOR)**: a second authenticated user gets 404 — never the data, never a 403 that would confirm the id exists — when reading, updating, or deleting another user's account/transaction.
- **Admin authorization**: `/api/admin/**` rejects a plain user (403) and an anonymous request (401); an admin's user-list response never carries a financial field; an admin can't disable their own account; disabling a user invalidates their existing token on the very next request.

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
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Login for a seeded `ROLE_ADMIN` account, created on startup if not already present | dev-only fallback (`admin@smartmoneymanager.com` / `Admin@12345`); unset (no admin seeded) in `prod` |
| `ADMIN_FULL_NAME` | Display name for the seeded admin account | `System Admin` |
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

**Admin** — `/api/admin` *(`ROLE_ADMIN` only)*
`GET /stats` · `GET /users` (paginated; `?search=&enabled=&sortBy=&sortDir=&page=&size=`) · `GET /users/{id}` · `PUT /users/{id}/enable` · `PUT /users/{id}/disable`

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
- [x] Admin panel
- [x] Testing and security hardening
- [x] Dockerization
- [x] Deployment preparation
