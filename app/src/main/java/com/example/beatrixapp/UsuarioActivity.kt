package com.example.beatrixapp

import android.graphics.Paint
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import android.graphics.Color
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class UsuarioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios)

        val usuarioABuscar = "afernandez" // Puedes probar con "admin", "qq", "afernandez"

        // 1. Datos Personales
        buscarUsuarioEnRaw(usuarioABuscar)

        // 2. Estadísticas (Proyectos y Tareas)
        cargarEstadisticas(usuarioABuscar)
    }

    // ... (La función buscarUsuarioEnRaw y rellenarDatosPersonales se quedan igual que antes) ...
    private fun buscarUsuarioEnRaw(usernameTarget: String) {
        val jsonString = leerArchivoRaw(R.raw.usuarios)
        if (jsonString != null) {
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
                if (usuarioEncontrado != null) rellenarDatosPersonales(usuarioEncontrado)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun rellenarDatosPersonales(usuario: JSONObject) {
        findViewById<TextView>(R.id.tvNombreCompleto).text = usuario.getString("nombreApellidos")
        findViewById<TextView>(R.id.tvNombreUsuario).text = usuario.getString("nombreUsuario")

        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        tvEmail.text = usuario.getString("email")
        tvEmail.paintFlags = tvEmail.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        findViewById<TextView>(R.id.tvTelefono).text = usuario.getString("telefono")

        val pass = usuario.getString("contrasena")
        findViewById<TextView>(R.id.tvContrasena).text = if (pass.isEmpty()) "No definida" else pass
    }

    // --- NUEVA LÓGICA DE ESTADÍSTICAS (Proyectos + Tareas) ---
    // --- LÓGICA DE ESTADÍSTICAS Y GRÁFICO ---
    private fun cargarEstadisticas(usernameTarget: String) {
        val jsonString = leerArchivoRaw(R.raw.proyectos)

        if (jsonString != null) {
            try {
                val jsonArray = JSONArray(jsonString)
                var contadorProyectos = 0
                var contadorTareas = 0

                // Mapa para guardar los estados: Ej: "En Progreso" -> 2
                val mapaEstados = HashMap<String, Int>()

                // 1. Recorremos Proyectos
                for (i in 0 until jsonArray.length()) {
                    val proyecto = jsonArray.getJSONObject(i)

                    // Contar proyectos
                    if (proyecto.has("UsuariosAsignados")) {
                        val usuariosProy = proyecto.getJSONArray("UsuariosAsignados")
                        for (j in 0 until usuariosProy.length()) {
                            if (usuariosProy.getJSONObject(j).getString("nombreUsuario") == usernameTarget) {
                                contadorProyectos++
                                break
                            }
                        }
                    }

                    // 2. Recorremos Tareas y guardamos ESTADOS
                    if (proyecto.has("Tareas")) {
                        val tareasArray = proyecto.getJSONArray("Tareas")
                        for (k in 0 until tareasArray.length()) {
                            val tarea = tareasArray.getJSONObject(k)

                            // Verificamos si el usuario está en la tarea
                            if (tarea.has("usuariosAsignados")) {
                                val usuariosTarea = tarea.getJSONArray("usuariosAsignados")
                                var usuarioEncontradoEnTarea = false

                                for (l in 0 until usuariosTarea.length()) {
                                    if (usuariosTarea.getJSONObject(l).getString("nombreUsuario") == usernameTarget) {
                                        usuarioEncontradoEnTarea = true
                                        break
                                    }
                                }

                                // Si es tarea del usuario, guardamos su estado
                                if (usuarioEncontradoEnTarea) {
                                    contadorTareas++
                                    val estado = tarea.optString("estado", "Desconocido")
                                    // Sumamos 1 al contador de ese estado
                                    mapaEstados[estado] = mapaEstados.getOrDefault(estado, 0) + 1
                                }
                            }
                        }
                    }
                }

                // Actualizamos los textos
                findViewById<TextView>(R.id.tvProyectosCount).text = contadorProyectos.toString()
                findViewById<TextView>(R.id.tvTareasCount).text = contadorTareas.toString()

                // LLAMAMOS A LA FUNCIÓN PARA PINTAR EL GRÁFICO
                if (contadorTareas > 0) {
                    configurarGrafico(mapaEstados)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun configurarGrafico(mapaEstados: HashMap<String, Int>) {
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        // 1. Convertimos el Mapa a una lista de entradas para el gráfico
        val entries = ArrayList<PieEntry>()
        for ((estado, cantidad) in mapaEstados) {
            // El float es el valor, el String es la etiqueta
            entries.add(PieEntry(cantidad.toFloat(), estado))
        }

        // 2. Configuración de colores y apariencia
        val dataSet = PieDataSet(entries, "")

        // Colores bonitos (Turquesas y variados)
        val colores = ArrayList<Int>()
        colores.add(Color.parseColor("#66BB6A")) // Verde
        colores.add(Color.parseColor("#FFCA28")) // Amarillo ambar
        colores.add(Color.parseColor("#EF5350")) // Rojo suave
        colores.add(Color.parseColor("#66BB6A")) // Verde suave
        colores.add(Color.parseColor("#AB47BC")) // Violeta
        dataSet.colors = colores

        dataSet.valueTextSize = 16f
        dataSet.valueTextColor = Color.WHITE

        // 3. Crear los datos y asignarlos
        val data = PieData(dataSet)
        pieChart.data = data

        // 4. Estilos extra del gráfico
        pieChart.description.isEnabled = false // Quitar descripción pequeña
        pieChart.centerText = "Mis Tareas"
        pieChart.setCenterTextSize(18f)
        pieChart.setEntryLabelColor(Color.BLACK) // Color del texto de las etiquetas
        pieChart.setDrawEntryLabels(false) // Esto oculta los textos ("Pendiente", "En Pausa")

        // --- NUEVO: Configuración de la LEYENDA (La guía exterior) ---
        val legend = pieChart.legend
        legend.isEnabled = true // Asegurarnos de que está activada
        legend.textSize = 16f   // Aumentar el tamaño del texto de la leyenda (pruébalo, 14f o 16f)
        legend.formSize = 16f   // Aumentar el tamaño de los cuadrados de color de la leyenda
        legend.textColor = Color.WHITE // Opcional: Poner el texto en blanco para que se vea bien sobre el fondo

        // Opcional: Ajustar la posición de la leyenda si es necesario
        // legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        // legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        // legend.orientation = Legend.LegendOrientation.HORIZONTAL
        // legend.setDrawInside(false)

        pieChart.animateY(1000) // Animación de 1 segundo al aparecer

        // Refrescar
        pieChart.invalidate()
    }

    private fun leerArchivoRaw(resourceId: Int): String? {
        return try {
            val inputStream = resources.openRawResource(resourceId)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            inputStream.close()
            sb.toString()
        } catch (e: Exception) {
            null
        }
    }
}