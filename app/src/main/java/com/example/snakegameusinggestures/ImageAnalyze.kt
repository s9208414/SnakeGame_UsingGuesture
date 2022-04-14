package com.example.snakegameusinggestures

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.snakegameusinggestures.ml.SignatureModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.ByteArrayOutputStream

class ImageAnalyze(context: Context) : ImageAnalysis.Analyzer {

    private lateinit var listener: OnAnalyzeListener
    private var lastAnalyzedTimestamp = 0L
    lateinit var bitmap: Bitmap
    private var context = context
    val fileName="signature_labels.txt"
    val inputString = context.applicationContext.assets.open(fileName).bufferedReader().use { it.readText() }

    val townList=inputString.split("\n")


    val model = SignatureModel.newInstance(context)

    interface OnAnalyzeListener {
        fun getAnalyzeResult(inferredCategory: String, score: Float)
    }
    override fun analyze(image: ImageProxy, rotationDegrees: Int) {
        //bitmap= MediaStore.Images.Media.getBitmap(context.contentResolver,image.image.toBitmap())


        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >= 0.5){
            lastAnalyzedTimestamp = currentTimestamp
            try {
                bitmap = image.toBitmap()
            }catch (e: Exception){
                Log.e("bitmap",e.printStackTrace().toString())
                Log.e("image", image.image?.format.toString())
            }


            var resized:Bitmap=Bitmap.createScaledBitmap(bitmap,224,224,true)
            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.UINT8)
            var tbuffer= TensorImage.fromBitmap(resized)
            var byteBuffer=tbuffer.buffer
            inputFeature0.loadBuffer(byteBuffer)
            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            var max=getMax(outputFeature0.floatArray)
            var sv = ShareVar()
            sv.setLabel(max)
            val inferredCategory = townList[max]
            listener.getAnalyzeResult(inferredCategory, 0.0.toFloat())  // Viewを更新
        }

    }





    //tv.setText(townList[max])
    fun getMax(arr: FloatArray):Int{
        var ind=0
        var min=0.0f
        Log.e("arr",arr.toString())
        for(i in 0..arr.size-1){
            if (arr[i]>min){
                ind=i
                min= arr[i]
            }
        }
        return  ind
    }
    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }


    fun setOnAnalyzeListener(listener: OnAnalyzeListener){
        this.listener = listener
    }
}