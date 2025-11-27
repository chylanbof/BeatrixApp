package com.example.beatrixapp.model

data class Proyecto (
    val nombreProyecto: String,
    val tareas: List<Tarea>,
    val fechaInicio: String,
    val fechaEntrega: String
                    )
