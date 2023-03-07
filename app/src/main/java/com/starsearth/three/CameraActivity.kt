package com.starsearth.three

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.core.ImageAnalysis.STRATEGY_BLOCK_PRODUCER
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.PredefinedCategory
import com.google.mlkit.vision.text.TextRecognition
import com.starsearth.three.application.StarsEarthApplication
import com.starsearth.three.domain.Action
import com.starsearth.two.listeners.SeOnTouchListener
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity(), SensorEventListener {

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var mRowType : Action.Companion.ROW_TYPE? = null
    private var mObjectDetector : ObjectDetector? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        intent.extras?.let {
            mRowType = it.get(Action.Companion.ROW_TYPE.ROW_TYPE_KEY.toString()) as? Action.Companion.ROW_TYPE
            if (mRowType == Action.Companion.ROW_TYPE.CAMERA_OCR) {
                tvInstructions?.text = "Point your camera at the door\nWe will tell you the text"
            }
            else if (mRowType == Action.Companion.ROW_TYPE.CAMERA_OBJECT_DETECTION) {
                // Live detection and tracking
                val options = ObjectDetectorOptions.Builder()
                    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                    .enableClassification()  // Optional
                    .build()
                mObjectDetector = ObjectDetection.getClient(options)
                tvInstructions?.text = "Point your camera around you"
            }
        }

        supportActionBar?.hide()

        // Request camera permissions
        if (allPermissionsGranted()) {
            //(application as? StarsEarthApplication)?.sayThis(tvInstructions?.text?.toString())
            startCamera()

            val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            //sensor?.let { sensorManager.registerListener(this,it,SensorManager.SENSOR_DELAY_NORMAL) } //uncomment this to get calls at onSensorChanged
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Setup the listener for take photo button
        camera_capture_button.setOnClickListener { takePhoto() }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onPause() {
        super.onPause()

        if ((application as? StarsEarthApplication)?.textToSpeech?.isSpeaking == true) {
            (application as? StarsEarthApplication)?.textToSpeech?.stop()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build()

            imageCapture = ImageCapture.Builder()
                .build()

            imageAnalyzer = ImageAnalysis.Builder()
                //.setBackpressureStrategy(STRATEGY_BLOCK_PRODUCER)
                .build()
                .also {
                  /*  it.setAnalyzer(cameraExecutor, CameraFeedAnalyzer { forSyntaxSake ->
                        //Log.d(TAG, "Average luminosity: $luma")
                    })  */
                    it.setAnalyzer(cameraExecutor, CameraFeedAnalyzer (this, mRowType, mObjectDetector))
                }

            // Select back camera
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)
                preview?.setSurfaceProvider(viewFinder.createSurfaceProvider(camera?.cameraInfo))
                cl?.contentDescription = tvInstructions?.text.toString()
                //(applicationContext as? StarsEarthApplication)?.sayThis(tvInstructions?.text.toString())
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create timestamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            /*SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"*/ "se_three.jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Setup image capture listener which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        Log.d("TAG", "********ON SENSOR CHANGE CALLED*********")
        Log.d("TAG", "********VALUE 1***********"+sensorEvent?.values?.get(0))
        Log.d("TAG", "********VALUE 2***********"+sensorEvent?.values?.get(1))
        Log.d("TAG", "********VALUE 3***********"+sensorEvent?.values?.get(2))
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}

//We dont actually need a parameter here, but we were not getting the syntax right
private class CameraFeedAnalyzer (private val activity: Activity, private val rowType: Action.Companion.ROW_TYPE?, objectDetector: ObjectDetector?) : ImageAnalysis.Analyzer {

    private val ORIENTATIONS = SparseIntArray()
    private var mActivity: Activity
    private var mRowType: Action.Companion.ROW_TYPE?
    private var mObjectDetector : ObjectDetector?

    init {
        mActivity = activity
        mRowType = rowType
        mObjectDetector = objectDetector
        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Throws(CameraAccessException::class)
    private fun getRotationCompensation(cameraId: String, activity: Activity, context: Context): Int {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        val deviceRotation = activity.windowManager.defaultDisplay.rotation
        var rotationCompensation = ORIENTATIONS.get(deviceRotation)

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        val cameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
        val sensorOrientation = cameraManager
            .getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SENSOR_ORIENTATION)!!
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360

        return rotationCompensation
    }


    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
    /*    val byteBuffer = imageProxy.planes[0].buffer
        val byteArray = byteBuffer.toByteArray()
        //val pixels = data.map { it.toInt() and 0xFF }
        //val luma = pixels.average()
        imageProxy.close()      */

        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            if (rowType == Action.Companion.ROW_TYPE.CAMERA_OBJECT_DETECTION) {
                mObjectDetector?.process(image)
                    ?.addOnSuccessListener { detectedObjects ->
                        Log.d(rowType.toString(), "*******SUCCESS***********"+detectedObjects.size)
                        for (detectedObject in detectedObjects) {
                            val boundingBox = detectedObject.boundingBox
                            val trackingId = detectedObject.trackingId
                            for (label in detectedObject.labels) {
                                val text = label.text
                                Toast.makeText(mActivity, text,
                                    Toast.LENGTH_LONG).show()
                                (mActivity?.application as? StarsEarthApplication)?.sayThis(text)
                                if (PredefinedCategory.FOOD == text) {

                                }
                                val index = label.index
                                if (PredefinedCategory.FOOD_INDEX == index) {

                                }
                                val confidence = label.confidence
                            }
                        }
                    }
                    ?.addOnFailureListener { e ->
                        Log.d("TAG", "*******FAILED WITH ERROR: " + e.message)
                        Toast.makeText(mActivity, "ERROR: "+e.message,
                            Toast.LENGTH_LONG).show()
                    }
                    ?.addOnCompleteListener {
                        mediaImage.close()
                        imageProxy.close()
                    }
            }
            else {
                // Pass image to an ML Kit Vision API
                val recognizer = TextRecognition.getClient()
                val result = recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        // Task completed successfully
                        Log.d("TAG", "*********SUCCESSFULLY CREATED********")
                        val txt = visionText.text
                        for (block in visionText.textBlocks) {
                            val blockText = block.text
                            val blockCornerPoints = block.cornerPoints
                            val blockFrame = block.boundingBox
                            for (line in block.lines) {
                                val lineText = line.text
                                val lineCornerPoints = line.cornerPoints
                                val lineFrame = line.boundingBox
                                for (element in line.elements) {
                                    val elementText = element.text
                                    val elementCornerPoints = element.cornerPoints
                                    val elementFrame = element.boundingBox
                                }
                            }
                        }
                        if (txt.isBlank() == false) {
                            val bundle = Bundle()
                            bundle.putString("text", txt)
                            val intent = Intent()
                            intent.putExtras(bundle)
                            mActivity.setResult(Activity.RESULT_OK, intent)
                            mActivity.finish()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        Log.d("TAG", "*******TEXT RECOGNITION FAILED********"+e.message)
                    }
                    .addOnCompleteListener {
                        //This ensures that the analyzer keeps getting frames. If not, it will only be called once
                        //Reference: https://stackoverflow.com/questions/56214555/android-mlkit-internal-error-has-occurred-when-executing-firebase-ml-tasks
                        mediaImage.close()
                        imageProxy.close()
                    }
            }


        }


    /*    val image = InputImage.fromByteBuffer(
            byteBuffer,
            /* image width */ 480,
            /* image height */ 360,
            imageProxy.imageInfo.rotationDegrees,
            InputImage.IMAGE_FORMAT_NV21 // or IMAGE_FORMAT_YV12
        )

        // Pass image to an ML Kit Vision API
        val recognizer = TextRecognition.getClient()
        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                val txt = visionText.text
                Log.d("TAG", "*******TEXT IS: " + txt)
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Log.d("TAG", "********FAILED**********"+e.message)
            }   */

    }
}

/*
private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    override fun analyze(image: ImageProxy) {

        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()

        listener(luma)

        image.close()
    }
}
*/