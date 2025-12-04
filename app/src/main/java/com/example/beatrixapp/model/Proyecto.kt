package com.example.beatrixapp.model

data class Proyecto(
    val nombreProyecto: String,
    val descripcionProyecto: String,
    val tareas: List<Tarea>,
    val fechaInicio: String,
    val fechaEntrega: String,
    val usuariosAsignados: List<Usuario>,
    var estado: String = "Pendiente"

)
