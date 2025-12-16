package com.example.beatrixapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.io.File
import java.io.FileOutputStream


class ResumenActivity : BaseActivity() {

    private val ARCHIVO_JSON = "proyectos.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.resumenactivity)

        val botonBottom: View = findViewById(R.id.boton_bottom)

        val btnHome: ImageView = botonBottom.findViewById(R.id.btn_home)
        val btnProyecto: ImageView = botonBottom.findViewById(R.id.btn_proyecto)
        val btnCalendario: ImageView = botonBottom.findViewById(R.id.btn_calendario)
        val btnPerfil: ImageView = botonBottom.findViewById(R.id.btn_perfil)

        btnHome.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
        btnProyecto.setOnClickListener { startActivity(Intent(this, ProyectosActivity::class.java)) }
        btnCalendario.setOnClickListener { startActivity(Intent(this, CalendarioActivity::class.java)) }
        btnPerfil.setOnClickListener { startActivity(Intent(this, UsuarioActivity::class.java)) }

        findViewById<Button>(R.id.btnDescargarPdf).setOnClickListener {
            generarPdfResumen()
        }

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

        // 📝 Texto resumen usando strings.xml
        val resumenTexto = buildString {
            append(getString(R.string.resumen_proyecto, proyecto.optString("NombreProyecto")))
            append("\n\n")
            append(getString(R.string.tareas_completadas, completadas))
            append("\n")
            append(getString(R.string.tareas_en_progreso, enProgreso))
            append("\n")
            append(getString(R.string.tareas_canceladas, canceladas))
            append("\n\n")
            append(getString(R.string.subtareas_completadas, subtareasCompletadas, totalSubtareas))
        }

        // Mostrar en TextView
        findViewById<TextView>(R.id.txtResumen).text = resumenTexto

        // 📊 Gráfico simple
        pintarBarraProgreso(subtareasCompletadas, totalSubtareas)
        pintarGraficaEstados(completadas, enProgreso, canceladas)
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

        if (completadas > 0) entries.add(PieEntry(completadas.toFloat(), getString(R.string.grafica_completadas)))
        if (enProgreso > 0) entries.add(PieEntry(enProgreso.toFloat(), getString(R.string.grafica_en_progreso)))
        if (canceladas > 0) entries.add(PieEntry(canceladas.toFloat(), getString(R.string.grafica_canceladas)))

        val dataSet = PieDataSet(entries, getString(R.string.grafica_titulo))
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
        pieChart.centerText = getString(R.string.grafica_centro)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.animateY(1000)
    }

    private fun generarPdfResumen() {

        val txtResumen = findViewById<TextView>(R.id.txtResumen)
        val pieChart = findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.pieChart)

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        paint.textSize = 14f
        paint.isAntiAlias = true

        // 📝 TEXTO
        var y = 40f
        txtResumen.text.toString().lines().forEach {
            canvas.drawText(it, 40f, y, paint)
            y += 24f
        }

        // 📊 GRÁFICO
        pieChart.invalidate()
        val bitmapOriginal = obtenerBitmapDeVista(pieChart)


        val anchoPdf = 400
        val altoPdf = 400

        val bitmapEscalado = Bitmap.createScaledBitmap(
            bitmapOriginal,
            anchoPdf,
            altoPdf,
            true
                                                      )

        val left = (pageInfo.pageWidth - anchoPdf) / 2f  // centrado
        val top = y + 20f

        canvas.drawBitmap(bitmapEscalado, left, top, null)

        pdfDocument.finishPage(page)

        // 💾 GUARDAR
        val fileName = "Resumen_${System.currentTimeMillis()}.pdf"
        val file = File(getExternalFilesDir(null), fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            mostrarPdf(file)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error_pdf), Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }


    private fun mostrarPdf(file: File) {

        val uri = androidx.core.content.FileProvider.getUriForFile(
            this,
            "$packageName.provider",
            file
                                                                  )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        startActivity(Intent.createChooser(intent, getString(R.string.abrir_pdf_con)))
    }

    private fun obtenerBitmapDeVista(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
                                        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }






}

