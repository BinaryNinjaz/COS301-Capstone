//
//  ImageLabeler.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/09/04.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Firebase
import Vision
import CoreML
import ImageIO

class ImageLabeler: NSObject {
  let options: VisionLabelDetectorOptions
  let vision: Vision
  let labelDetector: VisionLabelDetector
  weak var controller: UIViewController?
  var completion: ((String, Float) -> Void)?
  
  var intepreter: ModelInterpreter?
  
  init(on controller: UIViewController) {
    options = VisionLabelDetectorOptions(confidenceThreshold: 0.5)
    vision = Vision.vision()
    labelDetector = vision.labelDetector(options: options)
    
    self.controller = controller
    
    guard let modelPath = Bundle.main.path(forResource: "optimized_graph", ofType: "tflite") else {
      return
    }
    let localModelSource = LocalModelSource(modelName: "optimized_flowers", path: modelPath)
    _ = ModelManager.modelManager().register(localModelSource)
    let modelOptions = ModelOptions(cloudModelName: nil, localModelName: "optimized_flowers")
    
    intepreter = ModelInterpreter.modelInterpreter(options: modelOptions)
  }
  
  /// - Tag: MLModelSetup
  @available(iOS 11.0, *)
  lazy var classificationRequest: VNCoreMLRequest = {
    do {
      /*
       Use the Swift class `MobileNet` Core ML generates from the model.
       To use a different Core ML classifier model, add it to the project
       and replace `MobileNet` with that model's generated Swift class.
       */
      let model = try VNCoreMLModel(for: MobileNet().model)
      
      let request = VNCoreMLRequest(model: model, completionHandler: { [weak self] request, error in
        self?.processClassifications(for: request, error: error)
      })
      request.imageCropAndScaleOption = .centerCrop
      return request
    } catch {
      fatalError("Failed to load Vision ML model: \(error)")
    }
  }()
  
  @available(iOS 11.0, *)
  lazy var rectangleDetectionRequest: VNDetectRectanglesRequest = {
    let rectDetectRequest = VNDetectRectanglesRequest(completionHandler: self.handleDetectedRectangles)
    // Customize & configure the request to detect only certain rectangles.
    rectDetectRequest.maximumObservations = 15 // Vision currently supports up to 16.
    rectDetectRequest.minimumConfidence = 0.1 // Be confident.
    rectDetectRequest.minimumAspectRatio = 0.1 // height / width
    return rectDetectRequest
  }()
  
  func requestLabelsFromCamera(completion: @escaping (String, Float) -> Void) {
    let picker = UIImagePickerController()
    picker.sourceType = .camera
    picker.delegate = self
    picker.cameraDevice = .rear
    picker.allowsEditing = false
    picker.cameraCaptureMode = .photo
    self.completion = completion
    
    controller?.present(picker, animated: true, completion: nil)
  }
  
  @available(iOS 11.0, *)
  func detect(image: UIImage) {
    updateClassifications(for: image)
    
//    labelDetector.detect(in: VisionImage(image: image)) { (labels, err) in
//      if let err = err {
//        print(err.localizedDescription)
//        return
//      }
//
//      guard let labels = labels, !labels.isEmpty else {
//        print("Labels Empty")
//        return
//      }
//
//      if let best = labels.first {
//        self.completion?(best.label, best.confidence)
//      }
//
//      for label in labels {
//        print(label.confidence, label.label)
//      }
//    }
  }
}

extension ImageLabeler: UIImagePickerControllerDelegate & UINavigationControllerDelegate {
  func imagePickerController(
    _ picker: UIImagePickerController,
    didFinishPickingMediaWithInfo info: [String: Any]
  ) {
    guard let image = info[UIImagePickerControllerOriginalImage] as? UIImage else {
      return
    }
    
    if #available(iOS 11.0, *) {
      detect(image: image)
    } else {
      // Fallback on earlier versions
    }
    
    picker.dismiss(animated: true, completion: nil)
  }
  
  func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
    picker.dismiss(animated: true, completion: nil)
  }
}

@available(iOS 11.0, *)
extension ImageLabeler {
  /// - Tag: PerformRequests
  func updateClassifications(for image: UIImage) {
    let orientation = CGImagePropertyOrientation(rawValue: UInt32(image.imageOrientation.rawValue))!
    guard let ciImage = CIImage(image: image) else { fatalError("Unable to create \(CIImage.self) from \(image).") }
    
    DispatchQueue.global(qos: .userInitiated).async {
      let handler = VNImageRequestHandler(ciImage: ciImage, orientation: orientation)
      do {
        try handler.perform([self.classificationRequest, self.rectangleDetectionRequest])
      } catch {
        /*
         This handler catches general image processing errors. The `classificationRequest`'s
         completion handler `processClassifications(_:error:)` catches errors specific
         to processing that request.
         */
        print("Failed to perform classification.\n\(error.localizedDescription)")
      }
    }
  }
  
  /// Updates the UI with the results of the classification.
  /// - Tag: ProcessClassifications
  func processClassifications(for request: VNRequest, error: Error?) {
    DispatchQueue.main.async {
      guard let results = request.results else {
        print("Unable to classify image.\n\(error!.localizedDescription)")
        return
      }
      // The `results` will always be `VNClassificationObservation`s, as specified by the Core ML model in this project.
      let classifications = results as! [VNClassificationObservation]
      
      if classifications.isEmpty {
        print("Nothing recognized.")
      } else {
        // Display top classifications ranked by confidence in the UI.
        let topClassifications = classifications.prefix(2)
        let descriptions = topClassifications.map { classification -> String in
          // Formats the classification for display; e.g. "(0.37) cliff, drop, drop-off".
          self.completion?(classification.identifier, classification.confidence)
          return String(format: "  (%.2f) %@", classification.confidence, classification.identifier)
        }
        print("Classification:\n" + descriptions.joined(separator: "\n"))
      }
    }
  }
  
  /// -Tag: Count Rects
  fileprivate func handleDetectedRectangles(request: VNRequest?, error: Error?) {
    if let nsError = error as NSError? {
      print("Rectangle Detection Error: ", nsError)
      return
    }
    // Since handlers are executing on a background thread, explicitly send draw calls to the main thread.
    DispatchQueue.main.async {
      guard let results = request?.results as? [VNRectangleObservation] else {
          return
      }
      self.completion?("", Float(results.count))
    }
  }
}
