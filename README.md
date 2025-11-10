SolarScan - Empowering Pakistan With Solar Energy

**IU Spectrum 1.0 Hackathon Winner**

Overview

SolarScan is an Android app that scans or uploads electricity bills, extracts key billing fields, and generates data‑driven solar installation recommendations. It combines on‑device OCR with ML Kit, robust bill parsing, a cloud analysis API, and local persistence to deliver actionable insights including system size, cost, savings, payback, and CO₂ reduction.

Key Features

- Scan, upload, or enter bill data manually (tabbed scanner workflow)
- On‑device OCR (Google ML Kit Text Recognition)
- Robust bill text parsing for units, total cost, and billing date
- Cloud analysis via Retrofit (recommendations, assumptions, and budget fit)
- Local SQLite storage of bills and recommendations
- Dashboard with bill history and quick navigation to details
- Detailed recommendation view (system size, monthly production, savings, payback, CO₂ reduction, notes)

Tech Stack

- Language: Kotlin (JVM target 11)
- Min/Target/Compile SDK: minSdk 24, targetSdk 36, compileSdk 36
- UI: AndroidX, Material, ViewPager2, Navigation Component, ConstraintLayout
- Camera & OCR: CameraX, Google ML Kit Text Recognition
- Networking: Retrofit2, OkHttp (with logging), Gson
- Concurrency: Kotlin Coroutines, AndroidX Lifecycle/ViewModel
- Persistence: SQLite via `SQLiteOpenHelper`

Architecture

- UI: `Fragments` (`DashboardFragment`, `ScannerFragment`, `DetailsFragment`) hosted in `MainActivity`
- State: `ViewModel` (`SolarViewModel`, `BillViewModel`) with LiveData for loading/error/navigation events
- Data:
  - Remote: `ApiClient` + `ApiService` (`POST /api/analyze-bill`)
  - Local: `DatabaseHelper` (bills and recommendations tables)
  - Parsing: `BillParser` (regex‑based field extraction)
- Repository: `SolarRepository` orchestrates network calls and wraps results

User Flow

1) Scan/Upload/Manual: Capture bill text with CameraX + ML Kit, upload an image, or paste text.
2) Parse: Extract units, total cost, and billing date with `BillParser`.
3) Analyze: Send text (and optional budget) to backend (`/api/analyze-bill`).
4) Persist: Save bill and returned recommendation locally.
5) Review: Browse bills on Dashboard; open Details for system sizing, savings, and insights.

Project Structure

```
app/src/main/java/com/talha/solarscan/
  bill/                 # Bill model, adapter, parser, repository for bill list
  data/
    local/              # SQLite helper and recommendation model
    remote/             # Retrofit client, service, and API models
  fragments/
    scanner/            # Scan, Upload, Manual tabs and pager adapter
    DashboardFragment   # Bill history
    DetailsFragment     # Solar recommendation details
  main/                 # MainActivity
  repository/           # SolarRepository (network orchestration)
  viewmodel/            # SolarViewModel, Event wrapper
```

Backend API

- Base URL: configured in `ApiClient` (`https://ss-sand-three.vercel.app/`)
- Endpoint: `POST /api/analyze-bill`
- Request:
  - `text` (string): raw bill text
  - `budget` (int, optional): user budget
- Response: `parsedFields` (unitsKWh, totalCost, billingDate, …), `recommendation` (suggestedSystemKw, estMonthlyProductionKwh, estMonthlySavings, approxInstallCost, paybackYears, co2ReductionTonsPerYear, percentOffset, notes), `assumptions`, `meta`

Local Data

- Bills table: id, units, cost, billing_date, created_at
- Recommendations table: bill_id (unique FK), system size, production, savings, install cost, payback, CO₂ reduction, percent offset, notes, assumptions, created_at

Prerequisites

- Android Studio Koala+
- JDK 11
- Android SDK Platform 36 (compile/target) and build tools

Getting Started

1) Clone the repository.
2) Open in Android Studio and let Gradle sync.
3) Build and run on a device or emulator (API 24+).

Configuration

- API Base URL: edit `ApiClient.BASE_URL` if your backend endpoint differs.
- Logging: OkHttp logging interceptor is enabled (BODY). Adjust for release as needed.
- ProGuard: see `app/proguard-rules.pro` for release configurations.

Testing

- Unit tests: `app/src/test`
- Instrumented tests: `app/src/androidTest`

Security & Privacy

- OCR and parsing happen on device; analysis requests send extracted text (and optional budget) to the configured backend.
- Do not include sensitive PII in bills. Review and harden logging before production (disable BODY logs).

Build Notes

- Kotlin JVM target 11; ensure Gradle and Android Gradle Plugin match the included `gradle/wrapper` and `libs.versions.toml`.
- CameraX and ML Kit require camera permission and a real device for best results.

License

Copyright © The project authors. If you plan to open‑source, add a license here.


