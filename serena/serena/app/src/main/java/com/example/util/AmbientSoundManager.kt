package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

enum class AmbientSoundType(
    val id: String,
    val title: String,
    val emoji: String,
    val subtitle: String,
    val benefits: String
) {
    RAIN(
        id = "rain",
        title = "Lluvia de Calma",
        emoji = "🌧️",
        subtitle = "Gotas suaves en el cristal",
        benefits = "Alivia la sobrecarga sensorial y crea un refugio acogedor"
    ),
    OCEAN(
        id = "ocean",
        title = "Olas del Mar",
        emoji = "🌊",
        subtitle = "Mareas rítmicas de paz",
        benefits = "Sincroniza y acompasa el ritmo de tu respiración profunda"
    ),
    FOREST(
        id = "forest",
        title = "Bosque y Brisa",
        emoji = "🌲",
        subtitle = "Viento entre copas y hojas",
        benefits = "Conecta con la naturaleza y calma pensamientos acelerados"
    ),
    STREAM(
        id = "stream",
        title = "Arroyo de Montaña",
        emoji = "💧",
        subtitle = "Agua cristalina en movimiento",
        benefits = "Limpia la tensión muscular y estimula la serenidad"
    ),
    CAMPFIRE(
        id = "campfire",
        title = "Hoguera Acogedora",
        emoji = "🔥",
        subtitle = "Calor y crujidos relajantes",
        benefits = "Brinda sensación de protección, cobijo y confort"
    ),
    ZEN_BOWLS(
        id = "zen_bowls",
        title = "Cuencos Tibetanos",
        emoji = "🔔",
        subtitle = "Frecuencia armónica 432 Hz",
        benefits = "Equilibra las emociones y propicia una relajación profunda"
    )
}

object AmbientSoundManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var playbackJob: Job? = null
    private var timerJob: Job? = null
    private var audioTrack: AudioTrack? = null

    private const val SAMPLE_RATE = 22050
    private const val BUFFER_SIZE = 2048

    private val _currentSound = MutableStateFlow<AmbientSoundType?>(null)
    val currentSound: StateFlow<AmbientSoundType?> = _currentSound.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _volume = MutableStateFlow(0.75f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _timerMinutes = MutableStateFlow(0) // 0 = Continuo
    val timerMinutes: StateFlow<Int> = _timerMinutes.asStateFlow()

    private val _timerSecondsLeft = MutableStateFlow(0)
    val timerSecondsLeft: StateFlow<Int> = _timerSecondsLeft.asStateFlow()

    fun playSound(soundType: AmbientSoundType) {
        if (_currentSound.value == soundType && _isPlaying.value) {
            pause()
            return
        }

        stop()
        _currentSound.value = soundType
        _isPlaying.value = true

        startAudioLoop(soundType)
        startTimerCountdown()
    }

    fun togglePlayPause() {
        val sound = _currentSound.value ?: AmbientSoundType.RAIN
        if (_isPlaying.value) {
            pause()
        } else {
            playSound(sound)
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (_: Exception) {}
    }

    fun resume() {
        val sound = _currentSound.value ?: AmbientSoundType.RAIN
        _isPlaying.value = true
        startAudioLoop(sound)
    }

    fun stop() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        timerJob?.cancel()
        timerJob = null
        _timerSecondsLeft.value = 0

        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        try {
            audioTrack?.setVolume(clamped)
        } catch (_: Exception) {}
    }

    fun setTimer(minutes: Int) {
        _timerMinutes.value = minutes
        if (_isPlaying.value) {
            startTimerCountdown()
        }
    }

    private fun startTimerCountdown() {
        timerJob?.cancel()
        val minutes = _timerMinutes.value
        if (minutes <= 0) {
            _timerSecondsLeft.value = 0
            return
        }

        _timerSecondsLeft.value = minutes * 60
        timerJob = scope.launch {
            while (isActive && _timerSecondsLeft.value > 0) {
                delay(1000)
                _timerSecondsLeft.value -= 1
            }
            if (isActive && _timerSecondsLeft.value <= 0) {
                stop()
            }
        }
    }

    private fun startAudioLoop(soundType: AmbientSoundType) {
        playbackJob?.cancel()

        try {
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val actualBuf = maxOf(minBuf, BUFFER_SIZE * 2)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(actualBuf)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.setVolume(_volume.value)
            audioTrack?.play()
        } catch (e: Exception) {
            _isPlaying.value = false
            return
        }

        playbackJob = scope.launch(Dispatchers.Default) {
            val shortBuffer = ShortArray(BUFFER_SIZE)

            // State variables for procedural sound synthesis
            var filterState1 = 0.0f
            var filterState2 = 0.0f
            var filterState3 = 0.0f
            var dropletAmp = 0.0f
            var crackleAmp = 0.0f
            var sampleCounter = 0L

            while (isActive && _isPlaying.value) {
                for (i in 0 until BUFFER_SIZE) {
                    val sampleFloat: Float
                    val t = sampleCounter / SAMPLE_RATE.toFloat()

                    when (soundType) {
                        AmbientSoundType.RAIN -> {
                            // Gentle Rain: Filtered pink noise + soft droplet clicks
                            val white = (Random.nextFloat() * 2f - 1f)
                            filterState1 += 0.12f * (white - filterState1)
                            filterState2 += 0.06f * (white - filterState2)

                            if (Random.nextFloat() < 0.0025f) {
                                dropletAmp = Random.nextFloat() * 0.45f
                            }
                            dropletAmp *= 0.94f

                            val rainBase = (filterState1 * 0.35f + filterState2 * 0.25f)
                            sampleFloat = (rainBase + dropletAmp * (Random.nextFloat() * 2f - 1f)) * 0.7f
                        }

                        AmbientSoundType.OCEAN -> {
                            // Ocean Waves: Rhythmic sinusoidal noise modulation (wave period ~ 5 seconds)
                            val wavePhase = (sampleCounter % (SAMPLE_RATE * 5)) / (SAMPLE_RATE * 5f)
                            val swell = (0.5f + 0.5f * sin(wavePhase * 2.0 * PI).toFloat()).pow(1.8f)

                            val white = (Random.nextFloat() * 2f - 1f)
                            val cutoff = 0.03f + 0.14f * swell
                            filterState1 += cutoff * (white - filterState1)

                            sampleFloat = filterState1 * (0.25f + 0.75f * swell) * 0.85f
                        }

                        AmbientSoundType.FOREST -> {
                            // Forest & Breeze: Whispering wind with gentle melodic air
                            val breezeMod = (0.5f + 0.5f * sin(t * 0.2 * 2.0 * PI).toFloat())
                            val white = (Random.nextFloat() * 2f - 1f)
                            filterState1 += 0.04f * (white - filterState1)

                            // Soft periodic singing chime (bird/nature resonance)
                            val chimePeriod = (sampleCounter % (SAMPLE_RATE * 4)) / (SAMPLE_RATE * 4f)
                            val chimeEnv = if (chimePeriod < 0.2f) exp(-chimePeriod * 15f) else 0f
                            val chimeTone = (sin(t * 1920.0 * 2.0 * PI) * 0.08f + sin(t * 2400.0 * 2.0 * PI) * 0.05f).toFloat()

                            sampleFloat = (filterState1 * (0.35f + 0.3f * breezeMod) + chimeTone * chimeEnv) * 0.75f
                        }

                        AmbientSoundType.STREAM -> {
                            // Mountain Stream: Resonant bubbling water
                            val white = (Random.nextFloat() * 2f - 1f)
                            filterState1 += 0.18f * (white - filterState1)
                            filterState2 += 0.08f * (white - filterState2)

                            val bubble1 = sin(t * 850.0 * 2.0 * PI).toFloat() * 0.07f * (0.5f + 0.5f * sin(t * 3.5 * 2.0 * PI).toFloat())
                            val bubble2 = sin(t * 1220.0 * 2.0 * PI).toFloat() * 0.05f * (0.5f + 0.5f * sin(t * 5.1 * 2.0 * PI).toFloat())

                            sampleFloat = ((filterState1 * 0.3f + filterState2 * 0.2f) + bubble1 + bubble2) * 0.8f
                        }

                        AmbientSoundType.CAMPFIRE -> {
                            // Campfire: Low rumble + random soft wood crackles
                            val white = (Random.nextFloat() * 2f - 1f)
                            filterState1 += 0.035f * (white - filterState1)

                            if (Random.nextFloat() < 0.0012f) {
                                crackleAmp = Random.nextFloat() * 0.7f
                            }
                            crackleAmp *= 0.88f

                            val crackleSound = crackleAmp * (Random.nextFloat() * 2f - 1f)
                            sampleFloat = (filterState1 * 0.4f + crackleSound * 0.5f) * 0.8f
                        }

                        AmbientSoundType.ZEN_BOWLS -> {
                            // Tibetan Zen Bowls: 432 Hz healing harmonic resonance with 6-second cycle
                            val bowlPeriod = (sampleCounter % (SAMPLE_RATE * 6)) / (SAMPLE_RATE * 6f)
                            val bowlEnv = exp(-bowlPeriod * 2.2f)

                            val fund432 = sin(t * 432.0 * 2.0 * PI).toFloat() * 0.45f
                            val harm648 = sin(t * 648.0 * 2.0 * PI).toFloat() * 0.25f
                            val harm864 = sin(t * 864.0 * 2.0 * PI).toFloat() * 0.15f
                            val shimmer = sin(t * 1296.0 * 2.0 * PI).toFloat() * 0.05f

                            sampleFloat = (fund432 + harm648 + harm864 + shimmer) * bowlEnv * 0.85f
                        }
                    }

                    // Convert clamped float (-1.0 to 1.0) to 16-bit PCM
                    val clamped = sampleFloat.coerceIn(-1.0f, 1.0f)
                    shortBuffer[i] = (clamped * 32767).toInt().toShort()
                    sampleCounter++
                }

                try {
                    audioTrack?.write(shortBuffer, 0, BUFFER_SIZE)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }
}
