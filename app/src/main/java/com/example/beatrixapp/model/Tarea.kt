package com.example.beatrixapp.model


data class Tarea(
    val nombreTarea: String,
    val descripcion: String,
    val fechaInicio: String,
    val fechaEntrega: String,
    val estado: String,
    val usuariosAsignados: List<Usuario>,
    val subtareas: List<SubTarea> = emptyList()
                ) {
    var proyecto: Proyecto? = null
}