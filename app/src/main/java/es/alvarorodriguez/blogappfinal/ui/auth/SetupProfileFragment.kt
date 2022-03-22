package es.alvarorodriguez.blogappfinal.ui.auth

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import es.alvarorodriguez.blogappfinal.R
import es.alvarorodriguez.blogappfinal.core.Result
import es.alvarorodriguez.blogappfinal.data.remote.auth.AuthDataSource
import es.alvarorodriguez.blogappfinal.databinding.FragmentSetupProfileBinding
import es.alvarorodriguez.blogappfinal.domain.auth.AuthRepoImpl
import es.alvarorodriguez.blogappfinal.presentation.auth.AuthViewModel
import es.alvarorodriguez.blogappfinal.presentation.auth.AuthViewModelFactory

class SetupProfileFragment : Fragment(R.layout.fragment_setup_profile) {

    private lateinit var binding: FragmentSetupProfileBinding
    private val viewModel by viewModels<AuthViewModel> { AuthViewModelFactory(
        AuthRepoImpl(AuthDataSource()
    )) }
    private var bitmap: Bitmap? = null

    // --- Creamos una variable para recoger los datos que nos lleguen desde el otro Activity de vuelta --- //
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            binding.profileImage.setImageBitmap(imageBitmap)
            bitmap = imageBitmap
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSetupProfileBinding.bind(view)
        binding.profileImage.setOnClickListener { 
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startForResult.launch(takePictureIntent)
            }catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "No se encontró app para abrir la camara", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCreateProfile.setOnClickListener {
            val username = binding.etxtUsername.text.toString().trim()
            val alertDialog = AlertDialog.Builder(requireContext()).setTitle("Uploading photo...").create()
            bitmap?.let { bitmap ->
                if (username.isNotEmpty()) {
                    viewModel.updateUserProfile(bitmap, username).observe(viewLifecycleOwner) { result ->
                        when(result) {
                            is Result.Loading -> {
                                alertDialog.show()
                            }
                            is Result.Success -> {
                                alertDialog.dismiss()
                                findNavController().navigate(R.id.action_setupProfileFragment_to_homeScreenFragment)
                            }
                            is Result.Failure -> {
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
        }
    }
}