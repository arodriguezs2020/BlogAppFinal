package es.alvarorodriguez.blogappfinal.data.remote.camera

import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import es.alvarorodriguez.blogappfinal.data.model.Post
import es.alvarorodriguez.blogappfinal.data.model.Poster
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*

class CameraDataSource {
    // Cargar un post desde la pantalla de la Camara
    suspend fun uploadPhoto(imageBitmap: Bitmap, description: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val randomName = UUID.randomUUID().toString()
        val imageRef = FirebaseStorage.getInstance().reference.child("${user?.uid}/posts/$randomName")
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val downloadUrl = imageRef.putBytes(baos.toByteArray()).await().storage.downloadUrl.await().toString()
        user?.let { it ->
            it.displayName?.let { displayName ->
                FirebaseFirestore.getInstance().collection("posts").add(Post(
                    poster = Poster(username = displayName, uid = user.uid, profile_picture = it.photoUrl.toString()),
                    post_image = downloadUrl,
                    post_description = description,
                    likes = 0))
            }
        }

    }
}