package es.alvarorodriguez.blogappfinal.data.remote.home

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import es.alvarorodriguez.blogappfinal.core.Result
import es.alvarorodriguez.blogappfinal.data.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Suppress("UNCHECKED_CAST")
class HomeScreenDataSource {

    suspend fun getLatestPosts(): Result<List<Post>> {
        val postList = mutableListOf<Post>()

        withContext(Dispatchers.IO) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("posts")
                .orderBy("created_at", Query.Direction.DESCENDING).get().await()

            for (post in querySnapshot.documents) {
                // Debemos trasformar la informacion que nos llega del Firebase a un Object de tipo Post
                post.toObject(Post::class.java)?.let { fbPost ->

                    val isLiked = FirebaseAuth.getInstance().currentUser?.let { safeUser ->
                        isPostLiked(post.id, safeUser.uid)
                    }

                    // Esto lo utilizamos para solucionar el error que da nada mas crear el post, ya que sino no nos muestra la fecha
                    fbPost.apply {
                        created_at = post.getTimestamp("created_at", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()
                        id = post.id

                        if (isLiked != null) {
                            liked = isLiked
                        }
                    }

                    postList.add(fbPost)
                }
            }
        }

        /* Manera no recomendada, ya que si viene una cantidad elevada de datos podemos
           relentizar la visualizacion de esos datos

        postList.sortByDescending {
            it.created_at
        }

         */

        return Result.Success(postList)
    }

    // Con este metodo comprobamos si a el post se le ha dado like, para poder mostrar el corazon relleno o vacio al usuario
    private suspend fun isPostLiked(postId: String, uid: String): Boolean {
        val post = FirebaseFirestore.getInstance().collection("postsLikes").document(postId).get().await()
        if (!post.exists()) return false
        val likeArray: List<String> = post.get("likes") as List<String>
        return likeArray.contains(uid)
    }

    // Con este metodo lo que hacemos por un lado es crear una coleccion nueva para almacenar una
    // referencia al post que se le ha dado like con un array con los uid del usuario que le hayan dado like
    // Y por otro lado se le actualiza al post el numero de likes que tiene
    fun registerLikeButtonState(postId: String, liked: Boolean) {

        val increment = FieldValue.increment(1)
        val decrement = FieldValue.increment(-1)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val postRef = FirebaseFirestore.getInstance().collection("posts").document(postId)
        val postsLikesRef = FirebaseFirestore.getInstance().collection("postsLikes").document(postId)

        val database = FirebaseFirestore.getInstance()

        // Llamamos a este metodo que hace una transaccion con la BD de Firebase para poder dar like a un post
        databaseTransaction(database, postRef, liked, postsLikesRef, uid, increment, decrement)
    }

    private fun databaseTransaction(
        database: FirebaseFirestore,
        postRef: DocumentReference,
        liked: Boolean,
        postsLikesRef: DocumentReference,
        uid: String?,
        increment: FieldValue,
        decrement: FieldValue
    ) {
        database.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val likeCount = snapshot.getLong("likes")
            if (likeCount != null) {
                if (likeCount >= 0) {
                    if (liked) {
                        if (transaction.get(postsLikesRef).exists()) {
                            transaction.update(postsLikesRef, "likes", FieldValue.arrayUnion(uid))
                        } else {
                            transaction.set(
                                postsLikesRef,
                                hashMapOf("likes" to arrayListOf(uid)),
                                SetOptions.merge()
                            )
                        }
                        transaction.update(postRef, "likes", increment)
                    } else {
                        transaction.update(postRef, "likes", decrement)
                        transaction.update(postsLikesRef, "likes", FieldValue.arrayRemove(uid))
                    }
                }
            }
        }.addOnFailureListener {
            throw Exception(it.message)
        }
    }
}