package com.github.viniciusmo.automl

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import io.fotoapparat.Fotoapparat
import kotlinx.android.synthetic.main.activity_image_recognition.*

class ImageRecognitionActivity : AppCompatActivity(), PermissionListener {

    private lateinit var fotoapparat: Fotoapparat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_recognition)
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(this).check()
        fabTakePicture.setOnClickListener {
            takePicture()
        }
        FirebaseApp.initializeApp(applicationContext)
    }

    private fun takePicture() {
        val photoResult = fotoapparat.takePicture()
        photoResult
            .toBitmap()
            .whenAvailable { bitmapPhoto ->
                doImageRecognition(bitmapPhoto!!.bitmap)
            }
    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun doImageRecognition(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val localModel = FirebaseLocalModel.Builder("model")
            .setAssetFilePath("manifest.json")
            .build()
        FirebaseModelManager.getInstance().registerLocalModel(localModel)
        val labelerOptions = FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder()
            .setLocalModelName("model")
            .build()
        val labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(labelerOptions)

        labeler.processImage(image)
            .addOnSuccessListener { labels ->
                val imageLabels = labels
                    .map { ImageLabel(it.text, it.confidence) }
                    .toList()
                navigateToListImageResults(imageLabels)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Não foi possível realizar essa operação", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToListImageResults(labels: List<ImageLabel>) {
        val intent = ListImageLabelsActivity.callingIntent(this, ArrayList(labels))
        startActivity(intent)
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        fotoapparat = Fotoapparat(this, cameraView)
        fotoapparat.start()
    }

    override fun onPermissionRationaleShouldBeShown(
        permission: com.karumi.dexter.listener.PermissionRequest?,
        token: PermissionToken?
    ) {

    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        Toast.makeText(this, getString(R.string.error_permission), Toast.LENGTH_SHORT).show()
    }
}
