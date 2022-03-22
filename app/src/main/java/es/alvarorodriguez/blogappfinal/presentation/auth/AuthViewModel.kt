package es.alvarorodriguez.blogappfinal.presentation.auth

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import es.alvarorodriguez.blogappfinal.core.Result
import es.alvarorodriguez.blogappfinal.domain.auth.AuthRepo
import kotlinx.coroutines.Dispatchers
// Cada uno de los metodos llama a su respectivo metodo del repo
class AuthViewModel(private val repo: AuthRepo): ViewModel() {

    // Este llama al metodo que lo que hace es loguear a un usuario
    fun signIn(email: String, password: String) = liveData(Dispatchers.IO){
        emit(Result.Loading())
        try {
            emit(Result.Success(repo.signIn(email, password)))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }
    // Este llama al metodo que lo que hace es registrar a un usuario
    fun signUp(email: String, password: String, username: String) = liveData(Dispatchers.IO){
        emit(Result.Loading())
        try {
            emit(Result.Success(repo.signUp(email, password, username)))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }

    // Este llama al metodo que lo que ahce es crerate un perfil
    fun updateUserProfile(imageBitmap: Bitmap, username: String) = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        try {
            emit(Result.Success(repo.updateProfile(imageBitmap, username)))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }
}

// Este es el factory, con el cual podremos crear una instancia del ViewModel en el Fragment
@Suppress("UNCHECKED_CAST")
class AuthViewModelFactory(private val repo: AuthRepo): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(repo) as T
    }
}