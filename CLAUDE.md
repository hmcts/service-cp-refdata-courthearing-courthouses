## Repo: service-cp-refdata-courthearing-courthouses

Spring Boot service that proxies court house and court room reference data from the CP backend, exposing two separate REST controllers for house and room lookups.

**Pattern**: Stateless proxy
**Spring Boot version**: 4.0.1 (target 4.0.6+ per upgrade cycle)
**Implements**: `api-cp-refdata-courthearing-courthouses`

## Infrastructure

| Component | Technology | Purpose |
|---|---|---|
| CP Backend | External HTTP | Source of court house/room reference data |
| docker-compose | App container only | No WireMock in docker-compose — uses `.env` file for local config |

## Source Structure

```
uk.gov.hmcts.cp/
  Application.java                          @SpringBootApplication
  clients/
    CourtHousesClient                       RestTemplate → CP backend for both house and room lookups
  config/
    AppConfig                               @Bean RestTemplate
    AppPropertiesBackend                    @Value CP_BACKEND_URL
  controllers/
    CourtHousesController                   Handles GET /courthouses/{court_id}
    CourtRoomsController                    Handles GET /courthouses/{court_id}/courtrooms/{court_room_id}
    GlobalExceptionHandler                  @RestControllerAdvice; maps exceptions to HTTP codes
    RootController                          Returns 200 on GET /
  domain/
    CourtResponse                           Internal DTO mapping backend court house/room response
  filters/
    TracingFilter                           Reads/generates X-Correlation-Id; propagates via MDC
  mappers/
    CourtHouseMapper                        Maps CourtResponse domain DTO to API response models
  services/
    CourtHousesService                      Calls CourtHousesClient for both house and room endpoints
```

## Environment Variables

| Variable | Purpose | Default |
|---|---|---|
| `CP_BACKEND_URL` | Base URL of the CP backend (court reference data) | `http://localhost` |
| `CJSCPPUID` | User UUID header on all backend calls | `00000000-0000-0000-0000-000000000000` |
| `rpe.AppInsightsInstrumentationKey` | Azure Application Insights key | `00000000-0000-0000-0000-000000000000` |

## Repo-Specific Architecture Rules

- **Single client for two controllers**: `CourtHousesClient` handles both `/courthouses` and `/courthouses/{id}/courtrooms` calls — both controllers delegate to `CourtHousesService` which uses the same client.
- **No WireMock in docker-compose**: This repo uses a `.env` file for local environment configuration rather than WireMock stubs. Configure `CP_BACKEND_URL` to point at a real or mocked backend.
- **CJSCPPUID header**: `CourtHousesClient` sets `CJSCPPUID` on every backend request.

## Debugging

| Symptom | Cause / Fix |
|---|---|
| 404 on court house | Court ID not found in CP backend; check `CP_BACKEND_URL` and data |
| Missing court rooms in response | `CourtHouseMapper` may not be mapping `courtRooms` list; check mapper and backend response |
| Connection refused locally | `.env` file missing or `CP_BACKEND_URL` not set; confirm direnv loaded |

## Repo-Specific Notes

- `ci-build-publish.yml` present alongside standard `ci-draft.yml` / `ci-released.yml`.
- Local dev uses `.env` file (not `.envrc.example`) — ensure `CP_BACKEND_URL` and `CJSCPPUID` are set.
- No database; fully stateless.
