# Service CP Refdata Court Hearing CourtHouses

The Court Houses API provides information about court buildings and the rooms within them — the building name, postal address and room details for a given court.

| Endpoint | Returns |
|----------|---------|
| `GET /courthouses/{court_id}` | The court house and its rooms |
| `GET /courthouses/{court_id}/courtrooms/{court_room_id}` | A single court room within a court house |

## Architecture overview

The service is a stateless proxy with no database. It calls the CP reference-data backend (`CP_BACKEND_URL` + `/referencedata-service/query/api/rest/referencedata/courtrooms`), passing the `CJSCPPUID` header on every request, and maps the response to the API contract.

Actuator is mounted at the **root** context (`management.endpoints.web.base-path: /`), so the operational endpoints are `/health`, `/info` and `/prometheus` — not under `/actuator`.

Every request except an enumerated list of those infrastructure paths must present a valid Microsoft Entra app-only access token — see [Authentication](docs/Authentication.md).

## Software required (macOS)

- **Java 25** – the Gradle toolchain targets Java 25.
  Check with `java -version`. Install via [SDKMAN](https://sdkman.io/), [Homebrew](https://brew.sh/) (`brew install openjdk@25`), or from [Adoptium](https://adoptium.net/).

- **Docker** – required to run the WireMock stub, and by `./gradlew build`.
  Install from [Docker Desktop for Mac](https://docs.docker.com/desktop/install/mac-install/). Check with `docker --version`.

- **Gradle** – not required on your machine; the project uses the Gradle wrapper (`./gradlew`).

There is no database — the service holds no state.

## Running the service on a local machine

### 1. Start the local stack

The `docker-compose.yml` in the project root defines a WireMock stub for the reference-data backend and the service image itself:

```bash
# Stub backend only — enough to run the app from Gradle
docker compose up -d wiremock

# Full stack (WireMock + the service in a container, on port 4550)
docker compose up -d

# Stop everything
docker compose down
```

WireMock listens on `localhost:8080` and serves the mappings in `src/apiTest/resources/mappings`. That directory does not exist yet, so the stub currently starts with no stubbed responses — add mappings there to stub the reference-data backend.

### 2. Build and run the service

From the project root:

```bash
# Build without Docker
./gradlew build -x apiTest

# Run against the local WireMock stub — the local profile is what permits AUTH_MODE=OFF
SPRING_PROFILES_ACTIVE=local AUTH_MODE=OFF CP_BACKEND_URL=http://localhost:8080 ./gradlew bootRun
```

`./gradlew build` also runs `apiTest`, which starts the Docker Compose stack — use `-x apiTest` when Docker is not running.

Without `AUTH_MODE=OFF` and the `local` profile the service **will not start**: `auth.mode` defaults to `ENFORCE` and `AUTH_TENANT_ID` / `AUTH_AUDIENCE` / `AUTH_ROLES` have no defaults. That startup failure is deliberate — see [Authentication](docs/Authentication.md).

The service starts on **http://localhost:4550**.

### 3. Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `4550` | HTTP port |
| `CP_BACKEND_URL` | `http://localhost` | Reference-data backend base URL |
| `CJSCPPUID` | `00000000-0000-0000-0000-000000000000` | User UUID header sent on every backend call |
| `rpe.AppInsightsInstrumentationKey` | `00000000-0000-0000-0000-000000000000` | Azure Application Insights key |

The committed `.env` holds the values Docker Compose passes into the containerised service. It is **not** read by `./gradlew bootRun` — export the variables in your shell for Gradle runs. Its `CP_BACKEND_URL` and `CJSCPPUID` are `TO-BE-REPLACED` placeholders; point `CP_BACKEND_URL` at `http://wiremock:8080` to run the compose stack against the stub. It also sets `SPRING_PROFILES_ACTIVE=local` and `AUTH_MODE=OFF` so the containerised service starts without Entra configuration.

See [EnvironmentVariables.md](docs/EnvironmentVariables.md) for the `.env` / `.envrc` conventions.

### 4. Check the service is running

- Health: `curl http://localhost:4550/health`
- Build/version info: `curl http://localhost:4550/info`
- Metrics: `curl http://localhost:4550/prometheus`

These paths are exempt from token validation; the `/courthouses` endpoints are not.

### 5. Running tests

| Command | What it runs | Docker required |
|---------|-------------|-----------------|
| `./gradlew test` | Unit and Spring slice tests | No |
| `./gradlew apiTest` | API tests against the compose stack | Yes — builds the image, auto-starts and tears down the stack |
| `./gradlew build` | Both of the above | Yes |

`./gradlew apiTest` manages the compose lifecycle for you: it builds the service image and starts `wiremock` and `app` before the tests, then stops and removes them afterwards. There is no `src/apiTest` source set in this repo yet, so the task currently runs no tests — but it still starts and stops the stack, which is why `./gradlew build` needs Docker.

---

## Authentication

The service validates Entra app-only access tokens on every non-exempt request. `OFF` and `OBSERVE` are rejected at startup unless the `local` or `test` profile is active.

| Variable | Default | Description |
|----------|---------|-------------|
| `AUTH_MODE` | `ENFORCE` | `OFF`, `OBSERVE` or `ENFORCE` |
| `AUTH_TENANT_ID` | _(none — startup fails)_ | Token-issuing Entra tenant; derives issuer and JWKS URI |
| `AUTH_AUDIENCE` | _(none — startup fails)_ | This API's own audience |
| `AUTH_ROLES` | _(none — startup fails)_ | Comma-separated app roles accepted |
| `AUTH_CLOCK_SKEW_SECONDS` | `60` | Skew for `exp`/`nbf`, capped at 300 |
| `AUTH_JWKS_CACHE_TTL_SECONDS` | `3600` | JWKS cache lifetime |

Exempt paths are matched exactly, so `/health/x` and `/healthx` stay protected: `/`, `/health`, `/health/liveness`, `/health/readiness`, `/info`, `/prometheus`.

Full details, the claims validated and the Entra prerequisites: [docs/Authentication.md](docs/Authentication.md).

---

## Documentation

- [Authentication](docs/Authentication.md) — Entra access token validation, configuration and Entra prerequisites
- [Environment variables](docs/EnvironmentVariables.md) — `.env` and `.envrc` conventions
- [Logging](docs/Logging.md) — logback configuration, log fields and collecting logs in Azure
- [Pipeline](docs/PIPELINE.md) — CI and deployment pipeline
- [Release](docs/release.md) — the release tracks, and how to cut a release

Further documentation see the [HMCTS Marketplace Springboot template readme](https://github.com/hmcts/service-hmcts-marketplace-springboot-template/blob/main/README.md) and its [supporting docs](https://github.com/hmcts/service-hmcts-marketplace-springboot-template/blob/main/docs).

## Contribute to this repository

Contributions are welcome. See [CONTRIBUTING.md](.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).
