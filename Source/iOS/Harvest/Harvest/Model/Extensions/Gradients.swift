//
//  Gradients.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/23.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

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
  
  static var google: CAGradientLayer {
    return gradient(colors: UIColor.Bootstrap.google,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: UIColor.Bootstrap.google[1])
  }
  
  static var disabled: CAGradientLayer {
    return gradient(colors: UIColor.Bootstrap.disabled,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: UIColor.Bootstrap.disabled[1])
  }
}