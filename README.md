# Service CP Refdata Court Hearing CourtHouses

The Court Houses API provides information about court buildings and the rooms within them — the building name, postal address and room details for a given court.

| Endpoint | Returns |
|----------|---------|
| `GET /courthouses/{court_id}` | The court house and its rooms |
| `GET /courthouses/{court_id}/courtrooms/{court_room_id}` | A single court room within a court house |

## Architecture overview

The service is a stateless proxy with no database. It calls the CP reference-data backend (`CP_BACKEND_URL` + `/referencedata-service/query/api/rest/referencedata/courtrooms`), passing the `CJSCPPUID` header on every request, and maps the response to the API contract.

Actuator is mounted at the **root** context (`management.endpoints.web.base-path: /`), so the operational endpoints are `/health`, `/info` and `/prometheus` — not under `/actuator`.

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

# Run against the local WireMock stub
CP_BACKEND_URL=http://localhost:8080 ./gradlew bootRun
```

`./gradlew build` also runs `apiTest`, which starts the Docker Compose stack — use `-x apiTest` when Docker is not running.

The service starts on **http://localhost:4550**.

### 3. Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `4550` | HTTP port |
| `CP_BACKEND_URL` | `http://localhost` | Reference-data backend base URL |
| `CJSCPPUID` | `00000000-0000-0000-0000-000000000000` | User UUID header sent on every backend call |
| `rpe.AppInsightsInstrumentationKey` | `00000000-0000-0000-0000-000000000000` | Azure Application Insights key |

The committed `.env` holds the values Docker Compose passes into the containerised service. It is **not** read by `./gradlew bootRun` — export the variables in your shell for Gradle runs. Its `CP_BACKEND_URL` and `CJSCPPUID` are `TO-BE-REPLACED` placeholders; point `CP_BACKEND_URL` at `http://wiremock:8080` to run the compose stack against the stub.

See [EnvironmentVariables.md](docs/EnvironmentVariables.md) for the `.env` / `.envrc` conventions.

### 4. Check the service is running

- Health: `curl http://localhost:4550/health`
- Build/version info: `curl http://localhost:4550/info`
- Metrics: `curl http://localhost:4550/prometheus`

### 5. Running tests

| Command | What it runs | Docker required |
|---------|-------------|-----------------|
| `./gradlew test` | Unit and Spring slice tests | No |
| `./gradlew apiTest` | API tests against the compose stack | Yes — builds the image, auto-starts and tears down the stack |
| `./gradlew build` | Both of the above | Yes |

`./gradlew apiTest` manages the compose lifecycle for you: it builds the service image and starts `wiremock` and `app` before the tests, then stops and removes them afterwards. There is no `src/apiTest` source set in this repo yet, so the task currently runs no tests — but it still starts and stops the stack, which is why `./gradlew build` needs Docker.

---

## Documentation

- [Environment variables](docs/EnvironmentVariables.md) — `.env` and `.envrc` conventions
- [Logging](docs/Logging.md) — logback configuration, log fields and collecting logs in Azure
- [Pipeline](docs/PIPELINE.md) — CI and deployment pipeline
- [Release](docs/release.md) — the release tracks, and how to cut a release

Further documentation see the [HMCTS Marketplace Springboot template readme](https://github.com/hmcts/service-hmcts-marketplace-springboot-template/blob/main/README.md) and its [supporting docs](https://github.com/hmcts/service-hmcts-marketplace-springboot-template/blob/main/docs).

## Contribute to this repository

Contributions are welcome. See [CONTRIBUTING.md](.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).
