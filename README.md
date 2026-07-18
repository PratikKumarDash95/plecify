# Campus Recruitment Portal

Campus Recruitment Portal is a full-stack campus hiring platform with a Spring Boot backend and a React + Vite frontend. The backend handles authentication, placements, jobs, student/company flows, mail delivery, AI helpers, and persistence; the frontend provides the UI for those workflows.

## Tech Stack

- Backend: Java 21, Spring Boot 3.3.4, Spring Security, Spring Data JPA, Flyway, PostgreSQL
- Frontend: React 19, TypeScript, Vite, Tailwind CSS, TanStack Query
- Integrations: Google sign-in, Brevo email, OpenRouter AI, Supabase storage

## Project Structure

- `pom.xml` - backend build and dependency management
- `src/main/java` - Spring Boot application code
- `src/main/resources` - backend configuration and database migrations
- `frontend/` - Vite application and UI code
- `.env.example` - backend local environment template
- `frontend/.env.example` - frontend local environment template

## Prerequisites

- Java 21
- Maven 3.9+ or the included Maven wrapper
- Node.js 20+ and npm
- PostgreSQL

## Setup

1. Copy `.env.example` to `.env` and fill in the backend secrets.
2. If the frontend needs environment variables, copy `frontend/.env.example` to `frontend/.env`.
3. Install frontend dependencies with `cd frontend && npm install`.

## Run the App

### Backend

On macOS/Linux:

```bash
./run.sh
```

On Windows:

```cmd
run.cmd
```

The backend starts on port `8080` by default.

### Frontend

```bash
cd frontend
npm run dev
```

The Vite app runs on port `5173` by default.

## Environment Notes

- `.env` is gitignored and loaded by the backend run scripts.
- Keep real credentials out of `frontend/.env` and `frontend/.env.local`; only commit the example files.
- Default backend settings in `src/main/resources/application.yml` expect PostgreSQL, Brevo SMTP, and optional Google/OpenRouter values.

## Helpful Endpoints

- OpenAPI docs: `/swagger-ui.html`
- API spec: `/v3/api-docs`

## Development Notes

- Database migrations live under `src/main/resources/db/migration`.
- The frontend build uses `npm run build` and the backend uses the Maven wrapper.
