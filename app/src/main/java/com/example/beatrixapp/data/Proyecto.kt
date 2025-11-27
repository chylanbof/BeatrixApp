package com.example.beatrixapp.data

import java.time.LocalDateTime

data class Proyecto(
    val id: String,
    val usuarioAsignado: String,
    val nombre: String,
    val fechaInicio: LocalDateTime,
    val fechaFin: LocalDateTime,
    val estado: EstadoProyecto,
    val descripcion: String
)

enum class EstadoProyecto (val colorHex: String, val label: String) {
    EN_PROGRESO("#F5CBA7","En progreso"),
    TERMINADO("#95FF99","Terminado"),
    SIN_EMPEZAR("#C7A6E0","Sin empezar")

}

