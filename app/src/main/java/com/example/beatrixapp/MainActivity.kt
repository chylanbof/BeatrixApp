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
import com.example.beatrixapp.model.Usuario
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.graphics.Typeface

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

        copiarProyectosARutaInterna(this)

        listaDeProyectos = leerProyectosDesdeArchivo(this).toMutableList()

        listaDeProyectos.sortBy { formatoFecha.parse(it.fechaEntrega)!! }

        for (proyecto in listaDeProyectos) {
            agregarProyecto(proyecto, contenedorProyectos, this)
        }
    }

    private fun copiarProyectosARutaInterna(context: Context) {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            context.resources.openRawResource(R.raw.proyectos).use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
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
            val nombreProyecto = proyectoObj.getString("nombreProyecto")
            val descripcionProyecto = proyectoObj.getString("descripcionProyecto")
            val fechaInicio = proyectoObj.getString("fechaInicio")
            val fechaEntrega = proyectoObj.getString("fechaEntrega")
            val estadoProyecto = proyectoObj.optString("estado", "Pendiente")

            val usuariosProyectoList = mutableListOf<Usuario>()
            val usuariosArrayProyecto = proyectoObj.optJSONArray("usuariosAsignados") ?: JSONArray()
            for (p in 0 until usuariosArrayProyecto.length()) {
                val usuarioObj = usuariosArrayProyecto.getJSONObject(p)
                usuariosProyectoList.add(
                    Usuario(
                        nombreApellidos = usuarioObj.optString("nombreApellidos", null),
                        nombreUsuario = usuarioObj.optString("nombreUsuario", ""),
                        contrasena = usuarioObj.optString("contrasena", null),
                        email = usuarioObj.optString("email", null),
                        telefono = usuarioObj.optString("telefono", null),
                        rol = usuarioObj.optString("rol", null)
                    )
                )
            }

            proyectos.add(
                Proyecto(
                    nombreProyecto = nombreProyecto,
                    descripcionProyecto = descripcionProyecto,
                    tareas = emptyList(),
                    fechaInicio = fechaInicio,
                    fechaEntrega = fechaEntrega,
                    usuariosAsignados = usuariosProyectoList,
                    estado = estadoProyecto
                )
            )
        }

        return proyectos
    }

    private fun guardarProyectos(context: Context) {
        val file = File(context.filesDir, fileName)
        val jsonArray = JSONArray()
        for (p in listaDeProyectos) {
            val obj = JSONObject().apply {
                put("nombreProyecto", p.nombreProyecto)
                put("descripcionProyecto", p.descripcionProyecto)
                put("fechaInicio", p.fechaInicio)
                put("fechaEntrega", p.fechaEntrega)
                put("estado", p.estado)

                val usuariosArray = JSONArray()
                for (u in p.usuariosAsignados) {
                    val uObj = JSONObject().apply {
                        put("nombreApellidos", u.nombreApellidos)
                        put("nombreUsuario", u.nombreUsuario)
                        put("contrasena", u.contrasena)
                        put("email", u.email)
                        put("telefono", u.telefono)
                        put("rol", u.rol)
                    }
                    usuariosArray.put(uObj)
                }
                put("usuariosAsignados", usuariosArray)
            }
            jsonArray.put(obj)
        }
        file.writeText(jsonArray.toString())
    }

    private fun agregarProyecto(proyecto: Proyecto, contenedor: LinearLayout, context: Context) {
        // 加载布局
        val proyectoView = layoutInflater.inflate(R.layout.item_contenido_proyecto, contenedor, false)
        val txtTiempo = proyectoView.findViewById<TextView>(R.id.txtTiempo)
        val txtNombreProyecto = proyectoView.findViewById<TextView>(R.id.txtNombreProyecto)
        val spinnerEstado = proyectoView.findViewById<Spinner>(R.id.spinnerEstado)

        // coger los primeros 16 caracteres
        val inicioValor = proyecto.fechaInicio.take(16)
        val entregaValor = proyecto.fechaEntrega.take(16)
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
        txtNombreProyecto.text = proyecto.nombreProyecto

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
                guardarProyectos(context) // guardar los modificaciones
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // abrir el activity de proyecto, mostrar las tareas
        proyectoView.setOnClickListener {
            val intent = Intent(context, ProyectosActivity::class.java)
            intent.putExtra("PROYECTO_NOMBRE", proyecto.nombreProyecto)
            intent.putExtra("PROYECTO_DESCRIPCION", proyecto.descripcionProyecto)
            intent.putExtra("PROYECTO_FECHAINICIO", proyecto.fechaInicio)
            intent.putExtra("PROYECTO_FECHAENTREGA", proyecto.fechaEntrega)
            intent.putExtra("PROYECTO_ESTADO", proyecto.estado)
           startActivity(intent)

       }

      contenedor.addView(proyectoView)
   }

}
