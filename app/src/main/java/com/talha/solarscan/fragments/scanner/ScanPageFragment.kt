package com.talha.solarscan.fragments.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.talha.solarscan.R
import com.talha.solarscan.viewmodel.SolarViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanPageFragment : Fragment() {

    private val solarViewModel: SolarViewModel by activityViewModels()

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var previewView: PreviewView
    private lateinit var buttonCapture: MaterialButton
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var textLoading: TextView

    companion object {
        private const val TAG = "ScanPageFragment"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.page_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewView = view.findViewById(R.id.preview_view)
        buttonCapture = view.findViewById(R.id.fab_capture)
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        textLoading = view.findViewById(R.id.text_loading)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        buttonCapture.setOnClickListener {
            takePhoto()
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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        showLoading(true, "📸 Capturing image...")

        val photoFile = File(
            requireContext().cacheDir,
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    processImage(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    showLoading(false)
                    Toast.makeText(
                        context,
                        "Photo capture failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun processImage(file: File) {
        showLoading(true, "📄 Reading text from bill...")

        try {
            val image = InputImage.fromFilePath(requireContext(), Uri.fromFile(file))
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extractedText = visionText.text
                    Log.d(TAG, "Extracted text: $extractedText")

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
        showLoading(true, "🔄 Analyzing bill & calculating solar savings...")
        solarViewModel.fetchRecommendation(billText, budget = null)
    }

    private fun showLoading(show: Boolean, message: String = "Processing...") {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        textLoading.text = message
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    context,
                    "Camera permission is required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }
}