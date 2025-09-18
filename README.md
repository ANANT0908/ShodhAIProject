
# Shodh-a-Code Contest Platform

## Overview
**Shodh-a-Code** is a prototype coding-contest platform (full-stack + systems).  
It includes:
- A **Spring Boot** backend with a live code judge (runs submissions in a Docker-based execution environment).
- A **Next.js + Tailwind** frontend with a contest UI (join page, editor, submission flow, leaderboard).
- A **Postgres** database seeded with an example contest and problems.

This repository is organized for evaluation: everything required to run the system locally can be started with a single `docker-compose up --build`.

---

## Quick links
- Frontend: `http://localhost:3000`  
- Backend API base: `http://localhost:8080/api`  
- DB (Postgres): `localhost:5432` (if running with docker-compose)

---

## Project structure (quick map)
```
.
├── backend/                      # Spring Boot backend + judge logic
│   ├── src/main/java/...         # controllers, services, entities (Contest, Problem, Submission, User)
│   ├── src/main/resources/
│   │   ├── application.yml       # spring config + judge properties
│   │   └── initdb/seed.sql       # DB seed (sample contest & problems)
│   ├── Dockerfile                # backend container image
│   └── judge/                    # judge image Dockerfile (execution sandbox)
├── frontend/                     # Next.js + Tailwind frontend
│   ├── pages/                    # join page, contest page
│   ├── lib/api.js                # client API wrapper uses NEXT_PUBLIC_API_BASE
│   └── package.json
└── docker-compose.yml            # orchestrates Postgres + backend + frontend
```

---

## Prerequisites
- Docker (tested with Docker Desktop or Docker Engine)
- Docker Compose
- Node.js & npm (only if you prefer running frontend locally without Docker)
- Java & Maven (only if you prefer building backend locally without Docker)

> **Important:** The judge in this project executes `docker` commands (it expects access to a Docker daemon). See the **Judge / Deployment limitations** section below before attempting to run this in a hosted environment.

---

## Environment variables (summary)
| Variable | Where used | Purpose / example |
|---|---:|---|
| `NEXT_PUBLIC_API_BASE` | frontend | Backend API base URL. Example for dev: `http://localhost:8080` |
| `SPRING_DATASOURCE_URL` | backend | JDBC URL to Postgres. Example: `jdbc:postgresql://db:5432/shodh` |
| `SPRING_DATASOURCE_USERNAME` | backend | DB username (e.g. `shodh`) |
| `SPRING_DATASOURCE_PASSWORD` | backend | DB password (e.g. `shodh`) |
| `judge.image` (Spring property) | backend | Name/tag of judge image used to run submissions (default `shodh-judge:latest`) |
| `judge.timeout-seconds` (Spring property) | backend | Per-test-case timeout in seconds |

You can place values in a `.env` file that `docker-compose` will read (or export them in your shell).

Example `.env` (project root):
```env
POSTGRES_USER=shodh
POSTGRES_PASSWORD=shodh
POSTGRES_DB=shodh
NEXT_PUBLIC_API_BASE=http://localhost:8080
```

---

## Quickstart — run everything with Docker Compose
From the project root:
```bash
# build & run all services (frontend, backend, db)
docker-compose up --build
# run in background
docker-compose up -d --build

# view logs
docker-compose logs -f backend
docker-compose logs -f frontend

# stop & remove containers
docker-compose down
```

Default exposed ports (as configured in `docker-compose.yml`):
- Frontend: `3000`
- Backend: `8080`
- Postgres: `5432` (DB client access if needed)

---

## How the DB is seeded
A seed SQL file at:
```
backend/src/main/resources/initdb/seed.sql
```
pre-populates:
- 1 sample contest
- 2–3 sample problems (with testcases)
- sample users/submissions (if any)

The seed runs only when the Postgres volume is initialized. If you change `seed.sql`, delete the Postgres data volume and recreate the stack to re-seed.

---

## Build/run alternatives (without docker-compose)

### Backend (Maven & Java)
```bash
# build
cd backend
mvn -DskipTests package

# run with env override (example)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/shodh \
SPRING_DATASOURCE_USERNAME=shodh \
SPRING_DATASOURCE_PASSWORD=shodh \
java -jar target/*.jar
```

### Frontend (dev server)
```bash
cd frontend
npm install
NEXT_PUBLIC_API_BASE=http://localhost:8080 npm run dev
# frontend will be at http://localhost:3000
```

---

## Judge image (how to build locally)
The backend expects a judge image (by default `shodh-judge:latest`) used by `JudgeService` to run user code. Build it locally if you want judge functionality to run inside your host Docker daemon:

```bash
# from repo root
docker build -t shodh-judge:latest -f backend/judge/Dockerfile backend/judge
```

> Note: If you run backend inside a container and want it to call the host Docker daemon, you would need to mount `/var/run/docker.sock` into the backend container. That is insecure and **not recommended** for production. See Security & Limitations below.

---

## API — endpoints & examples

### 1) Fetch contest
**GET** `/api/contests/{contestId}`  
Returns contest details (title, problems list).

Example:
```bash
curl http://localhost:8080/api/contests/1
```

### 2) Get leaderboard
**GET** `/api/contests/{contestId}/leaderboard`  
Returns live leaderboard rows (username, score, rank, etc).

Example:
```bash
curl http://localhost:8080/api/contests/1/leaderboard
```

### 3) Submit code
**POST** `/api/submissions`  
Request JSON:
```json
{
  "contestId": "1",
  "problemId": 1,
  "username": "alice",
  "language": "java",
  "sourceCode": "public class Main{ public static void main(String[] a){ System.out.println(2+3);} }"
}
```
Returns:
```json
{ "submissionId": 123 }
```

Example:
```bash
curl -X POST http://localhost:8080/api/submissions \
  -H "Content-Type: application/json" \
  -d '{"contestId":"1","problemId":1,"username":"alice","language":"java","sourceCode":"..."}'
```

### 4) Poll submission status/result
**GET** `/api/submissions/{submissionId}`  
Returns latest status (e.g., `Pending`, `Running`, `Accepted`, `Wrong Answer`) along with details such as per-test results, score, timestamps.

Example:
```bash
curl http://localhost:8080/api/submissions/123
# { "id":123, "status":"Accepted", "score":100, "details":[...] }
```

---

## Frontend behavior & config
- The frontend stores the `username` in `localStorage` after joining a contest.
- On submission:
  1. Frontend `POST /api/submissions` → gets `submissionId`.
  2. Frontend polls `GET /api/submissions/{id}` every ~2–3 seconds to update the user.
- Leaderboard polling interval: ~15–30 seconds (adjustable in frontend code).
- Ensure `NEXT_PUBLIC_API_BASE` is set correctly (when using Docker Compose it is configured for container networking; when running locally set it to `http://localhost:8080`).

---

## CORS and production notes
During development CORS allows `http://localhost:3000`. Before deploying to production:
- Update backend CORS config to allow your production frontend domain.
- Review `backend/src/main/java/...CorsConfig.java` and `WebConfig.java` for allowed origins and remove broad `*` allowances if present.

---

## Judge / Deployment limitations (Important)
The judge implementation in this repository **executes `docker` commands from the backend** (it uses the host Docker daemon via CLI). This design works for **local testing**, but has serious implications for deployment and security:

- **Not compatible with many PaaS providers** (Render, Vercel, Heroku, etc.), because those environments usually do **not** provide a Docker daemon accessible from inside app containers, and they disallow spawning containers from within a container.
- **Security risk**: granting a container access to the host Docker socket (`/var/run/docker.sock`) effectively gives root control of the host. **Do not** mount the Docker socket in a production environment unless you fully understand the security implications.
- **Production alternatives**:
  1. Use a managed/external judge service (e.g., Judge0 or a self-hosted judge running on a dedicated VM) — the backend sends code & testcases over HTTP and receives results.
  2. Run a separate VM (DigitalOcean/AWS) that runs Docker and hosts your judge image, with the backend calling it via a secure API.
  3. Use stronger sandboxing (Firecracker, gVisor, Kata Containers) and run the judge in isolated VMs or a Kubernetes cluster with proper isolation.

Make this limitation explicit in your README and architecture discussion (this file does).

---

## Troubleshooting & common issues

- **`docker: command not found` or judge fails**  
  - If the judge fails to run `docker`, ensure you built the judge image and the backend has access to a Docker daemon. Running everything via `docker-compose` on a machine with Docker installed and the judge image built locally typically works.

- **Ports already in use**  
  - Edit the ports in `docker-compose.yml` to change host ports.

- **DB seed not applied**  
  - The seed SQL only runs on initial DB volume creation. Remove the Postgres data volume and recreate the stack:
    ```bash
    docker-compose down -v
    docker-compose up --build
    ```

- **Check logs**
  ```bash
  docker-compose logs -f backend
  docker-compose logs -f frontend
  ```

---

## Data model (high-level)
- **Contest** — contains Problems, title, start/end metadata.
- **Problem** — description, multiple TestCase (input/output), time/memory constraints.
- **User** — username, metadata.
- **Submission** — user + problem, language, sourceCode, status, result, score, timestamps.

(See backend `src/main/java/.../entity` packages for exact fields.)

---

## Design choices & trade-offs
- **Spring Boot** for backend: productivity, mature ecosystem, easy DB integration (JPA), robustness for REST APIs.
- **Next.js + Tailwind** for frontend: fast prototyping and a good developer experience.
- **Dockerized judge**: simple and isolated execution environment for each submission. Trade-offs: requires host Docker daemon & has deployment/security implications (see section above).
- **Polling (frontend)**: simple, reliable, and good enough for prototype live experience. A real-time system would use WebSockets for push updates.
- **Seeded DB**: evaluator can test the platform immediately with pre-inserted contest & problems.

## Where to look for key code
- Backend controllers: `backend/src/main/java/.../controller`
- Submission & Judge logic: `backend/src/main/java/.../service` (look for `SubmissionService`, `JudgeService`)
- Seed SQL: `backend/src/main/resources/initdb/seed.sql`
- Frontend API client: `frontend/lib/api.js`
- Frontend pages: `frontend/pages/` (join, contest pages, editor, leaderboard)
