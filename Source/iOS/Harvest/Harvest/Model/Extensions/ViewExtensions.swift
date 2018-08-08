//
//  ViewsExt.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/23.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import UIKit
import SnapKit

extension UIView {
  func apply(gradient: CAGradientLayer) {
    if let sublayers = layer.sublayers, sublayers.count > 0 {
      layer.sublayers?.remove(at: 0)
    }
    backgroundColor = .clear
    gradient.frame = bounds
    layer.insertSublayer(gradient, at: 0)
  }
}

extension UITextField {
  func addLeftImage(_ image: UIImage) {
    leftView = nil
    leftViewMode = .always
    let h = bounds.height - 4
    let wrapper = UIView(frame: CGRect(x: 0, y: 0, width: h + 8, height: h))
    let imageView = UIImageView(frame: CGRect(x: 8,
                                              y: 3,
                                              width: h - 2,
                                              height: h - 6))
    imageView.image = image.withRenderingMode(.alwaysTemplate)
    imageView.tintColor = UIColor(white: 0.5, alpha: 1)
    imageView.contentMode = .scaleAspectFit
    wrapper.addSubview(imageView)
    leftView = wrapper
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

enum Snap {
  static func fillParent(on controller: UIViewController) -> (ConstraintMaker) -> Void {
    return { make in
      make.top.equalTo(controller.topLayoutGuide.snp.bottom)
      make.bottom.equalTo(controller.bottomLayoutGuide.snp.top)
      
      if #available(iOS 11.0, *) {
        make.left.equalTo(controller.view.safeAreaLayoutGuide.snp.left)
        make.right.equalTo(controller.view.safeAreaLayoutGuide.snp.right)
      } else {
        make.left.equalTo(0)
        make.width.equalToSuperview()
      }
    }
  }
  
  static func center(in controller: UIViewController) -> (ConstraintMaker) -> Void {
    return { make in
      make.centerX.equalToSuperview()
      make.centerY.equalToSuperview()
    }
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

final class AnimationNode {
  var finalFrame: () -> Void
  var duration: TimeInterval
  var next: AnimationNode?
  
  init(duration: TimeInterval, final: @escaping () -> Void) {
    finalFrame = final
    self.duration = duration
    next = nil
  }
  
  func animate() {
    UIView.animate(withDuration: duration, animations: finalFrame) { _ in self.next?.animate() }
  }
}
