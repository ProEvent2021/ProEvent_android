package ru.myproevent.ui.views

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import com.yalantis.ucrop.UCrop
import java.io.File

class CropImageHandler(
    private val viewOnClick: View,
    private val pickImageCallback:
        (pickImageActivityContract: ActivityResultContract<Any?, Uri?>, cropActivityResultLauncher: ActivityResultLauncher<Uri>)
    -> ActivityResultLauncher<Any?>,
    private val cropCallback: (cropActivityContract: ActivityResultContract<Uri, Uri?>) -> ActivityResultLauncher<Uri>,
    private val isCircle: Boolean
) {
    private lateinit var pickImageActivityResultLauncher: ActivityResultLauncher<Any?>
    private val pickImageActivityContract = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return Intent(Intent.ACTION_GET_CONTENT)
                .setType(PICK_IMAGE_INTENT_TYPE)
                .addCategory(Intent.CATEGORY_OPENABLE)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return intent?.data
        }
    }
    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Uri>
    private val cropActivityContract = object : ActivityResultContract<Uri, Uri?>() {
        override fun createIntent(context: Context, input: Uri): Intent {
            val destinationName =
                Uri.fromFile(File.createTempFile(PREFIX_TEMP_FILE, SUFFIX_CROP_FILE))
            return buildCrop(input, destinationName, context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return intent?.let { UCrop.getOutput(it) }
        }
    }

    private fun buildCrop(input: Uri, destinationName: Uri, context: Context): Intent {
        return if (isCircle) {
            UCrop.of(input, destinationName)
                .withAspectRatio(CIRCLE_SCALE_X, CIRCLE_SCALE_Y)
                .withOptions(setOptions())
                .getIntent(context)
        } else {
            UCrop.of(input, destinationName)
                .withOptions(setOptions())
                .getIntent(context)
        }
    }

    private fun setOptions(): UCrop.Options {
        val options = UCrop.Options()
        options.setCircleDimmedLayer(isCircle)
        options.setShowCropFrame(false)
        options.setShowCropGrid(false)
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)
        return options
    }

    fun init() {
        cropActivityResultLauncher = cropCallback(cropActivityContract)
        pickImageActivityResultLauncher =
            pickImageCallback(pickImageActivityContract, cropActivityResultLauncher)
        viewOnClick.setOnClickListener { pickImageActivityResultLauncher.launch(null) }
    }

    companion object {
        const val CIRCLE_SCALE_X = 1f
        const val CIRCLE_SCALE_Y = 1f
        const val PREFIX_TEMP_FILE = "temp"
        const val SUFFIX_CROP_FILE = "crop"
        const val PICK_IMAGE_INTENT_TYPE = "image/*"
    }
}
