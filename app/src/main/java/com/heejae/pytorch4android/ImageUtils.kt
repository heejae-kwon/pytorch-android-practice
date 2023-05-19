package com.heejae.pytorch4android

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.photo.Photo
import java.nio.ByteBuffer


fun createRembgImage(img:Bitmap, mask:Bitmap): Bitmap? {
    val imgMat = Mat()
    val maskMat = Mat()
    Utils.bitmapToMat(img, imgMat)
    Utils.bitmapToMat(mask, maskMat)
    Imgproc.cvtColor(maskMat, maskMat, Imgproc.COLOR_RGBA2GRAY)
    Imgproc.resize(
        maskMat, maskMat, Size(img.width.toDouble(), img.height.toDouble())
    )
    val emptyMat = Mat.zeros(img.height, img.width, imgMat.type())
    val dstMat = Mat.zeros(img.height, img.width, imgMat.type())
    Imgproc.threshold(maskMat, maskMat, 0.0, 255.0, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU)
    Core.bitwise_or(imgMat, emptyMat, dstMat, maskMat)
    //  Core.add(emptyMat, imgMat, dstMat, maskResize)
    /*Photo.seamlessClone(
        imgMat,
        emptyMat,
        maskResize,
        Point((img.width / 2).toDouble(), (img.height / 2).toDouble()),
        dstMat,
        Photo.NORMAL_CLONE
    )*/
    val returnImg = Bitmap.createBitmap(img.width, img.height, Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(dstMat, returnImg)
    return returnImg
}
fun floatArrayToGrayscaleBitmap (
    floatArray: FloatArray,
    width: Int,
    height: Int,
    alpha :Byte = (255).toByte(),
    reverseScale :Boolean = false
) : Bitmap {

    // Create empty bitmap in RGBA format (even though it says ARGB but channels are RGBA)
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val byteBuffer = ByteBuffer.allocate(width * height * 4)

    // mapping smallest value to 0 and largest value to 255
    val maxValue = floatArray.maxOrNull() ?: 1.0f
    val minValue = floatArray.minOrNull() ?: 0.0f
    val delta = maxValue - minValue
    var tempValue: Byte

    // Define if float min..max will be mapped to 0..255 or 255..0
    val conversion = when (reverseScale) {
        false -> { v: Float -> ((v - minValue) / delta * 255).toInt().toByte() }
        true -> { v: Float -> (255 - (v - minValue) / delta * 255).toInt().toByte() }
    }

    // copy each value from float array to RGB channels and set alpha channel
    floatArray.forEachIndexed { i, value ->
        tempValue = conversion(value)
        byteBuffer.put(4 * i, tempValue)
        byteBuffer.put(4 * i + 1, tempValue)
        byteBuffer.put(4 * i + 2, tempValue)
        byteBuffer.put(4 * i + 3, alpha)
    }

    bmp.copyPixelsFromBuffer(byteBuffer)

    return bmp
}
