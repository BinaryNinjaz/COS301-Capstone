//
//  QuickAlert.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/26.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import SCLAlertView

extension SCLAlertView {
  convenience init(
    appearance: SCLAppearance,
    options: [(display: String, uid: String)],
    completion: @escaping (String) -> Void
  ) {
    self.init(appearance: appearance)
    
    for option in options {
      addButton(option.display) {
        completion(option.uid)
      }
    }
  }
}

extension SCLAlertView.SCLAppearance {
  static var warningAppearance: SCLAlertView.SCLAppearance {
    return SCLAlertView.SCLAppearance(
      showCloseButton: false,
      buttonsLayout: .horizontal
    )
  }
  
  static var optionsAppearance: SCLAlertView.SCLAppearance {
    return SCLAlertView.SCLAppearance(
      showCloseButton: false
    )
  }
}
