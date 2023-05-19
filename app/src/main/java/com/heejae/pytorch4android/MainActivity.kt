package com.heejae.pytorch4android


import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import com.heejae.pytorch4android.ui.theme.Pytorch4androidTheme
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.imgproc.Imgproc
import org.opencv.osgi.OpenCVInterface
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import java.io.File
import java.io.FileOutputStream
import java.io.IOException



class MainActivity : ComponentActivity() {
    
    init {
        val isIntialized = OpenCVLoader.initDebug()
        val d = Log.d(TAG, "isIntialized = $isIntialized")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Pytorch4androidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(context = this)
                }
            }
        }
    }
}

fun assetFilePath(context: Context, assetName: String): String? {
    val file = File(context.filesDir, assetName)
    if (file.exists() && file.length() > 0) {
        return file.absolutePath
    }
    try {
        context.assets.open(assetName).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }
    } catch (e: IOException) {
        Log.e(TAG, "Error process asset $assetName to file path")
    }
    return null
}

@Composable
fun App(context: Context) {
    val (mainImg, setMainImg) = remember {
        val fileIs = context.assets.open("prof1.jpg")
        mutableStateOf<Bitmap?>(BitmapFactory.decodeStream(fileIs))
    }
    val (module, setModule) = remember {
        val mModule = LiteModuleLoader.load(assetFilePath(context, "u2net.ptl"))
        mutableStateOf<Module?>(mModule)
    }
    MaterialTheme {
        Greeting("Android")
        RembgButton(onclick = {
            if (mainImg != null && module != null) {
                val outputs = runInference(mainImg, module)
                val maskImg = floatArrayToGrayscaleBitmap(outputs, 320, 320)
                val rembgImg = createRembgImage(mainImg, maskImg)
                setMainImg(rembgImg)
            }
        })
        if (mainImg != null) {
            ModelTestImage(image = mainImg.asImageBitmap())
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    MaterialTheme {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }
}

@Composable
fun RembgButton(onclick : ()->Unit) {
    MaterialTheme {
        Button(onClick = { onclick() }) {
            Text(text = "Simple Button")
        }
    }
}

@Composable
fun ModelTestImage(image: ImageBitmap) {
    MaterialTheme {
        Image(bitmap = image, contentDescription = null)
    }
}
