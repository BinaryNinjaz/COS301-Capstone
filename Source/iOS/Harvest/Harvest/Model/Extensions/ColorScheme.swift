//
//  ColorScheme.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/23.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

extension Array where Element == UIColor {
  static var green: [UIColor] {
    let s = UIColor(hue: 141.0 / 360.0, saturation: 0.78, brightness: 0.73, alpha: 1)
    let e = UIColor(hue: 141.0 / 360.0, saturation: 0.98, brightness: 0.5, alpha: 1)
    
    return [s, e]
  }
  
  static var blue: [UIColor] {
    let s = UIColor(hue: 211.0 / 360.0, saturation: 0.77, brightness: 0.69, alpha: 1)
    let e = UIColor(hue: 211.0 / 360.0, saturation: 0.77, brightness: 0.52, alpha: 1)
    
    return [s, e]
  }
  
  static var orange: [UIColor] {
    let s = UIColor.tangerine
    let e = UIColor(hue: 35.0 / 360.0, saturation: 0.7, brightness: 0.8, alpha: 1)
    
    return [s, e]
  }
  
  static var red: [UIColor] {
    let s = UIColor(hue: 5.0 / 360.0, saturation: 0.7, brightness: 0.8, alpha: 1)
    let e = UIColor(hue: 5.0 / 360.0, saturation: 0.7, brightness: 0.7, alpha: 1)
    
    return [s, e]
  }
  
  static var google: [UIColor] {
    let s = UIColor(hue: 8.0 / 360.0, saturation: 0.73, brightness: 0.93, alpha: 1)
    let e = UIColor(hue: 8.0 / 360.0, saturation: 0.73, brightness: 0.85, alpha: 1)
    
    return [s, e]
  }
  
  static var lightTile: [UIColor] {
    let s = UIColor(white: 1.00, alpha: 1)
    let e = UIColor(white: 0.90, alpha: 1)
    
    return [s, e]
  }
  
  static var harvestGreen: [UIColor] {
    let s = UIColor(hue: 144.0 / 360.0, saturation: 1.00, brightness: 0.82, alpha: 1)
    let e = UIColor(hue: 135.0 / 360.0, saturation: 1.00, brightness: 0.71, alpha: 1)
    
    return [s, e]
  }
  
  static var disabled: [UIColor] {
    let s = UIColor(white: 0.5, alpha: 1)
    let e = UIColor(white: 0.25, alpha: 1)
    
    return [s, e]
  }
}

extension UIColor {
  static var moss: UIColor {
    return UIColor(hue: 154.0 / 360.0, saturation: 1.0, brightness: 0.56, alpha: 1)
  }
  static var tangerine: UIColor {
    return UIColor(hue: 35.0 / 360.0, saturation: 1.0, brightness: 1.0, alpha: 1)
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
  
  static func gradient(_ g: [UIColor], atFraction a: CGFloat) -> UIColor {
    let a = max(min(a, 1), 0)
    return color(between: g[0], and: g[1], atFraction: a)
  }
  
  static var titleLabel: UIColor {
    return UIColor(white: 0.85, alpha: 1)
  }
}
