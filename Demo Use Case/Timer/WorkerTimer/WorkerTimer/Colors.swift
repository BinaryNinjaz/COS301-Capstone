//
//  Colors.swift
//  WorkerTimer
//
//  Created by Letanyan Arumugam on 2018/03/10.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

extension UIColor {
  static func random() -> UInt32 {
    let r = arc4random() % 256
    let g = arc4random() % 256
    let b = arc4random() % 256
    
    return r << 16 | g << 8 | b << b
  }
  
  static func color(_ c: UInt32, alpha: CGFloat) -> UIColor {
    let r = CGFloat(c >> 16) / 255.0
    let g = CGFloat(c >> 8 & 0x00FF) / 255.0
    let b = CGFloat(c & 0x0000FF) / 255.0
    
    return UIColor(red: r, green: g, blue: b, alpha: alpha)
  }
}
