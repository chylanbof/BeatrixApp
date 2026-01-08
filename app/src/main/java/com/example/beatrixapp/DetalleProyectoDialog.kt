package com.example.beatrixapp

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.beatrixapp.model.Proyecto
import com.example.beatrixapp.model.Tarea

// Recibe el proyecto pulsado y el usuario logueado para filtrar las tareas
class DetalleProyectoDialog(private val proyecto: Proyecto, private val loggedUser: String) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflamos tu layout dialog_detalle_proyecto
        return inflater.inflate(R.layout.dialog_detalle_proyecto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvNombre = view.findViewById<TextView>(R.id.tvNombreProyectoDetalle)
        val tvDescripcion = view.findViewById<TextView>(R.id.tvDescripcionProyectoDetalle)
        val recyclerTareas = view.findViewById<RecyclerView>(R.id.recyclerTareas)

        // 1. Mostrar datos del proyecto
        tvNombre.text = proyecto.nombreProyecto ?: "Proyecto sin Nombre"
        tvDescripcion.text = proyecto.descripcionProyecto

        // 2. Filtrar y Transformar las tareas y subtareas
        // La tarea solo se mostrará si el usuario tiene alguna asignación dentro de ella.
        val tareasFiltradasYTransformadas = proyecto.tareas.mapNotNull { tarea ->

            // a) Determinar si el usuario está asignado directamente a la Tarea
            val usuarioEnTarea = tarea.usuariosAsignados.any { it.nombreUsuario == loggedUser }

            // b) Filtrar las Subtareas: solo las asignadas al usuario
            val subtareasUsuario = tarea.subtarea.filter { subtarea ->
                subtarea.usuariosAsignadosSubTarea.any { it.nombreUsuario == loggedUser }
            }

            // Criterio de VISIBILIDAD DE LA TAREA en el diálogo:
            // La Tarea es visible si está asignada al usuario O si tiene Subtareas asignadas al usuario
            if (usuarioEnTarea || subtareasUsuario.isNotEmpty()) {
                // Devolvemos una copia de la Tarea con la lista de subtareas filtrada.
                // Esto asegura que solo se muestren las subtareas relevantes para el usuario.
                tarea.copy(
                    subtarea = subtareasUsuario
                )
            } else {
                null
            }
        }

        // 3. Configurar el RecyclerView con las tareas filtradas y transformadas
        if (tareasFiltradasYTransformadas.isNotEmpty()) {
            // Asegúrate de que TareaAdapter existe y está actualizado.
            val tareasAdapter = TareaAdapter(tareasFiltradasYTransformadas)
            recyclerTareas.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = tareasAdapter
            }
        } else {
            // Si no hay ninguna Tarea o Subtarea asignada al usuario, mostramos un mensaje
            recyclerTareas.visibility = View.GONE

            val parentLayout = view.findViewById<LinearLayout>(R.id.dialog_content_layout) // Asume que tienes un LinearLayout con esta ID

            val tvMensajeVacio = TextView(context).apply {
                text = "No tienes tareas ni subtareas asignadas en este proyecto."
                textSize = 16f
                setPadding(0, 16, 0, 0)
            }
            parentLayout?.addView(tvMensajeVacio)
        }
    }
}