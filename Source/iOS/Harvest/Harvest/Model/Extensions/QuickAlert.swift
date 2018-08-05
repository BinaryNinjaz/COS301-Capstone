//
//  QuickAlert.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/26.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import SCLAlertView
import GoogleMaps

extension SCLAlertView {
  convenience init(
    appearance: SCLAppearance,
    options: [(display: String, uid: String)],
    completion: @escaping (String, String) -> Void
  ) {
    self.init(appearance: appearance)
    
    for option in options {
      addButton(option.display) {
        completion(option.uid, option.display)
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

extension SCLAlertView {
  static func toggleMapType(for mapView: GMSMapView, from button: UIBarButtonItem?) {
    let alert = SCLAlertView(appearance: .optionsAppearance)
    
    alert.addButton("Hybrid") {
      mapView.mapType = .hybrid
      button?.title = "\(mapView.mapType.title)"
    }
    
    alert.addButton("Satellite") {
      mapView.mapType = .satellite
      button?.title = "\(mapView.mapType.title)"
    }
    
    alert.addButton("Normal") {
      mapView.mapType = .normal
      button?.title = "\(mapView.mapType.title)"
    }
    
    alert.addButton("Terrain") {
      mapView.mapType = .terrain
      button?.title = "\(mapView.mapType.title)"
    }
    
    alert.showEdit(
      "Map Type",
      subTitle: "Please select the map type you wish to view this map in.")
  }
}

extension SCLAlertView {
  static func showSuccessToast(message: String) {
    let appearance = SCLAlertView.SCLAppearance(
      showCloseButton: false
    )
    let notice = SCLAlertView(appearance: appearance)
    notice.showSuccess(message, subTitle: "", closeButtonTitle: nil)
    DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
      notice.hideView()
    }
  }
}
