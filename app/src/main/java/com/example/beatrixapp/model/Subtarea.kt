package com.example.beatrixapp.model

import com.google.gson.annotations.SerializedName

data class Subtarea(
    @SerializedName("NombreSubTarea")
    val nombreSubtarea: String,

    @SerializedName("DescripcionSubtarea")
    val descripcionSubTarea: String,

    @SerializedName("FechaInicioSubtarea")
    val fechaInicioSubtarea: String,

    @SerializedName("EstadosSubtarea")
    val fechaEntregaSubtarea: String,

    @SerializedName("EstadoSubtarea")
    val estadoSubtarea: String,

    @SerializedName("UsuariosAsignadosSubtarea")
    val usuariosAsignadosSubTarea: List<Usuario>
                   )
