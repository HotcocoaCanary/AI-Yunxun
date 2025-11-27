# Repository Guidelines

This document is a short, practical guide for contributors to **AI-Yunxun**.

## Project Structure & Modules

- `backend/` – Spring Boot (Java 17) service; main code in `src/main/java`, tests in `src/test/java`.
- `frontend/` – Next.js 14 + React app; main code in `src/`, shared styles in `styles/`, static assets in `public/`.
- `doc/` and `docs/` – architecture notes, API and product documentation.
- `docker-compose.yml` – local multi-service stack (backend, frontend, databases, vector store).

## Build, Test & Development

- Backend (from repo root): `cd backend && .\mvnw.cmd spring-boot:run` – run API locally.
- Backend tests: `cd backend && .\mvnw.cmd test` – run Spring Boot test suite.
- Frontend dev: `cd frontend && npm install && npm run dev` – start Next.js dev server.
- Frontend build: `cd frontend && npm run build && npm start` – production build + run.
- Full stack (optional): `docker compose up -d` – start services defined in `docker-compose.yml`.

## Coding Style & Naming

- Java: follow standard Spring Boot style, 4-space indentation, `yunxun.ai.canary.*` package naming, classes in `PascalCase`, methods and fields in `camelCase`.
- React/TypeScript: prefer functional components, `PascalCase` for component files, `camelCase` for helpers; keep feature-specific code grouped by domain.
- Use existing patterns for DTOs, controllers, and services; avoid introducing new architectures without discussion.
- Run `npm run lint` in `frontend/` and ensure the backend builds before opening a PR.

## Testing Guidelines

- Place backend tests under `backend/src/test/java` mirroring the package of the code under test.
- Use Spring Boot testing support (`@SpringBootTest`, `@WebMvcTest`) where appropriate.
- For frontend, at minimum ensure ESLint passes and exercise key flows manually in the browser.
- Aim to cover new business logic with tests and update or remove obsolete tests when refactoring.

## Commit & Pull Request Guidelines

- Use Conventional Commit-style messages: `feat(agent): ...`, `refactor(backend): ...`, `fix(frontend): ...`.
- Keep commits focused and small; group related backend and frontend changes in the same PR when they belong together.
- PRs should include: a clear description, linked issues (if any), screenshots or GIFs for UI changes, and notes on breaking changes or migrations.
- Ensure `mvnw test` (backend) and `npm run lint` (frontend) succeed before requesting review.

## Configuration & Security

- Do not commit secrets or real credentials; use environment variables and local config files ignored by Git.
- When adding new external services (DBs, queues, vector stores), document required environment variables and default local ports in `docs/`.

