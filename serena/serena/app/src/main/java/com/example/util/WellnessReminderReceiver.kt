package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WellnessReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val messages = listOf(
            Pair("Pausa mágica de respiración 🕊️", "Inhala como si olieras una flor hermosa y exhala como si soplaras una velita con calma."),
            Pair("¿Cómo está tu corazoncito hoy? 💖", "Tómate un minuto para sentir tus emociones y registrar cómo te sientes en Serena."),
            Pair("¡Hora de hidratarte! 💧", "Bebe un vaso de agua fresca y dale las gracias a tu cuerpo por todo lo que hace por ti."),
            Pair("¡Eres una persona muy especial! ⭐", "Recuerda que cada pequeño esfuerzo vale oro. ¡Sigue adelante paso a pasito!"),
            Pair("Momento de estiramiento 🤸", "Estira tus brazos hacia el cielo como un árbol grande y suelta cualquier tensión."),
            Pair("Pausa de Calma 4-7-8 🌿", "Inhala en 4 segundos, sostén suavemente y exhala en 8. La calma vive dentro de ti."),
            Pair("¿Cumpliste tus rutinas hoy? 🌱", "Tus hábitos saludables te esperan. ¡Entra a Serena y gana puntos para tus juguetes y premios!"),
            Pair("Un mensaje con mucho cariño 💌", "Tu familia y Serena están orgullosos de ti. ¡Que tengas un momento lleno de paz!"),
            Pair("Libreta de Sentimientos 📖", "¿Pasó algo emocionante o difícil hoy? Escríbelo en tu libreta y libera tu mente."),
            Pair("Técnica de los 5 sentidos 🧸", "Mira 3 cosas bonitas a tu alrededor, escucha 2 sonidos suaves y siente 1 textura que te dé confort."),
            Pair("¡Gran trabajo en tu bienestar! 🏆", "Hoy es una nueva oportunidad para aprender y sonreír. ¡Sigue acumulando estrellas!"),
            Pair("Abrazo de oso para ti 🐻", "Si algo te preocupa, recuerda que los días nublados siempre dan paso al sol."),
            Pair("Pausa creativa 🎨", "Tómate 5 minutos para imaginar tu lugar favorito o hacer un garabato divertido."),
            Pair("Serena piensa en ti ✨", "Respira hondo... siente tus pies sobre el suelo. Estás a salvo y en paz.")
        )
        val randomReminder = messages.random()

        NotificationHelper.showWellnessReminder(
            context = context,
            title = randomReminder.first,
            message = randomReminder.second
        )
    }
}
