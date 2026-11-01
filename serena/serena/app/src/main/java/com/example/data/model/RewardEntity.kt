package com.example.data.model

/**
 * Recompensa del catálogo, mapeada a la tabla `recompensas` de MySQL.
 */
data class RewardItemEntity(
    val id: Long = 0,
    val title: String,
    val description: String,
    val pointsCost: Int,
    val iconEmoji: String,
    val category: String,
    val isClaimed: Boolean = false,
    val claimedAt: Long? = null,
    val gameType: String = ""
)
