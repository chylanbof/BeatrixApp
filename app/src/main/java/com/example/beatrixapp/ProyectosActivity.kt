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

        // 1. Obtenemos la lista completa del JSON
        val listaProyectos = obtenerProyectosDesdeJSON()

        // 2. Configuramos el RecyclerView
        configurarRecyclerView(listaProyectos)
    }

    private fun configurarRecyclerView(proyectos: List<ProyectoUI>) {
        val recycler = findViewById<RecyclerView>(R.id.recyclerProyectos)

        // Esto le dice que se comporte como una lista vertical
        recycler.layoutManager = LinearLayoutManager(this)

        // Creamos una instancia de TU adaptador interno y se la asignamos
        val adapter = ProyectosAdapter(proyectos)
        recycler.adapter = adapter
    }

    private fun obtenerProyectosDesdeJSON(): List<ProyectoUI> {
        val proyectosEncontrados = mutableListOf<ProyectoUI>()

        try {
            val inputStream = resources.openRawResource(R.raw.proyectos)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }

            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val proyectoObj = jsonArray.getJSONObject(i)

                val nombreProyecto = proyectoObj.getString("NombreProyecto")
                val tareasArray = proyectoObj.getJSONArray("Tareas")
                val cantidadTareas = tareasArray.length()

                val info = "$cantidadTareas Tareas activas"
                proyectosEncontrados.add(ProyectoUI(nombreProyecto, info))
            }

        } catch (e: Exception) {
            Log.e("ProyectosActivity", "Error al leer JSON: ${e.message}")
        }

        return proyectosEncontrados
    }

    // ==========================================
    //      AQUÍ EMPIEZA EL ADAPTADOR INTERNO
    // ==========================================

    // Usamos 'inner class' para que pueda acceder al contexto de la Activity si fuera necesario
    inner class ProyectosAdapter(private val lista: List<ProyectoUI>) :
        RecyclerView.Adapter<ProyectosAdapter.ProyectoViewHolder>() {

        // --- VIEWHOLDER (Maneja las vistas de CADA ítem) ---
        inner class ProyectoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNombre: TextView = itemView.findViewById(R.id.tvNombreProyecto)
            val tvInfo: TextView = itemView.findViewById(R.id.tvFechaInfo)
        }

        // 1. Crea el diseño visual (infla el XML item_proyecto)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProyectoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_proyecto, parent, false)
            return ProyectoViewHolder(view)
        }

        // 2. Pone los datos en el diseño
        override fun onBindViewHolder(holder: ProyectoViewHolder, position: Int) {
            val proyecto = lista[position]

            holder.tvNombre.text = proyecto.nombre
            holder.tvInfo.text = proyecto.infoExtra

            // Aquí podrías añadir un onClickListener en el futuro
            /*
            holder.itemView.setOnClickListener {
                // Código al pulsar la tarjeta
            }
            */
        }

        // 3. Dice cuántos elementos hay
        override fun getItemCount(): Int {
            return lista.size
        }
    }
}