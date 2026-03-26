# TonTin Platform

TonTin Platform is a full-stack web application for managing rotating savings groups (called **Dâr** in the project vocabulary).  
It provides role-based workspaces for members and administrators, handles round lifecycle, contribution tracking, and payment reporting, and is packaged for local development and CI/CD deployment.

---

## 1. Application Overview

The platform solves a common operational problem in tontine-like communities: organizing participants, running fair rounds, and keeping contributions visible and auditable.

Instead of managing rounds manually in chat threads or spreadsheets, TonTin offers:

- a structured **member + organizer model**
- controlled **Dâr lifecycle** (pending, active, finished, cancelled)
- **round-level contribution tracking**
- **admin-level visibility** across users, dârs, and payments
- API-first backend with a modern Angular frontend

---

## 2. Main User Roles

### Client (`ROLE_CLIENT`)

A client can:
- register, verify email, and login
- view personal dashboard and joined dârs
- create/manage their own dâr (when organizer)
- view rounds and contribution status
- pay contribution when eligible
- view personal reports and profile

### Admin (`ROLE_ADMIN`)

An admin can:
- access dedicated admin dashboard
- list and filter users globally
- enable/disable user accounts
- create dârs for other users (organizer can be selected)
- start/finish/cancel dârs from admin tools
- view global payments/transactions and summaries

---

## 3. Functional Modules

### Authentication & Profile
- registration with email verification
- login + refresh token flow
- logout
- get/update current user profile

### Dâr Management
- create/update/delete dâr
- list user dârs and admin global dârs
- start dâr with validations
- finish dâr lifecycle

### Membership
- join dâr membership model (`Member`)
- organizer/member permissions
- status transitions (pending/active/leaved)

### Rounds
- round creation and ordering strategy
- recipient assignment per round
- round payment status progression

### Payments
- payment intent generation
- eligibility checks (cannot pay twice in same round, recipient cannot pay own round)
- payment confirmation and reporting
- admin global payment summary/list endpoints

### Admin Operations
- user moderation (enable/disable)
- admin-created dâr with selected organizer
- global reporting and transaction consultation

---

## 4. Architecture

### Backend (`platform-back`)
- Java 21, Spring Boot
- Spring Security + JWT
- JPA/Hibernate with PostgreSQL
- layered structure: Controller -> Service -> Repository
- DTO + Mapper boundary for API payload safety

### Frontend (`platform-front`)
- Angular 18 + TypeScript
- role-based routes/guards
- dashboard domains for client/admin
- API integration via services and interceptors

### Infra / DevOps
- Docker + Docker Compose (prod-like and dev)
- GitHub Actions CI/CD
- GHCR image publishing on `main`

---

## 5. Repository Structure

```text
TonTin-platform/
├── platform-back/                    # Spring Boot API
│   ├── src/main/java/com/tontin/platform
│   ├── src/main/resources
│   ├── src/test
│   ├── Dockerfile
│   └── Dockerfile.dev
├── platform-front/                   # Angular client
│   ├── src/app
│   ├── Dockerfile
│   └── Dockerfile.dev
├── docker-compose.yml                # production-like local stack
├── docker-compose.dev.yml            # hot-reload development stack
├── .env.docker.example               # Docker environment template
└── .github/workflows/                # CI/CD workflows
```

---

## 6. Data Model

Core business entities:

- `users`
- `darts`
- `members`
- `rounds`
- `payments`

Supporting entities:

- `notifications`
- `dart_messages`
- `dart_message_reactions`
- `logs`

For the detailed ER/class representation with fields and relations, see:

- `platform-back/DB_CLASS_DIAGRAM.md`

---

## 7. Key API Domains

Base API path: `/api/v1`

- `/auth` -> register/login/verify/refresh/me/logout
- `/dart` -> dâr lifecycle + admin dâr listing/creation
- `/member` -> membership operations
- `/round` -> round operations
- `/payments` -> contribution payment + reports + admin summaries
- `/user` -> user administration/search/status updates

Swagger:

- `http://localhost:9090/swagger-ui/index.html`
- `http://localhost:9090/v3/api-docs`

---

## 8. Email Verification UX Flow

1. User registers.
2. Backend sends verification email with:
   - `GET /api/v1/auth/verify?code=...`
3. Backend verifies account and redirects to frontend login:
   - `/auth/login?verified=true&message=...`
4. Login page shows verification success message.

This avoids plain JSON-only verify pages and returns users to app UI.

---

## 9. Local Development (without Docker)

### Backend

```bash
cd platform-back
./mvnw -q -DskipTests compile
./mvnw spring-boot:run
```

Backend URL: `http://localhost:9090`

### Frontend

```bash
cd platform-front
npm ci
npm start
```

Frontend URL: `http://localhost:4200`

---

## 10. Docker Development & Deployment

### Production-like stack

```bash
cp .env.docker.example .env
docker compose up --build
```

- frontend: `http://localhost:4200`
- backend: `http://localhost:9090/api`
- postgres: `localhost:5432`

### Development stack (hot reload)

```bash
cp .env.docker.example .env
docker compose -f docker-compose.dev.yml up --build
```

Includes mounted volumes for backend/frontend source and Maven/npm caches.

---

## 11. Environment Configuration

### Required security/database variables

- `SECURITY_JWT_SECRET_KEY` (required)
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### Mail

- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `APP_MAIL_FROM` (recommended)
- `APP_MAIL_SENDER_NAME` (optional display name)

### Stripe (optional for local development)

- `STRIPE_SECRET_KEY`
- `STRIPE_PUBLISHABLE_KEY`
- `STRIPE_WEBHOOK_SECRET`

---

## 12. Quality & Testing

### Backend

```bash
cd platform-back
./mvnw -q test
```

### Frontend

```bash
cd platform-front
npm run test
```

---

## 13. CI/CD

### CI (`.github/workflows/ci.yml`)
- backend compile + tests
- frontend install + production build
- docker compose file validation

### CD (`.github/workflows/cd.yml`)
- triggered on `main` push (or manual dispatch)
- builds backend/frontend Docker images
- publishes images to GHCR

---

## 14. Security Notes

- Never commit `.env` files or secrets.
- JWT secret must be strong and private.
- Use app passwords for SMTP providers.
- Prefer environment variables/secrets manager in production.
- Review role guards and endpoint authorization for every new feature.

---

## 15. Current Product Status

The current codebase includes:

- active admin management features in both backend and frontend
- payment report APIs for user and global admin views
- redirect-based verification flow integrated with login UX
- Dockerized runtime and CI/CD scaffolding

This README reflects the implemented system structure and runtime flow in the repository.