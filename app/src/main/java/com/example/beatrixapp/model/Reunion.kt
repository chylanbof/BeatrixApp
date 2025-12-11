package com.example.beatrixapp.model

data class Reunion(
    val titulo: String,
    val fechaHora: String,
    val usuariosReuniones: List<String>,
    val descripcion: String,
    val completado: Boolean
                  )