package com.example.beatrixapp.model

data class Tarea(
    val nombreTarea: String,
    val descripcion: String,
    val fechaInicio: String,
    val fechaEntrega: String,
    var estado: String = "Pendiente",
    val usuariosAsignados: List<Usuario>,
    val subTareas: List<Tarea> = emptyList()
)
