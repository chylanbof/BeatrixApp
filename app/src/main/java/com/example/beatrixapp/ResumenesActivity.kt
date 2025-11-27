package com.example.beatrixapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ResumenesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Conexión con el archivo activity_resumenes.xml
        setContentView(R.layout.activity_resumenes)
    }
}