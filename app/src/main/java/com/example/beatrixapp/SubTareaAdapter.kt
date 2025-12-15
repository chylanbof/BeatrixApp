package com.example.beatrixapp

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.beatrixapp.model.Subtarea
import android.view.View
import kotlin.collections.joinToString


class SubTareaAdapter(private val subTarea: List<Subtarea>) :
    RecyclerView.Adapter<SubTareaAdapter.SubTareasViewHolder>(){

    class SubTareasViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreSubtarea)
        val tvDescripcion = view.findViewById<TextView>(R.id.tvDescripcionSubTarea)
        val tvEstado = view.findViewById<TextView>(R.id.tvEstadoSubtarea)

        val tvUsuariosSub = view.findViewById<TextView>(R.id.tvUsuariosSubtarea)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubTareasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subtarea, parent, false)
        return SubTareasViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SubTareasViewHolder, position: Int) {
        val sub = subTarea[position]
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
        holder.tvUsuariosSub.text = "Usuarios: ${sub.usuariosAsignadosSubTarea.joinToString (", "){ it.nombreUsuario.toString() }}"



    }

    override fun getItemCount(): Int = subTarea.size
}