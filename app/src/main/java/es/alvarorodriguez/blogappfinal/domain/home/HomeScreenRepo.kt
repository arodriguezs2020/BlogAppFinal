package es.alvarorodriguez.blogappfinal.domain.home

import es.alvarorodriguez.blogappfinal.core.Result
import es.alvarorodriguez.blogappfinal.data.model.Post

interface HomeScreenRepo {
    suspend fun getLatestPosts(): Result<List<Post>>
    suspend fun registerLikeButtonState(postId: String, liked: Boolean)
}