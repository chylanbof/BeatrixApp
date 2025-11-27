package com.example.beatrixapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class ProyectosActivity : AppCompatActivity() {

    // Clase para guardar la info del PROYECTO
    data class ProyectoUI(val nombre: String, val infoExtra: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proyectos)

        val listaProyectos = obtenerProyectosDesdeJSON()
        actualizarTarjetas(listaProyectos)
    }

    private fun obtenerProyectosDesdeJSON(): List<ProyectoUI> {
        val proyectosEncontrados = mutableListOf<ProyectoUI>()

        try {
            // 1. Abrimos el archivo raw/proyectos.json
            val inputStream = resources.openRawResource(R.raw.proyectos)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }

            val jsonArray = JSONArray(jsonString)

            // 2. Recorremos SOLO los proyectos (Nivel 1)
            for (i in 0 until jsonArray.length()) {
                val proyectoObj = jsonArray.getJSONObject(i)

                // Extraemos el nombre del proyecto
                val nombreProyecto = proyectoObj.getString("NombreProyecto")

                // Extraemos el array de tareas solo para contarlas
                val tareasArray = proyectoObj.getJSONArray("Tareas")
                val cantidadTareas = tareasArray.length()

                // Como tu JSON tiene fechas "0001-01-01" en el proyecto,
                // mejor mostramos cuántas tareas tiene como subtítulo.
                val info = "$cantidadTareas Tareas activas"

                proyectosEncontrados.add(ProyectoUI(nombreProyecto, info))
            }

        } catch (e: Exception) {
            Log.e("ProyectosActivity", "Error: ${e.message}")
        }

        return proyectosEncontrados
    }

    private fun actualizarTarjetas(proyectos: List<ProyectoUI>) {
        // --- TARJETA 1 ---
        if (proyectos.size > 0) {
            findViewById<TextView>(R.id.text_title1).text = proyectos[0].nombre
            findViewById<TextView>(R.id.text_date1).text = proyectos[0].infoExtra
        } else {
            findViewById<CardView>(R.id.card1).visibility = View.GONE
        }

        // --- TARJETA 2 ---
        if (proyectos.size > 1) {
            findViewById<TextView>(R.id.text_title2).text = proyectos[1].nombre
            findViewById<TextView>(R.id.text_date2).text = proyectos[1].infoExtra
        } else {
            findViewById<CardView>(R.id.card2).visibility = View.GONE
        }

        // --- TARJETA 3 ---
        if (proyectos.size > 2) {
            findViewById<TextView>(R.id.text_title3).text = proyectos[2].nombre
            findViewById<TextView>(R.id.text_date3).text = proyectos[2].infoExtra
        } else {
            findViewById<CardView>(R.id.card3).visibility = View.GONE
        }

        // --- TARJETA 4 ---
        if (proyectos.size > 3) {
            findViewById<TextView>(R.id.text_title4).text = proyectos[3].nombre
            findViewById<TextView>(R.id.text_date4).text = proyectos[3].infoExtra
        } else {
            findViewById<CardView>(R.id.card4).visibility = View.GONE
        }
    }
}