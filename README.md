# ⚽ World Cup Cards — Intercambio de Álbum Mundialista

> Aplicación Android para gestión e intercambio de cartas del mundial.
> Monorepo con backend Spring Boot + app Android Kotlin Multiplatform.

## Arquitectura

```
📦 worldcup-cards/
├── 📱 android/        → App Android (Jetpack Compose, MVVM, MD3)
├── 🔄 shared/         → Módulo KMP compartido (modelos, DTOs, utilidades)
├── 🖥️ backend/        → API REST Spring Boot (Kotlin, JWT, PostgreSQL)
├── 🐳 infra/          → Docker, Nginx, docker-compose para despliegue
├── 🔧 .github/        → CI/CD (GitHub Actions)
└── 📋 docs/           → Documentación técnica
```

## Stack Tecnológico

### Android
- **Kotlin Multiplatform** (enfocado Android)
- **Jetpack Compose** + Material Design 3
- **MVVM** con ViewModel + StateFlow
- **Koin** para inyección de dependencias
- **Ktor Client** para networking
- **EncryptedSharedPreferences** + Android Keystore (sesión segura)
- **Coil** para carga de imágenes
- **Navigation Compose** para navegación
- **Dark Mode** nativo

### Backend
- **Kotlin** + **Spring Boot 3**
- **Spring Security** + JWT (BCrypt)
- **Spring Data JPA** / Hibernate + Flyway
- **PostgreSQL**
- **Docker** + Docker Compose
- Nginx como reverse proxy

### DevOps
- **GitHub Actions** (APK Debug + Release en tags)
- **Docker** multi-stage builds
- Configuración dinámica por entorno

## Requisitos

- Java 21 JDK
- Android SDK 34+
- Docker + Docker Compose (para backend local)
- PostgreSQL (o usar Docker)

## Inicio Rápido (Desarrollo Local)

### 1. Backend

```bash
cd backend
# Opción A: Con Docker
docker compose -f ../infra/docker-compose.yml up -d db

# Opción B: Directo con Gradle
./gradlew bootRun
```

El backend arranca en `http://localhost:8080`.

### 2. Android

```bash
cd android
./gradlew assembleDebug
```

Configurar la URL del servidor en la app:
- Abrir la app → Settings → Configurar servidor
- O editar `android/app/src/main/assets/server_config.json`

### 3. Compilación Completa

```bash
# Desde la raíz del proyecto
cd backend && ./gradlew build
cd ../android && ./gradlew assembleDebug
```

## Endpoints API

### Autenticación
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/auth/register` | Registrar usuario |
| POST | `/api/v1/auth/login` | Iniciar sesión |
| POST | `/api/v1/auth/refresh` | Refrescar token |

### Usuarios
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/users/me` | Perfil del usuario autenticado |
| PUT | `/api/v1/users/me` | Actualizar perfil |

### Cartas
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/cards` | Listar todas las cartas disponibles |
| GET | `/api/v1/cards/{id}` | Detalle de una carta |

### Álbum / Repetidas
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/album` | Obtener mi álbum completo |
| POST | `/api/v1/album/cards` | Agregar carta al álbum |
| GET | `/api/v1/album/repeated` | Listar mis cartas repetidas |
| DELETE | `/api/v1/album/cards/{id}` | Eliminar carta del álbum |

### Intercambios
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/exchanges` | Crear solicitud de intercambio |
| GET | `/api/v1/exchanges` | Listar intercambios del usuario |
| GET | `/api/v1/exchanges/available` | Ver intercambios disponibles (otros usuarios) |
| PUT | `/api/v1/exchanges/{id}/accept` | Aceptar intercambio |
| PUT | `/api/v1/exchanges/{id}/reject` | Rechazar intercambio |
| PUT | `/api/v1/exchanges/{id}/complete` | Completar intercambio |

## Variables de Entorno

### Backend

| Variable | Descripción | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Puerto del servidor | `8080` |
| `DB_HOST` | Host de PostgreSQL | `localhost` |
| `DB_PORT` | Puerto DB | `5432` |
| `DB_NAME` | Nombre DB | `worldcup_cards` |
| `DB_USER` | Usuario DB | `wcapp` |
| `DB_PASSWORD` | Contraseña DB | *(requerida)* |
| `JWT_SECRET` | Secreto JWT | *(requerida)* |
| `JWT_EXPIRATION` | Expiración en ms | `86400000` (24h) |

### Android

Configurable desde la app (Settings → Servidor):
- URL base del servidor
- Puerto

## CI/CD

- **Push a `main`** → Build de verificación (sin artefactos)
- **Tags en `main`** → Build + APK Debug + APK Release
  - Formato tag sugerido: `v1.0.0`, `v1.1.0`

```bash
# Crear release
git tag v1.0.0
git push origin v1.0.0
```

## Licencia

MIT
