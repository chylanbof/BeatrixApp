package com.example.beatrixapp

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import android.app.DatePickerDialog
import com.example.beatrixapp.utils.LocaleHelper
import java.util.Calendar






class Proyectos2Activity : BaseActivity() {

    private val ARCHIVO_JSON = "proyectos.json"

    private lateinit var jsonArrayGlobal: JSONArray
    private lateinit var proyectoActual: JSONObject
    private lateinit var tareasArrayGlobal: JSONArray

    private lateinit var tareasListener: RadioGroup.OnCheckedChangeListener


    private var tareaSeleccionadaIndex: Int = -1

    private val estadosDisponibles by lazy {
        arrayOf(
            getString(R.string.estado_pendiente),
            getString(R.string.estado_en_progreso),
            getString(R.string.estado_en_pausa),
            getString(R.string.estado_en_espera),
            getString(R.string.estado_revision),
            getString(R.string.estado_completado),
            getString(R.string.estado_cancelada)
               )
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.proyectos2)

        jsonArrayGlobal = leerJson()




        val nombreProyecto = intent.getStringExtra("nombreProyecto")
        if (nombreProyecto == null) {
            finish()
            return
        }

        // 1. Cargar el JSON


        var proyectoObj: JSONObject? = null

        // 2. Buscar el proyecto cuyo nombre coincida
        for (i in 0 until jsonArrayGlobal.length()) {
            val obj = jsonArrayGlobal.getJSONObject(i)
            if (obj.optString("NombreProyecto") == nombreProyecto) {
                proyectoObj = obj
                break
            }
        }

        if (proyectoObj == null) {
            finish()
            return
        }

        proyectoActual = proyectoObj

        // 3. Rellenar datos en pantalla
        rellenarPantalla(proyectoObj)
    }

    private fun rellenarPantalla(proyecto: JSONObject) {

        // 🟦 1. Nombre
        findViewById<TextView>(R.id.txtNombreProyecto).text =
            proyecto.optString(
                "NombreProyecto",
                getString(R.string.sin_nombre)
                              )


        // 🟦 2. Descripción
        findViewById<TextView>(R.id.txtDescripcionTarea).text =
            proyecto.optString("DescripcionProyecto", "")

        // 🟦 3. Fechas del proyecto (solo fecha)
        val fechaIni =
            proyecto.optString("fechaInicio", "").let { if (it.isNotEmpty()) soloFecha(it) else "" }
        val fechaFin = proyecto.optString("fechaEntrega", "")
            .let { if (it.isNotEmpty()) soloFecha(it) else "" }


        findViewById<TextView>(R.id.txtFechasTarea).text =
            listOf(fechaIni, fechaFin).filter { it.isNotEmpty() }.joinToString(" - ")

        // 🟦 4. Usuarios implicados (cuento cuántos) — opcional
        val usuarios = proyecto.optJSONArray("UsuariosAsignados") ?: JSONArray()
        val countUsuarios = usuarios.length()

        val layoutFechas = findViewById<LinearLayout>(R.id.layoutFechasUsuarios)

        val layoutUsuarios = findViewById<LinearLayout>(R.id.layoutUsuarios)
        layoutUsuarios.removeAllViews()

        val txtUsuarios = TextView(this).apply {
            text = getString(R.string.usuarios_numero, countUsuarios)
            textSize = 14f
            setTextColor(Color.WHITE)
            background = resources.getDrawable(R.drawable.users_badge, null)
            setPadding(20, 10, 20, 10)
        }




        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                                              )
        params.marginStart = 12
        params.gravity = android.view.Gravity.END

        txtUsuarios.layoutParams = params
        txtUsuarios.gravity = android.view.Gravity.END

        layoutUsuarios.addView(txtUsuarios)


        // 🟦 5. TAREAS CON RADIO BUTTONS
        val tareasLayout = findViewById<RadioGroup>(R.id.layoutRadioTareas)
        tareasArrayGlobal = proyecto.optJSONArray("Tareas") ?: JSONArray()

        tareasLayout.removeAllViews()

        for (i in 0 until tareasArrayGlobal.length()) {
            val tarea = tareasArrayGlobal.getJSONObject(i)

            val nombre = tarea.optString("nombreTarea", "Tarea ${i + 1}")
            val estado = tarea.optString("estado", "Sin estado")

            val rb = RadioButton(this).apply {
                id = i
                text = "$nombre  •  $estado"
                textSize = 14f
                setPadding(24, 16, 24, 16)

                // 🎨 Color de fondo según estado
                background = resources.getDrawable(R.drawable.rounded_orange, null)
                backgroundTintList =
                    ColorStateList.valueOf(fondoEstado(estado.lowercase()))

                // 📝 Color del texto (amarillo necesita negro)
                setTextColor(
                    if (estado.equals("pendiente", true)) Color.BLACK
                    else Color.WHITE
                            )
            }

            tareasLayout.addView(rb)
        }


        // 🔵 Restaurar selección SIN disparar el listener
        tareasLayout.setOnCheckedChangeListener(null)


        if (tareaSeleccionadaIndex != -1 &&
            tareaSeleccionadaIndex < tareasLayout.childCount) {

            tareasLayout.check(tareaSeleccionadaIndex)
        }

        if (tareaSeleccionadaIndex != -1 &&
            tareaSeleccionadaIndex < tareasArrayGlobal.length()) {

            val tarea = tareasArrayGlobal.getJSONObject(tareaSeleccionadaIndex)

            val f1 = tarea.optString("fechaInicio", "")
                .let { if (it.isNotEmpty()) soloFecha(it) else "" }
            val f2 = tarea.optString("fechaEntrega", "")
                .let { if (it.isNotEmpty()) soloFecha(it) else "" }

            findViewById<TextView>(R.id.txtMiniCalendario).text =
                listOf(f1, f2).filter { it.isNotEmpty() }.joinToString(" - ")

            val descripcion = tarea.optString("descripcion", "")
            val txtDesc = findViewById<TextView>(R.id.txtDescripcionTareaSeleccionada)

            if (descripcion.isNotEmpty()) {
                txtDesc.text = descripcion
                txtDesc.visibility = View.VISIBLE
            } else {
                txtDesc.visibility = View.GONE
            }
        }


        tareasListener = RadioGroup.OnCheckedChangeListener { _, checkedId ->
            if (checkedId >= 0 && checkedId < tareasArrayGlobal.length()) {

                tareaSeleccionadaIndex = checkedId

                val tarea = tareasArrayGlobal.getJSONObject(checkedId)

                val f1 = tarea.optString("fechaInicio", "")
                    .let { if (it.isNotEmpty()) soloFecha(it) else "" }
                val f2 = tarea.optString("fechaEntrega", "")
                    .let { if (it.isNotEmpty()) soloFecha(it) else "" }

                findViewById<TextView>(R.id.txtMiniCalendario).text =
                    listOf(f1, f2).filter { it.isNotEmpty() }.joinToString(" - ")

                val descripcion = tarea.optString("descripcion", "")
                val txtDesc = findViewById<TextView>(R.id.txtDescripcionTareaSeleccionada)

                if (descripcion.isNotEmpty()) {
                    txtDesc.text = descripcion
                    txtDesc.visibility = View.VISIBLE
                } else {
                    txtDesc.visibility = View.GONE
                }
                pintarSubtareas()
            }
        }

// ✅ Activar listener (AL FINAL)
        tareasLayout.setOnCheckedChangeListener(tareasListener)


        // 🟦 6. SUBTAREAS (opcional) — usa tareasArrayGlobal en lugar de la variable inexistente
        val subLayout = findViewById<LinearLayout>(R.id.layoutSubtareas)
        subLayout.removeAllViews()

        if (tareaSeleccionadaIndex != -1 &&
            tareaSeleccionadaIndex < tareasArrayGlobal.length()) {

            val tarea = tareasArrayGlobal.getJSONObject(tareaSeleccionadaIndex)
            val subtareas = tarea.optJSONArray("SubTareas") ?: JSONArray()

            for (j in 0 until subtareas.length()) {
                val sub = subtareas.getJSONObject(j)
                val txt = TextView(this)
                txt.text = "• ${sub.optString("NombreSubTarea", "Subtarea")}"
                subLayout.addView(txt)
            }
        }




        txtUsuarios.setOnClickListener {

            // Obtener usuarios
            val usuariosArray = proyecto.optJSONArray("UsuariosAsignados") ?: JSONArray()

            if (usuariosArray.length() == 0) {
                androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Usuarios")
                    .setMessage("No hay usuarios asignados.").setPositiveButton("OK", null).show()
                return@setOnClickListener
            }

            // Convertir a lista legible
            val listaUsuarios = Array(usuariosArray.length()) { i ->
                val usuarioObj = usuariosArray.getJSONObject(i)
                usuarioObj.optString("nombreUsuario", "Usuario sin nombre")
            }


            // Mostrar diálogo
            androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Usuarios asignados")
                .setItems(listaUsuarios, null).setPositiveButton("Cerrar", null).show()
        }

        findViewById<View>(R.id.btnSettings).setOnClickListener {

            val opciones = arrayOf(
                getString(R.string.opcion_cambiar_fecha),
                getString(R.string.opcion_cambiar_descripcion),
                getString(R.string.opcion_cambiar_estado),
                getString(R.string.opcion_generar_resumen)
                                  )


            androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Opciones del proyecto")
                .setItems(opciones) { _, which ->
                    when (which) {
                        0 -> cambiarFecha()
                        1 -> cambiarDescripcion()
                        2 -> cambiarEstado()
                        3 -> generarResumen()
                    }
                }.setNegativeButton("Cancelar", null).show()
        }


    }

    private fun soloFecha(fechaIso: String): String {
        return fechaIso.substringBefore("T")
    }

    private fun fondoEstado(estado: String): Int {
        return when (estado.lowercase()) {
            "pendiente" -> Color.YELLOW
            "en progreso" -> Color.rgb(81, 190, 207)
            "en espera" -> Color.rgb(39, 238, 245)
            "revisión" -> Color.MAGENTA
            "completado" -> Color.GREEN
            "cancelada" -> Color.RED
            else -> Color.LTGRAY
        }
    }

    private fun cambiarFecha() {

        val opciones = arrayOf(
            "Cambiar fecha del proyecto",
            "Cambiar fecha de la tarea seleccionada"
                              )

        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Cambiar fecha")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> cambiarFechaProyecto()
                    1 -> cambiarFechaTarea()
                }
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun cambiarFechaProyecto() {
        val opciones = arrayOf("Fecha inicio", "Fecha entrega")

        AlertDialog.Builder(this)
            .setTitle("¿Qué fecha quieres cambiar?")
            .setItems(opciones) { _, which ->
                mostrarDatePickerProyecto(if (which == 0) "fechaInicio" else "fechaEntrega")
            }
            .show()
    }

    private fun mostrarDatePickerProyecto(campo: String) {
        val calendario = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val nuevaFecha = String.format(
                    "%04d-%02d-%02dT00:00:00",
                    year, month + 1, day
                                              )

                proyectoActual.put(campo, nuevaFecha)
                guardarJson(jsonArrayGlobal)
                rellenarPantalla(proyectoActual)

                mostrarMensaje("Fecha actualizada correctamente")
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
                        ).show()
    }




    private fun cambiarFechaTarea() {

        val tareasLayout = findViewById<RadioGroup>(R.id.layoutRadioTareas)
        val tareaIndex = tareasLayout.checkedRadioButtonId

        if (tareaIndex == -1) {
            mostrarMensaje(getString(R.string.selecciona_tarea))
            return
        }

        val tarea = tareasArrayGlobal.getJSONObject(tareaIndex)

        val opciones = arrayOf("Fecha inicio", "Fecha entrega")

        AlertDialog.Builder(this)
            .setTitle("¿Qué fecha quieres cambiar?")
            .setItems(opciones) { _, which ->
                val campo = if (which == 0) "fechaInicio" else "fechaEntrega"
                mostrarDatePickerTarea(tarea, campo)
            }
            .show()
    }

    private fun mostrarDatePickerTarea(tarea: JSONObject, campo: String) {

        val calendario = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->

                val nuevaFecha = String.format(
                    "%04d-%02d-%02dT00:00:00",
                    year, month + 1, day
                                              )

                tarea.put(campo, nuevaFecha)

                guardarJson(jsonArrayGlobal)
                rellenarPantalla(proyectoActual)

                mostrarMensaje("Fecha de la tarea actualizada correctamente")
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
                        ).show()
    }

    private fun cambiarDescripcion() {
        val opciones = arrayOf(
            "Cambiar descripción del proyecto",
            "Cambiar descripción de la tarea seleccionada",
            "Cambiar descripción de una subtarea"
                              )

        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Cambiar fecha")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> cambiarDescripcionProyecto()
                    1 -> cambiarDescripcionTarea()
                    2 -> cambiarDescripcionSubtarea()
                }
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun cambiarDescripcionProyecto() {
        val descripcionActual = proyectoActual.optString("DescripcionProyecto", "")

        mostrarDialogoEditarDescripcion(
            "Editar descripción del proyecto",
            descripcionActual
                                       ) { nuevaDescripcion ->
            proyectoActual.put("DescripcionProyecto", nuevaDescripcion)
            guardarJson(jsonArrayGlobal)
            rellenarPantalla(proyectoActual)
        }
    }


    private fun cambiarDescripcionTarea() {

        if (tareaSeleccionadaIndex == -1 ||
            tareaSeleccionadaIndex >= tareasArrayGlobal.length()) {
            mostrarMensaje("Selecciona una tarea primero")
            return
        }

        val tarea = tareasArrayGlobal.getJSONObject(tareaSeleccionadaIndex)
        val descripcionActual = tarea.optString("descripcion", "")

        mostrarDialogoEditarDescripcion(
            "Editar descripción de la tarea",
            descripcionActual
                                       ) { nuevaDescripcion ->
            tarea.put("descripcion", nuevaDescripcion)
            guardarJson(jsonArrayGlobal)
            rellenarPantalla(proyectoActual)
        }
    }


    private fun cambiarDescripcionSubtarea() {

        if (tareaSeleccionadaIndex == -1) {
            mostrarMensaje("Selecciona una tarea primero")
            return
        }

        val tarea = tareasArrayGlobal.getJSONObject(tareaSeleccionadaIndex)
        val subtareas = tarea.optJSONArray("SubTareas") ?: JSONArray()

        if (subtareas.length() == 0) {
            mostrarMensaje("La tarea no tiene subtareas")
            return
        }

        val nombres = Array(subtareas.length()) { i ->
            subtareas.getJSONObject(i)
                .optString("NombreSubTarea", "Subtarea ${i + 1}")
        }

        AlertDialog.Builder(this)
            .setTitle("Selecciona una subtarea")
            .setItems(nombres) { _, indexSubtarea ->

                val subtarea = subtareas.getJSONObject(indexSubtarea)
                val descripcionActual =
                    subtarea.optString("DescripcionSubTarea", "")

                mostrarDialogoEditarDescripcion(
                    "Editar descripción de la subtarea",
                    descripcionActual
                                               ) { nuevaDescripcion ->
                    subtarea.put("DescripcionSubTarea", nuevaDescripcion)
                    guardarJson(jsonArrayGlobal)
                    rellenarPantalla(proyectoActual)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cambiarEstado() {
        val opciones = arrayOf(
            "Cambiar estado de la tarea seleccionada",
            "Cambiar estado de una subtarea"
                              )

        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Cambiar fecha")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> cambiarEstadoTarea()
                    1 -> cambiarEstadoSubtarea()
                }
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun cambiarEstadoTarea() {

        if (tareaSeleccionadaIndex == -1 ||
            tareaSeleccionadaIndex >= tareasArrayGlobal.length()) {
            mostrarMensaje("Selecciona una tarea primero")
            return
        }

        val tarea = tareasArrayGlobal.getJSONObject(tareaSeleccionadaIndex)

        AlertDialog.Builder(this)
            .setTitle("Nuevo estado de la tarea")
            .setItems(estadosDisponibles) { _, index ->
                val nuevoEstado = estadosDisponibles[index]

                tarea.put("estado", nuevoEstado)

                guardarJson(jsonArrayGlobal)
                rellenarPantalla(proyectoActual)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cambiarEstadoSubtarea() {

        if (tareaSeleccionadaIndex == -1) {
            mostrarMensaje("Selecciona una tarea primero")
            return
        }

        val tarea = tareasArrayGlobal.getJSONObject(tareaSeleccionadaIndex)
        val subtareas = tarea.optJSONArray("SubTareas") ?: JSONArray()

        if (subtareas.length() == 0) {
            mostrarMensaje("La tarea no tiene subtareas")
            return
        }

        val nombres = Array(subtareas.length()) { i ->
            subtareas.getJSONObject(i)
                .optString("NombreSubTarea", "Subtarea ${i + 1}")
        }

        AlertDialog.Builder(this)
            .setTitle("Selecciona una subtarea")
            .setItems(nombres) { _, indexSubtarea ->

                val subtarea = subtareas.getJSONObject(indexSubtarea)

                AlertDialog.Builder(this)
                    .setTitle("Nuevo estado de la subtarea")
                    .setItems(estadosDisponibles) { _, indexEstado ->

                        val nuevoEstado = estadosDisponibles[indexEstado]
                        subtarea.put("EstadoSubTarea", nuevoEstado)

                        guardarJson(jsonArrayGlobal)
                        rellenarPantalla(proyectoActual)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun generarResumen() {
        
        var hayCompletadas = false

        for (i in 0 until tareasArrayGlobal.length()) {
            val estado = tareasArrayGlobal
                .getJSONObject(i)
                .optString("estado", "")
                .lowercase()

            if (estado == "completado") {
                hayCompletadas = true
                break
            }
        }

        if (!hayCompletadas) {
            mostrarMensaje("No hay tareas finalizadas para generar un resumen")
            return
        }

        val intent = android.content.Intent(this, ResumenActivity::class.java)
        intent.putExtra("nombreProyecto", proyectoActual.optString("NombreProyecto"))
        startActivity(intent)
    }


    private fun mostrarMensaje(texto: String) {
        androidx.appcompat.app.AlertDialog.Builder(this).setMessage(texto)
            .setPositiveButton("OK", null).show()
    }

    private fun leerJson(): JSONArray {
        val file = File(filesDir, ARCHIVO_JSON)

        if (!file.exists()) {
            val input = resources.openRawResource(R.raw.proyectos)
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val jsonString = file.readText()
        return JSONArray(jsonString)
    }

    private fun guardarJson(jsonArray: JSONArray) {
        val file = File(filesDir, ARCHIVO_JSON)
        file.writeText(jsonArray.toString(2)) // bonito formateado
    }

    private fun pintarSubtareas() {
        val subLayout = findViewById<LinearLayout>(R.id.layoutSubtareas)
        subLayout.removeAllViews()

        if (tareaSeleccionadaIndex == -1 ||
            tareaSeleccionadaIndex >= tareasArrayGlobal.length()) {
            return
        }

        val tarea = tareasArrayGlobal.getJSONObject(tareaSeleccionadaIndex)
        val subtareas = tarea.optJSONArray("SubTareas") ?: JSONArray()

        for (i in 0 until subtareas.length()) {

            val sub = subtareas.getJSONObject(i)

            // 🟦 Contenedor vertical
            val contenedor = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(8, 8, 8, 16)
            }

            // 🟦 Fila horizontal (nombre + estado)
            val filaTitulo = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            // 🔹 Nombre subtarea
            val txtNombre = TextView(this).apply {
                text = "• ${sub.optString("NombreSubTarea", "Subtarea")}"
                textSize = 15f
                setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                                                        )
            }

            // 🔹 Estado subtarea (CLAVE CORRECTA)
            val estado = sub.optString("EstadoSubTarea", "Sin estado")

            val txtEstado = TextView(this).apply {
                text = estado
                textSize = 12f
                setPadding(14, 6, 14, 6)
                setTextColor(Color.WHITE)
                background = resources.getDrawable(R.drawable.rounded_orange, null)
                backgroundTintList =
                    ColorStateList.valueOf(fondoEstado(estado.lowercase()))
            }

            filaTitulo.addView(txtNombre)
            filaTitulo.addView(txtEstado)
            contenedor.addView(filaTitulo)

            // 🔹 Descripción subtarea
            val descripcion = sub.optString("DescripcionSubTarea", "")
            if (descripcion.isNotEmpty()) {
                val txtDescripcion = TextView(this).apply {
                    text = descripcion
                    textSize = 13f
                    setTextColor(Color.DKGRAY)
                    setPadding(24, 4, 0, 0)
                }
                contenedor.addView(txtDescripcion)
            }

            subLayout.addView(contenedor)
        }
    }



    private fun mostrarDialogoEditarDescripcion(
        titulo: String,
        descripcionActual: String,
        onGuardar: (String) -> Unit
                                               ) {
        val input = android.widget.EditText(this)
        input.setText(descripcionActual)
        input.setSelection(descripcionActual.length)

        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(input)
            .setPositiveButton(getString(R.string.guardar)) { _, _ ->
                val nuevaDescripcion = input.text.toString().trim()
                onGuardar(nuevaDescripcion)
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    // private fun cambiarIdioma(codigo: String) { Esto se debe poner donde cambiemos el idioma de la app
//        LocaleHelper.setLocale(this, codigo)
//        recreate()
//    }


}
