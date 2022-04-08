package com.example.flutter_camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.NonNull
import com.google.gson.Gson

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

/** FlutterCameraPlugin */
class FlutterCameraPlugin: FlutterPlugin, MethodCallHandler, ActivityAware,
  PluginRegistry.ActivityResultListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private var activity: Activity? = null
  private lateinit var result: Result
  private lateinit var permissionManager: PermissionManager

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_camera")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    this.result= result;
    permissionManager= PermissionManager(activity);
    when(call.method){
      "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
      "getImageFromGallery" -> {
        val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
        imagePickerIntent.type = "image/*"
        activity?.startActivityForResult(
          Intent.createChooser(
            imagePickerIntent,
            "Select Picture"
          ), PICK_IMAGE_RESULT_CODE
        )
      }
      "getImageFromCamera" -> {
        permissionManager.checkPermissionGranted(
          arrayOf(Manifest.permission.CAMERA),
          CAMERA_REQUEST_CODE
        ) {
          val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
          activity?.startActivityForResult(takePictureIntent, TAKE_IMAGE_RESULT_CODE)
        }
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
    if (resultCode == Activity.RESULT_OK) {
      when (requestCode) {
        PICK_IMAGE_RESULT_CODE -> {
          data?.let {
            val uri: Uri? = it.data
            uri?.let {
              activity?.let { act ->
                val file = Utils.createFileFromUri(
                  act.contentResolver,
                  uri,
                  act.cacheDir
                )
                result.success(file.path.toString())
              }
            }
          }
        }
        PICK_MULTI_IMAGE_RESULT_CODE -> {
          data?.clipData?.let {
            val count = it.itemCount
            val listPath = arrayListOf<Any>()
            val gson = Gson()
            for (i in 0 until count) {
              val uri = it.getItemAt(i).uri
              uri?.let {
                activity?.let { act ->
                  val file = Utils.createFileFromUri(
                    act.contentResolver,
                    uri,
                    act.cacheDir
                  )
                  listPath.add(gson.toJson(file.path.toString()))
                }
              }
            }
            result.success(listPath)
          }
        }
        TAKE_IMAGE_RESULT_CODE -> {
          data?.let {
            val imageBitmap = it.extras?.get("data") as Bitmap
            activity?.cacheDir?.let { it1 ->
              Utils.storeImageToAppCache(imageBitmap, it1) { path ->
                result.success(path)
              }
            }
          }
        }
      }
    }
    return false
  }

  companion object {
    const val PICK_CONTACT_REQUEST_CODE = 54324
    const val PICK_IMAGE_RESULT_CODE = 71243
    const val PICK_MULTI_IMAGE_RESULT_CODE = 71244
    const val TAKE_IMAGE_RESULT_CODE = 81287
    const val CAMERA_REQUEST_CODE = 34231
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
    binding.addActivityResultListener(this)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity=null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activity = binding.activity
    binding.addActivityResultListener(this)
  }

  override fun onDetachedFromActivity() {
    activity=null
  }
}
