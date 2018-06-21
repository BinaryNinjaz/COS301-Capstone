//
//  ViewsExt.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/23.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

extension UIView {
  func apply(gradient: CAGradientLayer) {
    layer.sublayers?.remove(at: 0)
    backgroundColor = .clear
    gradient.frame = bounds
    layer.insertSublayer(gradient, at: 0)
  }
}

extension UITextField {
  func addLeftImage(_ image: UIImage) {
    leftViewMode = .always
    let wrapper = UIView(frame: CGRect(x: 0, y: 0, width: 32, height: 22))
    let imageView = UIImageView(frame: CGRect(x: 8,
                                              y: 3,
                                              width: 24,
                                              height: 16))
    imageView.image = image.withRenderingMode(.alwaysTemplate)
    imageView.tintColor = UIColor(white: 0.5, alpha: 1)
    imageView.contentMode = .scaleAspectFit
    wrapper.addSubview(imageView)
    leftView = wrapper
  }
}

extension UIView {
  var isButtonEnabled: Bool {
    get {
      return isUserInteractionEnabled
    }
    set(value) {
      isUserInteractionEnabled = value
      apply(gradient: .disabledButton)
    }
  }
}

extension UIView {
  func parallaxEffect(
    x: (min: CGFloat, max: CGFloat),
    y: (min: CGFloat, max: CGFloat),
    enable: Bool = true
    ) {
    guard enable else {
      motionEffects.removeAll()
      return
    }
    
    let xMotion = UIInterpolatingMotionEffect(
      keyPath: "layer.transform.translation.x",
      type: .tiltAlongHorizontalAxis)
    xMotion.minimumRelativeValue = x.min
    xMotion.maximumRelativeValue = x.max
    
    let yMotion = UIInterpolatingMotionEffect(
      keyPath: "layer.transform.translation.y",
      type: .tiltAlongVerticalAxis)
    yMotion.minimumRelativeValue = y.min
    yMotion.maximumRelativeValue = y.max
    
    let motionEffectGroup = UIMotionEffectGroup()
    motionEffectGroup.motionEffects = [xMotion, yMotion]
    
    addMotionEffect(motionEffectGroup)
  }
}

extension UIView {
  func setWidth(_ w: CGFloat) {
    frame = CGRect(x: frame.origin.x,
                   y: frame.origin.y,
                   width: w,
                   height: frame.height)
  }
  
  func setHeight(_ h: CGFloat) {
    frame = CGRect(x: frame.origin.x,
                   y: frame.origin.y,
                   width: frame.width,
                   height: h)
  }
  
  func setOriginX(_ x: CGFloat) {
    frame = CGRect(x: x,
                   y: frame.origin.y,
                   width: frame.width,
                   height: frame.height)
  }
  
  func setOriginY(_ y: CGFloat) {
    frame = CGRect(x: frame.origin.x,
                   y: y,
                   width: frame.width,
                   height: frame.height)
  }
}

extension UIImage {
  func resizeImage(toWidth w: CGFloat) -> UIImage? {
    let scale = w / size.width
    let newHeight = size.height * scale
    UIGraphicsBeginImageContext(CGSize(width: w, height: newHeight))
    draw(in: CGRect(x: 0, y: 0, width: w, height: newHeight))
    let newImage = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    
    return newImage
  }
}
