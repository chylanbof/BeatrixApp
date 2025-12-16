package com.example.beatrixapp

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.beatrixapp.model.Tarea
import android.view.ViewGroup
import android.view.LayoutInflater
import android.graphics.Color
import androidx.recyclerview.widget.LinearLayoutManager


class TareaAdapter(private val tareas: List<Tarea>) : RecyclerView.Adapter<TareaAdapter.TareaViewHolder>() {

    class TareaViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreTarea)
        val tvDescripcion = view.findViewById<TextView>(R.id.tvDescripcionTarea)
        val tvEstado = view.findViewById<TextView>(R.id.tvEstadoTarea)
        val tvFechas = view.findViewById<TextView>(R.id.tvFechasTarea)
        val tvUsuarios = view.findViewById<TextView>(R.id.tvUsuariosTarea)

        val recyclerSubTareas: RecyclerView = view.findViewById(R.id.recyclerSubTareas)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareas[position]
        holder.tvNombre.text = tarea.nombreTarea
        holder.tvDescripcion.text = tarea.descripcion
        holder.tvEstado.text = tarea.estado

        holder.tvEstado.setBackgroundColor(when (tarea.estado) {
                                               "Pendiente" -> Color.YELLOW
                                               "En Proceso" -> Color.rgb(81, 190, 207)
                                               "En Espera" -> Color.rgb(39, 238, 245)
                                               "Revisión" -> Color.MAGENTA
                                               "Completado" -> Color.GREEN
                                               "Cancelada" -> Color.RED
                                               else -> Color.LTGRAY
                                           })
        holder.tvFechas.text = "Inicio: ${formatearFecha(tarea.fechaInicio)} / Entrega: ${formatearFecha(tarea.fechaEntrega)}"
        holder.tvUsuarios.text = "Usuarios: ${tarea.usuariosAsignados.joinToString(", ") { it.nombreUsuario.toString() }}"

        val subTareas = tarea.subtarea.orEmpty()
        holder.recyclerSubTareas.layoutManager = LinearLayoutManager(holder.view.context)
        holder.recyclerSubTareas.adapter = SubTareaAdapter(subTareas)

    }

    private fun formatearFecha(fecha: String): String {
        return fecha.substring(0, 10) // solo yyyy-MM-dd
    }

    override fun getItemCount(): Int = tareas.size
}