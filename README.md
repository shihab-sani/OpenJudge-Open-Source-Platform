## OpenJudge - Automated Open-Source Contribution Platform

An event-driven, asynchronous backend platform that bridges the gap between open-source maintainers and contributors. The application allows users to register projects, publish contribution tasks with automated evaluation rules, and submit code that is safely executed and verified inside isolated Docker sandboxes.

## System Architecture & Services

- **OpenJudge Application**: Core Spring Boot REST API managing users, projects, tasks, and submissions. Serves the Swagger UI dashboard for API demonstrations (4000).
- **Evaluation Worker**: A background thread inside the app that consumes Redis Streams to execute submitted code.
- **PostgreSQL Database**: Dedicated PostgreSQL container for persisting users, projects, tasks, and submission histories (5001).
- **Redis Queue**: Dedicated Redis container acting as the message broker for asynchronous code evaluation jobs (6001).
- **Docker Network**: Shared bridge network allowing containers to resolve each other by name and communicate securely.

## Prerequisites & Docker Network Setup

Before spinning up containers individually, ensure a shared Docker network named `open-judge-net` exists so the containers can resolve each other by name:
`docker network create open-judge-net`

## Configuration & Environment Setup

Below are the complete Docker run configurations for all services and infrastructure dependencies.

## 1. Databases & Message Queues

## open_judge_db (PostgreSQL)

- Container Name: open_judge_db
- Image: postgres:latest
- Bind Ports: 5001:5432
- Run Options: --network open-judge-net

## Environment Variables:

| Variable | Value |
| --- | --- |
| POSTGRES_DB | open_judge_db |
| POSTGRES_USER | root |
| POSTGRES_PASSWORD |  |

## open-judge-redis (Redis)

- Container Name: open-judge-redis
- Image: redis:latest
- Bind Ports: 6001:6379
- Command: redis-server --requirepass "give a strong password here"
- Run Options: --network open-judge-net

## 2. OpenJudge Application

- Container Name: open-judge
- Dockerfile Path: Dockerfile
- Bind Ports: 4000:4000
- Bind Mounts: `//var/run/docker.sock:/var/run/docker.sock` *(Required for the app to spawn sandbox containers via the host Docker daemon)*
- Run Options: --network open-judge-net

## Environment Variables:

| Variable | Value |
| --- | --- |
| SERVER_PORT | 4000 |
| SPRING_DATASOURCE_URL | jdbc:postgresql://open_judge_db:5432/open_judge_db |
| SPRING_DATASOURCE_USERNAME | root |
| SPRING_DATASOURCE_PASSWORD | |
| SPRING_DATA_REDIS_HOST | open-judge-redis |
| SPRING_DATA_REDIS_PORT | 6379 |
| SPRING_DATA_REDIS_PASSWORD | |
| GITHUB_TOKEN | *(Your GitHub Personal Access Token)* |
| DOCKER_HOST | unix:///var/run/docker.sock |

## Local Development Execution Order

To avoid service connection retries or boot-up failures, initialize and run the containers in the following order:

- 1. Create shared network: `docker network create open-judge-net`
- 2. Database & Queue instances (open_judge_db, open-judge-redis)
- 3. Spring Boot Application (open-judge)

---

## 3. API Documentation

The system exposes the following main REST endpoints (demonstrable via Swagger UI at `http://localhost:4000/swagger-ui.html`):
*   `POST /api/users`: Registers a new user (Maintainer or Contributor).
*   `POST /api/projects`: Registers a project and validates the repository URL against the GitHub API.
*   `POST /api/tasks`: Creates a contribution task with specific evaluation rules, language, and test cases.
*   `POST /api/submissions`: Accepts submitted code, returns a `202 Accepted`, and pushes the job to the Redis queue.
*   `GET /api/submissions/{id}`: Polls the submission status and returns the automated verdict.

---

## 4. Code Evaluation Pipeline & Logic

The `EvaluationWorker` consumes jobs from Redis and uses `SandBoxService` to evaluate code securely:
*   **Isolation & Security:** Runs the code in an ephemeral Docker container (e.g., `python:3.12-alpine`) with network disabled (`--network=none`), read-only root filesystem, and a strict memory/CPU limit.
*   **Execution:** Base64 encodes the source code and inputs to prevent shell injection. It spins up the container, executes the code, captures stdout/stderr, and forcefully destroys the container.
*   **Verdict Generation:** Compares the output to the expected test case output.
*   **Asynchronous Decoupling:** Prevents HTTP thread blocking by offloading heavy execution tasks to the background worker via Redis Streams.

## 5. Submission-State Transitions

*   **Safety First:** If the Docker daemon is unreachable or the code times out, the submission is gracefully updated to `INTERNAL_ERROR` or `TIME_LIMIT_EXCEEDED` rather than hanging indefinitely.
*   **Sequence:** `PENDING -> QUEUED -> EVALUATING -> COMPLETED`.
*   **Transaction Safety:** To prevent race conditions, the Redis event is only fired *after* the database transaction successfully commits.

## 6. Instructions for Demonstrating Main Scenarios

1.  **User & Project Creation:** Create a user via `/api/users`. Create a project via `/api/projects` using a real GitHub URL (e.g., `https://github.com/spring-projects/spring-boot`). The API will fetch real metadata.
2.  **Task Creation:** Create a task via `/api/tasks` (e.g., "Add Two Numbers") specifying `python` as the language and providing a test case input `2 3\n` with expected output `5`.
3.  **Successful Submission:** Submit the correct Python code. Poll the `GET` endpoint and watch the status transition to `ACCEPTED` as a Python container briefly spins up and down in Docker Desktop.
4.  **Failed Submission (Wrong Answer):** Submit incorrect code (e.g., `print(a - b)`). The poll will return `WRONG_ANSWER`.
5.  **Failed Submission (Timeout):** Submit an infinite `while True:` loop. The sandbox will enforce the 5-second timeout and return `TIME_LIMIT_EXCEEDED`.

---

## 7. Major Architectural Decisions

- **Domain-Driven Design (DDD)**: Core logic (Controllers, Services, Repositories) is strictly separated. Data Transfer Objects (DTOs) are used to validate inputs and decouple the API from database entities.
- **Asynchronous Queueing**: Utilizing Redis Streams ensures the API remains highly responsive even under heavy load, allowing evaluation workers to scale independently.
- **Fail-Safe Fallbacks**: The GitHub integration gracefully degrades if the API is rate-limited, allowing maintainers to still register projects without crashing the request.
- **Docker HTTP Client Swap**: Replaced the default deprecated Jersey transport with Apache HttpClient 5 (`docker-java-transport-httpclient5`) to resolve `javax.ws.rs` to `jakarta.ws.rs` migration conflicts in Spring Boot 4.

## 8. AI / Tool Usage

- AI-assisted coding tools were used to debug Docker socket mounting, resolve Hibernate lazy-loading proxy errors, and generate boilerplate code (DTOs, mappers). All architectural decisions regarding the async queue and Docker isolation were reviewed, understood, and manually implemented by the candidate.
```