package com.example.beatrixapp.model

data class SubTarea(
    val nombreSubTarea: String,
    val descripcionSubTarea: String,
    var fechaInicioSubtarea: String,
    var fechaEntregaSubtarea: String,
    val estadoSubTarea: String
                   )