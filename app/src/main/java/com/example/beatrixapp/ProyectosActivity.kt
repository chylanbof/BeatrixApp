package com.example.beatrixapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class ProyectosActivity : AppCompatActivity() {

    // Modelo de datos (Data Class)
    data class ProyectoUI(val nombre: String, val infoExtra: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proyectos)

        val usuarioLogueado = "mgomez"
        val listaProyectos = obtenerProyectosDesdeJSON(usuarioLogueado)
        configurarRecyclerView(listaProyectos)
    }

    private fun configurarRecyclerView(proyectos: List<ProyectoUI>) {
        val recycler = findViewById<RecyclerView>(R.id.recyclerProyectos)
        recycler.layoutManager = LinearLayoutManager(this)
        val adapter = ProyectosAdapter(proyectos)
        recycler.adapter = adapter
    }

    // AHORA RECIBE EL NOMBRE DE USUARIO COMO PARÁMETRO
    private fun obtenerProyectosDesdeJSON(usuarioBuscado: String): List<ProyectoUI> {
        val proyectosEncontrados = mutableListOf<ProyectoUI>()

        try {
            val inputStream = resources.openRawResource(R.raw.proyectos)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }

            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val proyectoObj = jsonArray.getJSONObject(i)

                // 1. Verificamos si el usuario está en el proyecto
                var usuarioEstaEnProyecto = false
                if (proyectoObj.has("UsuariosAsignados")) {
                    val usuariosAsignadosArray = proyectoObj.getJSONArray("UsuariosAsignados")
                    for (j in 0 until usuariosAsignadosArray.length()) {
                        if (usuariosAsignadosArray.getJSONObject(j).getString("nombreUsuario") == usuarioBuscado) {
                            usuarioEstaEnProyecto = true
                            break
                        }
                    }
                }

                // 2. Si está, añadimos el proyecto con la FECHA
                if (usuarioEstaEnProyecto) {
                    val nombreProyecto = proyectoObj.getString("NombreProyecto")

                    // --- CAMBIO AQUÍ: LEEMOS LA FECHA DE ENTREGA ---
                    val fechaRaw = proyectoObj.optString("fechaEntrega", "Sin fecha")

                    // Limpiamos la fecha para quitar la hora (ej: "2025-12-04T14:..." -> "2025-12-04")
                    val fechaLimpia = if (fechaRaw.contains("T")) {
                        fechaRaw.split("T")[0]
                    } else {
                        fechaRaw
                    }

                    val info = "Entrega: $fechaLimpia"

                    proyectosEncontrados.add(ProyectoUI(nombreProyecto, info))
                }
            }

        } catch (e: Exception) {
            Log.e("ProyectosActivity", "Error al leer JSON: ${e.message}")
        }

        return proyectosEncontrados
    }

    // ==========================================
    //      ADAPTADOR INTERNO (SIN CAMBIOS)
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
        }

        override fun getItemCount(): Int {
            return lista.size
        }
    }
}