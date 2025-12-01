package com.example.beatrixapp.data

import java.time.LocalDateTime

data class Proyecto(
    val nombreProyecto: String,
    val tareas: List<Tarea>,
    val fechaEntrega: LocalDateTime,
    val fechaInicio: LocalDateTime,
    val usuariosAsignados: List<Usuario>
)
