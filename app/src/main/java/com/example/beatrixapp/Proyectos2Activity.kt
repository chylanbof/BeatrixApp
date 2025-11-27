package com.example.beatrixapp
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.beatrixapp.model.Proyecto
import com.example.beatrixapp.model.Tarea
import com.example.beatrixapp.model.Usuario
import org.json.JSONArray

class Proyectos2Activity : AppCompatActivity() {

    private lateinit var txtNombreProyecto: TextView
    private lateinit var txtFechasTarea: TextView
    private lateinit var layoutCheckboxTareas: LinearLayout
    private lateinit var txtEstadoTarea: TextView
    private lateinit var txtMiniCalendario: TextView
    private lateinit var imgUsuario: ImageView

    private lateinit var lineaDescripcion1: View
    private lateinit var lineaDescripcion2: View

    private lateinit var txtDescripcionTarea: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.proyectos2)

        // Referencias a las vistas
        txtNombreProyecto = findViewById(R.id.txtNombreProyecto)
        txtFechasTarea = findViewById(R.id.txtFechasTarea)
        layoutCheckboxTareas = findViewById(R.id.layoutCheckboxTareas)
        txtEstadoTarea = findViewById(R.id.txtEstadoTarea)
        txtMiniCalendario = findViewById(R.id.txtMiniCalendario)
        imgUsuario = findViewById(R.id.imgFotoUsuario)
        lineaDescripcion1 = findViewById(R.id.lineaDescripcion1)
        lineaDescripcion2 = findViewById(R.id.lineaDescripcion2)
        txtDescripcionTarea = findViewById(R.id.txtDescripcionTarea)


        // JSON de ejemplo (puede venir de archivo o API)
        val inputStream = resources.openRawResource(R.raw.proyectos)
        val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val proyectos = parseProyectos(JSONArray(jsonString))

        // Mostrar primer proyecto y primera tarea
        if (proyectos.isNotEmpty()) {
            val proyecto = proyectos[0]
            cargarProyecto(proyecto)
            if (proyecto.tareas.isNotEmpty()) {
                mostrarTarea(proyecto.tareas[0])
            }
        }
    }

    private fun parseProyectos(jsonArray: JSONArray): List<Proyecto> {
        val proyectos = mutableListOf<Proyecto>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val nombreProyecto = obj.getString("NombreProyecto")

            // 💥 LEER FECHAS DEL PROYECTO
            val fechaInicioProyecto = obj.getString("fechaInicio").substring(0, 10)
            val fechaEntregaProyecto = obj.getString("fechaEntrega").substring(0, 10)

            val tareasArray = obj.getJSONArray("Tareas")
            val tareas = mutableListOf<Tarea>()

            // Parseo de tareas
            for (j in 0 until tareasArray.length()) {
                val t = tareasArray.getJSONObject(j)
                val nombreTarea = t.getString("nombreTarea")
                val descripcion = t.getString("descripcion")
                val fechaInicio = t.getString("fechaInicio").substring(0, 10)
                val fechaEntrega = t.getString("fechaEntrega").substring(0, 10)
                val estado = t.getString("estado")

                val usuariosArray = t.getJSONArray("usuariosAsignados")
                val usuarios = mutableListOf<Usuario>()
                for (k in 0 until usuariosArray.length()) {
                    val u = usuariosArray.getJSONObject(k)
                    val nombreUsuario = u.getString("nombreUsuario")
                    val email = u.optString("email", "")
                    usuarios.add(Usuario(nombreUsuario, email, R.drawable.avatarejemplo))
                }

                tareas.add(Tarea(nombreTarea, descripcion, fechaInicio, fechaEntrega, estado, usuarios))
            }

            // 📌 Crear Proyecto con fechas
            val proyecto = Proyecto(nombreProyecto, tareas, fechaInicioProyecto, fechaEntregaProyecto)

            tareas.forEach { it.proyecto = proyecto }

            proyectos.add(proyecto)
        }
        return proyectos
    }



    private fun cargarProyecto(proyecto: Proyecto) {
        txtNombreProyecto.text = proyecto.nombreProyecto

        // Fecha del PROYECTO arriba
        txtFechasTarea.text = "${proyecto.fechaInicio.substring(0, 10)} - ${proyecto.fechaEntrega.substring(0, 10)}"
    }


    private fun mostrarTarea(tarea: Tarea) {
        // Función interna para actualizar la vista según una tarea
        fun actualizarVista(tareaSeleccionada: Tarea) {
            // Fechas y mini calendario
            txtMiniCalendario.text = "${tareaSeleccionada.fechaInicio} → ${tareaSeleccionada.fechaEntrega}"

            // Estado de la tarea y color de fondo
            txtEstadoTarea.text = tareaSeleccionada.estado
            when (tareaSeleccionada.estado.lowercase()) {
                "en progreso" -> txtEstadoTarea.setBackgroundColor(Color.parseColor("#FFA500"))
                "pendiente" -> txtEstadoTarea.setBackgroundColor(Color.GRAY)
                "completada" -> txtEstadoTarea.setBackgroundColor(Color.GREEN)
            }

            // Imagen del primer usuario asignado
            if (tareaSeleccionada.usuariosAsignados.isNotEmpty()) {
                tareaSeleccionada.usuariosAsignados[0].avatar?.let { imgUsuario.setImageResource(it) }
            } else {
                imgUsuario.setImageResource(R.drawable.circle_gray)
            }

            // Actualizar descripción con texto del JSON
            txtDescripcionTarea.text = tareaSeleccionada.descripcion

            // Mostrar u ocultar líneas negras según haya descripción
            val hayDescripcion = tareaSeleccionada.descripcion.isNotEmpty()
            lineaDescripcion1.visibility = if (hayDescripcion) View.VISIBLE else View.GONE
            lineaDescripcion2.visibility = if (hayDescripcion) View.VISIBLE else View.GONE
        }


        // Limpiar layout y crear RadioGroup
        layoutCheckboxTareas.removeAllViews()
        val radioGroup = RadioGroup(this)
        radioGroup.orientation = RadioGroup.VERTICAL

        tarea.proyecto?.tareas?.forEach { subTarea ->
            val radioButton = RadioButton(this)
            radioButton.text = subTarea.nombreTarea
            if (subTarea.estado.lowercase() == "completada") {
                radioButton.paintFlags = radioButton.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            }

            radioGroup.addView(radioButton)
        }

        // Listener: actualizar vista al seleccionar un RadioButton
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)
            val nombreSeleccionado = radioButton.text.toString()
            val tareaSeleccionada = tarea.proyecto?.tareas?.firstOrNull { it.nombreTarea == nombreSeleccionado }
            tareaSeleccionada?.let { actualizarVista(it) }
        }

        layoutCheckboxTareas.addView(radioGroup)

        // Inicializamos con la tarea que se estaba mostrando
        actualizarVista(tarea)
    }
}

