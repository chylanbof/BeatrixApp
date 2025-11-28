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
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class LoginActivity : AppCompatActivity() {

    private lateinit var projectsJsonArray: JSONArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLoginActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Cargar JSON
        try {
            projectsJsonArray = loadProjectJson()
            Log.d("LOGIN", "JSON cargado correctamente. Total proyectos: ${projectsJsonArray.length()}")
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
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Nombre de usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProjectJson(): JSONArray {
        val inputStream = resources.openRawResource(R.raw.proyectos)
        val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
        val content = reader.readText()
        reader.close()

        Log.d("LOGIN", "Contenido JSON (primeros 200 caracteres): ${content.take(200)}")

        return JSONArray(content)
    }

    private fun checkLogin(username: String, password: String): Boolean {

        for (p in 0 until projectsJsonArray.length()) {
            val project = projectsJsonArray.getJSONObject(p)
            val tareas = project.optJSONArray("Tareas") ?: continue

            for (t in 0 until tareas.length()) {
                val tarea = tareas.getJSONObject(t)
                val usuarios = tarea.optJSONArray("usuariosAsignados") ?: continue

                for (u in 0 until usuarios.length()) {
                    val user: JSONObject = usuarios.getJSONObject(u)

                    val nombreUsuario = user.optString("nombreUsuario", "")
                    val contrasena = user.optString("contraseña", "")

                    Log.d("LOGIN", "Comprobando usuario: $nombreUsuario / $contrasena")

                    if (username == nombreUsuario && password == contrasena) {
                        return true
                    }
                }
            }
        }
        return false
    }
}
