
import 'dart:async';

import 'package:flutter/services.dart';

class FlutterCameraFalcon {
  static const MethodChannel _channel = MethodChannel('flutter_camera');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
  static Future<String?> get getImageFromGallery async {
    final imageUri = await _channel.invokeMethod("getImageFromGallery");
    return imageUri;
  }
  static Future<String?> get getImageFromCamera async {
    final imageUri = await _channel.invokeMethod("getImageFromCamera");
    return imageUri;
  }
}
