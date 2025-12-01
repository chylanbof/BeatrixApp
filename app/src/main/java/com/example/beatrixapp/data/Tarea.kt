package com.example.beatrixapp.data

import java.time.LocalDateTime

data class Tarea(
    val nombreProyecto: String,
    val descripcion: String,
    val fechaInicio: LocalDateTime,
    val fechaEntrega: LocalDateTime,
    val estado: EstadoTarea,
    val usuariosAsignados: List<Usuario>
)

enum class EstadoTarea {
    Pendiente,
    EnProgreso,
    Completada
}
