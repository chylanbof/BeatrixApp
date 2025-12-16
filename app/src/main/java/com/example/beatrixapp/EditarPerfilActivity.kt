package com.example.beatrixapp // Asegúrate de que esto coincida con tu paquete

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import android.widget.ImageButton

class EditarPerfilActivity : AppCompatActivity() {

    // Variables de la vista
    private lateinit var etNombreCompleto: EditText
    private lateinit var etEmail: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button
    private lateinit var btnAtras: ImageButton

    // Nombre del archivo donde guardaremos los cambios
    private val NOMBRE_ARCHIVO_JSON = "usuarios_data.json"

    // Usuario que estamos editando (esto debería venir del Login o la sesión)
    private var usuarioLogueadoActual = "afernandezzz" // Cambia esto por la variable global o Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)
        usuarioLogueadoActual = intent.getStringExtra("USUARIO_KEY") ?: "afernandezzz"
        // 1. Inicializar vistas
        etNombreCompleto = findViewById(R.id.etNombreCompleto)
        etEmail = findViewById(R.id.etEmail)
        etTelefono = findViewById(R.id.etTelefono)
        etContrasena = findViewById(R.id.etContrasena)
        btnGuardar = findViewById(R.id.btnGuardarCambios)
        btnCancelar = findViewById(R.id.btnCancelar)
        btnAtras = findViewById<ImageButton>(R.id.btnAtras)
        // 2. Cargar datos actuales en los campos
        cargarDatosDelUsuario()

        // 3. Configurar botón Cancelar
        btnCancelar.setOnClickListener {
            finish() // Cierra la actividad y vuelve atrás sin hacer nada
        }

        // 4. Configurar botón Guardar
        btnGuardar.setOnClickListener {
            guardarCambiosEnJSON()
        }

        btnAtras.setOnClickListener {
            finish()
        }
    }

    private fun cargarDatosDelUsuario() {
        // Leemos el JSON (primero intenta el interno editado, si no, el original raw)
        val jsonString = leerJSON()

        try {
            val jsonArray = JSONArray(jsonString)

            // Buscamos al usuario
            for (i in 0 until jsonArray.length()) {
                val usuario = jsonArray.getJSONObject(i)
                if (usuario.getString("nombreUsuario") == usuarioLogueadoActual) {

                    // Rellenamos los campos con lo que hay en el JSON
                    // Usamos optString para evitar errores si el campo está vacío o null
                    etNombreCompleto.setText(usuario.optString("nombreApellidos", ""))
                    etEmail.setText(usuario.optString("email", ""))
                    etTelefono.setText(usuario.optString("telefono", ""))
                    // La contraseña no la solemos mostrar por seguridad, o la dejamos vacía
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarCambiosEnJSON() {
        val jsonString = leerJSON()

        try {
            val jsonArray = JSONArray(jsonString)
            var encontrado = false

            for (i in 0 until jsonArray.length()) {
                val usuario = jsonArray.getJSONObject(i)

                // Buscamos al usuario original para modificarlo
                // NOTA: Si permites cambiar el nombre de usuario, deberías usar un ID fijo para buscarlo
                if (usuario.getString("nombreUsuario") == usuarioLogueadoActual) {

                    // Actualizamos los valores
                    usuario.put("nombreApellidos", etNombreCompleto.text.toString())
                    usuario.put("email", etEmail.text.toString())
                    usuario.put("telefono", etTelefono.text.toString())

                    // Solo cambiamos la contraseña si el usuario escribió algo nuevo
                    val nuevaPass = etContrasena.text.toString()
                    if (nuevaPass.isNotEmpty()) {
                        usuario.put("contrasena", nuevaPass)
                    }

                    encontrado = true
                    break
                }
            }

            if (encontrado) {
                // Escribir el nuevo JSON String en el almacenamiento interno del móvil
                escribirJSONEnAlmacenamientoInterno(jsonArray.toString())

                Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                finish() // Volvemos a la pantalla anterior
            } else {
                Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
        }
    }

    // --- FUNCIONES AUXILIARES DE LECTURA/ESCRITURA ---

    private fun leerJSON(): String {
        // 1. Intentamos leer del archivo interno (donde se guardan los cambios)
        try {
            val fileInputStream = openFileInput(NOMBRE_ARCHIVO_JSON)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            var text: String?
            while (bufferedReader.readLine().also { text = it } != null) {
                stringBuilder.append(text)
            }
            return stringBuilder.toString()
        } catch (e: Exception) {
            // 2. Si falla (es la primera vez), leemos del raw original
            return try {
                val inputStream = resources.openRawResource(R.raw.usuarios) // Asegúrate de tener usuarios.json en raw
                val reader = BufferedReader(InputStreamReader(inputStream))
                reader.use { it.readText() }
            } catch (e2: Exception) {
                "[]" // Si todo falla, devolvemos array vacío
            }
        }
    }

    private fun escribirJSONEnAlmacenamientoInterno(jsonContent: String) {
        try {
            val fileOutputStream = openFileOutput(NOMBRE_ARCHIVO_JSON, Context.MODE_PRIVATE)
            val outputStreamWriter = OutputStreamWriter(fileOutputStream)
            outputStreamWriter.write(jsonContent)
            outputStreamWriter.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}