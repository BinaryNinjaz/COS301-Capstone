//
//  Utils.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/29.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

extension UIViewController {
  func hideKeyboardWhenTappedAround() {
    let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(UIViewController.dismissKeyboard))
    tap.cancelsTouchesInView = false
    view.addGestureRecognizer(tap)
  }
  
  @objc func dismissKeyboard() {
    view.endEditing(true)
  }
}

extension UIColor {
  static var moss: UIColor {
    return UIColor(hue: 154.0 / 360.0, saturation: 1.0, brightness: 0.56, alpha: 1)
  }
  static var tangerine: UIColor {
    return UIColor(hue: 35.0 / 360.0, saturation: 1.0, brightness: 1.0, alpha: 1)
  }
}
