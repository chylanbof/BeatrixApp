package com.example.beatrixapp
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.beatrixapp.model.Proyecto
import com.example.beatrixapp.model.SubTarea
import com.example.beatrixapp.model.Tarea
import com.example.beatrixapp.model.Usuario
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

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

    private lateinit var btnSiguienteProyecto: View

    private lateinit var proyectos: List<Proyecto>

    private lateinit var layoutSubtareas: LinearLayout

    private lateinit var btnSettings: ImageView
    private var indexProyectoActual = 0

    private var fechaInicioSel = ""
    private var fechaFinSel = ""


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

        btnSiguienteProyecto = findViewById(R.id.btnSiguienteProyecto)

        layoutSubtareas = findViewById(R.id.layoutSubtareas)

        btnSettings = findViewById(R.id.btnSettings)

        btnSettings.setOnClickListener {
            mostrarMenuOpciones(it)
        }


        // JSON de ejemplo (puede venir de archivo o API)
        proyectos = parseProyectos(getJsonProyectos())

        mostrarProyectoActual()

        // Mostrar primer proyecto y primera tarea
        btnSiguienteProyecto.setOnClickListener {
            indexProyectoActual++

            if (indexProyectoActual >= proyectos.size) {
                indexProyectoActual = 0  // 🔄 vuelve al primero
            }

            mostrarProyectoActual()
        }
    }

    private fun mostrarProyectoActual() {
        val proyecto = proyectos[indexProyectoActual]

        cargarProyecto(proyecto)


        if (proyecto.tareas.isEmpty()) {
            layoutCheckboxTareas.removeAllViews()
            layoutSubtareas.removeAllViews()


            txtMiniCalendario.text = ""
            txtDescripcionTarea.text = ""


            return
        }
        mostrarTarea(proyecto.tareas[0])
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

                val subtareasList = mutableListOf<SubTarea>()
                val subtareasArray = t.optJSONArray("SubTareas")

                if (subtareasArray != null) {
                    for (s in 0 until subtareasArray.length()) {
                        val sub = subtareasArray.getJSONObject(s)

                        val nombreSub = sub.getString("NombreSubTarea")
                        val descSub = sub.getString("DescripcionSubTarea")
                        val fechaIniSub = sub.getString("FechaInicioSubtarea").substring(0, 10)
                        val fechaFinSub = sub.getString("FechaEntregaSubtarea").substring(0, 10)
                        val estadoSub = sub.getString("EstadoSubTarea")

                        subtareasList.add(
                            SubTarea(
                                nombreSub, descSub, fechaIniSub, fechaFinSub, estadoSub
                                    )
                                         )
                    }
                }

                for (k in 0 until usuariosArray.length()) {
                    val u = usuariosArray.getJSONObject(k)
                    val nombreUsuario = u.getString("nombreUsuario")
                    val email = u.optString("email", "")
                    usuarios.add(Usuario(nombreUsuario, email, R.drawable.avatarejemplo))
                }
                tareas.add(
                    Tarea(
                        nombreTarea,
                        descripcion,
                        fechaInicio,
                        fechaEntrega,
                        estado,
                        usuarios,
                        subtareasList
                         )
                          )

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

            // -------------------------
            // 🔹 MOSTRAR SUBTAREAS
            // -------------------------
            layoutSubtareas.removeAllViews()

            if (tareaSeleccionada.subtareas != null && tareaSeleccionada.subtareas.isNotEmpty()) {
                tareaSeleccionada.subtareas.forEach { sub ->
                    val subLayout = LinearLayout(this@Proyectos2Activity)
                    subLayout.orientation = LinearLayout.VERTICAL
                    subLayout.setPadding(8, 8, 8, 8)

                    val txtSubtarea = TextView(this@Proyectos2Activity)
                    txtSubtarea.text = "• ${sub.nombreSubTarea}"
                    txtSubtarea.textSize = 16f
                    txtSubtarea.setTextColor(Color.BLACK)

                    val txtEstado = TextView(this@Proyectos2Activity)
                    txtEstado.text = "Estado: ${sub.estadoSubTarea}"
                    txtEstado.textSize = 14f
                    txtEstado.setTextColor(Color.DKGRAY)

                    val txtFechas = TextView(this@Proyectos2Activity)
                    txtFechas.text = "${sub.fechaInicioSubtarea} → ${sub.fechaEntregaSubtarea}"
                    txtFechas.textSize = 14f
                    txtFechas.setTextColor(Color.GRAY)

                    subLayout.addView(txtSubtarea)
                    subLayout.addView(txtEstado)
                    subLayout.addView(txtFechas)

                    layoutSubtareas.addView(subLayout)
                }
            } else {
                val txtNoSubtareas = TextView(this@Proyectos2Activity)
                txtNoSubtareas.text = "No hay subtareas"
                txtNoSubtareas.setTextColor(Color.GRAY)
                layoutSubtareas.addView(txtNoSubtareas)
            }
        }

        // Limpiar layout y crear RadioGroup para seleccionar tareas
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

    private fun mostrarMenuOpciones(anchor: View) {
        val view = layoutInflater.inflate(R.layout.menu_tarea_popup, null)
        val popup = PopupWindow(
            view,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
                               )

        popup.elevation = 10f

        // Eventos del menú
        view.findViewById<TextView>(R.id.opCambiarFecha).setOnClickListener {
            popup.dismiss()
            mostrarSelectorCambioFecha(it)
        }

        view.findViewById<TextView>(R.id.opCambiarDescripcion).setOnClickListener {
            popup.dismiss()
            // TODO: abrir editor descripción
        }

        view.findViewById<TextView>(R.id.opCambiarEstado).setOnClickListener {
            popup.dismiss()
            // TODO: abrir dialog estados
        }

        view.findViewById<TextView>(R.id.opGenerarResumen).setOnClickListener {
            popup.dismiss()
            // TODO: generar resumen
        }

        // Mostrar justo debajo de la tuerca
        popup.showAsDropDown(anchor, -60, 20)
    }

    private fun mostrarSelectorCambioFecha(anchor: View) {
        val view = layoutInflater.inflate(R.layout.popup_selector_tipo_fecha, null)
        val popup = PopupWindow(
            view,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
                               )

        val proyecto = proyectos[indexProyectoActual]

        val opProyecto = view.findViewById<TextView>(R.id.opProyecto)
        val opTarea = view.findViewById<TextView>(R.id.opTarea)
        val opSubtarea = view.findViewById<TextView>(R.id.opSubtarea)

        // Deshabilitar según datos del JSON
        if (proyecto.tareas.isEmpty()) {
            opTarea.setTextColor(Color.GRAY)
            opTarea.isEnabled = false
        }

        val tareaActual = proyecto.tareas.firstOrNull()
        if (tareaActual == null || tareaActual.subtareas.isEmpty()) {
            opSubtarea.setTextColor(Color.GRAY)
            opSubtarea.isEnabled = false
        }

        // Clicks
        opProyecto.setOnClickListener {
            popup.dismiss()
            mostrarPopupCalendario(tipo = "proyecto")
        }

        opTarea.setOnClickListener {
            popup.dismiss()
            mostrarPopupCalendario(tipo = "tarea")
        }

        opSubtarea.setOnClickListener {
            popup.dismiss()
            mostrarPopupCalendario(tipo = "subtarea")
        }

        popup.showAsDropDown(anchor, -60, 20)
    }
    private fun mostrarPopupCalendario(tipo: String) {
        val view = layoutInflater.inflate(R.layout.popup_fecha_inicio_fin, null)
        val popup = PopupWindow(
            view,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
                               )

        val btnInicio = view.findViewById<TextView>(R.id.btnFechaInicio)
        val btnFin = view.findViewById<TextView>(R.id.btnFechaFin)
        val btnAplicar = view.findViewById<TextView>(R.id.btnAplicar)

        btnInicio.setOnClickListener {
            mostrarDatePicker { fecha ->
                fechaInicioSel = fecha
                btnInicio.text = "Inicio: $fecha"
            }
        }

        btnFin.setOnClickListener {
            mostrarDatePicker { fecha ->
                fechaFinSel = fecha
                btnFin.text = "Fin: $fecha"
            }
        }

        btnAplicar.setOnClickListener {
            if (fechaInicioSel.isNotEmpty() && fechaFinSel.isNotEmpty()) {
                aplicarCambioFecha(tipo, fechaInicioSel, fechaFinSel)
                popup.dismiss()
                mostrarProyectoActual()
            }
        }

        popup.showAtLocation(view, 0, 0, 0)
    }

    private fun mostrarDatePicker(onSelected:(String)->Unit) {
        val c = java.util.Calendar.getInstance()
        val year = c.get(java.util.Calendar.YEAR)
        val month = c.get(java.util.Calendar.MONTH)
        val day = c.get(java.util.Calendar.DAY_OF_MONTH)

        val dp = android.app.DatePickerDialog(this, { _, y, m, d ->
            val fecha = "%04d-%02d-%02d".format(y, m+1, d)
            onSelected(fecha)
        }, year, month, day)

        dp.show()
    }
    private fun aplicarCambioFecha(
        tipo: String,
        inicio: String,
        fin: String
                                  ) {
        val proyecto = proyectos[indexProyectoActual]

        when (tipo) {
            "proyecto" -> {
                proyecto.fechaInicio = inicio
                proyecto.fechaEntrega = fin
            }
            "tarea" -> {
                val tarea = proyecto.tareas.first()
                tarea.fechaInicio = inicio
                tarea.fechaEntrega = fin
            }
            "subtarea" -> {
                val subtarea = proyecto.tareas.first().subtareas.first()
                subtarea.fechaInicioSubtarea = inicio
                subtarea.fechaEntregaSubtarea = fin
            }
        }
        guardarJson()
    }
    private fun getJsonProyectos(): JSONArray {
        val fileName = "proyectos2.json"
        val file = File(filesDir, fileName)

        if (!file.exists()) {
            // Primera ejecución → copiar desde /raw/ al almacenamiento interno
            val inputStream = assets.open("proyectos2.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            file.writeText(jsonString)
        }

        // Leer SIEMPRE desde almacenamiento interno
        val jsonString = file.readText()
        return JSONArray(jsonString)
    }
    private fun guardarJson() {
        val file = File(filesDir, "proyectos2.json")

        val jsonArray = JSONArray()

        proyectos.forEach { p ->
            val obj = JSONObject()
            obj.put("NombreProyecto", p.nombreProyecto)
            obj.put("fechaInicio", p.fechaInicio)
            obj.put("fechaEntrega", p.fechaEntrega)

            val tareasArr = JSONArray()
            p.tareas.forEach { t ->
                val tObj = JSONObject()
                tObj.put("nombreTarea", t.nombreTarea)
                tObj.put("descripcion", t.descripcion)
                tObj.put("fechaInicio", t.fechaInicio)
                tObj.put("fechaEntrega", t.fechaEntrega)
                tObj.put("estado", t.estado)

                val subtArr = JSONArray()
                t.subtareas.forEach { s ->
                    val sObj = JSONObject()
                    sObj.put("NombreSubTarea", s.nombreSubTarea)
                    sObj.put("DescripcionSubTarea", s.descripcionSubTarea)
                    sObj.put("FechaInicioSubtarea", s.fechaInicioSubtarea)
                    sObj.put("FechaEntregaSubtarea", s.fechaEntregaSubtarea)
                    sObj.put("EstadoSubTarea", s.estadoSubTarea)
                    subtArr.put(sObj)
                }

                tObj.put("SubTareas", subtArr)
                tareasArr.put(tObj)
            }

            obj.put("Tareas", tareasArr)
            jsonArray.put(obj)
        }

        file.writeText(jsonArray.toString(4))
    }


}

