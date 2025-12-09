package com.example.beatrixapp

import android.annotation.SuppressLint
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
import androidx.appcompat.app.AlertDialog
import com.example.beatrixapp.model.Reunion
import com.example.beatrixapp.model.Subtarea

class CalendarioActivity : AppCompatActivity() {

    private lateinit var listaProyectos: List<Proyecto>
    private lateinit var  listaReunion: List<Reunion>

    private lateinit var  ListaSubtareas: List<Subtarea>

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

        // leer y parsear json reunion
        val jsonReunion = leerJSONDRaw(R.raw.reuniones)
        listaReunion = parsearReuniones(jsonReunion)

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

            val reunionDelDia = listaReunion.filter { reunion ->
                val fecha = reunion.fechaHora.substring(0, 10)
                fecha == fechaSeleccionada
            }

            mostrarReuniones(reunionDelDia, containerItems)

        }
    }

    private fun parsearProyectos(json: String): List<Proyecto> {
        val gson = Gson()
        val tipoListaProyecto = object : TypeToken<List<Proyecto>>(){}.type
        return gson.fromJson(json, tipoListaProyecto)
    }

    private fun parsearReuniones(json: String): List<Reunion>{
        val gson = Gson()
        val tipoListaReunion = object  : TypeToken<List<Reunion>>(){}.type
        return gson.fromJson(json, tipoListaReunion)
    }

    fun leerJSONDRaw(@RawRes idArchivo: Int): String{
        return resources.openRawResource(idArchivo).bufferedReader().use {it.readText()}
    }

    // Muestra proyectos del json
    fun mostrarProyectos(proyectos: List<Proyecto>, container: LinearLayout) {
        val inflater = layoutInflater


        for (proyecto in proyectos) {
            val view = inflater.inflate(R.layout.item_proyecto, container, false)

            val tvNombre = view.findViewById<TextView>(R.id.tvNombreProyecto)
            val tvDescripcionProyect = view.findViewById<TextView>(R.id.tvDescripcionProyecto)
            val tvTareas = view.findViewById<TextView>(R.id.tvTareasProyecto)

            tvNombre.text = proyecto.nombreProyecto
            tvDescripcionProyect.text = proyecto.descripcionProyecto ?: "Sin descripcón"

            // Mostrar nombre de tareas
            val nombresTareas = proyecto.tareas.orEmpty().map {
                it.nombreTarea ?: "sin Tareas"
            }

            tvTareas.text = if (nombresTareas.isNotEmpty()){
                nombresTareas.joinToString("\n")
            }else{
                "Sin Tareas"
            }

            view.setOnLongClickListener {
                mostrarDetalleProyecto(proyecto)
                true
            }
            container.addView(view)
        }
    }

    // Mostrar reuniones
    @SuppressLint("SetTextI18n")
    fun mostrarReuniones(reunion: List<Reunion>, container: LinearLayout){
        val inflater = layoutInflater

        for (reunion in reunion){
            val view = inflater.inflate(R.layout.item_reunion, container, false)

            val tvNombreReunion = view.findViewById<TextView>(R.id.tvNombreReunion)
            val tvHoraReunion = view.findViewById<TextView>(R.id.tvHoraReunion)
            val tvUsuarios = view.findViewById<TextView>(R.id.tvUsuariosReunion)
            val tvDescripcion = view.findViewById<TextView>(R.id.tvDescripcionReunion)

            tvNombreReunion.text = reunion.titulo.ifEmpty { "Sin título" }

            val soloFecha = reunion.fechaHora.substring(0, 10)
            val hora = reunion.fechaHora.substring(11, 16)

            tvHoraReunion.text = " $soloFecha $hora "

            tvUsuarios.text = if (reunion.usuariosReuniones.isNotEmpty()){
                reunion.usuariosReuniones.joinToString(", ")
            } else{
                "Sin participantes"
            }

            tvDescripcion.text = reunion.descripcion.ifEmpty { "Sin descripción" }

            container.addView(view)
        }
    }

    fun mostrarDetalleProyecto(proyecto: Proyecto){
        val dialogView = layoutInflater.inflate(R.layout.dialog_detalle_proyecto, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Cerrar"){dialog, _ -> dialog.dismiss()}

        // Referencias a textViews dentro del dialog
        val tvNombre = dialogView.findViewById<TextView>(R.id.tvNombreProyectoDetalle)
        val tvDescripcion = dialogView.findViewById<TextView>(R.id.tvDescripcionProyectoDetalle)
        val tvTareas = dialogView.findViewById<TextView>(R.id.tvTareasProyectoDetalle)

        tvNombre.text = proyecto.nombreProyecto
        tvDescripcion.text = proyecto.descripcionProyecto ?: "Sin descripción"

        // Generar el texto con las tareas y subtareas
        val detallesTareas = proyecto.tareas.orEmpty().joinToString("\n\n") { tarea ->
            val subTareas = tarea.subtarea.orEmpty().joinToString("\n") { sub ->
                "    • ${sub.nombreSubtarea}: ${sub.descripcionSubTarea} (${sub.estadoSubtarea})"
            }
            "- ${tarea.nombreTarea}: ${tarea.descripcion} [${tarea.estado}]\n  Inicio: ${tarea.fechaInicio} / Entrega: ${tarea.fechaEntrega}" +
                    if (subTareas.isNotEmpty()) "\n  Subtareas:\n$subTareas" else ""
        }

        tvTareas.text = detallesTareas.ifEmpty { "Sin tareas" }

        // Mostrar el dialog
        builder.show()
    }

    fun formatearFecha(fecha:String): String{
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault())
            val date = inputFormat.parse(fecha)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            fecha // si falla, devolvemos tal cual
        }
    }

}



