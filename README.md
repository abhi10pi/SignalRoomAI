# Signalroom

Signalroom is a monorepo for a full-stack civic signal platform.

## Monorepo structure

- frontend/ — Next.js app
- Backend/ — Spring Boot API
- ai-service/ — FastAPI service

## Local development

1. Copy `.env.example` to `.env`.
2. Run `docker compose up --build`.
3. Frontend: http://localhost:3000
4. Backend: http://localhost:8080/api/health
5. AI service: http://localhost:8000/health

## Environment variables

See `.env.example` for the application settings used locally.

## Services

- PostgreSQL on port 5432
- Redis on port 6379
- Spring Boot API on port 8080
- FastAPI AI service on port 8000
- Next.js frontend on port 3000
