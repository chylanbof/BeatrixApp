package com.example.beatrixapp
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.beatrixapp.model.Proyecto
import com.example.beatrixapp.model.SubTarea
import com.example.beatrixapp.model.Tarea
import com.example.beatrixapp.model.Usuario
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class Proyectos2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.proyectos2)

        val nombre = intent.getStringExtra("nombreProyecto")
    }
}