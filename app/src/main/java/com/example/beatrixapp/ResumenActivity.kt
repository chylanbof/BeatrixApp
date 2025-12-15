package com.example.beatrixapp

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate


class ResumenActivity : AppCompatActivity() {

    private val ARCHIVO_JSON = "proyectos.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.resumenactivity)

        val nombreProyecto = intent.getStringExtra("nombreProyecto") ?: return

        val jsonArray = leerJson()
        val proyecto = buscarProyecto(jsonArray, nombreProyecto) ?: return

        generarResumenProyecto(proyecto)
    }

    private fun generarResumenProyecto(proyecto: JSONObject) {

        val tareas = proyecto.optJSONArray("Tareas") ?: JSONArray()

        var completadas = 0
        var enProgreso = 0
        var canceladas = 0

        var totalSubtareas = 0
        var subtareasCompletadas = 0

        for (i in 0 until tareas.length()) {
            val tarea = tareas.getJSONObject(i)
            val estado = tarea.optString("estado", "").lowercase()

            when (estado) {
                "completado" -> completadas++
                "en progreso" -> enProgreso++
                "cancelada" -> canceladas++
            }

            val subtareas = tarea.optJSONArray("SubTareas") ?: JSONArray()
            totalSubtareas += subtareas.length()

            for (j in 0 until subtareas.length()) {
                val sub = subtareas.getJSONObject(j)
                if (sub.optString("EstadoSubTarea", "").equals("completado", true)) {
                    subtareasCompletadas++
                }
            }
        }

        // 📝 Texto resumen
        val resumenTexto = """
            Proyecto: ${proyecto.optString("NombreProyecto")}

            Tareas:
            ✔ Completadas: $completadas
            ⏳ En progreso: $enProgreso
            ⛔ Canceladas: $canceladas

            Subtareas completadas:
            $subtareasCompletadas / $totalSubtareas
        """.trimIndent()

        findViewById<TextView>(R.id.txtResumen).text = resumenTexto

        // 📊 Gráfico simple
        pintarBarraProgreso(
            subtareasCompletadas, totalSubtareas
                           )

        pintarGraficaEstados(
            completadas,
            enProgreso,
            canceladas
                            )
    }

    private fun pintarBarraProgreso(completadas: Int, total: Int) {

        val barra = findViewById<TextView>(R.id.txtBarra)

        if (total == 0) {
            barra.text = "[----------]"
            return
        }

        val porcentaje = completadas * 10 / total
        val llena = "█".repeat(porcentaje)
        val vacia = "░".repeat(10 - porcentaje)

        barra.text = "[$llena$vacia]"
    }

    private fun leerJson(): JSONArray {
        val file = java.io.File(filesDir, ARCHIVO_JSON)

        if (!file.exists()) {
            val input = resources.openRawResource(R.raw.proyectos)
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val jsonString = file.readText()
        return JSONArray(jsonString)
    }
    private fun buscarProyecto(
        jsonArray: JSONArray,
        nombreProyecto: String
                              ): JSONObject? {

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.optString("NombreProyecto") == nombreProyecto) {
                return obj
            }
        }
        return null
    }

    private fun pintarGraficaEstados(
        completadas: Int,
        enProgreso: Int,
        canceladas: Int
                                    ) {

        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val entries = ArrayList<PieEntry>()

        if (completadas > 0) entries.add(PieEntry(completadas.toFloat(), "Completadas"))
        if (enProgreso > 0) entries.add(PieEntry(enProgreso.toFloat(), "En progreso"))
        if (canceladas > 0) entries.add(PieEntry(canceladas.toFloat(), "Canceladas"))

        val dataSet = PieDataSet(entries, "Estado de las tareas")
        dataSet.colors = listOf(
            Color.GREEN,
            Color.CYAN,
            Color.RED
                               )

        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE

        val data = PieData(dataSet)

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.centerText = "Tareas"
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.animateY(1000)
    }



}

