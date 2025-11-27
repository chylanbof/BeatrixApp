package com.example.beatrixapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLoginActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        val etNombreUsuario = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)





        btnEntrar.setOnClickListener {
            val nombreUsuario = etNombreUsuario.text.toString().trim()
            val contrasena = etPassword.text.toString().trim()

            if (nombreUsuario.isEmpty())
            {
                Toast.makeText(this, "Por favor, ingrese el nombre de usuario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (contrasena.isEmpty())
            {
                Toast.makeText(this, "Por favor, la contraseña no puede ser null!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }



    }
}