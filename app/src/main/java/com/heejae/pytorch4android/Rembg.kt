package com.heejae.pytorch4android

import android.graphics.Bitmap
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils


fun preprocessInput(bitmap: Bitmap, IMAGE_SIZE: Int): Tensor {
    // Resize the bitmap image to the desired size
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true)
    return TensorImageUtils.bitmapToFloat32Tensor(
        resizedBitmap,
        TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
        TensorImageUtils.TORCHVISION_NORM_STD_RGB
    )
}

fun runInference(mBitmap : Bitmap, mModule: Module): FloatArray {

    val inputTensor = preprocessInput(mBitmap, 320)
    val intputs = inputTensor.dataAsFloatArray
    val outTensors =
        mModule.forward(IValue.from(inputTensor))
    return outTensors!!.toTuple()[0].toTensor().dataAsFloatArray
}
