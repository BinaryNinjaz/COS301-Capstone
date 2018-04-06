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
  
  enum Bootstrap {
    static var green: [UIColor] {
      let s = UIColor(hue: 109.0 / 360.0, saturation: 0.44, brightness: 0.71, alpha: 1)
      let e = UIColor(hue: 109.0 / 360.0, saturation: 0.49, brightness: 0.58, alpha: 1)
      
      return [s, e]
    }
    
    static var blue: [UIColor] {
      let s = UIColor(hue: 211.0 / 360.0, saturation: 0.61, brightness: 0.69, alpha: 1)
      let e = UIColor(hue: 211.0 / 360.0, saturation: 0.61, brightness: 0.52, alpha: 1)
      
      return [s, e]
    }
    
    static var orange: [UIColor] {
      let s = UIColor.tangerine
      let e = UIColor(hue: 35.0 / 360.0, saturation: 0.7, brightness: 0.8, alpha: 1)
      
      return [s, e]
    }
  }
  
  
}

extension CAGradientLayer {
  static func gradient(
    colors: [UIColor],
    locations: [NSNumber],
    cornerRadius: CGFloat,
    borderColor: UIColor
  ) -> CAGradientLayer {
    let gradientLayer = CAGradientLayer()
    gradientLayer.colors = colors.map { $0.cgColor }
    gradientLayer.locations = locations
    gradientLayer.cornerRadius = cornerRadius
    gradientLayer.borderWidth = 1
    gradientLayer.borderColor = borderColor.cgColor
    
    return gradientLayer
  }
  
  static var green: CAGradientLayer {
    return gradient(colors: UIColor.Bootstrap.green,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: UIColor.Bootstrap.green[1])
  }
  
  static var blue: CAGradientLayer {
    return gradient(colors: UIColor.Bootstrap.blue,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: UIColor.Bootstrap.blue[1])
  }
  
  static var orange: CAGradientLayer {
    return gradient(colors: UIColor.Bootstrap.orange,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: UIColor.Bootstrap.orange[1])
  }
}

extension UIView {
  func apply(gradient: CAGradientLayer) {
    layer.sublayers?.remove(at: 0)
    gradient.frame = bounds
    layer.insertSublayer(gradient, at: 0)
  }
}
