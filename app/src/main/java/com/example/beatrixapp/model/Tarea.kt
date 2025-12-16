package com.example.beatrixapp.model

import com.google.gson.annotations.SerializedName
data class Tarea(
    @SerializedName("nombreTarea")
    val nombreTarea: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("fechaInicio")
    val fechaInicio: String,

    @SerializedName("fechaEntrega")
    val fechaEntrega: String,

    @SerializedName("EstadoSubTarea")
    val estado: String,

    @SerializedName("usuariosAsignados")
    val usuariosAsignados: List<Usuario>,

    @SerializedName("SubTareas")
    val subtarea: List<Subtarea>,

    var proyecto: Proyecto? = null
                )