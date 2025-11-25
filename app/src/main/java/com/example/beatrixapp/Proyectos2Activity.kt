package com.example.beatrixapp
import android.graphics.Color
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
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

        // JSON de ejemplo (puede venir de archivo o API)
        val inputStream = resources.openRawResource(R.raw.proyectos)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
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
            val tareasArray = obj.getJSONArray("Tareas")
            val tareas = mutableListOf<Tarea>()

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
                    // Pon un avatar de ejemplo
                    usuarios.add(Usuario(nombreUsuario, email, R.drawable.avatarejemplo))
                }

                tareas.add(Tarea(nombreTarea, descripcion, fechaInicio, fechaEntrega, estado, usuarios))
            }

            proyectos.add(Proyecto(nombreProyecto, tareas))
        }
        return proyectos
    }

    private fun cargarProyecto(proyecto: Proyecto) {
        txtNombreProyecto.text = proyecto.nombreProyecto
    }

    private fun mostrarTarea(tarea: Tarea) {
        txtFechasTarea.text = "${tarea.fechaInicio} - ${tarea.fechaEntrega}"
        txtEstadoTarea.text = tarea.estado

        when (tarea.estado.lowercase()) {
            "en progreso" -> txtEstadoTarea.setBackgroundColor(Color.parseColor("#FFA500"))
            "pendiente" -> txtEstadoTarea.setBackgroundColor(Color.GRAY)
            "completada" -> txtEstadoTarea.setBackgroundColor(Color.GREEN)
        }

        layoutCheckboxTareas.removeAllViews()
        val subtareas = listOf("Diseñar pantalla de inicio", "Crear prototipo de flujo de usuario", "Revisar colores y tipografías")
        for (sub in subtareas) {
            val checkBox = CheckBox(this)
            checkBox.text = sub
            layoutCheckboxTareas.addView(checkBox)
        }

        txtMiniCalendario.text = "${tarea.fechaInicio} → ${tarea.fechaEntrega}"

        if (tarea.usuariosAsignados.isNotEmpty()) {
            tarea.usuariosAsignados[0].avatar?.let { imgUsuario.setImageResource(it) }
        }
    }
}
