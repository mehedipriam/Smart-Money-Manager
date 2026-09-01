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
2. Full Database Schema Creation
3. Authentication
4. Account Management
5. Category Management
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

---

Database setup, full installation steps, API documentation, and deployment instructions will be
added as their corresponding phases (2, 11, 16) are completed.
