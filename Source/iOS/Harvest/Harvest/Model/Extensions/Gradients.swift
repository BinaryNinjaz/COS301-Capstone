//
//  Gradients.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/23.
//  Copyright © 2018 University of Pretoria. All rights reserved.
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
    gradientLayer.masksToBounds = true
    gradientLayer.colors = colors.map { $0.cgColor }
    gradientLayer.locations = locations
    gradientLayer.cornerRadius = cornerRadius
    gradientLayer.borderWidth = 1
    gradientLayer.borderColor = borderColor.cgColor
    
    return gradientLayer
  }
  
  static var startSession: CAGradientLayer {
    return gradient(colors: .startSession,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: [UIColor].startSession[1])
  }
  
  static var registerWithHarvest: CAGradientLayer {
    return gradient(colors: .registerWithHarvest,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: [UIColor].registerWithHarvest[1])
  }
  
  static var stopSession: CAGradientLayer {
    return gradient(colors: .stopSession,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: [UIColor].stopSession[1])
  }
  
  static var deleteAllButton: CAGradientLayer {
    return gradient(colors: .deleteAllButton,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: [UIColor].deleteAllButton[1])
  }
  
  static var deleteButton: CAGradientLayer {
    return gradient(colors: .deleteButton,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: [UIColor].deleteButton[1])
  }
  
  static var disabledButton: CAGradientLayer {
    return gradient(colors: .disabledButton,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: [UIColor].disabledButton[1])
  }
  
  static var sessionTiles: CAGradientLayer {
    return gradient(colors: .sessionTiles,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: [UIColor].sessionTiles[1])
  }
  
  static var sessionTilesText: CAGradientLayer {
    return gradient(colors: .signUpButton,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: [UIColor].sessionTilesText[1])
  }
  
  static var signInButton: CAGradientLayer {
    return gradient(colors: .signInButton,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: [UIColor].signInButton[1])
  }
  
  static var signUpButton: CAGradientLayer {
    return gradient(colors: .signUpButton,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: [UIColor].signUpButton[1])
  }
  
  static var googleSignInButton: CAGradientLayer {
    return gradient(colors: .googleSignInButton,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: [UIColor].googleSignInButton[1])
  }
  
  static var cancelRegistrationButton: CAGradientLayer {
    return gradient(colors: .cancelRegistrationButton,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: [UIColor].cancelRegistrationButton[1])
  }
  
  static var orangeButton: CAGradientLayer {
    return gradient(colors: .deleteButton,
                    locations: [0.0, 1.0],
                    cornerRadius: 5,
                    borderColor: [UIColor].deleteButton[1])
  }
}
