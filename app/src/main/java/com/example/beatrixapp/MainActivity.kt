package com.example.beatrixapp

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.beatrixapp.model.Proyecto
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var contenedorProyectos: LinearLayout
    private lateinit var listaDeProyectos: MutableList<Proyecto>

    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
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

        //Usar botones para enviar a otros activitys
        val includeLayout = findViewById<View>(R.id.boton_bottom)

        val botonHome = includeLayout.findViewById<ImageView>(R.id.btn_home)
        botonHome.setOnClickListener {
            val intentHome = Intent(this, MainActivity::class.java)
            startActivity(intentHome)
        }

        val botonProyectos = includeLayout.findViewById<ImageView>(R.id.btn_proyecto)
        botonProyectos.setOnClickListener {
            val intentProyecto = Intent(this, ProyectosActivity:: class.java)
            startActivity(intentProyecto)
        }

        val botonCalendario = includeLayout.findViewById<ImageView>(R.id.btn_calendario)
        botonCalendario.setOnClickListener {
            val intentCalendario = Intent(this, CalendarioActivity:: class.java)
            startActivity(intentCalendario)
        }

        val botonUsuarios = includeLayout.findViewById<ImageView>(R.id.btn_perfil)
        botonUsuarios.setOnClickListener {
            val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
            val loggedUser = prefs.getString("loggedUser", null) ?: return@setOnClickListener

            val intentUsuario = Intent(this, UsuarioActivity::class.java)
            intentUsuario.putExtra("USERNAME", loggedUser) // Enviar el nombre de usuario que ha iniciado sesión
            startActivity(intentUsuario)
        }




        val username = intent.getStringExtra("USERNAME") ?: "Usuario"
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Bienvenido, $username"

        contenedorProyectos = findViewById(R.id.contenedorProyectos)

        // Copiar archivo JSON si no existe
        copiarProyectosARutaInterna(this)

        listaDeProyectos = leerProyectosDesdeArchivo(this).toMutableList()

        // Ordenar proyectos por fecha de entrega
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
        // Sólo mostrar los 3 proyectos más recientes.
        val proyectosMasCercanos = listaDeProyectos.take(3)

        for (proyecto in proyectosMasCercanos) {
            agregarProyecto(proyecto, contenedorProyectos, this)
        }

    }

    private fun copiarProyectosARutaInterna(context: Context) {

        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isDataCopied = prefs.getBoolean("is_data_copied", false)

        if (!isDataCopied) {
            val file = File(context.filesDir, fileName)
            try {
                context.resources.openRawResource(R.raw.proyectos).use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                prefs.edit().putBoolean("is_data_copied", true).apply()
                Log.i("COPIAR", "SUCCESS: Initial data copied and flag set.")
            } catch (e: Exception) {
                Log.e("COPIAR", "Error al copiar el archivo R.raw.proyectos", e)
            }
        } else {
            Log.i("COPIAR", "SKIP: Data already copied, skipping initialization.")
        }
    }

    private fun leerProyectosDesdeArchivo(context: Context): List<Proyecto> {
        val proyectos = mutableListOf<Proyecto>()
        val file = File(context.filesDir, fileName)

        if (!file.exists()) {
            Log.w("CARGAR", "WARNING: File $fileName not found. Returning empty list.")
            return proyectos
        }

        try {
            val jsonString = file.readText()
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val proyectoObj = jsonArray.getJSONObject(i)

                val nombreProyecto = proyectoObj.optString("NombreProyecto")
                val descripcionProyecto = proyectoObj.optString("DescripcionProyecto")
                val fechaInicio = proyectoObj.optString("fechaInicio")
                val fechaEntrega = proyectoObj.optString("fechaEntrega")

                proyectos.add(
                    Proyecto(
                        nombreProyecto = if (nombreProyecto.isNullOrEmpty()) null else nombreProyecto,
                        descripcionProyecto = descripcionProyecto,
                        tareas = emptyList(),
                        fechaInicio = fechaInicio,
                        fechaEntrega = fechaEntrega,
                        usuariosAsignados = emptyList()
                    )
                )
            }

            Log.i("CARGAR", "LOADED: Successfully loaded ${proyectos.size} projects.")

        } catch (e: Exception) {
            Log.e("CARGAR", "Error al leer proyectos desde archivo", e)
        }

        return proyectos
    }

    private fun agregarProyecto(proyecto: Proyecto, contenedor: LinearLayout, context: Context) {
        // convertir a objecto view
        val proyectoView = layoutInflater.inflate(R.layout.item_contenido_proyecto, contenedor, false)

        val txtNombreProyecto = proyectoView.findViewById<TextView>(R.id.txtNombreProyecto)
        val txtDescripcionProyecto = proyectoView.findViewById<TextView>(R.id.txtDescripcionProyecto)
        val txtTiempo = proyectoView.findViewById<TextView>(R.id.txtTiempo)

        txtNombreProyecto.text = getString(
            R.string.nombre_proyecto_text,
            proyecto.nombreProyecto ?: getString(R.string.sin_nombre_proyecto)
        )

        txtDescripcionProyecto.text = getString(
            R.string.descripcion_proyecto_text,
            proyecto.descripcionProyecto ?: getString(R.string.sin_descripcion_proyecto)
        )

        val inicioValor = proyecto.fechaInicio?.take(16) ?: "N/A"
        val entregaValor = proyecto.fechaEntrega?.take(16) ?: "N/A"

        val textoCompleto = getString(R.string.tiempo_proyecto, inicioValor, entregaValor)
        val spannable = SpannableString(textoCompleto)

        val inicioLabelLength = "Inicio: ".length
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            inicioLabelLength,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val entregaStart = inicioLabelLength + inicioValor.length
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            entregaStart,
            entregaStart + "\nEntrega: ".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        txtTiempo.text = spannable

        proyectoView.setOnClickListener {
            val intent = Intent(context, ProyectosActivity::class.java)
            intent.putExtra("PROYECTO_NOMBRE", proyecto.nombreProyecto ?: "")
            intent.putExtra("PROYECTO_DESCRIPCION", proyecto.descripcionProyecto ?: "")
            intent.putExtra("PROYECTO_FECHAINICIO", proyecto.fechaInicio ?: "")
            intent.putExtra("PROYECTO_FECHAENTREGA", proyecto.fechaEntrega ?: "")
            startActivity(intent)
        }

        contenedor.addView(proyectoView)
    }

}
