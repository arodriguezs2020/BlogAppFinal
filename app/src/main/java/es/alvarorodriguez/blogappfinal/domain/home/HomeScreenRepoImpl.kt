package es.alvarorodriguez.blogappfinal.domain.home

import es.alvarorodriguez.blogappfinal.core.Result
import es.alvarorodriguez.blogappfinal.data.model.Post
import es.alvarorodriguez.blogappfinal.data.remote.home.HomeScreenDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

// En esta clase lo que hacemos es llamar al DataSource que es el que tiene los metodos correspondientes que enlazan con el servidor,
// un metodo es para poder mostrar todos los posts que tenemos en el servidor y otro para guardar o eliminar un like en el seruvidor
class HomeScreenRepoImpl(private val dataSource: HomeScreenDataSource): HomeScreenRepo {
    override suspend fun getLatestPosts(): Result<List<Post>> = dataSource.getLatestPosts()
    override suspend fun registerLikeButtonState(postId: String, liked: Boolean) = dataSource.registerLikeButtonState(postId, liked)
}