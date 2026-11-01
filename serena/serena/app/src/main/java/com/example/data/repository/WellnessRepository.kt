package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.CanjearRequest
import com.example.data.api.CompletarTareaSyncRequest
import com.example.data.api.CrearNotaRequest
import com.example.data.api.CrearRutinaRequest
import com.example.data.api.CrearTareaSyncRequest
import com.example.data.api.CrearVinculoRequest
import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerateContentRequest
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.LoginRequest
import com.example.data.api.ProKidsApiService
import com.example.data.api.RegistrarAnimoRequest
import com.example.data.api.RegistroRequest
import com.example.data.api.RetrofitClient
import com.example.data.api.SincronizarPerfilRequest
import com.example.data.api.ToggleRutinaRequest
import com.example.data.api.UsuarioDto
import com.example.data.model.ChatMessageEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.MoodEntryEntity
import com.example.data.model.PrescribedTaskEntity
import com.example.data.model.RecordatorioEntity
import com.example.data.model.RewardItemEntity
import com.example.data.model.RolEntity
import com.example.data.model.RoutineEntity
import com.example.data.model.TareaDetalleRow
import com.example.data.model.TareaEntity
import com.example.data.model.UserEntity
import com.example.util.LinkingCode
import com.example.util.SessionStore
import com.example.util.TaskPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RegistrationOutcome(
    val user: RemoteUser,
    val message: String
)

data class RemoteUser(
    val id: Long,
    val nombre: String,
    val correo: String,
    val rol: String,
    val codigoPaciente: String,
    val puntosBalance: Int,
    val avatarEmojiStr: String
)

fun UsuarioDto.toEntity(): UserEntity = UserEntity(
    id = id,
    nombre = nombre,
    correo = correo ?: "",
    passwordHash = "",
    fechaNacimiento = fecha_nacimiento,
    estado = estado ?: "activo",
    fechaRegistro = System.currentTimeMillis(),
    ultimoAcceso = System.currentTimeMillis(),
    rol = rol ?: "PACIENTE",
    codigoPaciente = codigo_vinculacion ?: "",
    puntosBalance = puntos_balance ?: 0,
    avatarEmojiStr = avatar_emoji ?: "🌱"
)

/**
 * Repositorio 100% servidor-first para la aplicación Serena.
 *
 * MySQL (a través del backend ProKids) es la fuente principal de datos.
 * Los `StateFlow` de esta clase funcionan como caché en memoria de la información
 * devuelta por el servidor para que la interfaz en Compose responda al instante.
 */
class WellnessRepository(
    private val geminiService: GeminiApiService,
    private val proKidsServiceProvider: () -> ProKidsApiService,
    private val sessionStore: SessionStore
) {
    private val api: ProKidsApiService get() = proKidsServiceProvider()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val authHeader: String
        get() = "Bearer ${sessionStore.token ?: ""}"

    // ---------------- Estado en memoria ----------------

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserEntity>>(emptyList())
    val allUsers: StateFlow<List<UserEntity>> = _allUsers.asStateFlow()

    private val _linkedPatients = MutableStateFlow<List<UserEntity>>(emptyList())
    val linkedPatients: StateFlow<List<UserEntity>> = _linkedPatients.asStateFlow()

    private val _allRoles = MutableStateFlow<List<RolEntity>>(emptyList())
    val allRoles: StateFlow<List<RolEntity>> = _allRoles.asStateFlow()

    private val _allTareas = MutableStateFlow<List<TareaEntity>>(emptyList())
    val allTareas: StateFlow<List<TareaEntity>> = _allTareas.asStateFlow()

    private val _allRecordatorios = MutableStateFlow<List<RecordatorioEntity>>(emptyList())
    val allRecordatorios: StateFlow<List<RecordatorioEntity>> = _allRecordatorios.asStateFlow()

    private val _moods = MutableStateFlow<List<MoodEntryEntity>>(emptyList())

    private val _journal = MutableStateFlow<List<JournalEntryEntity>>(emptyList())

    private val _sharedJournal = MutableStateFlow<List<JournalEntryEntity>>(emptyList())
    val sharedJournalEntries: Flow<List<JournalEntryEntity>> = _sharedJournal

    private val _routines = MutableStateFlow<List<RoutineEntity>>(emptyList())

    private val _tasks = MutableStateFlow<List<PrescribedTaskEntity>>(emptyList())
    val allPrescribedTasks: Flow<List<PrescribedTaskEntity>> = _tasks

    private val _rewards = MutableStateFlow<List<RewardItemEntity>>(emptyList())
    val allRewards: Flow<List<RewardItemEntity>> = _rewards

    private val _detalle = MutableStateFlow<List<TareaDetalleRow>>(emptyList())

    private val _allChatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val allChatMessages: Flow<List<ChatMessageEntity>> = _allChatMessages

    private val _isAiTyping = MutableStateFlow(false)
    val isAiTyping: StateFlow<Boolean> = _isAiTyping.asStateFlow()

    init {
        // Restaura la sesión del servidor si hay token o correo guardado
        sessionStore.serverUrl?.let { RetrofitClient.proKidsBaseUrl = it }
        val savedEmail = sessionStore.syncedEmail
        if (!savedEmail.isNullOrBlank() && !sessionStore.token.isNullOrBlank()) {
            scope.launch {
                val placeholderUser = UserEntity(
                    nombre = savedEmail.substringBefore("@"),
                    correo = savedEmail
                )
                _currentUser.value = placeholderUser
                descargarDatosDe(placeholderUser)
            }
        }
    }

    // ---------------- Consultas y Filtros ----------------

    fun getTareasDetalleForToday(): Flow<List<TareaDetalleRow>> = _detalle

    fun getPatientsLinkedTo(userId: Long): Flow<List<UserEntity>> = _linkedPatients

    fun getMoodsForUser(userId: Long): Flow<List<MoodEntryEntity>> =
        _moods.map { list -> list.filter { it.userId == userId } }

    fun getMoodsForUsers(userIds: List<Long>): Flow<List<MoodEntryEntity>> =
        _moods.map { list -> list.filter { it.userId in userIds } }

    fun getMoodForToday(userId: Long): Flow<MoodEntryEntity?> =
        _moods.map { list -> list.firstOrNull { it.userId == userId && it.dateString == getTodayDateString() } }

    fun getRoutinesForUser(userId: Long): Flow<List<RoutineEntity>> =
        _routines.map { list -> list.filter { it.usuarioId == userId } }

    fun getJournalForUser(userId: Long): Flow<List<JournalEntryEntity>> =
        _journal.map { list -> list.filter { it.userId == userId } }

    fun getTasksForPatient(patientId: Long): Flow<List<PrescribedTaskEntity>> =
        _tasks.map { list -> list.filter { it.pacienteId == patientId } }

    fun getTodayDateString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    fun serverUrlActual(): String =
        sessionStore.serverUrl ?: BuildConfig.PROKIDS_BASE_URL

    fun configurarServidor(url: String) {
        sessionStore.serverUrl = url
        RetrofitClient.proKidsBaseUrl = url
    }

    // ---------------- Autenticación y Sesión ----------------

    fun adoptarUsuario(user: UserEntity) {
        _currentUser.value = user
        scope.launch { descargarDatosDe(user) }
    }

    suspend fun login(email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val resp = api.login(LoginRequest(correo = email, password = password))
            val body = resp.body()
            if (resp.isSuccessful && body?.status == "ok" && body.usuario != null) {
                val user = body.usuario.toEntity()
                body.token?.let { sessionStore.token = it }
                sessionStore.syncedEmail = email
                _currentUser.value = user
                descargarDatosDe(user)
                Result.success(user)
            } else {
                Result.failure(Exception(body?.mensaje ?: "Error al iniciar sesión (${resp.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrLoginUser(
        name: String,
        email: String,
        password: String,
        role: String,
        patientCode: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val req = RegistroRequest(
                nombre = name,
                correo = email,
                password = password,
                rol = role,
                codigo_vinculacion = patientCode.ifBlank { null }
            )
            val resp = api.registrar(req)
            val body = resp.body()
            if (resp.isSuccessful && body?.status == "ok") {
                body.token?.let { sessionStore.token = it }
                sessionStore.syncedEmail = email
                val user = UserEntity(
                    id = body.usuario_id ?: 0L,
                    nombre = name,
                    correo = email,
                    rol = body.rol ?: role,
                    codigoPaciente = body.codigo_vinculacion ?: patientCode
                )
                _currentUser.value = user
                descargarDatosDe(user)
                Result.success(user)
            } else {
                login(email, password)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sincronizarPerfilEnServidor(user: UserEntity): UserEntity? = withContext(Dispatchers.IO) {
        val token = authHeader
        try {
            val req = SincronizarPerfilRequest(
                rol = user.role,
                codigo_vinculacion = user.patientCode,
                puntos_balance = user.pointsBalance,
                avatar_emoji = user.avatarEmoji
            )
            val resp = api.sincronizarPerfil(token, req)
            val body = resp.body()
            if (resp.isSuccessful && body?.status == "ok" && body.usuario != null) {
                val updated = body.usuario.toEntity()
                _currentUser.value = updated
                updated
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun descargarDatosDe(user: UserEntity, soloTareas: Boolean = false) = withContext(Dispatchers.IO) {
        val token = authHeader
        val codigo = user.patientCode.ifBlank { null }
        try {
            // Tareas
            val respTareas = if (codigo != null) api.listarTareasPaciente(token, codigo) else api.listarMisTareas(token)
            if (respTareas.isSuccessful && respTareas.body()?.status == "ok") {
                val list = respTareas.body()?.tareas.orEmpty().map { dto ->
                    PrescribedTaskEntity(
                        id = dto.id,
                        title = dto.title,
                        description = dto.description ?: "",
                        assignedByRole = dto.assignedByRole ?: "PACIENTE",
                        assignedByName = dto.assignedByName ?: "",
                        pointsReward = dto.pointsReward ?: 50,
                        category = dto.category ?: "Autocuidado",
                        isCompleted = dto.isCompleted ?: false,
                        completedAt = dto.completedAt,
                        createdAt = dto.createdAt ?: System.currentTimeMillis(),
                        pacienteId = dto.pacienteId ?: user.id
                    )
                }
                _tasks.value = list
            }

            // Tareas Detalle Hoy
            if (codigo != null) {
                val respDetalle = api.listarDetalleHoy(token, codigo)
                if (respDetalle.isSuccessful && respDetalle.body()?.status == "ok") {
                    val list = respDetalle.body()?.detalle.orEmpty().map { d ->
                        TareaDetalleRow(
                            ejecucionId = d.ejecucionId,
                            asignacionId = d.asignacionId,
                            tareaId = d.tareaId,
                            usuarioId = d.usuarioId,
                            queHacer = d.queHacer,
                            descripcion = d.descripcion,
                            categoria = d.categoria ?: "Autocuidado",
                            frecuencia = d.frecuencia ?: "diaria",
                            horaLimite = d.horaLimite,
                            puntosRecompensa = d.puntosRecompensa ?: 50,
                            iconoKey = d.iconoKey ?: "heart",
                            fecha = d.fecha ?: "",
                            estado = d.estado ?: "pendiente",
                            fechaCompletado = d.fechaCompletado,
                            creadoPorNombre = d.creadoPorNombre ?: "",
                            creadoPorRol = d.creadoPorRol ?: ""
                        )
                    }
                    _detalle.value = list
                }
            }

            if (soloTareas) return@withContext

            // Registros de Ánimo
            val respAnimo = api.listarAnimos(token, codigo)
            if (respAnimo.isSuccessful && respAnimo.body()?.status == "ok") {
                _moods.value = respAnimo.body()?.registros.orEmpty().map { a ->
                    MoodEntryEntity(
                        id = a.id,
                        userId = a.usuario_id,
                        dateString = a.fecha_texto ?: "",
                        timestamp = a.timestamp ?: System.currentTimeMillis(),
                        moodScore = a.animo_score ?: 3,
                        moodLabel = a.animo_label ?: "",
                        moodEmoji = a.animo_emoji ?: "😐",
                        energyLevel = a.nivel_energia ?: 3,
                        emotionTags = a.etiquetas_emocion ?: "",
                        contextTags = a.etiquetas_contexto ?: "",
                        note = a.nota ?: ""
                    )
                }
            }

            // Libreta de Sentimientos
            val respNotas = api.listarNotas(token, codigo)
            if (respNotas.isSuccessful && respNotas.body()?.status == "ok") {
                _journal.value = respNotas.body()?.notas.orEmpty().map { n ->
                    JournalEntryEntity(
                        id = n.id,
                        userId = n.usuario_id,
                        title = n.titulo,
                        content = n.descripcion ?: "",
                        emotion = n.emocion ?: "Calma",
                        emotionEmoji = n.emocion_emoji ?: "💭",
                        intensity = n.intensidad ?: 3,
                        isSharedWithCareTeam = n.compartida_equipo ?: true,
                        reminderTimestamp = n.recordatorio,
                        timestamp = n.fecha_registro ?: System.currentTimeMillis(),
                        dateString = n.fecha_texto ?: ""
                    )
                }
            }

            // Libreta Compartida & Pacientes Vinculados
            if (user.role == "MEDICO" || user.role == "FAMILIAR") {
                val respShared = api.listarNotasCompartidas(token)
                if (respShared.isSuccessful && respShared.body()?.status == "ok") {
                    _sharedJournal.value = respShared.body()?.notas.orEmpty().map { n ->
                        JournalEntryEntity(
                            id = n.id,
                            userId = n.usuario_id,
                            title = n.titulo,
                            content = n.descripcion ?: "",
                            emotion = n.emocion ?: "Calma",
                            emotionEmoji = n.emocion_emoji ?: "💭",
                            intensity = n.intensidad ?: 3,
                            isSharedWithCareTeam = n.compartida_equipo ?: true,
                            reminderTimestamp = n.recordatorio,
                            timestamp = n.fecha_registro ?: System.currentTimeMillis(),
                            dateString = n.fecha_texto ?: ""
                        )
                    }
                }

                val respPacientes = api.misPacientes(token)
                if (respPacientes.isSuccessful && respPacientes.body()?.status == "ok") {
                    val list = respPacientes.body()?.pacientes.orEmpty().map { it.toEntity() }
                    _linkedPatients.value = list
                    _allUsers.value = list + listOf(user)
                }
            }

            // Rutinas
            val respRutinas = api.listarRutinas(token, codigo)
            if (respRutinas.isSuccessful && respRutinas.body()?.status == "ok") {
                _routines.value = respRutinas.body()?.rutinas.orEmpty().map { r ->
                    RoutineEntity(
                        id = r.id,
                        title = r.title,
                        description = r.description ?: "",
                        category = r.category ?: "Bienestar",
                        iconKey = r.iconKey ?: "heart",
                        completedDates = r.completedDates ?: "",
                        streak = r.streak ?: 0,
                        lastCompletedDate = r.lastCompletedDate ?: "",
                        targetDaysPerWeek = r.targetDaysPerWeek ?: 7,
                        usuarioId = r.usuario_id
                    )
                }
            }

            // Recompensas
            val respRecompensas = api.listarRecompensas(token, codigo)
            if (respRecompensas.isSuccessful && respRecompensas.body()?.status == "ok") {
                _rewards.value = respRecompensas.body()?.recompensas.orEmpty().map { rec ->
                    RewardItemEntity(
                        id = rec.id,
                        title = rec.titulo,
                        description = rec.descripcion ?: "",
                        pointsCost = rec.puntos_costo ?: 50,
                        iconEmoji = rec.icono_emoji ?: "🎁",
                        category = rec.categoria ?: "Diversión",
                        isClaimed = rec.canjeada ?: false,
                        claimedAt = rec.fecha_canje,
                        gameType = rec.tipo_juego ?: ""
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("WellnessRepository", "Error al descargar datos: ${e.message}")
        }
    }

    suspend fun servidorDisponible(): Boolean = withContext(Dispatchers.IO) {
        try {
            val resp = api.checkHealth()
            resp.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    fun logout() {
        sessionStore.clear()
        _currentUser.value = null
        _linkedPatients.value = emptyList()
        _moods.value = emptyList()
        _journal.value = emptyList()
        _routines.value = emptyList()
        _tasks.value = emptyList()
        _rewards.value = emptyList()
    }

    suspend fun linkPatientByCode(userId: Long, patientCode: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val token = authHeader
        val normalizedCode = LinkingCode.normalize(patientCode)
        try {
            val req = CrearVinculoRequest(codigo_paciente = normalizedCode)
            val resp = api.crearVinculo(token, req)
            val body = resp.body()
            if (resp.isSuccessful && body?.status == "ok" && body.paciente != null) {
                val patient = body.paciente.toEntity()
                _linkedPatients.value = _linkedPatients.value.filterNot { it.id == patient.id } + patient
                _allUsers.value = _allUsers.value.filterNot { it.id == patient.id } + patient
                Result.success(patient)
            } else {
                Result.failure(Exception(body?.mensaje ?: "No se encontró un paciente con ese código"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlinkPatient(userId: Long, patientId: Long) = withContext(Dispatchers.IO) {
        val token = authHeader
        try {
            api.desvincular(token, patientId)
            _linkedPatients.value = _linkedPatients.value.filterNot { it.id == patientId }
        } catch (e: Exception) {
            Log.e("WellnessRepository", "Error al desvincular: ${e.message}")
        }
    }

    suspend fun regenerarLinkingCode(userId: Long): Result<String> = withContext(Dispatchers.IO) {
        val token = authHeader
        try {
            val resp = api.regenerarCodigo(token)
            val body = resp.body()
            if (resp.isSuccessful && body?.status == "ok" && !body.codigo_vinculacion.isNullOrBlank()) {
                val newCode = body.codigo_vinculacion
                _currentUser.value = _currentUser.value?.copy(codigoPaciente = newCode)
                Result.success(newCode)
            } else {
                Result.failure(Exception(body?.mensaje ?: "No se pudo regenerar el código"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deactivateAccount(userId: Long) = withContext(Dispatchers.IO) {
        val token = authHeader
        try {
            api.eliminarCuenta(token)
        } catch (e: Exception) {
            Log.e("WellnessRepository", "Error al eliminar cuenta: ${e.message}")
        } finally {
            logout()
        }
    }

    // ---------------- Chat / Asistente Virtual ----------------

    suspend fun sendMessage(userText: String) = withContext(Dispatchers.IO) {
        if (userText.isBlank()) return@withContext
        _isAiTyping.value = true
        val userMsg = ChatMessageEntity(
            id = System.currentTimeMillis(),
            sender = "user",
            message = userText,
            timestamp = System.currentTimeMillis()
        )
        _allChatMessages.value = _allChatMessages.value + userMsg
        try {
            val request = GeminiGenerateContentRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = "Eres Serena, una asistente de bienestar emocional empática. Responde brevemente y de forma cálida al usuario:\n$userText"))
                    )
                ),
                generationConfig = GeminiGenerationConfig(temperature = 0.7f, maxOutputTokens = 300)
            )
            val response = geminiService.generateContent("", request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Estoy aquí para apoyarte. ¿Cómo te sientes en este momento?"
            val aiMsg = ChatMessageEntity(
                id = System.currentTimeMillis() + 1,
                sender = "serena",
                message = replyText,
                timestamp = System.currentTimeMillis()
            )
            _allChatMessages.value = _allChatMessages.value + aiMsg
        } catch (e: Exception) {
            val fallbackMsg = ChatMessageEntity(
                id = System.currentTimeMillis() + 1,
                sender = "serena",
                message = "Tómate un momento para respirar profundo. Estoy aquí para escucharte y acompañarte.",
                timestamp = System.currentTimeMillis()
            )
            _allChatMessages.value = _allChatMessages.value + fallbackMsg
        } finally {
            _isAiTyping.value = false
        }
    }

    suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        _allChatMessages.value = emptyList()
    }

    // ---------------- Ánimo ----------------

    suspend fun saveMood(
        userId: Long,
        moodScore: Int,
        moodLabel: String,
        moodEmoji: String,
        energyLevel: Int,
        emotionTags: List<String>,
        contextTags: List<String>,
        note: String
    ) = withContext(Dispatchers.IO) {
        val token = authHeader
        val dateStr = getTodayDateString()
        try {
            val req = RegistrarAnimoRequest(
                fecha_texto = dateStr,
                animo_score = moodScore,
                animo_label = moodLabel,
                animo_emoji = moodEmoji,
                nivel_energia = energyLevel,
                etiquetas_emocion = emotionTags.joinToString(","),
                etiquetas_contexto = contextTags.joinToString(","),
                nota = note,
                paciente_codigo = _currentUser.value?.patientCode
            )
            val resp = api.crearAnimo(token, req)
            val newId = resp.body()?.id ?: System.currentTimeMillis()
            val entry = MoodEntryEntity(
                id = newId,
                userId = userId,
                dateString = dateStr,
                timestamp = System.currentTimeMillis(),
                moodScore = moodScore,
                moodLabel = moodLabel,
                moodEmoji = moodEmoji,
                energyLevel = energyLevel,
                emotionTags = emotionTags.joinToString(","),
                contextTags = contextTags.joinToString(","),
                note = note
            )
            _moods.value = _moods.value.filterNot { it.dateString == dateStr && it.userId == userId } + entry
        } catch (e: Exception) {
            Log.e("WellnessRepository", "Error al guardar ánimo: ${e.message}")
        }
    }

    suspend fun deleteMood(id: Long) = withContext(Dispatchers.IO) {
        val token = authHeader
        try {
            api.borrarAnimo(token, id)
            _moods.value = _moods.value.filterNot { it.id == id }
        } catch (e: Exception) {
            Log.e("WellnessRepository", "Error al borrar ánimo: ${e.message}")
        }
    }

    // ---------------- Rutinas ----------------

    suspend fun toggleRoutineCompletion(routine: RoutineEntity) = withContext(Dispatchers.IO) {
        val token = authHeader
        val dateStr = getTodayDateString()
        try {
            val resp = api.toggleRutina(token, routine.id, ToggleRutinaRequest(fecha = dateStr))
            val body = resp.body()
            if (resp.isSuccessful && body?.status == "ok" && body.rutina != null) {
                val updated = body.rutina
                _routines.value = _routines.value.map { r ->
                    if (r.id == routine.id) {
                        r.copy(
                            completedDates = updated.completedDates ?: r.completedDates,
                            streak = updated.streak ?: r.streak,
                            lastCompletedDate = updated.lastCompletedDate ?: r.lastCompletedDate
                        )
                    } else r
                }
            }
        } catch (e: Exception) {
            Log.e("WellnessRepository", "Error al cambiar estado de rutina: ${e.message}")
        }
    }

    suspend fun addCustomRoutine(
        userId: Long,
        title: String,
        desc: String,
        category: String,
        iconKey: String
    ) = withContext(Dispatchers.IO) {
        val token = authHeader
        try {
            val req = CrearRutinaRequest(
                title = title,
                description = desc,
                category = category,
                icon_key = iconKey,
                paciente_codigo = _currentUser.value?.patientCode
            )
            val resp = api.crearRutina(token, req)
            val newId = resp.body()?.id ?: System.currentTimeMillis()
            val routine = RoutineEntity(
                id = newId,
                title = title,
                description = desc,
                category = category,
                iconKey = iconKey,
                usuarioId = userId
            )
            _routines.value = _routines.value + routine
        } catch (e: Exception) {
            Log.e("WellnessRepository", "Error al crear rutina: ${e.message}")
        }
    }

    suspend fun deleteRoutine(id: Long) = withContext(Dispatchers.IO) {
        val token = authHeader
        try {
            api.borrarRutina(token, id)
            _routines.value = _routines.value.filterNot { it.id == id }
        } catch (e: Exception) {
            Log.e("WellnessRepository", "Error al borrar rutina: ${e.message}")
        }
    }

    // ---------------- Libreta de Sentimientos ----------------

    suspend fun addJournalEntry(
        userId: Long,
        title: String,
        content: String,
        emotion: String,
        emotionEmoji: String,
        intensity: Int,
        isSharedWithCareTeam: Boolean
    ) = withContext(Dispatchers.IO) {
        val token = authHeader
        val dateStr = getTodayDateString()
        try {
            val req = CrearNotaRequest(
                titulo = title,
                descripcion = content,
                emocion = emotion,
                emocion_emoji = emotionEmoji,
                intensidad = intensity,
                compartida_equipo = isSharedWithCareTeam,
                fecha_texto = dateStr,
                paciente_codigo = _currentUser.value?.patientCode
            )
            val resp = api.crearNota(token, req)
            val newId = resp.body()?.id ?: System.currentTimeMillis()
            val entry = JournalEntryEntity(
                id = newId,
                userId = userId,
                title = title,
                content = content,
                emotion = emotion,
                emotionEmoji = emotionEmoji,
                intensity = intensity,
                isSharedWithCareTeam = isSharedWithCareTeam,
                dateString = dateStr,
                timestamp = System.currentTimeMillis()
            )
            _journal.value = _journal.value + entry
        } catch (e: Exception) {
            Log.e("WellnessRepository", "Error al crear entrada en libreta: ${e.message}")
        }
    }

    suspend fun deleteJournalEntry(id: Long) = withContext(Dispatchers.IO) {
        val token = authHeader
        try {
            api.borrarNota(token, id)
            _journal.value = _journal.value.filterNot { it.id == id }
        } catch (e: Exception) {
            Log.e("WellnessRepository", "Error al borrar entrada de libreta: ${e.message}")
        }
    }

    // ---------------- Tareas Asignadas ----------------

    suspend fun assignTask(
        title: String,
        description: String,
        assignedByRole: String,
        assignedByName: String,
        assignedById: Long,
        pacienteId: Long,
        pointsReward: Int = 60,
        category: String = "Autocuidado"
    ) = withContext(Dispatchers.IO) {
        val token = authHeader
        val targetPatientCode = _linkedPatients.value.find { it.id == pacienteId }?.patientCode
            ?: _currentUser.value?.patientCode
        try {
            val req = CrearTareaSyncRequest(
                titulo = title,
                descripcion = description,
                categoria = category,
                puntos_recompensa = pointsReward,
                paciente_codigo = targetPatientCode
            )
            val resp = api.crearTarea(token, req)
            val newId = resp.body()?.asignacion_id ?: System.currentTimeMillis()
            val task = PrescribedTaskEntity(
                id = newId,
                title = title,
                description = description,
                assignedByRole = assignedByRole,
                assignedByName = assignedByName,
                pointsReward = pointsReward,
                category = category,
                pacienteId = pacienteId
            )
            _tasks.value = _tasks.value + task
        } catch (e: Exception) {
            Log.e("WellnessRepository", "Error al asignar tarea: ${e.message}")
        }
    }

    suspend fun completePrescribedTask(task: PrescribedTaskEntity) = withContext(Dispatchers.IO) {
        val token = authHeader
        try {
            val req = CompletarTareaSyncRequest(paciente_codigo = _currentUser.value?.patientCode)
            api.completarTarea(token, task.id, req)
            _tasks.value = _tasks.value.map { t ->
                if (t.id == task.id) t.copy(isCompleted = true, completedAt = System.currentTimeMillis()) else t
            }
            _currentUser.value?.let { u ->
                _currentUser.value = u.copy(puntosBalance = u.puntosBalance + task.pointsReward)
            }
        } catch (e: Exception) {
            Log.e("WellnessRepository", "Error al completar tarea: ${e.message}")
        }
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        val token = authHeader
        try {
            api.eliminarTarea(token, id)
            _tasks.value = _tasks.value.filterNot { it.id == id }
        } catch (e: Exception) {
            Log.e("WellnessRepository", "Error al eliminar tarea: ${e.message}")
        }
    }

    suspend fun deleteTaskWithPermission(task: PrescribedTaskEntity, role: String): Boolean = withContext(Dispatchers.IO) {
        val allowed = TaskPermissions.canDelete(role, task.assignedByRole)
        if (allowed) {
            deleteTask(task.id)
        }
        allowed
    }

    // ---------------- Recompensas ----------------

    suspend fun claimReward(reward: RewardItemEntity, balance: Int, pacienteId: Long): Boolean = withContext(Dispatchers.IO) {
        if (balance < reward.pointsCost) return@withContext false
        val token = authHeader
        try {
            val req = CanjearRequest(
                recompensa_id = reward.id,
                paciente_codigo = _currentUser.value?.patientCode
            )
            val resp = api.canjearRecompensa(token, req)
            val body = resp.body()
            if (resp.isSuccessful && body?.status == "ok") {
                _rewards.value = _rewards.value.map { r ->
                    if (r.id == reward.id) r.copy(isClaimed = true, claimedAt = System.currentTimeMillis()) else r
                }
                body.puntos_balance?.let { newBal ->
                    _currentUser.value?.let { u ->
                        _currentUser.value = u.copy(puntosBalance = newBal)
                    }
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("WellnessRepository", "Error al canjear recompensa: ${e.message}")
            false
        }
    }
}
