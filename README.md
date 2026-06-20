# PDF Toolkit

A production-grade full-stack web application for PDF & document operations: **compress,
merge, split, PDF↔Word, and image↔PDF**. Every operation runs as an asynchronous job with a
tracked status lifecycle and a download center.

- **Frontend:** React 19 · TypeScript · Vite · Material UI v6 · React Query · Axios · React Router
- **Backend:** Spring Boot 3.3 · Java 21 · Maven · PostgreSQL · Redis · Apache PDFBox · Apache POI
- **Infra:** Docker · Docker Compose · Nginx

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the full design and [`docs/PLAN.md`](docs/PLAN.md) for the build plan.

---

## Quick start (Docker)

```bash
cp .env.example .env          # adjust secrets as needed
docker compose up --build
```

| Service  | URL |
|----------|-----|
| Frontend | http://localhost:3000 |
| Backend  | http://localhost:8080 |
| API docs | http://localhost:8080/swagger-ui.html |
| Health   | http://localhost:8080/actuator/health |

---

## Local development

### Backend
Requires **JDK 21** and **Maven 3.9+**, plus a running PostgreSQL and Redis (the simplest way
is `docker compose up postgres redis`).

```bash
cd backend
mvn spring-boot:run            # uses the default 'local' profile
mvn clean verify               # compile + unit + integration tests + JaCoCo report
```

JaCoCo coverage report: `backend/target/site/jacoco/index.html`.

### Frontend
Requires **Node 20+**.

```bash
cd frontend
npm install
npm run dev                    # Vite dev server at http://localhost:5173 (proxies /api -> :8080)
npm run build                  # production build
npm run test                   # Vitest
```

> For local dev, run the backend (and Postgres/Redis) first, then the frontend — the Vite dev
> server proxies `/api` to `http://localhost:8080`.

---

## Operations & API

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/files/upload`         | Upload file(s) |
| POST | `/api/pdf/compress`         | Compress a PDF (`level` = LOW/MEDIUM/HIGH) |
| POST | `/api/pdf/merge`            | Merge PDFs (multipart order = merge order) |
| POST | `/api/pdf/split`            | Split (`mode` = RANGE/PAGES/EVERY, `spec` e.g. `1-5` or `1,3,5`) |
| POST | `/api/convert/pdf-to-word`  | PDF → DOCX (text-only) |
| POST | `/api/convert/image-to-pdf` | Images → single PDF |
| POST | `/api/convert/pdf-to-image` | PDF → images (`format` = JPG/PNG, `dpi`) |
| GET  | `/api/jobs/{id}`            | Job status |
| GET  | `/api/jobs`                 | Job history |
| GET  | `/api/download/{id}`        | Download output (file or ZIP) |

### Example

```bash
# Merge two PDFs
curl -F "files=@a.pdf" -F "files=@b.pdf" http://localhost:8080/api/pdf/merge
# -> { "id": "...", "status": "QUEUED", ... }

curl http://localhost:8080/api/jobs/<id>          # poll until COMPLETED
curl -OJ http://localhost:8080/api/download/<id>  # download result
```

---

## Configuration

Environment variables (see `.env.example`): database/Redis connection, `STORAGE_BASE_DIR`,
`MAX_FILE_SIZE_MB`, `RATE_LIMIT_REQUESTS` / `RATE_LIMIT_WINDOW_SECONDS`, and `JWT_SECRET` /
`JWT_EXPIRATION_MS`.

## Security notes

- File-type validation uses **Apache Tika** magic-byte detection (not just extension).
- Files are stored under server-generated UUID paths; client filenames are never used as paths
  (path-traversal protection).
- Per-IP rate limiting backed by Redis.
- JWT security scaffolding is wired but endpoints are currently open (`permitAll`). Enabling
  enforced auth requires only a user store and authorization rules — no structural change.

## Project layout

```
pdf-toolkit/
├── docker-compose.yml
├── docs/            ARCHITECTURE.md, PLAN.md
├── backend/         Spring Boot service
└── frontend/        React app
```
