package es.alvarorodriguez.blogappfinal.presentation.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import es.alvarorodriguez.blogappfinal.core.Result
import es.alvarorodriguez.blogappfinal.domain.camera.CameraRepo
import kotlinx.coroutines.Dispatchers

class CameraViewModel(private val repo: CameraRepo): ViewModel() {

    // Este metodo lo que hace es subir una imagen y una descripcion
    fun uploadPhoto(imageBitmap: Bitmap, description: String) = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        try {
            emit(Result.Success(repo.uploadPhoto(imageBitmap, description)))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }
}

@Suppress("UNCHECKED_CAST")
class CameraViewModelFactory(private val repo: CameraRepo): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CameraViewModel(repo) as T
    }
}