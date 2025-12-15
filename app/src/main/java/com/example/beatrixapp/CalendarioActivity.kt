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
import kotlin.math.log
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RawRes
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.beatrixapp.model.Reunion
import com.example.beatrixapp.model.Subtarea
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.beatrixapp.model.Usuario
import android.graphics.Color

class CalendarioActivity : AppCompatActivity() {

    private lateinit var listaProyectos: List<Proyecto>
    private lateinit var  listaReunion: List<Reunion>

    @SuppressLint("CutPasteId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        val loggedUser = "afernandez"
        //Recuperar el usuario Logueado
       /* val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val loggedUser = prefs.getString("loggerUser", null)*/

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
                val fechaEntrega = proyecto.fechaEntrega.substring(0,10)
                fechaSeleccionada >= fechaInicio && fechaSeleccionada <= fechaEntrega
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
            val intentHome = Intent(this, MainActivity:: class.java)
            startActivity(intentHome)
        }

        val botonUsuarios = includeLayout.findViewById<ImageView>(R.id.btn_home)
        botonUsuarios.setOnClickListener {
            val intentHome = Intent(this, ProyectosActivity:: class.java)
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
    fun mostrarProyectos(proyectos: List<Proyecto>, container: LinearLayout) {
        val loggedUser = "afernandez"

       /* val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val loggedUser = prefs.getString("loggerUser", null)?: return
*/
        val inflater = layoutInflater

        // Filtrar proyectos donde el usuario tenga alguna tarea o subtarea asignada
        val proyectosFiltrados = proyectos.filter { proyecto ->
            proyecto.tareas.orEmpty().any { tarea ->
                // Usuario en la tarea
                val enTarea = tarea.usuariosAsignados?.any { it.nombreUsuario == loggedUser } == true
                // Usuario en alguna subtarea
                val enSubtarea = tarea.subtarea?.any { subtarea ->
                    subtarea.usuariosAsignadosSubTarea?.any { it.nombreUsuario == loggedUser } == true
                } == true

                enTarea || enSubtarea
            }
        }

        for (proyecto in proyectosFiltrados) {


            val view = inflater.inflate(R.layout.item_proyecto, container, false)

            val tvNombre = view.findViewById<TextView>(R.id.tvNombreProyecto)
            val tvDescripcionProyect = view.findViewById<TextView>(R.id.tvDescripcionProyecto)
            val tvTareas = view.findViewById<TextView>(R.id.tvTareasProyecto)

            tvNombre.text = proyecto.nombreProyecto
            tvDescripcionProyect.text = proyecto.descripcionProyecto

            // Filtrar tareas visibles para el usuario
            val tareasUsuario = proyecto.tareas.orEmpty().mapNotNull { tarea ->
                val subtareasUsuario = tarea.subtarea.filter { subtarea ->
                    subtarea.usuariosAsignadosSubTarea.any { it.nombreUsuario == loggedUser }
                }.orEmpty()

                val usuarioEnTarea = tarea.usuariosAsignados.any { it.nombreUsuario == loggedUser }

                if (!usuarioEnTarea && subtareasUsuario.isEmpty()) return@mapNotNull null

                // Copiar tarea con solo las subtareas visibles para el usuario
                tarea.copy(subtarea = subtareasUsuario)
            }

            val nombreTareas = tareasUsuario.map {it.nombreTarea}

            tvTareas.text = if (nombreTareas.isNotEmpty()){
                nombreTareas.joinToString("\n")
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
    fun mostrarReuniones(reunion: List<Reunion>, container: LinearLayout, listaUsuarios: List<Usuario>){

        val loggedUser = "afernandez"

        /* val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val loggedUser = prefs.getString("loggerUser", null)?: return
*/

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

        val loggedUser = "afernandez"

       /* val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val loggedUser = prefs.getString("loggerUser", null)?: return
*/

        val dialogView = layoutInflater.inflate(R.layout.dialog_detalle_proyecto, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton( "Cerrar" ) { dialog, _ -> dialog.dismiss() }

        // Referencias
        val tvNombre = dialogView.findViewById<TextView>(R.id.tvNombreProyectoDetalle)
        val tvDescripcion = dialogView.findViewById<TextView>(R.id.tvDescripcionProyectoDetalle)
        val recyclerTareas = dialogView.findViewById<RecyclerView>(R.id.recyclerTareas)

        tvNombre.text = proyecto.nombreProyecto
        tvDescripcion.text = proyecto.descripcionProyecto

        // Filtrar Tareas y subtareas del usuario
        val tareasFiltradas = proyecto.tareas.orEmpty().mapNotNull { tarea ->

            // Subtareas del usuario
            val subFiltradas = tarea.subtarea.filter { subtarea ->
                subtarea.usuariosAsignadosSubTarea.any { it.nombreUsuario == loggedUser }
            }.orEmpty()

            val usuarioEnTarea = tarea.usuariosAsignados.any { it.nombreUsuario == loggedUser }

            when {
                usuarioEnTarea -> {
                    // Usuario pertenece a la tarea: devolvemos tarea con subtareas filtradas
                    tarea.copy(subtarea = subFiltradas)
                }
                subFiltradas.isNotEmpty() -> {
                    // Usuario solo en subtareas: mostramos tarea y subtareas visibles
                    tarea.copy(
                        nombreTarea = tarea.nombreTarea,
                        descripcion = tarea.descripcion,
                        subtarea = subFiltradas
                              )
                }
                else -> null
            }
        }

        // Configurar RecyclerView
        recyclerTareas.layoutManager = LinearLayoutManager(this)
        recyclerTareas.adapter = TareaAdapter(tareasFiltradas)

        // Mostrar el dialog
        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.rgb(245,158,125))

    }
}



