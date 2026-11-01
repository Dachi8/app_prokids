package com.example.data.model

/**
 * Registro diario de ánimo, mapeado a la tabla `registros_animo` de MySQL.
 */
data class MoodEntryEntity(
    val id: Long = 0,
    val userId: Long = 1,
    val dateString: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val moodScore: Int, // 1: Muy difícil, 2: Desanimado, 3: Neutral, 4: Bien, 5: Excelente
    val moodLabel: String,
    val moodEmoji: String,
    val energyLevel: Int = 3,
    val emotionTags: String = "",
    val contextTags: String = "",
    val note: String = ""
)

enum class MoodType(
    val score: Int,
    val emoji: String,
    val labelEs: String,
    val description: String
) {
    EXCELLENT(5, "😄", "Excelente", "Lleno de energía y gratitud"),
    GOOD(4, "🙂", "Bien", "Tranquilo y con buen ánimo"),
    NEUTRAL(3, "😐", "Neutral", "Un día equilibrado o regular"),
    SAD(2, "😔", "Desanimado", "Con poca energía o tristeza"),
    OVERWHELMED(1, "😰", "Abrumado", "Con estrés, ansiedad o dolor");

    companion object {
        fun fromScore(score: Int): MoodType {
            return entries.find { it.score == score } ?: NEUTRAL
        }
    }
}
