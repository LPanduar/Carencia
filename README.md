# Carencia — App móvil

App móvil del videojuego **Carencia**: un joven de 17 años busca a su hermana, cuya existencia niega su propia familia, cruzando portales que se abren desde el espejo de su baño.

| Carpeta | Contenido |
|---|---|
| `CarenciaApi/CarenciaAPi` | API REST en ASP.NET (.NET 10) con **Dapper**, **Swagger**, JWT y SQL Server |
| `CarenciaApi/Domain` | Biblioteca de clases: entidades, DTOs y `Response<T>` |
| `CarenciaApi/CarenciaAPi/Database` | Scripts SQL de la base (**no están en el repositorio**; se comparten por privado dentro del equipo) |
| `CarenciaApp` | App Android en **Kotlin + Jetpack Compose** (diseño del Figma oficial) |

## Funcionalidades

- **Splash**, **Inicio**, **Galería**, **Niveles** y **Novedades** con navegación inferior.
- Contenido dinámico desde la API: niveles, galería, novedades y estadísticas.
- **Búsqueda y filtros** por categoría en galería y novedades.
- **Panel de administración** (login exclusivo de admin, rol en JWT):
  CRUD de novedades (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`) y de la galería oficial.
- Componentes del dispositivo:
  - **Cámara** ("El espejo", CameraX): las fotos se guardan en el teléfono.
  - **Almacenamiento**: "Mis fotos" y descarga de fotos oficiales a la galería del teléfono.
  - **Notificaciones**: aviso cuando el admin publica una novedad (WorkManager).

## Cómo correrlo

### 1. Base de datos
Los scripts SQL **no se suben al repositorio**. Pídelos al equipo y cópialos en
`CarenciaApi/CarenciaAPi/Database/` (la carpeta está en `.gitignore`).

En SQL Server (SSMS → F5):
1. `CarenciaApi/CarenciaAPi/Database/00_CrearBaseLocal.sql` → crea la base `CarenciaDB`.
2. `CarenciaApi/CarenciaAPi/Database/CarenciaDB.sql` **dentro de `CarenciaDB`** → tablas, datos iniciales
   y el usuario administrador `admin` (la contraseña inicial se comparte por privado dentro del equipo).

### 2. API
La **cadena de conexión** y la **clave del JWT** no están en el repositorio.
Configúralas una sola vez con User Secrets (reemplaza `TU_INSTANCIA_SQL` por el nombre de tu instancia de SQL Server):

```bash
cd CarenciaApi/CarenciaAPi
dotnet user-secrets set "ConnectionStrings:DefaultConnection" "Server=TU_INSTANCIA_SQL;Database=CarenciaDB;Trusted_Connection=True;TrustServerCertificate=True;"
dotnet user-secrets set "Jwt:Key" "una-clave-larga-y-aleatoria-de-al-menos-32-caracteres"
dotnet run --launch-profile http
```

En Visual Studio también puedes hacerlo con clic derecho al proyecto → **Administrar secretos de usuario**.

Swagger: <http://localhost:5101/swagger>
(en producción van en las variables de entorno `ConnectionStrings__DefaultConnection` y `Jwt__Key`).

### 3. App Android
Abre `CarenciaApp` en Android Studio y ejecuta (variante **debug**).
- Emulador: usa `http://10.0.2.2:5101/` (ya configurado en el `buildType` debug).
- Celular físico en tu red: cambia la URL del `buildType` debug en `app/build.gradle.kts` por la IP de tu PC.
- Android 17+: acepta el permiso **"Dispositivos cercanos"** (acceso a la red local).

## Producción (MonsterASP.NET)

### API + base de datos
1. En el panel de MonsterASP crea un **sitio web** (te da una URL `https://xxxx.runasp.net`) y una **base de datos MSSQL**.
2. Conéctate a esa base con SSMS (datos del panel) y ejecuta **sólo** `Database/CarenciaDB.sql`.
3. En el sitio → **Environment variables**, agrega (nunca en archivos del repo):

   | Variable | Valor |
   |---|---|
   | `ConnectionStrings__DefaultConnection` | La cadena de conexión que te da el panel |
   | `Jwt__Key` | Una clave aleatoria de 64+ caracteres (distinta a la de desarrollo) |
   | `Swagger__Habilitado` | `true` si quieres Swagger en línea para la demo (opcional) |

4. Descarga el **perfil de publicación (Web Deploy)** del panel y en Visual Studio:
   clic derecho a `CarenciaAPi` → **Publicar** → **Importar perfil** → **Publicar**.
5. Entra a la app con `admin` y **cambia la contraseña** desde el panel de administración.

### APK de producción
1. Pon la URL del sitio en `CarenciaApp/gradle.properties` → `carencia.apiUrl=https://xxxx.runasp.net/`.
2. La firma se lee de `CarenciaApp/keystore.properties` + `carencia-release.jks` (sólo en la PC de quien publica; **no se suben**).
3. `./gradlew :app:assembleRelease` → `app/build/outputs/apk/release/app-release.apk`.

## Flujo de trabajo (Git Flow)

- `main`: versiones entregables (etiquetas `vX.Y.Z`).
- `develop`: integración.
- `feature/*`: nuevas funciones, salen de `develop` y regresan a `develop`.
- `release/*` y `hotfix/*`: preparación de versiones y correcciones urgentes.

```bash
git flow feature start nombre
git flow feature finish nombre
```
