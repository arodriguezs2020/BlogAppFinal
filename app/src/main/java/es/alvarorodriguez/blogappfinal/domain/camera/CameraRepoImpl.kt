package es.alvarorodriguez.blogappfinal.domain.camera

import android.graphics.Bitmap
import es.alvarorodriguez.blogappfinal.data.remote.camera.CameraDataSource

class CameraRepoImpl(private val dataSource: CameraDataSource): CameraRepo {
    override suspend fun uploadPhoto(imageBitmap: Bitmap, description: String) =
        dataSource.uploadPhoto(imageBitmap, description)
}