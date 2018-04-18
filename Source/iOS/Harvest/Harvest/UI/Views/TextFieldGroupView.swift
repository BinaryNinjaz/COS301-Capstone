//
//  TextFieldGroupView.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/18.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

class TextFieldGroupView: UIView {

  override init(frame: CGRect) {
    super.init(frame: frame)
  }
  
  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }
  
  override func draw(_ rect: CGRect) {
    UIColor.clear.setFill()
    UIBezierPath(rect: rect).fill()
    
    UIGraphicsGetCurrentContext()?.setLineWidth(10)
    
    let back = UIColor(white: 1.0, alpha: 0.8)
    let border = UIColor(white: 0.9, alpha: 1)
    
    let path = UIBezierPath(rect: rect)
    back.setFill()
    border.setStroke()
    
    path.fill()
    let seps = Int((rect.height - 42) / 38)
    
    let line = UIBezierPath(rect: CGRect(x: 0.0, y: 42, width: rect.width, height: 1.0))
    border.setFill()
    line.fill()
    for i in 1..<seps {
      let line = UIBezierPath(rect: CGRect(x: 0.0,
                                           y: 42 + CGFloat(i) * 38,
                                           width: rect.width,
                                           height: 1.0))
      line.fill()
    }
    
    layer.cornerRadius = 5
    layer.masksToBounds = true
  }

}
