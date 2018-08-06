//
//  ColorScheme.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/23.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

extension Array where Element == UIColor {
  static var startSession: [UIColor] {
    let s = #colorLiteral(red: 0, green: 0.8211187066, blue: 0.3284474826, alpha: 1)
    let e = #colorLiteral(red: 0, green: 0.71, blue: 0.1775, alpha: 1)
    
    return [s, e]
  }
  
  static var registerWithHarvest: [UIColor] {
    let s = #colorLiteral(red: 0, green: 0.4823529412, blue: 1, alpha: 1)
    let e = #colorLiteral(red: 0, green: 0.3870704402, blue: 0.8024631076, alpha: 1)
    
    return [s, e]
  }
  
  static var stopSession: [UIColor] {
    let s = #colorLiteral(red: 0.9254902005, green: 0.4841795829, blue: 0.1019607857, alpha: 1)
    let e = #colorLiteral(red: 0.7450980544, green: 0.3913085078, blue: 0.07450980693, alpha: 1)
    
    return [s, e]
  }
  
  static var deleteAllButton: [UIColor] {
    let s = #colorLiteral(red: 1, green: 0.1619047607, blue: 0, alpha: 1)
    let e = #colorLiteral(red: 0.7534722222, green: 0.09253167753, blue: 0, alpha: 1)
    
    return [s, e]
  }
  
  static var deleteButton: [UIColor] {
    let s = #colorLiteral(red: 1, green: 0.5298576868, blue: 0, alpha: 1)
    let e = #colorLiteral(red: 0.7534722222, green: 0.4043834145, blue: 0, alpha: 1)
    
    return [s, e]
  }
  
  static var sessionTiles: [UIColor] {
    let s = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 1)
    let e = #colorLiteral(red: 0.9479004608, green: 0.9479004608, blue: 0.9479004608, alpha: 1)
    
    return [s, e]
  }
  
  static var sessionTilesText: [UIColor] {
    let s = #colorLiteral(red: 0, green: 0.8211187066, blue: 0.3284474826, alpha: 1)
    let e = #colorLiteral(red: 0, green: 0.71, blue: 0.1775, alpha: 1)
    
    return [s, e]
  }
  
  static var signInButton: [UIColor] {
    let s = #colorLiteral(red: 0.1568627451, green: 0.6549019608, blue: 0.2705882353, alpha: 1)
    let e = #colorLiteral(red: 0.1568627451, green: 0.5098039216, blue: 0.2705882353, alpha: 1)
    
    return [s, e]
  }
  
  static var signUpButton: [UIColor] {
    let s = #colorLiteral(red: 0, green: 0.4823529412, blue: 1, alpha: 1)
    let e = #colorLiteral(red: 0, green: 0.3870704402, blue: 0.8024631076, alpha: 1)
    
    return [s, e]
  }
  
  static var googleSignInButton: [UIColor] {
    let s = #colorLiteral(red: 1, green: 0.2216981132, blue: 0.1933962264, alpha: 1)
    let e = #colorLiteral(red: 0.831372549, green: 0.1843137255, blue: 0.1607843137, alpha: 1)
    
    return [s, e]
  }
  
  static var cancelRegistrationButton: [UIColor] {
    let s = #colorLiteral(red: 0.1568627451, green: 0.6549019608, blue: 0.2705882353, alpha: 1)
    let e = #colorLiteral(red: 0.1568627451, green: 0.5098039216, blue: 0.2705882353, alpha: 1)
    
    return [s, e]
  }
  
  static var disabledButton: [UIColor] {
    let s = #colorLiteral(red: 0.6000000238, green: 0.6000000238, blue: 0.6000000238, alpha: 1)
    let e = #colorLiteral(red: 0.501960814, green: 0.501960814, blue: 0.501960814, alpha: 1)
    
    return [s, e]
  }
}

extension UIColor {
  static var harvestGreen: UIColor {
    return #colorLiteral(red: 0, green: 0.71, blue: 0.1775, alpha: 1)
  }
  static var addOrchard: UIColor {
    return #colorLiteral(red: 0, green: 0.5898008943, blue: 1, alpha: 1)
  }
  static var invalidInput: UIColor {
    return #colorLiteral(red: 1, green: 0.7601397671, blue: 0.7510166566, alpha: 1)
  }
  
  static func color(
    between p: UIColor,
    and q: UIColor,
    atFraction a: CGFloat
  ) -> UIColor {
    var pr: CGFloat = 0.0,
    pg: CGFloat = 0.0,
    pb: CGFloat = 0.0,
    pa: CGFloat = 0.0
    
    p.getRed(&pr, green: &pg, blue: &pb, alpha: &pa)
    
    var qr: CGFloat = 0.0,
    qg: CGFloat = 0.0,
    qb: CGFloat = 0.0,
    qa: CGFloat = 0.0
    
    q.getRed(&qr, green: &qg, blue: &qb, alpha: &qa)
    
    let bb = (1.0 - a)
    
    let (r, g, b, al) = (
      bb * pr + a * qr,
      bb * pg + a * qg,
      bb * pb + a * qb,
      bb * pa + a * qa
    )
    
    return UIColor(red: r, green: g, blue: b, alpha: al)
  }
  
  static func gradientColor(from g: [UIColor], atFraction a: CGFloat) -> UIColor {
    let a = max(min(a, 1), 0)
    return color(between: g[0], and: g[1], atFraction: a)
  }
  
  static var titleLabel: UIColor {
    return UIColor(white: 0.85, alpha: 1)
  }
}
