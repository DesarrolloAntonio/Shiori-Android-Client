# Shiori Android Client

Android client for [Shiori](https://github.com/go-shiori/shiori) bookmark manager.

## Architecture

Clean Architecture with 6 modules:

```
:presentation (app) ─── Jetpack Compose, ViewModels, Navigation, Koin DI
    ├── :domain ──────── Use cases / business logic
    │   ├── :data ────── Room DB, DataStore, Repositories, WorkManager sync
    │   │   ├── :network ── Retrofit APIs, OkHttp interceptors, DTOs
    │   │   ├── :model ──── Shared domain models (pure Kotlin)
    │   │   └── :common ─── Result<T> sealed class, ErrorHandler
    │   ├── :model
    │   └── :common
    ├── :model
    ├── :network
    └── :common
```

## Key Tech Stack

- **UI:** Jetpack Compose + Material3 + Navigation Compose
- **DI:** Koin 3.3.3
- **Network:** Retrofit 2.9.0 + OkHttp + Gson + ScalarsConverterFactory
- **DB:** Room 2.6.1 with KSP
- **Async:** Kotlin Coroutines + Flow + Paging 3
- **Background:** WorkManager (SyncWorker for offline-first sync)
- **Images:** Coil 2.7.0
- **Preferences:** DataStore (Preferences + Proto)
- **SDK:** minSdk 26, targetSdk 35, JDK 21

## Module Details

### :network
- `RetrofitNetwork.kt` - All API endpoint definitions
- `NetworkingModule.kt` - OkHttp client with interceptors:
  - **Session→Bearer interceptor**: Converts `X-Session-Id` header to `Authorization: Bearer` for v1.8.0+ compatibility
  - **NetworkLoggerInterceptor**: Logs requests/responses to StateFlow for debug UI
- DTOs handle dual response format (legacy + v1.8.0 wrapped `{"ok":bool,"message":data}`)

### :data
- `BookmarksRepositoryImpl.kt` - Main repository, handles network+cache via NetworkBoundResource
- `SyncWorker.kt` - WorkManager worker for offline CREATE/UPDATE/DELETE/CACHE sync
- `BookmarksDao.kt` - Room DAO with `@Transaction` for tag cross-refs
- `BookmarksDatabase.kt` - Room DB v7, entities: BookmarkEntity, TagEntity, BookmarkTagCrossRef, BookmarkHtmlEntity
- `SettingsPreferencesDataSourceImpl.kt` - DataStore for user prefs, session, token

### :domain
- Use cases in `domain/usecase/` - one per operation
- `AddBookmarkUseCase` creates temp ID with `System.currentTimeMillis()/1000`
- `SyncBookmarksUseCase` handles paginated sync with server
- `SendLogoutUseCase` performs full cleanup (cancel workers, reset prefs, delete data)

### :presentation
- ViewModels: `FeedViewModel`, `BookmarkViewModel`, `LoginViewModel`, `SettingsViewModel`, `SearchViewModel`, `ReadableContentViewModel`
- Navigation: Single-activity with `NavHost` in `Navigation.kt`
- DI: `AppModule.kt` registers all Koin modules
- Flavors: `production` (signed release) and `staging` (debug)

### :model
- Pure data classes: `Bookmark`, `Tag`, `User`, `Account`, etc.
- `Bookmark.isPendingServerProcessing` detects temp IDs (id > 1_000_000)

### :common
- `Result<T>` sealed class: Success/Loading/Error
- `ErrorType` sealed class: DatabaseError, IOError, HttpError, SessionExpired, Unknown
- `ErrorHandler` interface

## Shiori v1.8.0+ API Compatibility

- Server wraps ALL responses in `{"ok":bool,"message":data}` via MessageResponseMiddleware
- Header `X-Shiori-Response-Format: new` bypasses wrapping (not used by app)
- Login field must be `"remember_me"` (bool), NOT `"remember"` (int)
- OkHttp interceptor converts `X-Session-Id` → `Authorization: Bearer` for legacy endpoints
- ScalarsConverterFactory sends String @Body as `text/plain` — need explicit `@Headers("Content-Type: application/json")`
- V1 login endpoint: `POST /api/v1/auth/login`; refresh with `POST /api/v1/auth/refresh`
- Bookmark list/create/update/delete are still the **legacy** `/api/bookmarks` routes.
  There is no `GET`/`POST /api/v1/bookmarks` — verified against the upstream route table
  (`internal/http/server.go`). The v1 bookmark routes are only `{id}/readable`, `{id}/tags`,
  `bulk/tags` and `cache`.
- The legacy `/api/accounts` family and `/api/login`, `/api/logout` were removed upstream

## Branching & Workflow

- `develop` for active development
- `master` for releases
- PRs target `master`

## Build & Run

```bash
# Debug build
./gradlew :presentation:assembleProductionDebug

# Release build (needs signing env vars)
./gradlew :presentation:assembleProductionRelease

# Tests
./gradlew test
./gradlew connectedAndroidTest
```

## Code Conventions

- Kotlin throughout, no Java
- Compose for all UI (no XML layouts)
- Koin for DI (not Hilt/Dagger)
- `Flow<Result<T>>` pattern for async operations with loading/error states
- `UiState<T>` wrapper in presentation layer
- `NetworkBoundResource` / `NetworkNoCacheResource` for repo operations
- Room entities separate from domain models (mapped via `Mapper.kt`)
