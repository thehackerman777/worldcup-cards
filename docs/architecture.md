# Arquitectura

## Monorepo Structure

```
worldcup-cards/
│
├── backend/                 # Spring Boot API REST
│   ├── src/main/kotlin/
│   │   ├── config/         # Spring config, seguridad, CORS
│   │   ├── controller/     # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA Entities
│   │   ├── repository/     # Spring Data Repositories
│   │   ├── security/       # JWT + Filters
│   │   └── service/        # Business logic
│   ├── Dockerfile
│   └── build.gradle.kts
│
├── android/                 # Android App
│   ├── app/
│   │   ├── di/             # Koin modules
│   │   ├── navigation/     # Routes + NavHost
│   │   ├── ui/             # Compose screens
│   │   │   ├── theme/      # MD3 theme
│   │   │   └── screens/    # Feature screens
│   │   ├── data/           # Remote + local data
│   │   │   ├── remote/     # Ktor API service
│   │   │   └── local/      # Session manager
│   │   ├── domain/         # Domain models
│   │   └── security/       # EncryptedSharedPrefs
│   └── build.gradle.kts
│
├── shared/                  # KMP shared module
│   ├── src/commonMain/     # Models, DTOs, utils
│   └── src/androidMain/    # Platform specifics
│
├── infra/                   # Docker + Nginx
│   ├── docker-compose.yml
│   ├── nginx/nginx.conf
│   └── scripts/deploy.sh
│
└── .github/workflows/       # CI/CD
```

## Clean Architecture (Android)

```
UI Layer (Compose)
    ↓ ViewModel
Domain Layer (Models, Use Cases)
    ↓ Repository Interface
Data Layer (ApiService, SessionManager)
    ↓
Ktor Client → Backend API
EncryptedSharedPreferences → Local storage
```

## Capas del Backend

```
Controller (HTTP)
    ↓
Service (Business Logic)
    ↓
Repository (Data Access)
    ↓
Entity (JPA → PostgreSQL)
```

## Principios Aplicados

- **SOLID:** Cada clase tiene una responsabilidad única
- **DRY:** Código compartido en KMP/Service layer
- **Separation of Concerns:** Capas bien definidas
- **Dependency Injection:** Koin / Spring DI
- **Repository Pattern:** Abstracción de datos
