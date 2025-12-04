package com.example.beatrixapp

import android.os.Build
import android.os.Bundle
import android.widget.CalendarView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.beatrixapp.model.Proyecto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.log
import android.util.Log
import androidx.annotation.RawRes
import android.widget.TextView

class CalendarioActivity : AppCompatActivity() {

    private lateinit var listaProyectos: List<Proyecto>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        // Referencias de vistas
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val containerItems = findViewById<LinearLayout>(R.id.container_items)

        // leer y parsear json proyectos
        val jsonProyectos = leerJSONDRaw(R.raw.proyectos)
        listaProyectos = parsearProyectos(jsonProyectos)


        // Listener de cambio de fecha
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->

            //limpiamos los items anteriores
            containerItems.removeAllViews()

            //formato de fecha yyyy-mm-old
            val fechaSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)

            //Filtramos proyectos activos de esa fecha
            val proyectoDelDia = listaProyectos.filter { proyecto ->
                val fechaInicio = proyecto.fechaInicio.substring(0,10)
                val fechaEntrega = proyecto.fechaEntrega.substring(0,10)
                fechaSeleccionada >= fechaInicio && fechaSeleccionada <= fechaEntrega
            }

            mostrarProyectos(proyectoDelDia, containerItems)
        }
    }

    private fun parsearProyectos(json: String): List<Proyecto> {
        val gson = Gson()
        val tipoListaProyecto = object : TypeToken<List<Proyecto>>(){}.type
        return gson.fromJson(json, tipoListaProyecto)
    }

    fun leerJSONDRaw(@RawRes idArchivo: Int): String{
        return resources.openRawResource(idArchivo).bufferedReader().use {it.readText()}
    }

    // Muestra proyectos del json
    fun mostrarProyectos(proyectos: List<Proyecto>, container: LinearLayout) {
        val inflater = layoutInflater

        //Limpiamos el contenedor antes de añadir nuevos items
        container.removeAllViews()

        for (proyecto in proyectos) {
            val view = inflater.inflate(R.layout.item_proyecto, container, false)

            val tvNombre = view.findViewById<TextView>(R.id.tvNombreProyecto)
            val tvDescripcion = view.findViewById<TextView>(R.id.tvDescripcionProyecto)

            tvNombre.text = proyecto.nombreProyecto

            val nombresTareas = proyecto.tareas.orEmpty().map { it.nombreTarea ?: "Sin tareas" }

            tvDescripcion.text = if (nombresTareas.isNotEmpty()) {
                nombresTareas.joinToString("\n")
            } else{
                "Sin tareas"
            }

            container.addView(view)
        }
    }
}



