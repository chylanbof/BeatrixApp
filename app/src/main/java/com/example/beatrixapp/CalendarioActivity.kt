package com.example.beatrixapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.CalendarView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.beatrixapp.model.Proyecto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.view.View
import android.widget.ImageView
import androidx.annotation.RawRes
import android.widget.TextView
import com.example.beatrixapp.model.Reunion
import com.example.beatrixapp.model.Usuario

class CalendarioActivity : AppCompatActivity() {

    private lateinit var listaProyectos: List<Proyecto>
    private lateinit var  listaReunion: List<Reunion>

    @SuppressLint("CutPasteId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        //val loggedUser = "mgomez"
        //Recuperar el usuario Logueado
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val loggedUser = prefs.getString("loggedUser", null)

        //Si no hay usuario no muestra nada
        if (loggedUser == null){
            finish()
            return
        }

        //parsear el nombre de usuario con el nombre original.
        val jsonUsuarios = leerJSONDRaw(R.raw.usuarios) // Cargar JSON de usuarios
        val gson = Gson()
        val tipoListaUsuario = object : TypeToken<List<Usuario>>(){}.type
        val listaUsuarios: List<Usuario> = gson.fromJson(jsonUsuarios, tipoListaUsuario)

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
                val fechaEntrega = proyecto.fechaEntrega?.substring(0,10)
                if (fechaInicio != null && fechaEntrega != null) {
                    fechaSeleccionada >= fechaInicio && fechaSeleccionada <= fechaEntrega
                } else {
                    false // si alguna fecha es null, no incluir
                }
            }

            mostrarProyectos(proyectoDelDia, containerItems)

            val reunionDelDia = listaReunion.filter { reunion ->
                val fecha = reunion.fechaHora.substring(0, 10)
                fecha == fechaSeleccionada
            }

            mostrarReuniones(reunionDelDia, containerItems, listaUsuarios)

        }

        //Usar botones para enviar a otros activitys
        val includeLayout = findViewById<View>(R.id.boton_bottom)

        val botonHome = includeLayout.findViewById<ImageView>(R.id.btn_home)
        botonHome.setOnClickListener {
            val intentHome = Intent(this, MainActivity::class.java)
            startActivity(intentHome)
        }

        val botonProyectos = includeLayout.findViewById<ImageView>(R.id.btn_proyecto)
        botonProyectos.setOnClickListener {
            val intentProyecto = Intent(this, ProyectosActivity:: class.java)
            startActivity(intentProyecto)
        }

        val botonUsuarios = includeLayout.findViewById<ImageView>(R.id.btn_perfil)
        botonUsuarios.setOnClickListener {
            val intentHome = Intent(this, UsuarioActivity:: class.java)
            startActivity(intentHome)
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
    // Dentro de CalendarioActivity.kt
    fun mostrarProyectos(proyectos: List<Proyecto>, container: LinearLayout) {

        //val loggedUser = "mgomez"

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val loggedUser = prefs.getString("loggedUser", null)

        val inflater = layoutInflater

        // 1. FILTRADO: Determinar si el proyecto es VISIBLE para el usuario.
        val proyectosFiltrados = proyectos.filter { proyecto ->

            // --- CRITERIO DE VISIBILIDAD DE PROYECTO ---

            // Criterio 1: Asignado directamente al Proyecto (en UsuariosAsignados del Proyecto)
            val asignadoDirectamente = proyecto.usuariosAsignados.any { it.nombreUsuario == loggedUser }

            // Criterio 2: Asignado a CUALQUIER Tarea O CUALQUIER Subtarea
            val asignadoATareaOSubtarea = proyecto.tareas.any { tarea ->
                // Condición A: Usuario asignado a la Tarea
                val enTarea = tarea.usuariosAsignados.any { it.nombreUsuario == loggedUser }

                // Condición B: Usuario asignado a alguna Subtarea de esta Tarea
                val enSubtarea = tarea.subtarea.any { subtarea ->
                    subtarea.usuariosAsignadosSubTarea.any { it.nombreUsuario == loggedUser }
                }

                enTarea || enSubtarea
            }

            // El proyecto es visible si cumple Criterio 1 O Criterio 2
            asignadoDirectamente || asignadoATareaOSubtarea
        }

        // 2. Iterar sobre los proyectos VISIBLES
        for (proyecto in proyectosFiltrados) {
            val view = inflater.inflate(R.layout.item_proyecto, container, false)

            val tvNombre = view.findViewById<TextView>(R.id.tvNombreProyecto)
            val tvDescripcionProyect = view.findViewById<TextView>(R.id.tvDescripcionProyecto)
            val tvTareas = view.findViewById<TextView>(R.id.tvTareasProyecto)

            tvNombre.text = proyecto.nombreProyecto
            tvDescripcionProyect.text = proyecto.descripcionProyecto

            // 3. GENERAR LISTA DE TAREAS/SUBTAREAS VISIBLES PARA EL TEXTO INICIAL (tvTareas)

            // Creamos una lista de las Tareas donde el usuario está asignado (ya sea directamente o en subtarea)
            val tareasVisibles = proyecto.tareas.filter { tarea ->
                val enTarea = tarea.usuariosAsignados.any { it.nombreUsuario == loggedUser }
                val enSubtarea = tarea.subtarea.any { subtarea ->
                    subtarea.usuariosAsignadosSubTarea.any { it.nombreUsuario == loggedUser }
                }
                enTarea || enSubtarea
            }

            val nombreTareas = tareasVisibles.map { it.nombreTarea }

            // Aquí manejamos el mensaje para los proyectos que no tienen tareas,
            // pero son visibles por asignación directa (Criterio 1)
            tvTareas.text = if (nombreTareas.isNotEmpty()){
                "Tareas asignadas: \n" + nombreTareas.joinToString("\n")
            } else if (proyecto.tareas.isEmpty() && proyecto.usuariosAsignados.any { it.nombreUsuario == loggedUser }) {
                // El usuario está asignado al proyecto, pero no hay tareas aún.
                "Asignado al proyecto (sin tareas creadas)."
            } else {
                // Este caso es poco probable con la lógica de filtrado inicial, pero es un fallback
                "Sin Tareas directas o subtareas asignadas."
            }


            // 4. Implementación del Long-Click (Muestra el DetalleProyectoDialog)
            view.setOnLongClickListener {
                mostrarDetalleProyecto(proyecto)
                true
            }
            container.addView(view)
        }
    }

    // Mostrar reuniones
    @SuppressLint("SetTextI18n")
    fun mostrarReuniones(reunion: List<Reunion>, container: LinearLayout, listaUsuarios: List<Usuario>){

       // val loggedUser = "mgomez"

         val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val loggedUser = prefs.getString("loggedUser", null)?: return

        val inflater = layoutInflater

        val reunionesUsuario = reunion.filter { reunion ->
            reunion.usuariosReuniones.any { nombreApellido ->
                val usuario = listaUsuarios.find { it.nombreApellidos == nombreApellido }
                usuario?.nombreUsuario == loggedUser
            }
        }

        for (reunion in reunionesUsuario){

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

    fun mostrarDetalleProyecto(proyecto: Proyecto) {

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val loggedUser = prefs.getString("loggedUser", null)?: return

        // Usar el DialogFragment. Toda la lógica de filtrado va DENTRO del diálogo.
        val dialog = DetalleProyectoDialog(proyecto, loggedUser)
        dialog.show(supportFragmentManager, "DetalleProyectoDialogTag")
    }

}



