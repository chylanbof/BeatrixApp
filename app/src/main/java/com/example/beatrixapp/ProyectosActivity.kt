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

    // 1. El usuario que queremos filtrar
    private val targetUsername = "mgomez"

    data class ProyectoUI(val nombre: String, val infoExtra: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proyectos)

        val btnPerfil: ImageView = findViewById(R.id.btn_perfil)
        btnPerfil.setOnClickListener {
            val intent = Intent(this, UsuarioActivity::class.java)
            startActivity(intent)
        }

        // Cargamos los proyectos filtrados por el usuario asignado
        val listaProyectos = obtenerProyectosDesdeJSON()
        configurarRecyclerView(listaProyectos)
    }

    private fun configurarRecyclerView(proyectos: List<ProyectoUI>) {
        val recycler = findViewById<RecyclerView>(R.id.recyclerProyectos)
        recycler.layoutManager = LinearLayoutManager(this)
        val adapter = ProyectosAdapter(proyectos)
        recycler.adapter = adapter
    }

    private fun obtenerProyectosDesdeJSON(): List<ProyectoUI> {
        val proyectosEncontrados = mutableListOf<ProyectoUI>()

        try {
            val inputStream = resources.openRawResource(R.raw.proyectos)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            // Recorremos cada Proyecto
            for (i in 0 until jsonArray.length()) {
                val proyectoObj = jsonArray.getJSONObject(i)

                // 2. Obtenemos la lista de Usuarios Asignados a este proyecto
                val usuariosAsignadosArray = proyectoObj.getJSONArray("UsuariosAsignados")
                var usuarioEncontrado = false

                // 3. Buscamos dentro de la lista de usuarios si está "mgomez"
                for (j in 0 until usuariosAsignadosArray.length()) {
                    val usuarioObj = usuariosAsignadosArray.getJSONObject(j)
                    val nombreUsuario = usuarioObj.optString("nombreUsuario", "")

                    if (nombreUsuario == targetUsername) {
                        usuarioEncontrado = true
                        break // Si lo encontramos, dejamos de buscar en este proyecto
                    }
                }

                // 4. Si el usuario está asignado, lo añadimos a la lista de la interfaz
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