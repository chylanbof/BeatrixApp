package com.example.beatrixapp.model

data class Proyecto (
    val nombreProyecto: String,
    val tareas: List<Tarea>,
    var fechaInicio: String,
    var fechaEntrega: String,
    val descripcionProyecto: String = ""
                    )
