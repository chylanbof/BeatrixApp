package com.example.beatrixapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.beatrixapp.model.Proyecto
// 注意：只需要导入您在 MainActivity 中直接引用的模型类
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.util.Log // 用于调试

class MainActivity : AppCompatActivity() {

    private lateinit var contenedorProyectos: LinearLayout
    private lateinit var listaDeProyectos: MutableList<Proyecto>

    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    private val estados = arrayOf("Pendiente", "En Progreso", "Completada")
    private val fileName = "proyectos.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val username = intent.getStringExtra("USERNAME") ?: "Usuario"
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Bienvenido, $username"

        contenedorProyectos = findViewById(R.id.contenedorProyectos)

        // asegurar si existe archivo json
        copiarProyectosARutaInterna(this)

        listaDeProyectos = leerProyectosDesdeArchivo(this).toMutableList()

        try {
            listaDeProyectos.sortBy {
                val fullDateString = it.fechaEntrega
                val dateString = fullDateString?.take(19)

                if (dateString.isNullOrBlank()) {
                    Date(0)
                } else {
                    try {
                        formatoFecha.parse(dateString) ?: Date(0)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error de formato de fecha: $dateString", e)
                        Date(0)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error sorting projects", e)
        }

        for (proyecto in listaDeProyectos) {
            agregarProyecto(proyecto, contenedorProyectos, this)
        }
    }
    private fun copiarProyectosARutaInterna(context: Context) {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            try {
                context.resources.openRawResource(R.raw.proyectos).use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error al copiar el archivo R.raw.proyectos", e)
            }
        }
    }

    private fun leerProyectosDesdeArchivo(context: Context): List<Proyecto> {
        val proyectos = mutableListOf<Proyecto>()
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return proyectos

        val jsonString = file.readText()
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val proyectoObj = jsonArray.getJSONObject(i)

            val nombreProyecto = proyectoObj.optString("NombreProyecto")
            val descripcionProyecto = proyectoObj.optString("DescripcionProyecto")
            val fechaInicio = proyectoObj.optString("fechaInicio")
            val fechaEntrega = proyectoObj.optString("fechaEntrega")

            val estadoProyecto = proyectoObj.optString("estado", "Pendiente")

            proyectos.add(
                Proyecto(
                    nombreProyecto = if (nombreProyecto.isNullOrEmpty()) null else nombreProyecto,
                    descripcionProyecto = descripcionProyecto,
                    tareas = emptyList(),
                    fechaInicio = fechaInicio,
                    fechaEntrega = fechaEntrega,
                    usuariosAsignados = emptyList(),
                    estado = estadoProyecto
                )
            )
        }
        return proyectos
    }

    private fun guardarProyectos(context: Context) {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return

        try {
            val jsonString = file.readText()
            val jsonArray = JSONArray(jsonString)

            for (proyectoMemoria in listaDeProyectos) {
                for (i in 0 until jsonArray.length()) {
                    val proyectoJson = jsonArray.getJSONObject(i)

                    val nombreProyectoJson = proyectoJson.optString("NombreProyecto")
                    if (nombreProyectoJson == proyectoMemoria.nombreProyecto) {
                        proyectoJson.put("estado", proyectoMemoria.estado)
                        break
                    }
                }
            }

            file.writeText(jsonArray.toString(2))
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al guardar proyectos", e)
            Toast.makeText(context, "Error al guardar proyectos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun agregarProyecto(proyecto: Proyecto, contenedor: LinearLayout, context: Context) {
        val proyectoView = layoutInflater.inflate(R.layout.item_contenido_proyecto, contenedor, false)
        val txtTiempo = proyectoView.findViewById<TextView>(R.id.txtTiempo)
        val txtNombreProyecto = proyectoView.findViewById<TextView>(R.id.txtNombreProyecto)
        val spinnerEstado = proyectoView.findViewById<Spinner>(R.id.spinnerEstado)

        txtNombreProyecto.text = proyecto.nombreProyecto ?: "Proyecto Desconocido"

        val inicioValor = proyecto.fechaInicio?.take(16) ?: "N/A"
        val entregaValor = proyecto.fechaEntrega?.take(16) ?: "N/A"

        val inicioLabel = "Inicio: "
        val entregaLabel = "\nEntrega: "
        val textoCompleto = inicioLabel + inicioValor + entregaLabel + entregaValor
        val spannable = SpannableString(textoCompleto)

        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            inicioLabel.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val entregaStart = inicioLabel.length + inicioValor.length
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            entregaStart,
            entregaStart + entregaLabel.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        txtTiempo.text = spannable

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, estados)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstado.adapter = adapter
        spinnerEstado.setSelection(estados.indexOf(proyecto.estado))

        val initialView = spinnerEstado.selectedView as? TextView
        initialView?.setTextColor(
            when (proyecto.estado.lowercase()) {
                "pendiente" -> Color.YELLOW
                "en progreso" -> Color.CYAN
                "completada" -> Color.GREEN
                else -> Color.BLACK
            }
        )

        spinnerEstado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val nuevoEstado = estados[position]
                proyecto.estado = nuevoEstado
                (view as? TextView)?.setTextColor(
                    when (nuevoEstado.lowercase()) {
                        "pendiente" -> Color.YELLOW
                        "en progreso" -> Color.CYAN
                        "completada" -> Color.GREEN
                        else -> Color.BLACK
                    }
                )
                guardarProyectos(context)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        proyectoView.setOnClickListener {
            val intent = Intent(context, ProyectosActivity::class.java)

            intent.putExtra("PROYECTO_NOMBRE", proyecto.nombreProyecto ?: "")
            intent.putExtra("PROYECTO_DESCRIPCION", proyecto.descripcionProyecto ?: "")
            intent.putExtra("PROYECTO_FECHAINICIO", proyecto.fechaInicio ?: "")
            intent.putExtra("PROYECTO_FECHAENTREGA", proyecto.fechaEntrega ?: "")
            intent.putExtra("PROYECTO_ESTADO", proyecto.estado)
            startActivity(intent)
        }

        contenedor.addView(proyectoView)
    }

}