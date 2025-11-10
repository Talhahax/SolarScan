package com.talha.solarscan.fragments.scanner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.talha.solarscan.R
import com.talha.solarscan.viewmodel.SolarViewModel

class UploadPageFragment : Fragment() {

    private val solarViewModel: SolarViewModel by activityViewModels()

    private lateinit var buttonChooseFile: MaterialButton
    private lateinit var buttonProcess: MaterialButton
    private lateinit var imagePreview: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.page_upload, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonChooseFile = view.findViewById(R.id.button_choose_file)
        buttonProcess = view.findViewById(R.id.button_process)
        imagePreview = view.findViewById(R.id.image_preview)
        progressBar = view.findViewById(R.id.progress_bar)
        progressText = view.findViewById(R.id.text_loading)

        buttonChooseFile.setOnClickListener {
            openImagePicker()
        }

        buttonProcess.setOnClickListener {
            selectedImageUri?.let { uri ->
                processImage(uri)
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        // Only observe errors - loading is handled manually, navigation by parent
        solarViewModel.errorLiveData.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showLoading(false)
                Toast.makeText(context, "❌ Error: $error", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                imagePreview.setImageURI(uri)
                imagePreview.visibility = View.VISIBLE
                buttonProcess.visibility = View.VISIBLE
            }
        }
    }

    private fun processImage(uri: Uri) {
        showLoading(true)

        try {
            val image = InputImage.fromFilePath(requireContext(), uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extractedText = visionText.text

                    if (extractedText.isNotBlank()) {
                        fetchSolarRecommendation(extractedText)
                    } else {
                        showLoading(false)
                        Toast.makeText(
                            context,
                            "No text found in image. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    Toast.makeText(
                        context,
                        "Text recognition failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } catch (e: Exception) {
            showLoading(false)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchSolarRecommendation(billText: String) {
        solarViewModel.fetchRecommendation(billText, budget = null)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        progressText.visibility = if (show) View.VISIBLE else View.GONE
        buttonProcess.isEnabled = !show
        buttonChooseFile.isEnabled = !show
    }
}