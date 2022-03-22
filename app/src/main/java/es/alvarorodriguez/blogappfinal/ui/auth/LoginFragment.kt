package es.alvarorodriguez.blogappfinal.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import es.alvarorodriguez.blogappfinal.R
import es.alvarorodriguez.blogappfinal.core.Result
import es.alvarorodriguez.blogappfinal.data.remote.auth.AuthDataSource
import es.alvarorodriguez.blogappfinal.databinding.FragmentLoginBinding
import es.alvarorodriguez.blogappfinal.domain.auth.AuthRepoImpl
import es.alvarorodriguez.blogappfinal.presentation.auth.AuthViewModel
import es.alvarorodriguez.blogappfinal.presentation.auth.AuthViewModelFactory

class LoginFragment : Fragment(R.layout.fragment_login) {

    // Inicializamos el binding, para de esta forma poder acceder a los campos de nuestro framgment
    private lateinit var binding: FragmentLoginBinding

    // Inicializamos el firebaseAuth de manera perezosa, lo que quiere decir que solo se va a estar
    // ejecutando cuando lo vallamaos a llamar, no siempre
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    // Con este metodo lo que hace es crear una instancia del ViewModel en el Fragmento
    private val viewModel by viewModels<AuthViewModel> { AuthViewModelFactory(AuthRepoImpl(
        AuthDataSource()
    )) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        isUserLoggedIn()
        doLogin()
        goToSignUpPage()
    }

    // Con este metodo comoprobamos si el usuario y contraseña estan autenticados en nuestra base
    // de datos de Firebase
    private fun isUserLoggedIn() {
        firebaseAuth.currentUser?.let { user ->
            if(user.displayName.isNullOrEmpty()) {
                findNavController().navigate(R.id.action_loginFragment_to_setupProfileFragment)
            } else {
                findNavController().navigate(R.id.action_loginFragment_to_homeScreenFragment)
            }
        }
    }

    // Este metodo lo llamamos desde que se crea el fragmento pero solo se ejecutará cuando el
    // usuario haga click en el botón
    private fun doLogin() {
        binding.btnSignin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            validateCredentials(email, password)
            signIn(email, password)
        }
    }

    // Este metodo lo que aces es que cuando le den al click del texto te lleva al registro
    private fun goToSignUpPage() {
        binding.textSignup.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    // Con este metodo lo que hacemos es validar los campos de email y password
    private fun validateCredentials(email: String, password: String) {

        // Validamos si el campo email esta vacio
        if (email.isEmpty()) {
            binding.editTextEmail.error = "E-mail is empty"
            return
        }

        // Validamos si el email que introducen tiene un formato de tipo email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "E-mail isn't correct"
            return
        }

        // Validamos si el campo de password esta vacio
        if (password.isEmpty()) {
            binding.editTextPassword.error = "Password is empty"
            return
        }
    }

    // Con este metodo hacemos login si todo ha ido bien
    private fun signIn(email: String, password: String) {
        viewModel.signIn(email, password).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSignin.isEnabled = false
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if(result.data?.displayName.isNullOrEmpty()) {
                        findNavController().navigate(R.id.action_loginFragment_to_setupProfileFragment)
                    } else {
                        findNavController().navigate(R.id.action_loginFragment_to_homeScreenFragment)
                    }
                }
                is Result.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSignin.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        "Error: ${result.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}