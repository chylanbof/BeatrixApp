package com.example.beatrixapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class Proyectos2Activity : AppCompatActivity() {

    private lateinit var tareasArrayGlobal: JSONArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.proyectos2)

        val nombreProyecto = intent.getStringExtra("nombreProyecto")
        if (nombreProyecto == null) {
            finish()
            return
        }

        // 1. Cargar el JSON
        val jsonString = resources.openRawResource(R.raw.proyectos)
            .bufferedReader().use { it.readText() }

        val jsonArray = JSONArray(jsonString)

        var proyectoObj: JSONObject? = null

        // 2. Buscar el proyecto cuyo nombre coincida
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.optString("NombreProyecto") == nombreProyecto) {
                proyectoObj = obj
                break
            }
        }

        if (proyectoObj == null) {
            finish()
            return
        }

        // 3. Rellenar datos en pantalla
        rellenarPantalla(proyectoObj)
    }

    private fun rellenarPantalla(proyecto: JSONObject) {

        // 🟦 1. Nombre
        findViewById<TextView>(R.id.txtNombreProyecto).text =
            proyecto.optString("NombreProyecto", "Sin nombre")

        // 🟦 2. Descripción
        findViewById<TextView>(R.id.txtDescripcionTarea).text =
            proyecto.optString("DescripcionProyecto", "")

        // 🟦 3. Fechas del proyecto (solo fecha)
        val fechaIni = proyecto.optString("fechaInicio", "").let { if (it.isNotEmpty()) soloFecha(it) else "" }
        val fechaFin = proyecto.optString("fechaEntrega", "").let { if (it.isNotEmpty()) soloFecha(it) else "" }


            findViewById<TextView>(R.id.txtFechasTarea).text =
            listOf(fechaIni, fechaFin)
                .filter { it.isNotEmpty() }
                .joinToString(" - ")

        // 🟦 4. Usuarios implicados (cuento cuántos) — opcional
        val usuarios = proyecto.optJSONArray("UsuariosAsignados") ?: JSONArray()
        val countUsuarios = usuarios.length()

        val layoutFechas = findViewById<LinearLayout>(R.id.layoutFechasUsuarios)

        val txtUsuarios = TextView(this).apply {
            text = "Usuarios: $countUsuarios"
            textSize = 14f
            setTextColor(Color.WHITE)
            background = resources.getDrawable(R.drawable.users_badge, null)
            setPadding(20, 10, 20, 10)
        }



        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
                                              )
        params.marginStart = 12
        params.gravity = android.view.Gravity.END

        txtUsuarios.layoutParams = params
        txtUsuarios.gravity = android.view.Gravity.END

        layoutFechas.addView(txtUsuarios)


        // 🟦 5. TAREAS CON RADIO BUTTONS
        val tareasLayout = findViewById<RadioGroup>(R.id.layoutRadioTareas)
        tareasArrayGlobal = proyecto.optJSONArray("Tareas") ?: JSONArray()

        tareasLayout.removeAllViews()

        for (i in 0 until tareasArrayGlobal.length()) {
            val tarea = tareasArrayGlobal.getJSONObject(i)

            val rb = RadioButton(this)
            rb.text = tarea.optString("nombreTarea", "Tarea ${i+1}") +
                    "  •  " + tarea.optString("estado", "")
            rb.id = i   // identificador: índice de la tarea
            tareasLayout.addView(rb)
        }

        // Cuando se selecciona una tarea, actualiza el calendario (TextView txtMiniCalendario)
        tareasLayout.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId >= 0 && checkedId < tareasArrayGlobal.length()) {

                val tarea = tareasArrayGlobal.getJSONObject(checkedId)

                // 🔵 Actualizar calendario
                val f1 = tarea.optString("fechaInicio", "").let { if (it.isNotEmpty()) soloFecha(it) else "" }
                val f2 = tarea.optString("fechaEntrega", "").let { if (it.isNotEmpty()) soloFecha(it) else "" }
                findViewById<TextView>(R.id.txtMiniCalendario).text =
                    listOf(f1, f2).filter { it.isNotEmpty() }.joinToString(" - ")

                // 🔵 Mostrar SOLO subtareas de esta tarea
                val subLayout = findViewById<LinearLayout>(R.id.layoutSubtareas)
                subLayout.removeAllViews()

                val subtareas = tarea.optJSONArray("SubTareas") ?: JSONArray()

                for (j in 0 until subtareas.length()) {
                    val sub = subtareas.getJSONObject(j)
                    val txt = TextView(this)
                    txt.text = "• ${sub.optString("NombreSubTarea", "Subtarea")}"
                    subLayout.addView(txt)
                }

            } else {
                findViewById<TextView>(R.id.txtMiniCalendario).text = "Calendario dinámico aquí"
            }
        }

        // 🟦 6. SUBTAREAS (opcional) — usa tareasArrayGlobal en lugar de la variable inexistente
        val subLayout = findViewById<LinearLayout>(R.id.layoutSubtareas)
        subLayout.removeAllViews()

        for (i in 0 until tareasArrayGlobal.length()) {
            val tarea = tareasArrayGlobal.getJSONObject(i)
            val subtareas = tarea.optJSONArray("SubTareas") ?: JSONArray()

            for (j in 0 until subtareas.length()) {
                val sub = subtareas.getJSONObject(j)
                val txt = TextView(this)
                txt.text = "• ${sub.optString("NombreSubTarea", "Subtarea")}"
                subLayout.addView(txt)
            }
        }


        // 🟦 7. Estado (pongo el estado de la PRIMERA tarea, o "Sin tareas" si no hay)
        val txtEstado = findViewById<TextView>(R.id.txtEstadoTarea)
        val estado = if (tareasArrayGlobal.length() > 0)
            tareasArrayGlobal.getJSONObject(0).optString("estado", "Sin estado")
        else
            "Sin tareas"

        txtEstado.text = estado

        txtEstado.backgroundTintList =
            ColorStateList.valueOf(fondoEstado(estado.trim().lowercase()))

        if (estado.trim().equals("Pendiente", ignoreCase = true)) {
            txtEstado.setTextColor(Color.BLACK)
        } else {
            txtEstado.setTextColor(Color.WHITE)
        }

        txtUsuarios.setOnClickListener {

            // Obtener usuarios
            val usuariosArray = proyecto.optJSONArray("UsuariosAsignados") ?: JSONArray()

            if (usuariosArray.length() == 0) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Usuarios")
                    .setMessage("No hay usuarios asignados.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            // Convertir a lista legible
            val listaUsuarios = Array(usuariosArray.length()) { i ->
                val usuarioObj = usuariosArray.getJSONObject(i)
                usuarioObj.optString("nombreUsuario", "Usuario sin nombre")
            }


            // Mostrar diálogo
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Usuarios asignados")
                .setItems(listaUsuarios, null)
                .setPositiveButton("Cerrar", null)
                .show()
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

}
