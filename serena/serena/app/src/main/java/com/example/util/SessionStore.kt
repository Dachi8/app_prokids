package com.example.util

import android.content.Context

/**
 * Almacena la sesión con el backend ProKids: token de acceso y dirección
 * del servidor. Se guarda en SharedPreferences para que el dispositivo siga
 * sincronizándose aunque cierre la app.
 */
class SessionStore(
    context: Context,
    prefsName: String = PREFS_NAME
) {

    private val prefs = context.applicationContext
        .getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    /** Token JWT que devuelve el servidor al registrarse o iniciar sesión. */
    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().apply {
            if (value.isNullOrBlank()) remove(KEY_TOKEN) else putString(KEY_TOKEN, value)
        }.apply()

    /** Dirección del backend (por ejemplo http://192.168.1.50:4000/). */
    var serverUrl: String?
        get() = prefs.getString(KEY_SERVER_URL, null)
        set(value) = prefs.edit().apply {
            if (value.isNullOrBlank()) remove(KEY_SERVER_URL) else putString(KEY_SERVER_URL, value)
        }.apply()

    /** Correo con el que se registró el perfil en el servidor. */
    var syncedEmail: String?
        get() = prefs.getString(KEY_SYNCED_EMAIL, null)
        set(value) = prefs.edit().apply {
            if (value.isNullOrBlank()) remove(KEY_SYNCED_EMAIL) else putString(KEY_SYNCED_EMAIL, value)
        }.apply()

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "serena_sesion"
        const val KEY_TOKEN = "token"
        const val KEY_SERVER_URL = "server_url"
        const val KEY_SYNCED_EMAIL = "synced_email"
    }
}