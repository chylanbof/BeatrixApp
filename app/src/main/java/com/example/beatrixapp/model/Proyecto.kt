package com.example.beatrixapp.model

import com.google.gson.annotations.SerializedName

data class Proyecto (
    @SerializedName("NombreProyecto")
    val nombreProyecto: String,

    @SerializedName("DescripcionProyecto")
    val descripcionProyecto: String,

    @SerializedName("Tareas")
    val tareas: List<Tarea>,

    @SerializedName("fechaInicio")
    val fechaInicio: String,

    @SerializedName("fechaEntrega")
    val fechaEntrega: String,

    @SerializedName("UsuariosAsignados")
    val usuariosAsignados: List<Usuario>,

    var estado: String="Pendiente"
                    )
