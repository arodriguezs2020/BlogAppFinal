package es.alvarorodriguez.blogappfinal.domain.auth

import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseUser

// Esta interfaz solo tiene los metodos que va a utilizar el AuthRepoImpl
interface AuthRepo {
    suspend fun signIn(email: String, password: String) : FirebaseUser?
    suspend fun signUp(email: String, password: String, username: String): FirebaseUser?
    suspend fun updateProfile(imageBitmap: Bitmap, username: String)
}