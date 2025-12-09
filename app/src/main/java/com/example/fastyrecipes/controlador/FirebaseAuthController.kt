package com.example.fastyrecipes.controller

import com.example.fastyrecipes.modelo.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseAuthController {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    private val usuariosCollection = db.collection("usuarios")

    // REGISTRAR USUARIO
    suspend fun registrarUsuario(email: String, password: String, nombre: String): Result<Usuario> {
        return try {
            println("🔄 Intentando registrar usuario: $email")

            // 1. Crear usuario en Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("No se pudo obtener ID de usuario")

            println("✅ Usuario creado en Firebase Auth: $userId")

            // 2. Crear documento en Firestore
            val usuario = Usuario(
                id = userId,
                nombre = nombre,
                correo = email,
                contraseña = "", // No almacenamos la contraseña en Firestore
                fechaRegistro = System.currentTimeMillis(),
                recetasGuardadas = emptyList(),
                historialBusquedas = emptyList()
            )

            // 3. Guardar en Firestore
            usuariosCollection.document(userId).set(usuario).await()

            println("✅ Usuario guardado en Firestore: $nombre")

            Result.success(usuario)

        } catch (e: Exception) {
            println("❌ Error al registrar: ${e.message}")
            Result.failure(Exception("Error al registrar usuario: ${e.message}"))
        }
    }

    // INICIAR SESIÓN
    suspend fun iniciarSesion(email: String, password: String): Result<Usuario> {
        return try {
            println("🔄 Intentando iniciar sesión: $email")

            // 1. Autenticar con Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("No se pudo obtener ID de usuario")

            println("✅ Autenticación exitosa: $userId")

            // 2. Obtener datos del usuario de Firestore
            val usuario = obtenerUsuario(userId)

            if (usuario != null) {
                Result.success(usuario)
            } else {
                println("⚠️ Usuario no encontrado en Firestore")
                Result.failure(Exception("Error en los datos del usuario"))
            }

        } catch (e: Exception) {
            println("❌ Error al iniciar sesión: ${e.message}")

            // Mensajes de error específicos
            val mensajeError = when {
                e.message?.contains("no user record") == true -> "Usuario no registrado"
                e.message?.contains("password is invalid") == true -> "Contraseña incorrecta"
                e.message?.contains("badly formatted") == true -> "Correo electrónico inválido"
                else -> "Error al iniciar sesión: ${e.message}"
            }

            Result.failure(Exception(mensajeError))
        }
    }

    // OBTENER USUARIO DE FIRESTORE
    suspend fun obtenerUsuario(usuarioId: String): Usuario? {
        return try {
            val document = usuariosCollection.document(usuarioId).get().await()
            if (document.exists()) {
                document.toObject(Usuario::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo usuario: ${e.message}")
            null
        }
    }

    // OBTENER USUARIO POR CORREO
    suspend fun obtenerUsuarioPorCorreo(correo: String): Usuario? {
        return try {
            val query = usuariosCollection.whereEqualTo("correo", correo).get().await()
            query.documents.firstOrNull()?.toObject(Usuario::class.java)
        } catch (e: Exception) {
            println("❌ Error obteniendo usuario por correo: ${e.message}")
            null
        }
    }

    // OBTENER USUARIO ACTUAL
    fun obtenerUsuarioActual(): Usuario? {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            Usuario(
                id = firebaseUser.uid,
                nombre = firebaseUser.displayName ?: "Usuario",
                correo = firebaseUser.email ?: "",
                contraseña = "",
                fechaRegistro = System.currentTimeMillis(),
                recetasGuardadas = emptyList(),
                historialBusquedas = emptyList()
            )
        } else {
            null
        }
    }

    // GUARDAR USUARIO EN FIRESTORE
    suspend fun guardarUsuario(usuario: Usuario) {
        try {
            usuariosCollection.document(usuario.id).set(usuario).await()
            println("✅ Usuario actualizado en Firestore: ${usuario.nombre}")
        } catch (e: Exception) {
            println("❌ Error guardando usuario: ${e.message}")
            throw e
        }
    }

    // CERRAR SESIÓN
    fun cerrarSesion() {
        auth.signOut()
        println("✅ Sesión cerrada")
    }

    // VERIFICAR SI ESTÁ AUTENTICADO
    fun estaAutenticado(): Boolean {
        return auth.currentUser != null
    }
}