package com.example.snakegameusinggestures

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.snakegameusinggestures.callbacks.OnCrashListener
import com.example.snakegameusinggestures.callbacks.OnEatenFoodListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors

private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity(), LifecycleOwner {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var viewFinder: TextureView
    var point = 0
    var isStartCam = false
    lateinit var bitmap: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val fileName="signature_label.txt"
        viewFinder = findViewById(R.id.view_finder)

        // 相機開啟和遊戲開始
        activateCameraBtn.setOnClickListener {
            start()
            viewFinder.bringToFront()
            //viewFinder.post { startCamera() }
        }
        if (allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }


        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
        snake.eatenListener = object : OnEatenFoodListener() {
            override fun onEaten() {
                point += 1
                //tvPoint.text = point.toString()
            }
        }

        snake.crashListener = object : OnCrashListener() {
            override fun onCrash() {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("蛇蛇掛了!")
                    .setMessage("你想怎麼做?")
                    .setPositiveButton("重新開始", DialogInterface.OnClickListener { dialog, which ->
                        snake.restart()
                        dialog.dismiss()
                    })
                    .setNegativeButton("離開遊戲", DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        finish()
                    }).show()
            }

        }
    }

    private fun startCamera() {

        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(viewFinder.width, viewFinder.height))
        }.build()

        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener {
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)
            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE
            )
        }.build()

        val imageAnalyzer = ImageAnalyze(applicationContext)
        //顯示模型推論結果
        imageAnalyzer.setOnAnalyzeListener(object : ImageAnalyze.OnAnalyzeListener {
            override fun getAnalyzeResult(inferredCategory: String, score: Float) {
                viewFinder.post {
                    inferredCategoryText.text = "推論結果: $inferredCategory"
                    Log.e("推論結果",inferredCategory)
                    move(inferredCategory)
                }
            }
        })
        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            setAnalyzer(executor, imageAnalyzer)
        }

        CameraX.bindToLifecycle(this, preview, analyzerUseCase)
        isStartCam = true


    }

    private fun updateTransform() {
        val matrix = Matrix()
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        val rotationDegrees = when (viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        viewFinder.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }
    fun start() {
        if (!snake.isStarted) {
            snake.start(viewFinder)
        }
    }

    fun move(result: String) {

        when(result){
            "up" -> {
                snake.direction = SnakeGameView.DIRECTION.DIRECTION_UP
                viewFinder.bringToFront()
            }
            "down" -> {
                snake.direction = SnakeGameView.DIRECTION.DIRECTION_DOWN
                viewFinder.bringToFront()
            }
            "left" -> {
                snake.direction = SnakeGameView.DIRECTION.DIRECTION_LEFT
                viewFinder.bringToFront()
            }
            "right" -> {
                snake.direction = SnakeGameView.DIRECTION.DIRECTION_RIGHT
                viewFinder.bringToFront()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        snake.isRunning = false
    }
}
