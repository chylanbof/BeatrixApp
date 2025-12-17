package com.example.beatrixapp.model

import com.google.gson.annotations.SerializedName

data class Subtarea(
    @SerializedName("NombreSubTarea")
    val nombreSubtarea: String,

    @SerializedName("DescripcionSubTarea")
    val descripcionSubTarea: String,

    @SerializedName("FechaInicioSubtarea")
    val fechaInicioSubtarea: String,

    @SerializedName("FechaEntregaSubtarea")
    val fechaEntregaSubtarea: String,

    @SerializedName("EstadoSubTarea")
    val estadoSubtarea: String,

    @SerializedName("UsuariosAsignadosSubtarea")
    val usuariosAsignadosSubTarea: List<Usuario>
)