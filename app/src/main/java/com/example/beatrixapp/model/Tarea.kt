package com.example.beatrixapp.model

data class Tarea(
    val nombreTarea: String,
    val descripcion: String,
    val fechaInicio: String,
    val fechaEntrega: String,
    val estado: String,
    val usuariosAsignados: List<Usuario>,
    var proyecto: Proyecto? = null
                )