package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.WellnessRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class WellnessViewModel(
    private val repository: WellnessRepository
) : ViewModel() {

    // --- Users & Session (ProKids IAM) ---
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allRoles: StateFlow<List<RolEntity>> = repository.allRoles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- ProKids Tareas (3 niveles) & Recordatorios ---
    val allTareas: StateFlow<List<TareaEntity>> = repository.allTareas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayTareasDetalle: StateFlow<List<TareaDetalleRow>> = repository.getTareasDetalleForToday()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allRecordatorios: StateFlow<List<RecordatorioEntity>> = repository.allRecordatorios
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Perfil autenticado; el repositorio restaura la sesiÃ³n guardada. */
    val currentUser: StateFlow<UserEntity?> = repository.currentUser

    @OptIn(ExperimentalCoroutinesApi::class)
    val linkedPatients: StateFlow<List<UserEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null && (user.role == "MEDICO" || user.role == "FAMILIAR")) {
                repository.getPatientsLinkedTo(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedPatient = MutableStateFlow<UserEntity?>(null)
    val selectedPatient: StateFlow<UserEntity?> = _selectedPatient.asStateFlow()

    init {
        // Al iniciar sesiÃ³n, un mÃ©dico o familiar supervisa automÃ¡ticamente a su
        // primer paciente vinculado (puede cambiar a otro en cualquier momento).
        viewModelScope.launch {
            combine(currentUser, linkedPatients) { user, patients -> user to patients }
                .collect { (user, patients) ->
                    if (user != null && user.role != "PACIENTE" &&
                        _selectedPatient.value == null && patients.isNotEmpty()
                    ) {
                        _selectedPatient.value = patients.first()
                    }
                }
        }
    }

    /**
     * Identificador del perfil cuyos datos se estÃ¡n mostrando.
     * - PACIENTE: su propio id.
     * - MEDICO / FAMILIAR: el paciente que estÃ¡ supervisando.
     */
    private val activePatientId: StateFlow<Long?> = combine(currentUser, _selectedPatient) { user, selected ->
        when {
            user == null -> null
            user.role == "PACIENTE" -> user.id
            else -> selected?.id
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /** Patient points observed dynamically from patient user in db */
    val patientPoints: StateFlow<Int> =
        combine(repository.allUsers, currentUser, _selectedPatient) { users, user, selected ->
            val targetId = when {
                user == null -> null
                user.role == "PACIENTE" -> user.id
                else -> selected?.id
            }
            if (targetId != null) {
                users.find { it.id == targetId }?.pointsBalance ?: 0
            } else {
                users.firstOrNull { it.role == "PACIENTE" }?.pointsBalance ?: 0
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // --- Chat & Mood & Routines ---
    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.allChatMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val moodEntries: StateFlow<List<MoodEntryEntity>> =
        combine(activePatientId, linkedPatients) { id, patients ->
            if (id != null) {
                repository.getMoodsForUser(id)
            } else if (patients.isNotEmpty()) {
                // Sin paciente seleccionado mostramos el historial de todos sus pacientes.
                repository.getMoodsForUsers(patients.map { it.id })
            } else {
                flowOf(emptyList())
            }
        }.flatMapLatest { flow ->
            flow
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayMood: StateFlow<MoodEntryEntity?> = currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getMoodForToday(user.id) else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val routines: StateFlow<List<RoutineEntity>> = activePatientId
        .flatMapLatest { id ->
            if (id != null) repository.getRoutinesForUser(id) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Journal / Libreta de Sentimientos ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val allJournalEntries: StateFlow<List<JournalEntryEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getJournalForUser(user.id) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Notas compartidas por los pacientes vinculados al perfil (mÃ©dico / familiar). */
    val sharedJournalEntries: StateFlow<List<JournalEntryEntity>> =
        combine(repository.sharedJournalEntries, linkedPatients) { entries, patients ->
            if (patients.isEmpty()) {
                emptyList()
            } else {
                val ids = patients.map { it.id }.toSet()
                entries.filter { it.userId in ids }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Prescribed Tasks (Doctor / Family / Patient) ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val prescribedTasks: StateFlow<List<PrescribedTaskEntity>> = activePatientId
        .flatMapLatest { id ->
            if (id != null) repository.getTasksForPatient(id) else repository.allPrescribedTasks
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Rewards Catalog ---
    val rewards: StateFlow<List<RewardItemEntity>> = repository.allRewards
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isAiTyping = MutableStateFlow(false)
    val isAiTyping: StateFlow<Boolean> = _isAiTyping.asStateFlow()

    /** Estado de sincronizaciÃ³n con el backend ProKids. */
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    /** true si el backend ProKids estÃ¡ accesible. */
    private val _serverOnline = MutableStateFlow(false)
    val serverOnline: StateFlow<Boolean> = _serverOnline.asStateFlow()

    private val _greetingMessage = MutableStateFlow(getGreetingForCurrentTime())
    val greetingMessage: StateFlow<String> = _greetingMessage.asStateFlow()

    private val _dailyAffirmation = MutableStateFlow(getDailyAffirmation())
    val dailyAffirmation: StateFlow<String> = _dailyAffirmation.asStateFlow()

    fun getTodayDateString(): String = repository.getTodayDateString()

    // --- User Actions ---

    /** Acceso rápido cuando la sesión ya está autenticada. */
    fun selectUser(user: UserEntity) {
        _selectedPatient.value = null
        repository.adoptarUsuario(user)
    }

    /**
     * Inicia sesiÃ³n o crea una cuenta contra MySQL.
     *
     * - [isLogin] = true: valida correo y contraseÃ±a.
     * - [isLogin] = false: registra la cuenta (si el correo ya existe y la
     *   contraseÃ±a coincide, el backend devuelve la sesiÃ³n existente).
     */
    fun loginOrRegister(
        name: String,
        email: String,
        password: String,
        role: String,
        patientCode: String,
        isLogin: Boolean,
        onComplete: () -> Unit,
        onMessage: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSyncing.value = true
            val resultado = if (isLogin) {
                repository.login(email, password)
            } else {
                repository.createOrLoginUser(name, email, password, role, patientCode)
            }
            _isSyncing.value = false
            resultado.fold(
                onSuccess = { usuario ->
                    _serverOnline.value = true
                    onMessage("Bienvenido/a, ${usuario.name}")
                    onComplete()
                },
                onFailure = { error ->
                    onMessage(error.message ?: "No se pudo iniciar sesiÃ³n")
                }
            )
        }
    }

    /**
     * Vuelve a publicar el perfil en el backend y descarga los datos del
     * paciente que se estÃ© supervisando.
     */
    fun sincronizarConServidor(user: UserEntity) {
        viewModelScope.launch {
            _isSyncing.value = true
            val actualizado = repository.sincronizarPerfilEnServidor(user)
            if (actualizado != null) {
                _serverOnline.value = true
                _selectedPatient.value?.let { paciente ->
                    repository.descargarDatosDe(paciente)
                }
                if (user.role == "PACIENTE") {
                    repository.descargarDatosDe(actualizado, soloTareas = true)
                }
            } else {
                _serverOnline.value = false
            }
            _isSyncing.value = false
        }
    }

    /** Descarga del servidor los datos del paciente vinculado. */
    fun refrescarDatosDelPaciente() {
        val paciente = _selectedPatient.value ?: return
        viewModelScope.launch {
            _isSyncing.value = true
            repository.descargarDatosDe(paciente)
            _isSyncing.value = false
        }
    }

    /** Comprueba si el servidor ProKids estÃ¡ accesible. */
    fun comprobarServidor(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repository.servidorDisponible()
            _serverOnline.value = ok
            onResult(ok)
        }
    }

    /** Cambia la direcciÃ³n del backend ProKids. */
    fun configurarServidor(url: String) {
        repository.configurarServidor(url)
        currentUser.value?.let { sincronizarConServidor(it) }
    }

    fun urlServidorActual(): String = repository.serverUrlActual()

    fun logout() {
        _selectedPatient.value = null
        repository.logout()
    }

    fun selectPatient(patient: UserEntity) {
        _selectedPatient.value = patient
        // Al cambiar de paciente se descargan sus datos del servidor.
        viewModelScope.launch {
            repository.descargarDatosDe(patient)
        }
    }

    /**
     * Vincula un paciente adicional al perfil del mÃ©dico o familiar.
     * Se pueden vincular tantos pacientes como se necesite.
     */
    fun linkPatient(patientCode: String, onResult: (Boolean, String) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.linkPatientByCode(user.id, patientCode)
            result.fold(
                onSuccess = { patient ->
                    _selectedPatient.value = patient
                    onResult(true, "Â¡Paciente ${patient.name} vinculado con Ã©xito!")
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "No se pudo vincular al paciente")
                }
            )
        }
    }

    /** Elimina el vÃ­nculo con uno de los pacientes, sin tocar sus datos. */
    fun unlinkPatient(patient: UserEntity, onResult: (Boolean, String) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.unlinkPatient(user.id, patient.id)
            if (_selectedPatient.value?.id == patient.id) {
                _selectedPatient.value = null
            }
            onResult(true, "${patient.name} ya no estÃ¡ vinculado a tu perfil")
        }
    }

    /** Genera un nuevo cÃ³digo de vinculaciÃ³n para el perfil actual. */
    fun regenerateLinkingCode(onResult: (Boolean, String) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.regenerarLinkingCode(user.id).fold(
                onSuccess = { code ->
                    onResult(true, "Nuevo cÃ³digo generado: $code")
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "No se pudo generar un nuevo cÃ³digo")
                }
            )
        }
    }

    fun deactivateAccount(onComplete: () -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.deactivateAccount(user.id)
            _selectedPatient.value = null
            onComplete()
        }
    }

    // --- Chat Actions ---
    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            _isAiTyping.value = true
            try {
                repository.sendMessage(userText)
            } finally {
                _isAiTyping.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    // --- Mood Actions ---
    fun saveMood(
        moodScore: Int,
        moodLabel: String,
        moodEmoji: String,
        energyLevel: Int,
        emotionTags: List<String>,
        contextTags: List<String>,
        note: String,
        onSuccess: () -> Unit = {}
    ) {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.saveMood(
                userId = userId,
                moodScore = moodScore,
                moodLabel = moodLabel,
                moodEmoji = moodEmoji,
                energyLevel = energyLevel,
                emotionTags = emotionTags,
                contextTags = contextTags,
                note = note
            )
            onSuccess()
        }
    }

    fun deleteMood(id: Long) {
        viewModelScope.launch {
            repository.deleteMood(id)
        }
    }

    // --- Routines Actions ---
    fun toggleRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            repository.toggleRoutineCompletion(routine)
        }
    }

    fun addRoutine(title: String, desc: String, category: String, iconKey: String = "heart") {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.addCustomRoutine(userId, title, desc, category, iconKey)
        }
    }

    fun deleteRoutine(id: Long) {
        viewModelScope.launch {
            repository.deleteRoutine(id)
        }
    }

    // --- Journal / Libreta de Sentimientos ---
    fun addJournalEntry(
        title: String,
        content: String,
        emotion: String,
        emotionEmoji: String,
        intensity: Int,
        isSharedWithCareTeam: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.addJournalEntry(
                userId = userId,
                title = title,
                content = content,
                emotion = emotion,
                emotionEmoji = emotionEmoji,
                intensity = intensity,
                isSharedWithCareTeam = isSharedWithCareTeam
            )
            onSuccess()
        }
    }

    fun deleteJournalEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteJournalEntry(id)
        }
    }

    // --- Prescribed Tasks (Doctor / Family) ---
    fun assignTask(
        title: String,
        description: String,
        category: String,
        pointsReward: Int = 60,
        onSuccess: () -> Unit = {}
    ) {
        val user = currentUser.value ?: return
        val pacienteId = activePatientId.value
        viewModelScope.launch {
            repository.assignTask(
                title = title,
                description = description,
                assignedByRole = user.role,
                assignedByName = user.name,
                assignedById = user.id,
                pacienteId = pacienteId ?: 1L,
                pointsReward = pointsReward,
                category = category
            )
            onSuccess()
        }
    }

    /**
     * El paciente puede aÃ±adir sus propias metas personales.
     * Quedan marcadas como `PACIENTE` para que solo Ã©l pueda gestionarlas
     * (y no pueda eliminar las tareas de su mÃ©dico o su familia).
     */
    fun addPatientPersonalTask(
        title: String,
        description: String,
        category: String = "Personal",
        onSuccess: () -> Unit = {}
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.assignTask(
                title = title,
                description = description,
                assignedByRole = "PACIENTE",
                assignedByName = user.name.ifBlank { "MÃ­ mismo" },
                assignedById = user.id,
                pacienteId = user.id,
                pointsReward = 30,
                category = category
            )
            onSuccess()
        }
    }

    fun completeTask(task: PrescribedTaskEntity) {
        viewModelScope.launch {
            repository.completePrescribedTask(task)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    /**
     * Elimina una tarea respetando los permisos:
     * el paciente no puede borrar tareas asignadas por su mÃ©dico o su familia.
     */
    fun deleteTaskWithPermission(task: PrescribedTaskEntity, onResult: (Boolean) -> Unit) {
        val role = currentUser.value?.role ?: "PACIENTE"
        viewModelScope.launch {
            val allowed = repository.deleteTaskWithPermission(task, role)
            onResult(allowed)
        }
    }

    // --- Rewards Actions ---
    fun claimReward(reward: RewardItemEntity, onResult: (Boolean) -> Unit) {
        val pacienteId = activePatientId.value ?: currentUser.value?.id ?: 1L
        val balance = patientPoints.value
        viewModelScope.launch {
            val success = repository.claimReward(reward, balance, pacienteId)
            onResult(success)
        }
    }

    // --- Wellness Reminder Notification ---
    fun sendWellnessNotification(context: Context): Boolean {
        return NotificationHelper.showWellnessReminder(
            context = context,
            title = "Momento de Bienestar ðŸŒ±",
            message = "TÃ³mate una pausa de 2 minutos para respirar, escribir en tu libreta y registrar tu Ã¡nimo."
        )
    }

    fun schedulePeriodicReminders(context: Context, intervalHours: Int): Boolean {
        return NotificationHelper.schedulePeriodicReminders(context, intervalHours)
    }

    fun cancelPeriodicReminders(context: Context) {
        NotificationHelper.cancelPeriodicReminders(context)
    }

    fun isPeriodicReminderActive(context: Context): Boolean {
        return NotificationHelper.isPeriodicReminderActive(context)
    }

    fun getSavedIntervalHours(context: Context): Int {
        return NotificationHelper.getSavedIntervalHours(context)
    }

    private fun getGreetingForCurrentTime(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Buenos dÃ­as"
            in 12..18 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }

    private fun getDailyAffirmation(): String {
        val affirmations = listOf(
            "Merezco paz, amor y paciencia conmigo mismo/a en cada paso de este dÃ­a.",
            "No tengo que tener todo resuelto hoy. Un paso pequeÃ±o a la vez es suficiente.",
            "Mis emociones son vÃ¡lidas. Me permito sentirlas sin juzgarme.",
            "Soy mÃ¡s fuerte y resiliente de lo que mis dudas intentan hacerme creer.",
            "Mi valor como persona no depende de mi productividad ni de complacer a los demÃ¡s.",
            "Elijo respirar hondo, soltar lo que no puedo controlar y abrazar el presente.",
            "Hoy me trato con la misma ternura y comprensiÃ³n que le ofrecerÃ­a a un amigo querido."
        )
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return affirmations[dayOfYear % affirmations.size]
    }
}

class WellnessViewModelFactory(
    private val repository: WellnessRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WellnessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WellnessViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
