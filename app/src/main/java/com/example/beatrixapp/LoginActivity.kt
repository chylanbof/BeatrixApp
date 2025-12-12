package com.example.beatrixapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.beatrixapp.model.Proyecto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class LoginActivity : AppCompatActivity() {

    private lateinit var proyectosList: List<Proyecto>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLoginActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // cargar JSON
        try {
            proyectosList = loadProjectJson()
            Log.d("LOGIN", "JSON cargado correctamente. Total proyectos: ${proyectosList.size}")
        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar el archivo JSON", Toast.LENGTH_LONG).show()
            Log.e("LOGIN", "Error al cargar JSON", e)
            return
        }

        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        val etNombreUsuario = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        btnEntrar.setOnClickListener {
            val username = etNombreUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty()) {
                Toast.makeText(this, "Por favor, introduzca el nombre de usuario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "La contraseña no puede estar vacía", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (checkLogin(username, password)) {
                Toast.makeText(this, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USERNAME", username)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProjectJson(): List<Proyecto> {
        val inputStream = resources.openRawResource(R.raw.proyectos)
        val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
        val content = reader.readText()
        reader.close()
        val listType = object : TypeToken<List<Proyecto>>() {}.type
        return Gson().fromJson(content, listType)
    }

    private fun checkLogin(username: String, password: String): Boolean {
        for (proyecto in proyectosList) {
            for (tarea in proyecto.tareas) {

                for (user in tarea.usuariosAsignados) {
                    val contrasena = user.contrasena ?: ""
                    if (username.equals(user.nombreUsuario, ignoreCase = true)
                        && (contrasena.isEmpty() || password == contrasena)) {
                        return true
                    }
                }


                for (subtarea in tarea.subtarea) {
                    for (userSub in subtarea.usuariosAsignadosSubTarea) {
                        val contrasenaSub = userSub.contrasena ?: ""
                        if (username.equals(userSub.nombreUsuario, ignoreCase = true)
                            && (contrasenaSub.isEmpty() || password == contrasenaSub)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

}
