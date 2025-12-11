package com.example.beatrixapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.beatrixapp.model.Subtarea
import android.view.View


class SubTareaAdapter(private val subTareas: List<Subtarea>) :
    RecyclerView.Adapter<SubTareaAdapter.SubTareasViewHolder>(){

    class SubTareasViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreSubtarea)
        val tvDescripcion = view.findViewById<TextView>(R.id.tvDescripcionSubTarea)
        val tvEstado = view.findViewById<TextView>(R.id.tvEstadoSubtarea)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubTareasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subtarea, parent, false)
        return SubTareasViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubTareasViewHolder, position: Int) {
        val sub = subTareas[position]
        holder.tvNombre.text = sub.nombreSubtarea
        holder.tvDescripcion.text = sub.descripcionSubTarea ?: "Sin Descripcion"
        holder.tvEstado.text = sub.estadoSubtarea ?: "Sin Estado"

        holder.tvEstado.setBackgroundColor(when (sub.estadoSubtarea) {
                                               "Pendiente" -> Color.YELLOW
                                               "En Proceso" -> Color.rgb(81, 190, 207)
                                               "En Espera" -> Color.rgb(39, 238, 245)
                                               "Revisión" -> Color.MAGENTA
                                               "Completado" -> Color.GREEN
                                               "Cancelada" -> Color.RED
                                               else -> Color.LTGRAY
                                           })

    }

    override fun getItemCount(): Int = subTareas.size
}