package com.cse535.native_app1

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
//import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.WorkerThread
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
//import com.cse535.native_app1.databinding.ActivityMainBinding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {

    //private lateinit var binding: ActivityMainBinding
    val selectedImageUris = mutableListOf<Uri>()

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("i am here", "2")
        if (resultCode == RESULT_OK && data != null) {
            //val selectedImageUris = mutableListOf<Uri>()

            if (data.data != null) {
                // Single image selected
                selectedImageUris.add(data.data!!)
            } else {
                // Multiple images selected
                val clipData = data.clipData
                clipData?.let {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        selectedImageUris.add(uri)
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("i am here", "0")
        super.onCreate(savedInstanceState)

        setContent{
            Log.d("i am here", "1")
            //val txt: String = stringFromJNI()
            //val txt = "Hello from Kotlin"
            var selectedImageUris1 = remember { mutableListOf<Uri>() }
            Button(onClick = {
                //selectedImageUris.clear()
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.type = "image/*"
                startActivityForResult(intent, 1)
                selectedImageUris1 = selectedImageUris
                for (uri in selectedImageUris1) {
                    Log.d("i am here", uri.toString())
                }

            }){
                Text("Select Image")
            }
            if (selectedImageUris1.isNotEmpty()){
                Log.d("i am here", "5")
                val uri = selectedImageUris1[0]
                val c = this
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(c.getContentResolver(), uri)
                Image(bitmap.asImageBitmap(), contentDescription = "Image")
            }
        }
    }

    /**
     * A native method that is implemented by the 'native_app1' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String
    external fun resizeImage(imgArray: Array<ByteArray>, destSize: Int) : ByteArray
    companion object {
        // Used to load the 'native_app1' library on application startup.
        init {
            System.loadLibrary("native_app1")
        }
    }
}


@Composable
fun ClassifierApp(){

}


fun uriToByteArray(uri: Uri, context: Context): ByteArray? {
    val inputStream = context.contentResolver.openInputStream(uri)
    val byteArrayOutputStream = ByteArrayOutputStream()
    val bufferSize = 1024
    val buffer = ByteArray(bufferSize)
    var len: Int

    try {
        while (inputStream?.read(buffer).also { len = it ?: -1 } != -1) {
            byteArrayOutputStream.write(buffer, 0, len)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        inputStream?.close()
    }

    return byteArrayOutputStream.toByteArray()
}


@Composable
fun ImageFromUri(uri: Uri, contentDescription: String = "") {
    Log.d("i am here", "3")
    val context = LocalContext.current
    val bitmap = produceState<Bitmap?>(initialValue = null) {
        value = uriToBitmap(uri, context)
    }.value
    Log.d("i am here", "4")
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit
        )
    }
}

@WorkerThread
fun uriToBitmap(uri: Uri, context: Context): Bitmap? {
    val inputStream = context.contentResolver.openInputStream(uri)
    return inputStream?.use {
        BitmapFactory.decodeStream(it)
    }
}

