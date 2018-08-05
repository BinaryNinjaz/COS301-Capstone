//
//  LocationTracker.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/06/27.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import CoreLocation

final class LocationTracker: NSObject, CLLocationManagerDelegate {
  static let shared = LocationTracker()
  
  var wantsLocation: Bool = false
  var locationManager: CLLocationManager?
  
  func requestLocation(wantsLocation: Bool) {
    if wantsLocation {
      if locationManager == nil {
        locationManager = CLLocationManager()
        locationManager?.delegate = self
        locationManager?.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
      }
      if !CLLocationManager.locationServicesEnabled() {
        locationManager?.requestAlwaysAuthorization()
      }
      if CLLocationManager.locationServicesEnabled() {
        locationManager?.requestLocation()
      }
    }
  }
  
  func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    guard let loc = locations.first else {
      return
    }
    HarvestDB.update(location: loc.coordinate)
  }
  
  func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
    print(error)
  }
}
