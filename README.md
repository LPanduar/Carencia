# Carencia — App móvil

App móvil del videojuego **Carencia**: un joven de 17 años busca a su hermana, cuya existencia niega su propia familia, cruzando portales que se abren desde el espejo de su baño.

| Carpeta | Contenido |
|---|---|
| `CarenciaApi/CarenciaAPi` | API REST en ASP.NET (.NET 10) con **Dapper**, **Swagger**, JWT y SQL Server |
| `CarenciaApi/Domain` | Biblioteca de clases: entidades, DTOs y `Response<T>` |
| `CarenciaApi/CarenciaAPi/Database` | Script `CarenciaDB.sql` (tablas, procedimientos almacenados y datos) |
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
Ejecuta `CarenciaApi/CarenciaAPi/Database/CarenciaDB.sql` en SQL Server (SSMS → F5).
Crea la base `CarenciaDB` con los datos iniciales y el usuario administrador `admin`
(la contraseña inicial se comparte por privado dentro del equipo).

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
Abre `CarenciaApp` en Android Studio y ejecuta.
- Emulador: usa `http://10.0.2.2:5101/` (ya configurado).
- Celular físico: cambia `API_BASE_URL` en `app/build.gradle.kts` por la IP de tu PC.
- Android 17+: acepta el permiso **"Dispositivos cercanos"** (acceso a la red local).

## Flujo de trabajo (Git Flow)

- `main`: versiones entregables (etiquetas `vX.Y.Z`).
- `develop`: integración.
- `feature/*`: nuevas funciones, salen de `develop` y regresan a `develop`.
- `release/*` y `hotfix/*`: preparación de versiones y correcciones urgentes.

```bash
git flow feature start nombre
git flow feature finish nombre
```
