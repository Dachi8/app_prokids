# Serena Wellness App - Guía de Exportación y Conexión para Android Studio

Este proyecto está 100% configurado para abrirse directamente en **Android Studio** (Koala, Ladybug, Iguana o superior) y para sincronizar o fusionar su base de datos local **Room (SQLite)** con otra base de datos SQLite preexistente, base de datos de producción o backend remoto.

---

## 🚀 1. Cómo abrir y ejecutar el proyecto en Android Studio

1. **Descargar / Exportar el proyecto:**
   - En AI Studio, ve al menú superior de opciones (tres puntos o botón de exportar/ajustes) y selecciona **Export to ZIP** o **Push to GitHub**.
   - Si descargas el ZIP, descomprímelo en tu computadora en una carpeta de tu preferencia (ej. `~/AndroidStudioProjects/SerenaApp`).

2. **Abrir en Android Studio:**
   - Abre Android Studio.
   - Selecciona **File > Open...** (o *Open* desde la pantalla de bienvenida).
   - Navega a la carpeta descomprimida del proyecto y haz clic en **OK**.

3. **Sincronización de Gradle:**
   - Android Studio detectará automáticamente el archivo `settings.gradle.kts` y el archivo `gradle/libs.versions.toml`.
   - Deja que termine el **Gradle Sync** automático. Se utilizará Gradle 9.3+ y JDK 17 o 21 (incluidos por defecto en Android Studio).

4. **Ejecutar la App:**
   - Selecciona un emulador (Android 8.0 o superior, API 26-36) o un dispositivo físico por USB/Wi-Fi.
   - Presiona el botón verde de **Run (Shift + F10)**.

---

## 🗄️ 2. Base de Datos Actual (Room Database)

- **Nombre del archivo en el dispositivo:** `serena_wellness_database`
- **Ruta interna en Android:** `/data/data/com.aistudio.wellness.serena.qkrvmx/databases/serena_wellness_database`
- **Herramienta para inspeccionarla en vivo en Android Studio:**
  - Ve a **View > Tool Windows > App Inspection** y selecciona la pestaña **Database Inspector**.
  - Verás todas las tablas, columnas y podrás ejecutar consultas SQL en tiempo real:
    - `users` (perfiles de usuario, roles de familiar, médico o paciente)
    - `mood_entries` (registro diario de estado de ánimo)
    - `routines` (hábitos y rutinas con horarios y categorías)
    - `chat_messages` (historial de mensajes con Serena)
    - `journal_entries` (libreta de reflexiones y emociones)
    - `prescribed_tasks` (tareas asignadas por doctores o familiares)
    - `rewards` (catálogo de recompensas y juegos didácticos)

---

## 🔄 3. Opciones para integrar con tu otra base de datos

Dependiendo del tipo de base de datos que tengas, dispones de tres vías preparadas:

### Opción A: Tienes un archivo SQLite preexistente (`.db` o `.sqlite`)
Si ya tienes una base de datos SQLite lista con datos (por ejemplo, exportada de otra app o sistema):
1. Coloca tu archivo (ejemplo: `serena_data_backup.db`) en la carpeta:
   `app/src/main/assets/serena_data_backup.db`
2. En `AppDatabase.kt`, en el método `Room.databaseBuilder`, simplemente encadena `.createFromAsset`:
   ```kotlin
   Room.databaseBuilder(
       context.applicationContext,
       AppDatabase::class.java,
       DATABASE_NAME
   )
   .createFromAsset("serena_data_backup.db")
   .fallbackToDestructiveMigration(dropAllTables = true)
   .build()
   ```

### Opción B: Tienes una base de datos con esquemas o tablas adicionales
Si tu otra base de datos tiene tablas complementarias (ejemplo: citas médicas, medicación, etc.):
1. Crea las entidades `@Entity` correspondientes en `com.example.data.model`.
2. Crea los `@Dao` correspondientes en `com.example.data.local`.
3. Agrégalas al array de `entities` en `AppDatabase`:
   ```kotlin
   @Database(
       entities = [
           ChatMessageEntity::class,
           MoodEntryEntity::class,
           RoutineEntity::class,
           UserEntity::class,
           JournalEntryEntity::class,
           PrescribedTaskEntity::class,
           RewardItemEntity::class,
           TuNuevaEntidad::class // <-- Aquí
       ],
       version = 7, // Incrementa la versión
       exportSchema = false
   )
   ```

### Opción C: Integración con base de datos remota / en la nube (Firestore / REST API / Supabase / PostgreSQL)
Si tu otra base de datos reside en la nube o un servidor web:
- El proyecto ya tiene configurado **Retrofit**, **OkHttp**, y las dependencias de **Firebase Firestore** listas para activarse en `app/build.gradle.kts`.
- Puedes sincronizar tablas de Room mediante repositorios que consuman tu API REST o Firestore y guarden localmente en Room con `OnConflictStrategy.REPLACE`.

---

## 📱 4. Experiencia Visual y Botones de Navegación del Sistema

- La aplicación oculta automáticamente la barra de botones de navegación del sistema (`WindowInsetsCompat.Type.navigationBars()`) durante el uso mediante `WindowInsetsControllerCompat`.
- El usuario puede disfrutar de la experiencia a pantalla completa limpia, sin botones de navegación estorbando la interfaz de la app.
