# Job Portal API

[![CI](https://github.com/jacob-zino/job-portal-api/actions/workflows/ci.yml/badge.svg)](https://github.com/jacob-zino/job-portal-api/actions/workflows/ci.yml)

A Spring Boot REST API for a job portal: recruiters post jobs, candidates apply, and admins manage users. It is secured with Spring Security and JWT, uses MongoDB for storage, and ships with a small HTML/JavaScript front end and generated API docs.

> University project built by a team of two (2025): **Jacob Zino** ([@jacob-zino](https://github.com/jacob-zino)) and **Magaly El Khoury** ([@magalykhoury](https://github.com/magalykhoury)).

## Features
- **Authentication:** register and log in; passwords hashed with BCrypt; stateless JWT authentication.
- **Role-based access:** `ADMIN`, `RECRUITER` and user roles enforced with `@PreAuthorize`.
- **Jobs:** create, update, delete, search, and filter by company, location and recruiter, with paginated endpoints.
- **Applications:** apply to jobs, track application status, and view applications per job, email or status.
- **Admin:** list users, enable or disable accounts, view statistics.
- **Front end:** static pages (`/`, `/jobs.html`, `/login.html`, `/admin.html`, …) served by the app.
- **Spring Shell CLI:** an optional console client (profile `console`).
- **Tests:** JUnit tests for controllers and services.

## Tech stack
Java 21 · Spring Boot 3.2 · Spring Security · JWT (jjwt) · Spring Data MongoDB · Spring Shell · JUnit 5 / Mockito · Gradle · Docker

## Architecture
Layered `controller → service → repository` design over MongoDB documents (`User`, `Job`, `Application`, `Recruiter`).

| Base path | Purpose |
|---|---|
| `/api/auth` | `POST /register`, `POST /login` (returns a JWT) |
| `/api/jobs` | Job CRUD, search and filters (`/search`, `/company/{company}`, `/location/{location}`, `/paged`) |
| `/api/applications` | Apply and track applications (`/me`, `/job/{jobId}`, `/status/{status}`) |
| `/api/recruiters` | Recruiter profiles and their jobs (`/me/jobs`) |
| `/api/users` | Current-user profile (`/me`) |
| `/api/admin` | User management and statistics (`ADMIN` only) |

Every endpoint except `/api/auth/**` and the static pages requires `Authorization: Bearer <token>`.

## Getting started
**Prerequisites:** JDK 21 and a MongoDB instance (local or MongoDB Atlas).

1. Set the environment variables (see `.env.example`):

   | Variable | Required | Description |
   |---|---|---|
   | `MONGODB_URI` | yes | MongoDB connection string |
   | `JWT_SECRET` | yes | Long random string used to sign tokens |
   | `ADMIN_PASSWORD` | no | If set, an `admin` account is created on first start |
   | `PORT` | no | HTTP port (default `8080`) |

2. Run the app:
   ```bash
   ./gradlew bootRun          # Windows: gradlew.bat bootRun
   ```
   Then open http://localhost:8080.

3. Run the tests:
   ```bash
   ./gradlew test
   ```

### Docker
```bash
docker build -t job-portal-api .
docker run -p 8080:8080 -e MONGODB_URI=... -e JWT_SECRET=... job-portal-api
```

## Known limitations
- No refresh tokens; a JWT is valid until it expires (24 h by default).
- Built for learning purposes and not hardened for production use.
