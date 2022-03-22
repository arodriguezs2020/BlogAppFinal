package es.alvarorodriguez.blogappfinal.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import es.alvarorodriguez.blogappfinal.core.Result
import es.alvarorodriguez.blogappfinal.data.model.Post
import es.alvarorodriguez.blogappfinal.domain.home.HomeScreenRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeScreenViewModel(private val repo: HomeScreenRepo): ViewModel() {

    // Con este metodo lo que hacemos es devolver los diferentes estados: Loading, Success y Failure que nos devuelve
    fun fetchLatestPosts() = liveData(Dispatchers.IO) {
        emit(Result.Loading())
        kotlin.runCatching {
            repo.getLatestPosts()
        }.onSuccess { postList ->
            emit(postList)
        }.onFailure { error ->
            emit(Result.Failure(Exception(error.message)))
        }

        /*
        try {
            emit(repo.getLatestPosts())
        } catch (e: java.lang.Exception) {
            emit(Result.Failure(e))
        }
        */
    }

    val latestPosts: StateFlow<Result<List<Post>>> = flow {
        kotlin.runCatching {
            repo.getLatestPosts()
        }.onSuccess { postList ->
            emit(postList)
        }.onFailure { error ->
            emit(Result.Failure(Exception(error.message)))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Result.Loading()
    )

    private val posts = MutableStateFlow<Result<List<Post>>>(Result.Loading())

    fun fetchPosts() = viewModelScope.launch {
        kotlin.runCatching {
            repo.getLatestPosts()
        }.onSuccess { postList ->
            posts.value = postList
        }.onFailure { error ->
            posts.value = Result.Failure(Exception(error.message))
        }
    }

    fun getPosts(): StateFlow<Result<List<Post>>> = posts

    // Con este metodo hacemos lo mismo pero con el tema de los likes
    fun registerLikeButtonState(postId: String, liked: Boolean) =
        liveData(viewModelScope.coroutineContext + Dispatchers.Main) {
            emit(Result.Loading())
            kotlin.runCatching {
                repo.registerLikeButtonState(postId, liked)
            }.onSuccess {
                emit(Result.Success(Unit))
            }.onFailure { error ->
                emit(Result.Failure(Exception(error.message)))
            }
        }
}

class HomeScreenViewModelFactory(private val repo: HomeScreenRepo): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(HomeScreenRepo::class.java).newInstance(repo)
    }
}