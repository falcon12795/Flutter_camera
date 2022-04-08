import 'dart:developer';
import 'dart:io';

import 'package:flutter/material.dart';

import 'package:flutter/services.dart';
import 'package:flutter_camera/flutter_camera.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _imageUri="";
  @override
  void initState() {
    super.initState();
  }
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Camera'),
        ),
        body: SingleChildScrollView(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children:[
              _imageUri==""?
              const SizedBox.shrink():
                  SizedBox(
                    height: 200,
                    width: 200,
                    child: Image.file(File(_imageUri),fit: BoxFit.fill,),
                  ),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  ElevatedButton(onPressed:_getImageFromGallery ,child:const Text("Device"),),
                  ElevatedButton(onPressed: _getImageFromCamera,child:const Text("Camera"),),
                ],
              )],
          ),
        )
      ),
    );
  }
  void _getImageFromGallery() async {
    String? imageUri;
    try {
      imageUri = await FlutterCamera.getImageFromGallery;
    } on PlatformException catch (platEx) {
      log("_getImageFromGallery/PlatformException  $platEx");
    } catch (ex) {
      log("_getImageFromGallery/Exception  $ex");
    }
    setState(() {
      _imageUri = imageUri??"";
    });
  }
  void _getImageFromCamera() async {
    String? imageUri;
    try {
      imageUri = await FlutterCamera.getImageFromCamera;
    } on PlatformException catch (platEx) {
      log("_takeImageFromCamera/PlatformException  $platEx");
    } catch (ex) {
      log("_takeImageFromCamera/Exception $ex");
    }
    setState(() {
      _imageUri = imageUri??"";
    });
  }

}
