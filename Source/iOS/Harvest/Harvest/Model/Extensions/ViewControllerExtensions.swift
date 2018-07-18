//
//  VCExt.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/23.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

extension UIViewController {
  func hideKeyboardWhenTappedAround() {
    let tap = UITapGestureRecognizer(target: self,
                                     action: #selector(
                                      UIViewController.dismissKeyboard))
    tap.cancelsTouchesInView = false
    view.addGestureRecognizer(tap)
  }
  
  @objc func dismissKeyboard() {
    view.endEditing(true)
  }
}
