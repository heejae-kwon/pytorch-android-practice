package com.heejae.pytorch4android

import android.graphics.Bitmap
import java.nio.ByteBuffer

fun floatArrayToGrayscaleBitmap (
    floatArray: FloatArray,
    width: Int,
    height: Int,
    alpha :Byte = (255).toByte(),
    reverseScale :Boolean = false
) : Bitmap {

    // Create empty bitmap in RGBA format (even though it says ARGB but channels are RGBA)
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val byteBuffer = ByteBuffer.allocate(width*height*4)

    // mapping smallest value to 0 and largest value to 255
    val maxValue = floatArray.maxOrNull() ?: 1.0f
    val minValue = floatArray.minOrNull() ?: 0.0f
    val delta = maxValue-minValue
    var tempValue :Byte

    // Define if float min..max will be mapped to 0..255 or 255..0
    val conversion = when(reverseScale) {
        false -> { v: Float -> ((v-minValue)/delta*255).toInt().toByte() }
        true -> { v: Float -> (255-(v-minValue)/delta*255).toInt().toByte() }
    }

    // copy each value from float array to RGB channels and set alpha channel
    floatArray.forEachIndexed { i, value ->
        tempValue = conversion(value)
        byteBuffer.put(4*i, tempValue)
        byteBuffer.put(4*i+1, tempValue)
        byteBuffer.put(4*i+2, tempValue)
        byteBuffer.put(4*i+3, alpha)
    }

    bmp.copyPixelsFromBuffer(byteBuffer)

    return bmp
}
