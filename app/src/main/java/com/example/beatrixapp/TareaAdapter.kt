package com.example.beatrixapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.beatrixapp.model.Tarea

class TareaAdapter(private val tareas: List<Tarea>) :
    RecyclerView.Adapter<TareaAdapter.TareaViewHolder>() {

    class TareaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // IDs del layout item_tarea.xml
        val tvNombre: TextView = view.findViewById(R.id.tvNombreTarea)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionTarea)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoTarea)
        val tvFechas: TextView = view.findViewById(R.id.tvFechasTarea)
        val tvUsuarios: TextView = view.findViewById(R.id.tvUsuariosTarea)
        val recyclerSubTareas: RecyclerView = view.findViewById(R.id.recyclerSubTareas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareas[position]

        // 1. Configurar datos de la tarea
        holder.tvNombre.text = tarea.nombreTarea
        holder.tvDescripcion.text = tarea.descripcion
        // Muestra solo la fecha (los primeros 10 caracteres)
        holder.tvFechas.text = "Inicio: ${tarea.fechaInicio.substring(0, 10)} | Entrega: ${tarea.fechaEntrega.substring(0, 10)}"

        // 2. Configurar estado y color
        val estadoTarea = tarea.estado ?: "Desconocido" // Si tarea.estado es null, usamos "Desconocido"
        holder.tvEstado.text = estadoTarea
        holder.tvEstado.setBackgroundColor(when (estadoTarea) {
            "Pendiente" -> Color.YELLOW
            "En Proceso" -> Color.rgb(81, 190, 207)
            "En Espera" -> Color.rgb(39, 238, 245)
            "Revisión" -> Color.MAGENTA
            "Completado" -> Color.GREEN
            "Cancelada" -> Color.RED
            else -> Color.LTGRAY
        })

        // 3. Configurar usuarios asignados
        holder.tvUsuarios.text = "Asignados: ${tarea.usuariosAsignados.joinToString (", "){ it.nombreUsuario.toString() }}"

        // 4. Configurar el RecyclerView anidado para Subtareas
        if (tarea.subtarea.isNotEmpty()) {
            holder.recyclerSubTareas.visibility = View.VISIBLE
            holder.recyclerSubTareas.layoutManager = LinearLayoutManager(holder.itemView.context)
            // Usa tu clase SubTareaAdapter existente
            holder.recyclerSubTareas.adapter = SubTareaAdapter(tarea.subtarea)
        } else {
            holder.recyclerSubTareas.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = tareas.size
}