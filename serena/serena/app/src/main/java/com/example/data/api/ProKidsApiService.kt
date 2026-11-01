package com.example.data.api

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Servicio Retrofit del backend ProKids (Node.js + MySQL).
 *
 * Toda la información de Serena vive en MySQL. La app no guarda datos en el
 * dispositivo: cada pantalla lee y escribe a través de este servicio.
 *
 * Los endpoints de bienestar aceptan `codigo=SERENA-XXXX` para que un médico
 * o familiar consulte la información de un paciente vinculado.
 */
interface ProKidsApiService {
    @GET("api/health")
    suspend fun checkHealth(): Response<HealthResponse>

    // ---------------- Autenticación y perfil ----------------

    @POST("api/auth/registro")
    suspend fun registrar(
        @Body request: RegistroRequest
    ): Response<RegistroResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/auth/perfil")
    suspend fun obtenerPerfil(
        @Header("Authorization") token: String
    ): Response<PerfilResponse>

    @POST("api/auth/sincronizar-perfil")
    suspend fun sincronizarPerfil(
        @Header("Authorization") token: String,
        @Body request: SincronizarPerfilRequest
    ): Response<PerfilResponse>

    @POST("api/auth/regenerar-codigo")
    suspend fun regenerarCodigo(
        @Header("Authorization") token: String
    ): Response<RegenerarCodigoResponse>

    @DELETE("api/auth/cuenta")
    suspend fun eliminarCuenta(
        @Header("Authorization") token: String
    ): Response<GenericResponse>

    // ---------------- Vinculación entre dispositivos ----------------

    @GET("api/vinculaciones/paciente/{codigo}")
    suspend fun resolverCodigo(
        @Header("Authorization") token: String,
        @Path("codigo") codigo: String
    ): Response<ResolverVinculacionResponse>

    @POST("api/vinculaciones")
    suspend fun crearVinculo(
        @Header("Authorization") token: String,
        @Body request: CrearVinculoRequest
    ): Response<CrearVinculoResponse>

    @GET("api/vinculaciones/mis-pacientes")
    suspend fun misPacientes(
        @Header("Authorization") token: String
    ): Response<MisPacientesResponse>

    @DELETE("api/vinculaciones/{pacienteId}")
    suspend fun desvincular(
        @Header("Authorization") token: String,
        @Path("pacienteId") pacienteId: Long
    ): Response<GenericResponse>

    // ---------------- Tareas (plantilla → asignación → ejecución) ----------------

    @GET("api/tareas-prokids/mis-tareas")
    suspend fun listarMisTareas(
        @Header("Authorization") token: String
    ): Response<ListarTareasResponse>

    @GET("api/tareas-prokids/paciente/{codigo}")
    suspend fun listarTareasPaciente(
        @Header("Authorization") token: String,
        @Path("codigo") codigo: String
    ): Response<ListarTareasResponse>

    @GET("api/tareas-prokids/detalle/{codigo}")
    suspend fun listarDetalleHoy(
        @Header("Authorization") token: String,
        @Path("codigo") codigo: String
    ): Response<ListarDetalleResponse>

    @POST("api/sync/tareas")
    suspend fun crearTarea(
        @Header("Authorization") token: String,
        @Body request: CrearTareaSyncRequest
    ): Response<CrearTareaSyncResponse>

    @PATCH("api/sync/tareas/{tareaId}/completar")
    suspend fun completarTarea(
        @Header("Authorization") token: String,
        @Path("tareaId") tareaId: Long,
        @Body request: CompletarTareaSyncRequest
    ): Response<GenericResponse>

    @DELETE("api/sync/tareas/{tareaId}")
    suspend fun eliminarTarea(
        @Header("Authorization") token: String,
        @Path("tareaId") tareaId: Long
    ): Response<GenericResponse>

    // ---------------- Ánimo ----------------

    @GET("api/animo")
    suspend fun listarAnimos(
        @Header("Authorization") token: String,
        @Query("codigo") codigo: String? = null
    ): Response<ListarAnimoResponse>

    @POST("api/animo")
    suspend fun crearAnimo(
        @Header("Authorization") token: String,
        @Body request: RegistrarAnimoRequest
    ): Response<CrearAnimoResponse>

    @DELETE("api/animo/{id}")
    suspend fun borrarAnimo(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<GenericResponse>

    // ---------------- Libreta de sentimientos ----------------

    @GET("api/notas")
    suspend fun listarNotas(
        @Header("Authorization") token: String,
        @Query("codigo") codigo: String? = null
    ): Response<ListarNotasResponse>

    @GET("api/notas/compartidas")
    suspend fun listarNotasCompartidas(
        @Header("Authorization") token: String
    ): Response<ListarNotasResponse>

    @POST("api/notas")
    suspend fun crearNota(
        @Header("Authorization") token: String,
        @Body request: CrearNotaRequest
    ): Response<CrearNotaResponse>

    @DELETE("api/notas/{id}")
    suspend fun borrarNota(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<GenericResponse>

    // ---------------- Recompensas ----------------

    @GET("api/recompensas")
    suspend fun listarRecompensas(
        @Header("Authorization") token: String,
        @Query("codigo") codigo: String? = null
    ): Response<RecompensasListResponse>

    @POST("api/recompensas/canjear")
    suspend fun canjearRecompensa(
        @Header("Authorization") token: String,
        @Body request: CanjearRequest
    ): Response<CanjearResponse>

    // ---------------- Rutinas ----------------

    @GET("api/rutinas")
    suspend fun listarRutinas(
        @Header("Authorization") token: String,
        @Query("codigo") codigo: String? = null
    ): Response<ListarRutinasResponse>

    @POST("api/rutinas")
    suspend fun crearRutina(
        @Header("Authorization") token: String,
        @Body request: CrearRutinaRequest
    ): Response<CrearRutinaResponse>

    @PATCH("api/rutinas/{id}/toggle")
    suspend fun toggleRutina(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: ToggleRutinaRequest
    ): Response<ToggleRutinaResponse>

    @DELETE("api/rutinas/{id}")
    suspend fun borrarRutina(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<GenericResponse>

    // ---------------- Dispositivos push ----------------

    @GET("api/dispositivos")
    suspend fun listarDispositivos(
        @Header("Authorization") token: String
    ): Response<ListarDispositivosResponse>

    @POST("api/dispositivos")
    suspend fun registrarDispositivo(
        @Header("Authorization") token: String,
        @Body request: RegistrarDispositivoRequest
    ): Response<GenericResponse>
}

// ===================== Autenticación =====================

@JsonClass(generateAdapter = true)
data class HealthResponse(
    val status: String,
    val mensaje: String? = null
)

@JsonClass(generateAdapter = true)
data class RegistroRequest(
    val nombre: String,
    val correo: String,
    val password: String? = null,
    val fecha_nacimiento: String? = null,
    val rol: String? = null,
    val codigo_vinculacion: String? = null,
    val puntos_balance: Int? = null,
    val avatar_emoji: String? = null
)

@JsonClass(generateAdapter = true)
data class RegistroResponse(
    val status: String,
    val mensaje: String? = null,
    val usuario_id: Long? = null,
    val token: String? = null,
    val codigo_vinculacion: String? = null,
    val rol: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val correo: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val correo: String? = null,
    val rol: String? = null,
    val codigo_vinculacion: String? = null,
    val puntos_balance: Int? = null,
    val avatar_emoji: String? = null,
    val fecha_nacimiento: String? = null,
    val estado: String? = null,
    val parentesco: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val status: String,
    val token: String? = null,
    val usuario: UsuarioDto? = null,
    val mensaje: String? = null
)

@JsonClass(generateAdapter = true)
data class PerfilResponse(
    val status: String,
    val usuario: UsuarioDto? = null,
    val mensaje: String? = null
)

@JsonClass(generateAdapter = true)
data class SincronizarPerfilRequest(
    val rol: String? = null,
    val codigo_vinculacion: String? = null,
    val puntos_balance: Int? = null,
    val avatar_emoji: String? = null
)

@JsonClass(generateAdapter = true)
data class RegenerarCodigoResponse(
    val status: String,
    val codigo_vinculacion: String? = null,
    val mensaje: String? = null
)

// ===================== Vinculación =====================

@JsonClass(generateAdapter = true)
data class ResolverVinculacionResponse(
    val status: String,
    val paciente: UsuarioDto? = null,
    val mensaje: String? = null
)

@JsonClass(generateAdapter = true)
data class CrearVinculoRequest(
    val codigo_paciente: String,
    val parentesco: String? = null
)

@JsonClass(generateAdapter = true)
data class CrearVinculoResponse(
    val status: String,
    val mensaje: String? = null,
    val paciente: UsuarioDto? = null
)

@JsonClass(generateAdapter = true)
data class MisPacientesResponse(
    val status: String,
    val pacientes: List<UsuarioDto>? = null,
    val mensaje: String? = null
)

// ===================== Tareas =====================

/** Una asignación de tarea con su estado de hoy. `id` = tarea_asignaciones.id. */
@JsonClass(generateAdapter = true)
data class TareaServidorDto(
    val id: Long,
    val title: String,
    val description: String? = null,
    val assignedByRole: String? = null,
    val assignedByName: String? = null,
    val pointsReward: Int? = null,
    val category: String? = null,
    val isCompleted: Boolean? = null,
    val completedAt: Long? = null,
    val createdAt: Long? = null,
    val pacienteId: Long? = null,
    val tareaId: Long? = null
)

@JsonClass(generateAdapter = true)
data class ListarTareasResponse(
    val status: String,
    val tareas: List<TareaServidorDto>? = null,
    val mensaje: String? = null
)

/** Fila de `tarea_ejecuciones` unida a plantilla y creador. */
@JsonClass(generateAdapter = true)
data class TareaDetalleDto(
    val ejecucionId: Long,
    val asignacionId: Long,
    val tareaId: Long,
    val usuarioId: Long,
    val queHacer: String,
    val descripcion: String? = null,
    val categoria: String? = null,
    val frecuencia: String? = null,
    val horaLimite: String? = null,
    val puntosRecompensa: Int? = null,
    val iconoKey: String? = null,
    val fecha: String? = null,
    val estado: String? = null,
    val fechaCompletado: Long? = null,
    val creadoPorNombre: String? = null,
    val creadoPorRol: String? = null
)

@JsonClass(generateAdapter = true)
data class ListarDetalleResponse(
    val status: String,
    val detalle: List<TareaDetalleDto>? = null,
    val fecha: String? = null,
    val mensaje: String? = null
)

@JsonClass(generateAdapter = true)
data class CrearTareaSyncRequest(
    val titulo: String,
    val descripcion: String? = null,
    val categoria: String? = null,
    val puntos_recompensa: Int? = null,
    val paciente_codigo: String? = null,
    val frecuencia: String? = null,
    val hora_limite: String? = null,
    val icono_key: String? = null
)

@JsonClass(generateAdapter = true)
data class CrearTareaSyncResponse(
    val status: String,
    val mensaje: String? = null,
    val tarea_id: Long? = null,
    val asignacion_id: Long? = null
)

@JsonClass(generateAdapter = true)
data class CompletarTareaSyncRequest(
    val paciente_codigo: String? = null
)

// ===================== Ánimo =====================

@JsonClass(generateAdapter = true)
data class AnimoServidorDto(
    val id: Long,
    val usuario_id: Long,
    val fecha_texto: String? = null,
    val timestamp: Long? = null,
    val animo_score: Int? = null,
    val animo_label: String? = null,
    val animo_emoji: String? = null,
    val nivel_energia: Int? = null,
    val etiquetas_emocion: String? = null,
    val etiquetas_contexto: String? = null,
    val nota: String? = null
)

@JsonClass(generateAdapter = true)
data class ListarAnimoResponse(
    val status: String,
    val registros: List<AnimoServidorDto>? = null,
    val mensaje: String? = null
)

@JsonClass(generateAdapter = true)
data class CrearAnimoResponse(
    val status: String,
    val id: Long? = null,
    val registro: AnimoServidorDto? = null,
    val mensaje: String? = null
)

@JsonClass(generateAdapter = true)
data class RegistrarAnimoRequest(
    val fecha_texto: String? = null,
    val animo_score: Int? = null,
    val animo_label: String? = null,
    val animo_emoji: String? = null,
    val nivel_energia: Int? = null,
    val etiquetas_emocion: String? = null,
    val etiquetas_contexto: String? = null,
    val nota: String? = null,
    val paciente_codigo: String? = null
)

// ===================== Notas =====================

@JsonClass(generateAdapter = true)
data class NotaServidorDto(
    val id: Long,
    val usuario_id: Long,
    val titulo: String,
    val descripcion: String? = null,
    val emocion: String? = null,
    val emocion_emoji: String? = null,
    val intensidad: Int? = null,
    val compartida_equipo: Boolean? = null,
    val recordatorio: Long? = null,
    val fecha_registro: Long? = null,
    val fecha_texto: String? = null
)

@JsonClass(generateAdapter = true)
data class ListarNotasResponse(
    val status: String,
    val notas: List<NotaServidorDto>? = null,
    val mensaje: String? = null
)

@JsonClass(generateAdapter = true)
data class CrearNotaRequest(
    val titulo: String,
    val descripcion: String? = null,
    val emocion: String? = null,
    val emocion_emoji: String? = null,
    val intensidad: Int? = null,
    val compartida_equipo: Boolean? = null,
    val recordatorio: Long? = null,
    val fecha_texto: String? = null,
    val paciente_codigo: String? = null
)

@JsonClass(generateAdapter = true)
data class CrearNotaResponse(
    val status: String,
    val id: Long? = null,
    val nota: NotaServidorDto? = null,
    val mensaje: String? = null
)

// ===================== Recompensas =====================

@JsonClass(generateAdapter = true)
data class RecompensasListResponse(
    val status: String,
    val recompensas: List<RecompensaDto>? = null,
    val mensaje: String? = null
)

@JsonClass(generateAdapter = true)
data class RecompensaDto(
    val id: Long,
    val titulo: String,
    val descripcion: String? = null,
    val puntos_costo: Int? = null,
    val icono_emoji: String? = null,
    val categoria: String? = null,
    val tipo_juego: String? = null,
    val canjeada: Boolean? = null,
    val fecha_canje: Long? = null
)

@JsonClass(generateAdapter = true)
data class CanjearRequest(
    val recompensa_id: Long,
    val paciente_codigo: String? = null
)

@JsonClass(generateAdapter = true)
data class CanjearResponse(
    val status: String,
    val mensaje: String? = null,
    val puntos_balance: Int? = null,
    val recompensa_id: Long? = null
)

// ===================== Rutinas =====================

@JsonClass(generateAdapter = true)
data class RutinaServidorDto(
    val id: Long,
    val usuario_id: Long,
    val title: String,
    val description: String? = null,
    val category: String? = null,
    val iconKey: String? = null,
    val completedDates: String? = null,
    val streak: Int? = null,
    val lastCompletedDate: String? = null,
    val targetDaysPerWeek: Int? = null
)

@JsonClass(generateAdapter = true)
data class ListarRutinasResponse(
    val status: String,
    val rutinas: List<RutinaServidorDto>? = null,
    val mensaje: String? = null
)

@JsonClass(generateAdapter = true)
data class CrearRutinaRequest(
    val title: String,
    val description: String? = null,
    val category: String? = null,
    val icon_key: String? = null,
    val target_days_per_week: Int? = null,
    val paciente_codigo: String? = null
)

@JsonClass(generateAdapter = true)
data class CrearRutinaResponse(
    val status: String,
    val id: Long? = null,
    val rutina: RutinaServidorDto? = null,
    val mensaje: String? = null
)

@JsonClass(generateAdapter = true)
data class ToggleRutinaRequest(
    val fecha: String
)

@JsonClass(generateAdapter = true)
data class ToggleRutinaResponse(
    val status: String,
    val rutina: RutinaServidorDto? = null,
    val mensaje: String? = null
)

// ===================== Dispositivos =====================

@JsonClass(generateAdapter = true)
data class RegistrarDispositivoRequest(
    val fcm_token: String,
    val plataforma: String? = "android"
)

@JsonClass(generateAdapter = true)
data class DispositivoDto(
    val id: Long,
    val usuario_id: Long,
    val fcm_token: String,
    val plataforma: String? = null,
    val fecha_registro: Long? = null
)

@JsonClass(generateAdapter = true)
data class ListarDispositivosResponse(
    val status: String,
    val dispositivos: List<DispositivoDto>? = null,
    val mensaje: String? = null
)

@JsonClass(generateAdapter = true)
data class GenericResponse(
    val status: String,
    val mensaje: String? = null
)
