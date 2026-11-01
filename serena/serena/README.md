<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/e852209a-4296-4059-b04b-164ad57070e9

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.

---

# Serena · ProKids — Documentación técnica

## Base de datos (Room)

| Detalle | Valor |
|---|---|
| Motor | Room (SQLite) |
| Archivo | `bdd_proyecto_asistente.db` |
| Versión actual | **9** |
| Migraciones | `MIGRATION_8_9` (`AppDatabase.MIGRATION_8_9`) |

La migración **8 → 9** añade el aislamiento de datos por perfil:

- `prescribed_tasks.paciente_id` — cada tarea pertenece a un paciente concreto.
- `routine_items.usuario_id` — las rutinas quedan aisladas por usuario.
- Índice `index_usuarios_codigo_paciente` — acelera la búsqueda del código de vinculación.

El esquema se valida automáticamente en `DatabaseMigrationTest` (Robolectric), que recrea la base v8,
aplica la migración y comprueba que los datos previos se conservan.

## Códigos de vinculación

- Cada perfil (paciente, médico o familiar) recibe un **código único** `SERENA-XXXX`
  generado con `SecureRandom` y validado contra toda la base de datos
  (`WellnessRepository.generateUniqueLinkingCode`).
- El código se ve en **Configuración → Código de Vinculación Único** (con botón *Copiar*
  y opción de **generar uno nuevo**).
- Un médico o familiar puede **vincular varios pacientes**: la relación vive en
  `representantes_pacientes` y se gestiona desde Configuración o desde el chip `+ Vincular`
  de los tableros de médico y familiar.

## Desactivar cuenta

Configuración → **Desactivar cuenta** elimina el usuario, su código de vinculación,
sus vínculos y todos sus datos (tareas, rutinas, registros de ánimo y libreta).

## Permisos sobre tareas

Regla implementada en `TaskPermissions`:

| Perfil | Puede eliminar |
|---|---|
| Paciente | Solo sus propias metas (`PACIENTE`) |
| Médico | Solo las tareas que prescribió (`MEDICO`) |
| Familiar | Solo las tareas que asignó (`FAMILIAR`) |
| Administrador | Todas |

## Diseño responsivo

`ui/theme/Responsive.kt` centraliza las medidas por tipo de pantalla:

| Clase | Ancho (dp) | Escala | Ancho máximo de contenido |
|---|---|---|---|
| `COMPACT` | < 600 | 1.00 | sin límite |
| `MEDIUM` | 600 – 839 | 1.06 | 640 dp |
| `EXPANDED` | 840 – 1199 | 1.12 | 760 dp |
| `ULTRA_WIDE` | ≥ 1200 (1920x1080, 2400x1080) | 1.20 | 900 dp |

Escala de botones, iconos, tipografías y espaciados; el área táctil nunca baja de 44 dp.

## Pruebas

```bash
./gradlew testDebugUnitTest
```

Cubre el generador de códigos, la matriz de permisos, las dimensiones responsivas
y la migración de base de datos.

---

# Vincular dos dispositivos (paciente ↔ médico / familiar)

## Cómo funciona

Cada perfil tiene un **código de vinculación** único (`SERENA-XXXX`) que lo
identifica en el backend ProKids. Ese código es la llave que permite que un
móvil encuentre al perfil que está en **otro** dispositivo.

```
  Móvil A (paciente)              Servidor ProKids            Móvil B (médico)
  ───────────────────             ──────────────────          ──────────────────
  Crea su perfil  ──registro──▶  guarda nombre + código
  Muestra SERENA-1234  ◀───────  ◀──resuelve por código──  Escribe SERENA-1234
                                                          Crea el vínculo
  Recibe las tareas  ◀──────────  ◀────tareas/ánimo/notas──  Asigna una tarea
```

La app sigue siendo **local first**: si el servidor está apagado, todo
funciona igual en local. La sincronización es un extra.

## Puesta en marcha

### 1. Backend (en tu computadora)

```bash
# MySQL: inicia XAMPP (Apache + MySQL)
cd C:\Users\Vheiron\Desktop\app_prokids-main\app_prokids-main
npm run dev            # http://localhost:4000
```

Aplica el esquema (solo la primera vez):

```bash
C:\xampp\mysql\bin\mysql.exe -u root < database/schema.sql
```

> Si la base ya existía, aplica solo la migración de vinculación:
> `C:\xampp\mysql\bin\mysql.exe -u root < database/migracion_vinculacion.sql`

### 2. Dirección del servidor en la app

| Desde | Dirección |
|---|---|
| Emulador de Android | `http://10.0.2.2:4000/` |
| Teléfono físico (misma Wi-Fi) | `http://<IP-DE-TU-PC>:4000/` — ej. `http://192.168.2.3:4000/` |
| Servidor público | `https://tu-servidor.com/` |

Puedes cambiarla **en caliente** en **Configuración → Sincronización con ProKids**
(botón *Probar* para comprobar la conexión), o fijarla en `local.properties`:

```properties
PROKIDS_BASE_URL=http://192.168.2.3:4000/
```

### 3. Vincular

1. El paciente abre la app y ve su código en **Configuración → Código de
   Vinculación Único** (botón *Copiar*).
2. El médico/familiar abre **Configuración → Pacientes Vinculados → Vincular**
   (o el chip `+ Vincular` en su tablero) y escribe ese código.
3. Se crea el vínculo en los dos dispositivos y en el servidor.

## Qué se sincroniza

| Dato | Dirección |
|---|---|
| Tareas | médico/familiar → paciente, y paciente → médico (cumplimientos) |
| Ánimo | paciente → médico/familiar |
| Libreta (notas compartidas) | paciente → médico/familiar |
| Perfil y código | siempre |

## Endpoints nuevos del backend

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/vinculaciones/paciente/:codigo` | Resuelve un código → paciente |
| `POST` | `/api/vinculaciones` | Crea el vínculo |
| `GET` | `/api/vinculaciones/mis-pacientes` | Lista los pacientes vinculados |
| `DELETE` | `/api/vinculaciones/:pacienteId` | Elimina el vínculo |
| `GET/POST/PATCH/DELETE` | `/api/sync/tareas/...` | Tareas (3 niveles de ProKids) |
| `GET/POST` | `/api/sync/animo/...` | Registros de ánimo |
| `GET/POST/DELETE` | `/api/sync/notas/...` | Notas de la libreta |

> Nota técnica: el paciente se identifica en el servidor por su **código**, no
> por su id numérico, porque cada dispositivo tiene su propia base local y los
> ids no coinciden entre la base del móvil y la de MySQL.


