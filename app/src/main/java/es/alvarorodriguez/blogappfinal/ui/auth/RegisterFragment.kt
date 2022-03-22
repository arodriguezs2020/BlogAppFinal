package es.alvarorodriguez.blogappfinal.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import es.alvarorodriguez.blogappfinal.R
import es.alvarorodriguez.blogappfinal.core.Result
import es.alvarorodriguez.blogappfinal.data.remote.auth.AuthDataSource
import es.alvarorodriguez.blogappfinal.databinding.FragmentRegisterBinding
import es.alvarorodriguez.blogappfinal.domain.auth.AuthRepoImpl
import es.alvarorodriguez.blogappfinal.presentation.auth.AuthViewModel
import es.alvarorodriguez.blogappfinal.presentation.auth.AuthViewModelFactory

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var binding: FragmentRegisterBinding

    private val viewModel by viewModels<AuthViewModel> { AuthViewModelFactory(
        AuthRepoImpl(AuthDataSource()))}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)
        signUp()
    }

    // Con este metodo recogemos todos los datos y llamamos al metodo de validar y si pasa creamos el usuario
    private fun signUp() {

        binding.btnSignup.setOnClickListener {

            val username = binding.editTextUsername.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

            if (validateRegister(password, confirmPassword, username,email)) return@setOnClickListener

            // Este metodo crea el usuario
            createUser(email, password, username)
        }
    }

    // Con este metodo creamos el usuario llamando a los diferentes metodos del viewModel
    private fun createUser(email: String, password: String, username: String) {
        viewModel.signUp(email, password, username).observe(viewLifecycleOwner) { result ->
            when(result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSignup.isEnabled = false
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    findNavController().navigate(R.id.action_registerFragment_to_setupProfileFragment)
                }
                is Result.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSignup.isEnabled = true
                    Toast.makeText(requireContext(), "Error: ${result.exception}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Con este metodo lo que hacemos es validar el registro
    private fun validateRegister(
        password: String,
        confirmPassword: String,
        username: String,
        email: String
    ): Boolean {
        if (password != confirmPassword) {
            binding.editTextConfirmPassword.error = "Password does not match"
            binding.editTextPassword.error = "Password does not match"
            return true
        }

        if (username.isEmpty()) {
            binding.editTextUsername.error = "Username is empty"
            return true
        }

        if (email.isEmpty()) {
            binding.editTextEmail.error = "Email is empty"
            return true
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "E-mail isn't correct"
            return true
        }

        if (password.isEmpty()) {
            binding.editTextPassword.error = "Password is empty"
            return true
        }

        if (confirmPassword.isEmpty()) {
            binding.editTextConfirmPassword.error = "Password is empty"
            return true
        }
        return false
    }
}