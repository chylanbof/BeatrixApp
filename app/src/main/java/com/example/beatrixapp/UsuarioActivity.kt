package com.example.beatrixapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class UsuarioActivity : AppCompatActivity() {

    // Variable global para simular la sesión.
    private var usuarioLogueado = "afernandez"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios)

        // 1. Cargar Datos Personales Iniciales
        cargarDatosDelPerfil(usuarioLogueado)

        // 2. Cargar Estadísticas y Gráficos
        cargarEstadisticas(usuarioLogueado)

        // 3. Configurar Botón "Editar Perfil"
        val btnEditar = findViewById<Button>(R.id.btnEditarPerfil)
        btnEditar.setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            // Enviamos el usuario por si la otra activity lo necesita
            intent.putExtra("USUARIO_KEY", usuarioLogueado)
            startActivity(intent)
        }
    }

    // Al volver de "Editar Perfil", recargamos los datos por si hubo cambios
    override fun onResume() {
        super.onResume()
        cargarDatosDelPerfil(usuarioLogueado)
        // Opcional: Si editar perfil afectara a las estadísticas, descomenta la siguiente línea:
        // cargarEstadisticas(usuarioLogueado)
    }

    // ==========================================
    //      LÓGICA DE DATOS DEL PERFIL
    // ==========================================

    private fun cargarDatosDelPerfil(usernameTarget: String) {
        // Leemos JSON (priorizando cambios guardados en memoria interna)
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
        // CORREGIDO: Ahora usamos la función inteligente para proyectos también
        val jsonString = leerJSONProyectosPriorizandoInterno()

        if (jsonString.isNotEmpty()) {
            try {
                val jsonArray = JSONArray(jsonString)
                var contadorProyectos = 0
                var contadorTareas = 0

                // Mapa para el Gráfico 1 (Estado de mis tareas)
                val mapaEstadosTareas = HashMap<String, Int>()

                // Recorremos Proyectos
                for (i in 0 until jsonArray.length()) {
                    val proyecto = jsonArray.getJSONObject(i)

                    // A. Contar Proyectos Asignados al usuario
                    if (proyecto.has("UsuariosAsignados")) {
                        val usuariosProy = proyecto.getJSONArray("UsuariosAsignados")
                        for (j in 0 until usuariosProy.length()) {
                            if (usuariosProy.getJSONObject(j).getString("nombreUsuario") == usernameTarget) {
                                contadorProyectos++
                                break
                            }
                        }
                    }

                    // B. Contar Tareas y Estados
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

                // 1. Actualizar Textos
                findViewById<TextView>(R.id.tvProyectosCount).text = contadorProyectos.toString()
                findViewById<TextView>(R.id.tvTareasCount).text = contadorTareas.toString()

                // 2. Pintar Gráfico Izquierdo (Mis Tareas)
                if (contadorTareas > 0) {
                    configurarGraficoTareas(mapaEstadosTareas)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- GRÁFICO 1: MIS TAREAS (IZQUIERDA) ---
    private fun configurarGraficoTareas(mapaEstados: HashMap<String, Int>) {
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val entries = ArrayList<PieEntry>()
        for ((estado, cantidad) in mapaEstados) {
            entries.add(PieEntry(cantidad.toFloat(), estado))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawValues(false) // Quitar números dentro del gráfico

        // Colores personalizados
        val colores = ArrayList<Int>()
        colores.add(Color.parseColor("#66BB6A")) // Turquesa
        colores.add(Color.parseColor("#FFCA28")) // Amarillo
        colores.add(Color.parseColor("#EF5350")) // Rojo
        colores.add(Color.parseColor("#166269")) // Verde Oscuro
        colores.add(Color.parseColor("#AB47BC")) // Violeta
        dataSet.colors = colores

        val data = PieData(dataSet)
        pieChart.data = data

        // Estilo visual del gráfico
        pieChart.description.isEnabled = false
        pieChart.centerText = "Mis Tareas"
        pieChart.setCenterTextSize(14f)
        pieChart.setDrawEntryLabels(false)

        // Leyenda
        val legend = pieChart.legend
        legend.isEnabled = true
        legend.textColor = Color.WHITE
        legend.textSize = 12f
        legend.formSize = 12f
        legend.isWordWrapEnabled = true

        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    // ==========================================
    //      UTILIDADES DE ARCHIVOS
    // ==========================================

    // Lee el archivo JSON original de la carpeta raw (Backup)
    private fun leerArchivoRaw(resourceId: Int): String? {
        return try {
            val inputStream = resources.openRawResource(resourceId)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }

    // 1. LECTURA INTELIGENTE DE USUARIOS
    private fun leerJSONUsuariosPriorizandoInterno(): String {
        val nombreArchivoEditado = "usuarios_data.json"
        try {
            // Intento 1: Leer memoria interna
            val fileInputStream = openFileInput(nombreArchivoEditado)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            var text: String?
            while (bufferedReader.readLine().also { text = it } != null) {
                stringBuilder.append(text)
            }
            return stringBuilder.toString()
        } catch (e: Exception) {
            // Intento 2: Si falla, leer raw
            return leerArchivoRaw(R.raw.usuarios) ?: "[]"
        }
    }

    // 2. LECTURA INTELIGENTE DE PROYECTOS (NUEVA FUNCIÓN)
    private fun leerJSONProyectosPriorizandoInterno(): String {
        val nombreArchivoEditado = "proyectos_data.json"
        try {
            // Intento 1: Leer memoria interna
            val fileInputStream = openFileInput(nombreArchivoEditado)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            var text: String?
            while (bufferedReader.readLine().also { text = it } != null) {
                stringBuilder.append(text)
            }
            return stringBuilder.toString()
        } catch (e: Exception) {
            // Intento 2: Si falla, leer raw original
            return leerArchivoRaw(R.raw.proyectos) ?: "[]"
        }
    }
}