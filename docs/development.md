# Flujo de Desarrollo

## Workflow

```
1. Crear rama feature/XXX desde develop
2. Desarrollo local
3. Push a GitHub
4. GitHub Actions valida compilación (opcional)
5. PR a develop → merge
6. PR a main → merge
7. Tag vX.Y.Z en main → GitHub Actions genera APK
```

## Configuración del Dev-VPS

Ver `skills/kmp-dev-workflow.md` para el setup completo del dev-vps.

Resumen:
```bash
# Encender dev-vps (si detenido)
aws ec2 start-instances --instance-ids i-07427f8a9350a88e5

# SSH
ssh dev-vps

# Clonar proyecto
cd ~/projects
git clone git@github.com:tu-user/worldcup-cards.git

# Compilar
cd worldcup-cards
# Backend
cd backend && ./gradlew build
# Android
cd ../android && ./gradlew assembleDebug
```

## Comandos de Compilación

### Backend
```bash
cd backend
./gradlew build                    # Compilar + tests
./gradlew bootRun                  # Iniciar servidor
./gradlew bootJar                  # Generar JAR
```

### Android
```bash
cd android
./gradlew :app:assembleDebug       # APK Debug
./gradlew :app:assembleRelease     # APK Release
```

### Docker (backend + DB)
```bash
cd infra
docker compose up -d               # Iniciar todo
docker compose down                # Detener todo
docker compose logs -f backend     # Ver logs
```

## Commits

Seguir Conventional Commits:
- `feat: add user card management`
- `fix: resolve JWT token refresh`
- `docs: update API documentation`
- `refactor: extract exchange service`
- `chore: update dependencies`

## GitFlow

```
main      ─── v1.0.0 tags ───
develop   ─── Integración continua ───
feature/* ─── Desarrollo de features ───
```

## Checklist de Avance

### ✅ Fase 1 — Monorepo + Backend
- [x] Estructura del monorepo
- [x] Backend Spring Boot (Kotlin)
- [x] Entidades JPA (User, Card, UserCard, Exchange, ExchangeItem)
- [x] Repositorios Spring Data
- [x] Seguridad (JWT + BCrypt)
- [x] Servicios (Auth, User, Card, Album, Exchange)
- [x] Controladores REST
- [x] Migraciones Flyway
- [x] Docker + Docker Compose + Nginx
- [x] Configuración por entorno

### ✅ Fase 2 — Módulo Compartido KMP
- [x] Modelos serializables
- [x] DTOs de requests/responses
- [x] Cliente API (Ktor)
- [x] Utilidades de rareza

### ✅ Fase 3 — Android App
- [x] Gradle multi-módulo
- [x] Tema Material Design 3 + Dark Mode
- [x] Seguridad (EncryptedSharedPreferences)
- [x] DI con Koin
- [x] Sesión segura
- [x] Navegación (NavHost)
- [x] Pantalla Login/Register
- [x] Pantalla Home (estadísticas)
- [x] Pantalla Álbum
- [x] Pantalla Catálogo de Cartas
- [x] Pantalla Detalle de Carta
- [x] Pantalla Intercambios
- [x] Pantalla Detalle de Intercambio
- [x] Pantalla Nuevo Intercambio
- [x] Pantalla Configuración (URL dinámica)

### ✅ Fase 4 — Infraestructura + CI/CD
- [x] Dockerfile backend
- [x] Docker Compose (PostgreSQL + Backend + Nginx)
- [x] Nginx reverse proxy
- [x] Script de deploy
- [x] GitHub Actions (APK Debug + Release en tags)
- [x] GitHub Actions (Docker image en tags)

### ❌ Fase 5 — Próximos Pasos
- [ ] Keystore para release signing
- [ ] Pruebas unitarias
- [ ] End-to-end testing
- [ ] Pantalla de escaneo QR para intercambios
- [ ] Push notifications
- [ ] Analytics
- [ ] Crash reporting (Sentry)
- [ ] CI/CD avanzado con self-hosted runner
