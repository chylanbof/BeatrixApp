package com.example.beatrixapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class ProyectosActivity : AppCompatActivity() {

    // 1. CAMBIO: Variable de sesión dinámica (ya no es fija "mgomez")
    private var usuarioLogueado: String? = null

    data class ProyectoUI(val nombre: String, val infoExtra: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proyectos)

        // 2. RECUPERAR SESIÓN REAL
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        usuarioLogueado = prefs.getString("loggedUser", null)

        // 3. SEGURIDAD: Si no hay sesión, cerramos la actividad
        if (usuarioLogueado == null) {
            finish()
            return
        }

        // 4. LÓGICA DE INTERFAZ
        val btnPerfil: ImageView = findViewById(R.id.btn_perfil)
        btnPerfil.setOnClickListener {
            startActivity(Intent(this, UsuarioActivity::class.java))
        }

        // Cargamos los proyectos usando el usuario de la sesión
        val listaProyectos = obtenerProyectosDesdeJSON()
        configurarRecyclerView(listaProyectos)

        // CONFIGURAR MENÚ INFERIOR
        setupNavigationButtons()
    }

    private fun configurarRecyclerView(proyectos: List<ProyectoUI>) {
        val recycler = findViewById<RecyclerView>(R.id.recyclerProyectos)
        recycler.layoutManager = LinearLayoutManager(this)
        val adapter = ProyectosAdapter(proyectos)
        recycler.adapter = adapter
    }

    private fun obtenerProyectosDesdeJSON(): List<ProyectoUI> {
        val proyectosEncontrados = mutableListOf<ProyectoUI>()

        // Usamos una variable local para evitar problemas de nulabilidad dentro del bucle
        val target = usuarioLogueado ?: return proyectosEncontrados

        try {
            val inputStream = resources.openRawResource(R.raw.proyectos)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val proyectoObj = jsonArray.getJSONObject(i)
                val usuariosAsignadosArray = proyectoObj.getJSONArray("UsuariosAsignados")
                var usuarioEncontrado = false

                // BUSCAR AL USUARIO LOGUEADO EN EL PROYECTO
                for (j in 0 until usuariosAsignadosArray.length()) {
                    val usuarioObj = usuariosAsignadosArray.getJSONObject(j)
                    val nombreUsuario = usuarioObj.optString("nombreUsuario", "")

                    if (nombreUsuario == target) {
                        usuarioEncontrado = true
                        break
                    }
                }

                if (usuarioEncontrado) {
                    val nombreProyecto = proyectoObj.getString("NombreProyecto")
                    val tareasArray = proyectoObj.getJSONArray("Tareas")
                    val cantidadTareas = tareasArray.length()

                    val info = "$cantidadTareas Tareas activas"
                    proyectosEncontrados.add(ProyectoUI(nombreProyecto, info))
                }
            }

        } catch (e: Exception) {
            Log.e("ProyectosActivity", "Error al leer JSON: ${e.message}")
        }

        return proyectosEncontrados
    }

    // ==========================================
    //      NAVEGACIÓN (Sincronizada)
    // ==========================================

    private fun setupNavigationButtons() {
        val includeLayout = findViewById<View>(R.id.boton_bottom)

        includeLayout.findViewById<ImageView>(R.id.btn_home).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        includeLayout.findViewById<ImageView>(R.id.btn_proyecto).setOnClickListener {
            // Ya estamos aquí
        }
        includeLayout.findViewById<ImageView>(R.id.btn_perfil).setOnClickListener {
            startActivity(Intent(this, UsuarioActivity::class.java))
        }
        includeLayout.findViewById<ImageView>(R.id.btn_calendario).setOnClickListener {
            startActivity(Intent(this, CalendarioActivity::class.java))
        }
    }

    // ==========================================
    //      ADAPTADOR INTERNO
    // ==========================================

    inner class ProyectosAdapter(private val lista: List<ProyectoUI>) :
        RecyclerView.Adapter<ProyectosAdapter.ProyectoViewHolder>() {

        inner class ProyectoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNombre: TextView = itemView.findViewById(R.id.tvNombreProyecto)
            val tvInfo: TextView = itemView.findViewById(R.id.tvFechaInfo)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProyectoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_proyectos, parent, false)
            return ProyectoViewHolder(view)
        }

        override fun onBindViewHolder(holder: ProyectoViewHolder, position: Int) {
            val proyecto = lista[position]
            holder.tvNombre.text = proyecto.nombre
            holder.tvInfo.text = proyecto.infoExtra

            holder.itemView.setOnClickListener {
                val intent = Intent(this@ProyectosActivity, Proyectos2Activity::class.java)
                intent.putExtra("nombreProyecto", proyecto.nombre)
                intent.putExtra("infoProyecto", proyecto.infoExtra)
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int = lista.size
    }
}