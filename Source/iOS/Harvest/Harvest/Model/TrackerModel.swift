//
//  TrackerModel.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/29.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import CoreLocation

struct Tracker {
  var trackCount: Int
  var sessionStart: Date
  var lastCollection: Date
  
  init() {
    trackCount = 0
    sessionStart = Date()
    lastCollection = sessionStart
  }
  
  mutating func collect(yield: Double, at loc: CLLocation) {
    let collectionDate = Date()
    let duration = collectionDate.timeIntervalSince(lastCollection)
    lastCollection = Date()
    
    HarvestDB.collect(yield: yield,
                      from: HarvestUser.current.name,
                      inAmountOfSeconds: duration,
                      at: loc.coordinate,
                      on: collectionDate)
  }
  
  mutating func track(location: CLLocation) {
    UserDefaults.standard.track(location: location, index: trackCount)
    trackCount += 1
  }
  
  func storeTrack() {
    var track = [(Double, Double)]()
    let now = Date()
    
    for i in 0..<trackCount {
      let d = i.description
      let lat = UserDefaults.standard.double(forKey: "lat" + d)
      let lng = UserDefaults.standard.double(forKey: "lng" + d)
      track.append((lat, lng))
    }
    
    let duration = now.timeIntervalSince(sessionStart)
    
    HarvestDB.track(track, from: HarvestUser.current.name, inAmountOfSeconds: duration, on: sessionStart)
  }
}

extension UserDefaults {
  func track(location: CLLocation, index: Int) {
    let d = String(index)
    set(location.coordinate.latitude, forKey: "lat" + d)
    set(location.coordinate.longitude, forKey: "lng" + d)
  }
}
