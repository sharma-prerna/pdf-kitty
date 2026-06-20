# PDF Toolkit — Architecture

## 1. Overview

PDF Toolkit is a full-stack web application for document operations: compress, merge, split,
PDF→Word, Image→PDF, PDF→Image. Each operation is modeled as an **asynchronous job**
that moves through a status lifecycle and produces one or more downloadable outputs.

```
┌────────────┐      HTTP/JSON + multipart     ┌──────────────────────────────┐
│  Frontend  │  ───────────────────────────▶  │          Backend             │
│ React + MUI│                                │      Spring Boot (Java 21)    │
│ React Query│  ◀───────  poll job status ──  │                               │
└────────────┘                                │  controller → service →       │
                                              │  queue(async) → processor →   │
                                              │  storage                      │
                                              └───────┬───────────────┬───────┘
                                                      │               │
                                              ┌───────▼──────┐  ┌─────▼──────┐
                                              │  PostgreSQL  │  │   Redis    │
                                              │ jobs/files   │  │ cache +    │
                                              │ metadata     │  │ rate-limit │
                                              └──────────────┘  └────────────┘
                                              local FS: /data/storage/{jobId}/...
```

## 2. Technology stack

| Layer        | Choice |
|--------------|--------|
| Frontend     | React 19, TypeScript, Vite, Material UI v6, TanStack React Query v5, Axios, React Router, react-dropzone |
| Backend      | Spring Boot 3.3.x, Java 21, Maven |
| PDF/Doc libs | Apache PDFBox 3.x, Apache POI (XWPF), Apache Tika-core |
| Persistence  | PostgreSQL 16 + Spring Data JPA + Flyway |
| Cache/queue  | Redis 7 (caching, rate-limiting); async processing via ThreadPoolTaskExecutor |
| Security     | Spring Security + jjwt (JWT-ready, endpoints currently open) |
| Mapping      | MapStruct |
| Tests        | JUnit 5, Mockito, Testcontainers, JaCoCo / Vitest, RTL, MSW |
| Infra        | Docker, Docker Compose, Nginx (frontend serving + /api proxy) |

## 3. Backend — clean architecture

Package root: `com.pdftoolkit`

```
controller/    Thin REST endpoints. @Valid DTOs in, DTOs out. No business logic.
service/       Interfaces: FileService, JobService, PdfOperationService.
service/impl/   Orchestration: create job → store files → enqueue → expose status/results.
repository/    Spring Data JPA repositories.
entity/        ProcessingJob, UploadedFile, OutputFile (+ enums OperationType, JobStatus).
dto/           Request/response records. ErrorResponse.
mapper/        MapStruct entity↔DTO mappers.
exception/     Custom exceptions + GlobalExceptionHandler (@RestControllerAdvice).
config/        Async, CORS, Redis, OpenAPI, app properties.
security/      SecurityConfig, JwtService, JwtAuthFilter (wired, permitAll for now).
storage/       StorageService interface + LocalStorageService (path-traversal safe).
processor/     FileProcessor interface, ProcessorRegistry, one impl per operation.
queue/         JobQueue interface + AsyncJobQueue (executor-backed worker).
util/          ZipUtil, FileValidationUtil (Tika magic-byte detection).
```

### Request → result flow
1. Client POSTs files + params to an operation endpoint (e.g. `POST /api/pdf/merge`).
2. Controller validates input, delegates to `PdfOperationService`.
3. Service creates a `ProcessingJob` (`UPLOADED`), stores each upload via `StorageService`
   (server-generated UUID paths), persists `UploadedFile` rows, then calls `JobQueue.submit`.
4. `JobQueue` marks job `QUEUED`, hands it to the async executor.
5. Worker marks `PROCESSING`, resolves the `FileProcessor` from `ProcessorRegistry` by
   `OperationType`, runs it, writes `OutputFile`(s), marks `COMPLETED` (or `FAILED` + message).
6. Client polls `GET /api/jobs/{id}` until terminal, then `GET /api/download/{id}` (single file
   or a ZIP bundle when the op emits many outputs).

### Processors (strategy pattern)
`FileProcessor { OperationType type(); ProcessResult process(ProcessContext ctx); }`
resolved via `ProcessorRegistry` (a `Map<OperationType, FileProcessor>` populated by Spring).

| Processor            | Implementation |
|----------------------|----------------|
| CompressProcessor    | PDFBox: downsample/re-encode embedded images per level (Low/Med/High → DPI + JPEG quality), object-stream compression, strip metadata. |
| MergeProcessor       | PDFBox `PDFMergerUtility`; input order = merge order. |
| SplitProcessor       | Page-range / page-list / every-page; emits multiple OutputFiles. |
| PdfToWordProcessor   | PDFBox `PDFTextStripper` → POI `XWPF`. **Text-only** (layout not preserved). |
| ImageToPdfProcessor  | PDFBox: one page per image, scaled to fit; input order. |
| PdfToImageProcessor  | PDFBox `PDFRenderer.renderImageWithDPI` → `ImageIO` (JPG/PNG) at chosen DPI. |

## 4. Data model

```
ProcessingJob
  id UUID (PK)          operationType ENUM     status ENUM
  originalFileName      outputFileName          errorMessage
  createdAt updatedAt processingStartedAt completedAt

UploadedFile                         OutputFile
  id UUID (PK)                         id UUID (PK)
  jobId FK -> ProcessingJob            jobId FK -> ProcessingJob
  fileName filePath fileSize           fileName filePath fileSize
  mimeType uploadedAt                  mimeType createdAt
```

`OperationType`: COMPRESS, MERGE, SPLIT, PDF_TO_WORD, IMAGE_TO_PDF, PDF_TO_IMAGE.
`JobStatus`: UPLOADED, QUEUED, PROCESSING, COMPLETED, FAILED.

Schema is created and versioned by **Flyway** (`db/migration/V1__init.sql`).

## 5. REST API

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/files/upload`         | Upload file(s), returns stored file metadata |
| POST | `/api/pdf/compress`         | Compress a PDF (level=LOW/MEDIUM/HIGH) |
| POST | `/api/pdf/merge`            | Merge PDFs (multipart order = merge order) |
| POST | `/api/pdf/split`            | Split by range/list/every-page |
| POST | `/api/convert/pdf-to-word`  | PDF → DOCX |
| POST | `/api/convert/image-to-pdf` | Images → single PDF |
| POST | `/api/convert/pdf-to-image` | PDF → images (format=JPG/PNG, dpi=N) |
| GET  | `/api/jobs/{id}`            | Job status + metadata |
| GET  | `/api/jobs`                 | Job history |
| GET  | `/api/download/{id}`        | Download output (file or ZIP) |

All operation endpoints return a `JobResponse` immediately; processing is async.

## 6. Cross-cutting concerns

- **Validation:** Spring multipart limits + custom size check + **Tika magic-byte** type
  detection (not just extension). Allowed: pdf, jpg, jpeg, png.
- **Exception handling:** `GlobalExceptionHandler` maps custom + framework exceptions to a
  uniform `ErrorResponse` (timestamp, status, code, message, path).
- **Security:** `SecurityConfig` filter chain + `JwtAuthFilter`/`JwtService` are present and
  wired; all endpoints are `permitAll()` for now. Flipping to enforced auth = add user store +
  change the authorization rules; no structural change needed.
- **Rate limiting:** per-IP Redis filter (`INCR` + `EXPIRE`), configurable window/limit.
- **Caching:** Redis caches job-status lookups.
- **Storage safety:** files stored under `${storage.base-dir}/{jobId}/` with UUID names; the
  client filename is never used as a path. Paths normalized and asserted to stay under the base
  dir (path-traversal protection).
- **Async:** `@EnableAsync` with a dedicated bounded executor; the `JobQueue` interface allows
  swapping to Redis Streams / a broker later without touching services.
- **Observability:** SLF4J structured logs per job (jobId, op, status transitions); Actuator
  health endpoint for container healthchecks.

## 7. Frontend architecture

```
src/api/       Axios client (baseURL /api) + typed endpoint functions.
src/types/     DTO mirrors: Job, OperationType, JobStatus, results.
src/hooks/     useUpload, useOperation(op) mutations; useJob(id) query (polls until terminal);
               useJobHistory.
src/components/ FileDropzone, FileList (reorderable), JobStatusCard, OperationLayout.
src/pages/     Dashboard (operation cards), OperationPage (per-op controls), ProcessingPage
               (status + progress + download), HistoryPage (table).
src/theme/     MUI theme. App.tsx: Router + QueryClientProvider.
```

Job status is polled with React Query `refetchInterval` until `COMPLETED`/`FAILED`, then the
download button is enabled.

## 8. Deployment

`docker compose up --build` starts four services on a bridge network:

| Service  | Image / build | Port |
|----------|---------------|------|
| postgres | postgres:16-alpine | 5432 |
| redis    | redis:7-alpine | 6379 |
| backend  | multi-stage maven → temurin-21-jre | 8080 |
| frontend | node build → nginx:alpine (serves SPA, proxies `/api` → backend) | 3000 |

Persistent volumes: `postgres-data`, `redis-data`, `storage-data` (`/data/storage`).

## 9. Future-ready extensions

- **AWS S3** storage: add an `S3StorageService` behind the existing `StorageService` interface.
- **Distributed queue:** replace `AsyncJobQueue` with a Redis Streams / SQS worker behind
  `JobQueue`.
- **Auth:** enable JWT enforcement + a user/role store; the security scaffolding already exists.
