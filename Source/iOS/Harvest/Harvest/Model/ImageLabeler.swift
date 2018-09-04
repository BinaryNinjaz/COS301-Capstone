//
//  ImageLabeler.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/09/04.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Firebase

struct ImageLabeler {
  init() {
    let options = VisionLabelDetectorOptions(confidenceThreshold: 0.5)
    let vision = Vision.vision()
    let labelDetector = vision.labelDetector(options: options)
    let image = VisionImage(image: #imageLiteral(resourceName: "Farms"))
    
    labelDetector.detect(in: image) { (labels, err) in
      if let err = err {
        print(err.localizedDescription)
        return
      }
      
      guard let labels = labels else {
        print("No Labels")
        return
      }
      
      for label in labels {
        print(label.confidence, label.label)
      }
    }
  }
}
