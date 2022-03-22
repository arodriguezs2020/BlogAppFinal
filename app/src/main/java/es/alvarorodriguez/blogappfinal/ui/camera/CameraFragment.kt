package es.alvarorodriguez.blogappfinal.ui.camera

import android.app.Activity
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
import es.alvarorodriguez.blogappfinal.data.remote.camera.CameraDataSource
import es.alvarorodriguez.blogappfinal.databinding.FragmentCameraBinding
import es.alvarorodriguez.blogappfinal.domain.camera.CameraRepoImpl
import es.alvarorodriguez.blogappfinal.presentation.camera.CameraViewModel
import es.alvarorodriguez.blogappfinal.presentation.camera.CameraViewModelFactory

class CameraFragment : Fragment(R.layout.fragment_camera) {

    private lateinit var binding: FragmentCameraBinding
    private var bitmap: Bitmap? = null

    // --- Creamos una variable para recoger los datos que nos lleguen desde el otro Activity de vuelta --- //
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                binding.imgAddPhoto.setImageBitmap(imageBitmap)
                bitmap = imageBitmap
            }
        }

    // --- Creamos la instancia del ViewModel de la camara --- //
    private val viewModel by viewModels<CameraViewModel> {
        CameraViewModelFactory(
            CameraRepoImpl(
                CameraDataSource()
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        binding = FragmentCameraBinding.bind(view)
        try {
            startForResult.launch(takePictureIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                "No se encontró ninguna app para abrir la camara.",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.btnUploadPhoto.setOnClickListener {
            bitmap?.let { bitmap ->
                viewModel.uploadPhoto(bitmap, binding.etxPhotoDescription.text.toString().trim())
                    .observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is Result.Loading -> {
                                Toast.makeText(requireContext(), "Uploading photo...", Toast.LENGTH_SHORT).show()
                            }
                            is Result.Success -> {
                                findNavController().navigate(R.id.action_cameraFragment_to_homeScreenFragment)
                            }
                            is Result.Failure -> {
                                Toast.makeText(requireContext(), "Error ${result.exception}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }
    }
}