//
//  QuickAlert.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/26.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

extension UIAlertController {
  static func alertController(
    title: String,
    message: String
  ) -> UIAlertController {
    let alert = UIAlertController(
      title: title,
      message: message,
      preferredStyle: .alert)
    
    let okay = UIAlertAction(title: "Okay", style: .default, handler: nil)
    
    alert.addAction(okay)
    
    return alert
  }
  
  static func present(
    title: String,
    message: String,
    on controller: UIViewController?,
    completion: (() -> Void)? = nil
  ) {
    if let controller = controller {
      let alert = UIAlertController.alertController(title: title, message: message)
      controller.present(alert, animated: true, completion: completion)
    }
  }
  
  static func present(
    title: String,
    message: String,
    options: [(display: String, uid: String)],
    on controller: UIViewController?,
    completion: @escaping (String) -> Void
  ) {
    let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
    
    for option in options {
      let button = UIAlertAction(title: option.display, style: .default) { _ in
        completion(option.uid)
      }
      alert.addAction(button)
    }
    
    controller?.present(alert, animated: true, completion: nil)
  }
}
