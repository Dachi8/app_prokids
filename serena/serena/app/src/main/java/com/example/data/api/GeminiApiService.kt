package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateContentRequest
    ): GeminiGenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        // El backend ProKids exige el esquema "Bearer <token>".
        // Este interceptor lo añade automáticamente si no viene puesto.
        .addInterceptor { chain ->
            val original = chain.request()
            val header = original.header("Authorization")
            val updated = if (!header.isNullOrBlank() && !header.startsWith("Bearer ")) {
                original.newBuilder().header("Authorization", "Bearer $header").build()
            } else {
                original
            }
            chain.proceed(updated)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val geminiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    // ------------------------------------------------------------------
    // Backend ProKids / Asistente Virtual (Node.js + MySQL/XAMPP)
    //
    //  - 10.0.2.2 es localhost desde el emulador de Android.
    //  - En un teléfono físico hay que usar la IP local de la PC
    //    (por ejemplo http://192.168.1.50:4000/).
    //  - La URL se puede cambiar en tiempo de ejecución desde la
    //    pantalla de Configuración de la app.
    // ------------------------------------------------------------------
    @Volatile
    var proKidsBaseUrl: String = BuildConfig.PROKIDS_BASE_URL
        set(value) {
            val normalized = value.trim().let { if (it.endsWith("/")) it else "$it/" }
            if (field != normalized) field = normalized
        }

    @Volatile
    private var cachedProKidsService: ProKidsApiService? = null

    @Volatile
    private var cachedProKidsUrl: String = ""

    /** Cliente del backend ProKids. Se reconstruye si cambia la URL. */
    val proKidsService: ProKidsApiService
        get() {
            val url = proKidsBaseUrl
            val cached = cachedProKidsService
            if (cached != null && cachedProKidsUrl == url) return cached
            val service = Retrofit.Builder()
                .baseUrl(url)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(ProKidsApiService::class.java)
            cachedProKidsService = service
            cachedProKidsUrl = url
            return service
        }
}
