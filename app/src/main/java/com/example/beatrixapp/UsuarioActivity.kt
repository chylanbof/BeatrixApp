package com.example.beatrixapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.beatrixapp.utils.LocaleHelper
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class UsuarioActivity : BaseActivity() {

    // PROPIEDAD DE CLASE: Ahora es nula por defecto y se llena con la sesión real
    private var usuarioLogueado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleHelper.setLocale(this, LocaleHelper.getIdiomaGuardado(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios)

        // 1. RECUPERAR SESIÓN: Usamos la misma lógica que en CalendarioActivity
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        usuarioLogueado = prefs.getString("loggedUser", null)

        // 2. SEGURIDAD: Si no hay usuario, cerramos la pantalla
        if (usuarioLogueado == null) {
            finish()
            return
        }

        // 3. CARGAR DATOS (Usamos !! porque ya validamos que no es null arriba)
        cargarDatosDelPerfil(usuarioLogueado!!)
        cargarEstadisticas(usuarioLogueado!!)

        // 4. CONFIGURAR BOTÓN EDITAR
        val btnEditar = findViewById<Button>(R.id.btnEditarPerfil)
        btnEditar.setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            intent.putExtra("USUARIO_KEY", usuarioLogueado)
            startActivity(intent)
        }

        // 5. CONFIGURAR IDIOMA
        val btnCambiarIdioma = findViewById<ImageButton>(R.id.btnCambiarIdioma)
        btnCambiarIdioma.setOnClickListener {
            mostrarDialogoIdioma()
        }

        // 6. NAVEGACIÓN (Bottom Menu)
        setupNavigationButtons()
    }

    // Al volver de "Editar Perfil", recargamos los datos
    override fun onResume() {
        super.onResume()
        usuarioLogueado?.let {
            cargarDatosDelPerfil(it)
        }
    }

    // ==========================================
    //      LÓGICA DE DATOS DEL PERFIL
    // ==========================================

    private fun cargarDatosDelPerfil(usernameTarget: String) {
        val jsonString = leerJSONUsuariosPriorizandoInterno()
        if (jsonString.isNotEmpty()) {
            try {
                val jsonArray = JSONArray(jsonString)
                var usuarioEncontrado: JSONObject? = null

                for (i in 0 until jsonArray.length()) {
                    val usuarioObj = jsonArray.getJSONObject(i)
                    if (usuarioObj.getString("nombreUsuario") == usernameTarget) {
                        usuarioEncontrado = usuarioObj
                        break
                    }
                }

                if (usuarioEncontrado != null) {
                    rellenarDatosPersonales(usuarioEncontrado)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun rellenarDatosPersonales(usuario: JSONObject) {
        findViewById<TextView>(R.id.tvNombreCompleto).text = usuario.optString("nombreApellidos", "Sin nombre")
        findViewById<TextView>(R.id.tvNombreUsuario).text = usuario.optString("nombreUsuario", "")

        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        tvEmail.text = usuario.optString("email", "")
        tvEmail.paintFlags = tvEmail.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        findViewById<TextView>(R.id.tvTelefono).text = usuario.optString("telefono", "")

        val pass = usuario.optString("contrasena", "")
        findViewById<TextView>(R.id.tvContrasena).text = if (pass.isEmpty()) "No definida" else pass
    }

    // ==========================================
    //      LÓGICA DE ESTADÍSTICAS Y GRÁFICOS
    // ==========================================

    private fun cargarEstadisticas(usernameTarget: String) {
        val jsonString = leerJSONProyectosPriorizandoInterno()

        if (jsonString.isNotEmpty()) {
            try {
                val jsonArray = JSONArray(jsonString)
                var contadorProyectos = 0
                var contadorTareas = 0
                val mapaEstadosTareas = HashMap<String, Int>()

                for (i in 0 until jsonArray.length()) {
                    val proyecto = jsonArray.getJSONObject(i)

                    // Proyectos asignados
                    if (proyecto.has("UsuariosAsignados")) {
                        val usuariosProy = proyecto.getJSONArray("UsuariosAsignados")
                        for (j in 0 until usuariosProy.length()) {
                            if (usuariosProy.getJSONObject(j).getString("nombreUsuario") == usernameTarget) {
                                contadorProyectos++
                                break
                            }
                        }
                    }

                    // Tareas y Estados
                    if (proyecto.has("Tareas")) {
                        val tareasArray = proyecto.getJSONArray("Tareas")
                        for (k in 0 until tareasArray.length()) {
                            val tarea = tareasArray.getJSONObject(k)
                            if (tarea.has("usuariosAsignados")) {
                                val usuariosTarea = tarea.getJSONArray("usuariosAsignados")
                                var esTareaMia = false
                                for (l in 0 until usuariosTarea.length()) {
                                    if (usuariosTarea.getJSONObject(l).getString("nombreUsuario") == usernameTarget) {
                                        esTareaMia = true
                                        break
                                    }
                                }

                                if (esTareaMia) {
                                    contadorTareas++
                                    val estado = tarea.optString("estado", "Otros")
                                    mapaEstadosTareas[estado] = mapaEstadosTareas.getOrDefault(estado, 0) + 1
                                }
                            }
                        }
                    }
                }

                findViewById<TextView>(R.id.tvProyectosCount).text = contadorProyectos.toString()
                findViewById<TextView>(R.id.tvTareasCount).text = contadorTareas.toString()

                if (contadorTareas > 0) {
                    configurarGraficoTareas(mapaEstadosTareas)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun configurarGraficoTareas(mapaEstados: HashMap<String, Int>) {
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val entries = ArrayList<PieEntry>()
        for ((estado, cantidad) in mapaEstados) {
            entries.add(PieEntry(cantidad.toFloat(), estado))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawValues(false)

        val colores = ArrayList<Int>()
        colores.add(Color.parseColor("#66BB6A"))
        colores.add(Color.parseColor("#FFCA28"))
        colores.add(Color.parseColor("#EF5350"))
        colores.add(Color.parseColor("#166269"))
        colores.add(Color.parseColor("#AB47BC"))
        dataSet.colors = colores

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.centerText = "Mis Tareas"
        pieChart.setCenterTextSize(14f)
        pieChart.setDrawEntryLabels(false)

        val legend = pieChart.legend
        legend.isEnabled = true
        legend.textColor = Color.WHITE
        legend.isWordWrapEnabled = true

        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    // ==========================================
    //      UTILIDADES DE ARCHIVOS
    // ==========================================

    private fun leerArchivoRaw(resourceId: Int): String? {
        return try {
            val inputStream = resources.openRawResource(resourceId)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.use { it.readText() }
        } catch (e: Exception) { null }
    }

    private fun leerJSONUsuariosPriorizandoInterno(): String {
        return try {
            openFileInput("usuarios_data.json").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            leerArchivoRaw(R.raw.usuarios) ?: "[]"
        }
    }

    private fun leerJSONProyectosPriorizandoInterno(): String {
        return try {
            openFileInput("proyectos_data.json").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            leerArchivoRaw(R.raw.proyectos) ?: "[]"
        }
    }

    // ==========================================
    //      INTERFAZ Y NAVEGACIÓN
    // ==========================================

    private fun setupNavigationButtons() {
        val includeLayout = findViewById<View>(R.id.boton_bottom)

        includeLayout.findViewById<ImageView>(R.id.btn_home).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        includeLayout.findViewById<ImageView>(R.id.btn_proyecto).setOnClickListener {
            startActivity(Intent(this, ProyectosActivity::class.java))
        }
        includeLayout.findViewById<ImageView>(R.id.btn_perfil).setOnClickListener {
            // Ya estamos aquí
        }
        includeLayout.findViewById<ImageView>(R.id.btn_calendario).setOnClickListener {
            startActivity(Intent(this, CalendarioActivity::class.java))
        }
    }

    private fun mostrarDialogoIdioma() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_idioma, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<LinearLayout>(R.id.btnEspañol).setOnClickListener {
            cambiarIdioma("es")
            dialog.dismiss()
        }
        dialogView.findViewById<LinearLayout>(R.id.btnCatalan).setOnClickListener {
            cambiarIdioma("ca")
            dialog.dismiss()
        }
        dialogView.findViewById<LinearLayout>(R.id.btnIngles).setOnClickListener {
            cambiarIdioma("en")
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun cambiarIdioma(codigo: String) {
        LocaleHelper.setLocale(this, codigo)
        recreate()
    }
}