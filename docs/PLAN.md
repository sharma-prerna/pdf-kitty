# Plan: Full-Stack PDF Toolkit Web Application

## Context

A **greenfield**, production-grade PDF Toolkit web app. Users upload files and run seven
document operations (compress, merge, split, Word↔PDF, image↔PDF), each tracked as an async
job with status and a download center.

**Confirmed decisions:**
- **UI:** Material UI (MUI v6).
- **Conversion engine:** Pure-Java only (PDFBox, POI, docx4j) — no external binaries.
- **Scope (pass 1):** "Working core first" — full scaffold + DB + Docker + **all** backend
  processors, frontend wired end-to-end (upload → job → poll → download) for every op.
- **Auth:** JWT-ready architecture (security package + filter chain structured for JWT) but
  endpoints left open so the app is usable immediately.

> Build environment note: at authoring time the shell could not execute commands, so all
> source is hand-authored and must be built/run by the developer (`mvn`, `npm`, `docker`).

## Build order
1. Scaffold repo, `docker-compose.yml`, README, `.env.example`, docs.
2. Backend: pom, config, entities, Flyway schema, repositories, DTOs/mappers, exceptions.
3. Storage + util (ZipUtil, validation) + queue abstraction.
4. Processors (all 7) with unit tests.
5. Services + controllers + security/rate-limit/CORS; wire async queue.
6. Backend integration tests (Testcontainers).
7. Frontend scaffold, theme, api client, types, hooks.
8. Frontend components + pages.
9. Frontend tests.
10. Dockerfiles + nginx; finalize README.

## Verification (once a shell is available)
- `cd backend && mvn clean verify` — compile, unit + Testcontainers integration tests, JaCoCo.
- `cd frontend && npm install && npm run build && npm run test`.
- `docker compose up --build` — smoke-test each op: upload → `QUEUED→PROCESSING→COMPLETED` →
  download (zip for split / pdf→image). Verify rejects: oversized, unsupported type, traversal.

See [ARCHITECTURE.md](./ARCHITECTURE.md) for the full design.
