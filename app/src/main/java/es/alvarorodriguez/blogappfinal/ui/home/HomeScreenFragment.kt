package es.alvarorodriguez.blogappfinal.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import es.alvarorodriguez.blogappfinal.R
import es.alvarorodriguez.blogappfinal.core.Result
import es.alvarorodriguez.blogappfinal.core.hide
import es.alvarorodriguez.blogappfinal.core.show
import es.alvarorodriguez.blogappfinal.data.model.Post
import es.alvarorodriguez.blogappfinal.data.remote.home.HomeScreenDataSource
import es.alvarorodriguez.blogappfinal.databinding.FragmentHomeScreenBinding
import es.alvarorodriguez.blogappfinal.domain.home.HomeScreenRepoImpl
import es.alvarorodriguez.blogappfinal.presentation.home.HomeScreenViewModel
import es.alvarorodriguez.blogappfinal.presentation.home.HomeScreenViewModelFactory
import es.alvarorodriguez.blogappfinal.ui.home.adapter.HomeScreenAdapter
import es.alvarorodriguez.blogappfinal.ui.home.adapter.OnPostClickListener
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeScreenFragment : Fragment(R.layout.fragment_home_screen), OnPostClickListener {

    // Con esta variable lo que hacemos es un binding al Fragmento del Home
    private lateinit var binding: FragmentHomeScreenBinding

    // Con esta variable lo que estamos creando es una instancia del ViewModel que nos permitira estar en comunicacion a traves de metodos con las
    // diferentes capas de la arquitectura y poder hacer llamadas al servidor desde otras capas
    private val viewModel by viewModels<HomeScreenViewModel> { HomeScreenViewModelFactory(
        HomeScreenRepoImpl(
        HomeScreenDataSource()
    )
    ) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeScreenBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.latestPosts.collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            binding.progressBar.show()
                        }

                        is Result.Success -> {
                            binding.progressBar.hide()
                            if(result.data.isEmpty()) {
                                binding.emptyContainer.show()
                                return@collect
                            } else {
                                binding.emptyContainer.hide()
                            }
                            binding.rvHome.adapter = HomeScreenAdapter(result.data, this@HomeScreenFragment )
                        }

                        is Result.Failure -> {
                            binding.progressBar.hide()
                            Toast.makeText(
                                requireContext(),
                                "Ocurrio un error: ${result.exception}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        /*
        // Esta pieza de codigo lo que hace es estar observando los cambios en el servidor para en caso de que se añada un
        // post nuevo o se elimine el usuario vea el resultado. Tambien checkea los tres estado de carga: Loading, Success y Failure
        viewModel.fetchLatestPosts().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.show()
                }

                is Result.Success -> {
                    binding.progressBar.hide()
                    if(result.data.isEmpty()) {
                        binding.emptyContainer.show()
                        return@observe
                    } else {
                        binding.emptyContainer.hide()
                    }
                    binding.rvHome.adapter = HomeScreenAdapter(result.data, this)
                }

                is Result.Failure -> {
                    binding.progressBar.hide()
                    Toast.makeText(
                        requireContext(),
                        "Ocurrio un error: ${result.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        */
    }

    // Este metodo solo nos sirve para estar escuchando cuando le demos like o le quitemos el like y solo
    // mostramos un mensaje de error en caso de que suceda un fallo
    override fun onLikeButtonClick(post: Post, liked: Boolean) {
        viewModel.registerLikeButtonState(post.id, liked).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> { }

                is Result.Success -> {}

                is Result.Failure -> {
                    Toast.makeText(
                        requireContext(),
                        "Ocurrio un error: ${result.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}