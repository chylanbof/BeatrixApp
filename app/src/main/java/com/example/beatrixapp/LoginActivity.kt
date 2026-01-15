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
import com.example.beatrixapp.model.Usuario
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class LoginActivity : BaseActivity() {

    private lateinit var usuariosList: List<Usuario>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLoginActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Cargar JSON de usuarios
        try {
            usuariosList = loadUsersJson()
            Log.d("LOGIN", "Usuarios cargados correctamente. Total: ${usuariosList.size}")
        } catch (e: Exception) {
            //Toast.makeText(this, "Error al cargar el archivo de usuarios", Toast.LENGTH_LONG).show()
            Toast.makeText(this, getString(R.string.login_error_loading), Toast.LENGTH_LONG).show()
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
                Toast.makeText(this, getString(R.string.login_empty_username), Toast.LENGTH_SHORT).show()
            }
            else if (password.isEmpty()) {
                //Toast.makeText(this, "La contraseña no puede estar vacía", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, getString(R.string.login_empty_password), Toast.LENGTH_SHORT).show()
            } else {
                if (checkLogin(username, password)) {
                    Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT)
                        .show()
                    val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
                    prefs.edit().putString("loggedUser", username).apply()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun loadUsersJson(): List<Usuario> {
        val inputStream = resources.openRawResource(R.raw.usuarios) // archivo usuarios.json
        val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
        val content = reader.readText()
        reader.close()
        val listType = object : TypeToken<List<Usuario>>() {}.type
        return Gson().fromJson(content, listType)
    }

    private fun checkLogin(username: String, password: String): Boolean {
        for (user in usuariosList) {
            val contrasena = user.contrasena ?: ""
            if (username.equals(user.nombreUsuario, ignoreCase = true)
                && (contrasena.isEmpty() || password == contrasena)) {
                return true
            }
        }
        return false
    }
}
