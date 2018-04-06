//
//  TrackerModel.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/29.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import CoreLocation

struct CollectionPoint {
  var location: CLLocation
  var date: Date
}

struct WorkerCollection {
  var count: Int
  var collectionPoints: [CollectionPoint]
}

struct Tracker {
  var trackCount: Int
  var sessionStart: Date
  var lastCollection: Date
  var collections: [Worker: WorkerCollection]
  
  init() {
    trackCount = 0
    sessionStart = Date()
    lastCollection = sessionStart
    collections = [:]
  }
  
  mutating func collect(for worker: Worker, at loc: CLLocation) {
    guard var collection = collections[worker] else {
      let cp = CollectionPoint(location: loc, date: Date())
      let c = WorkerCollection(count: 1, collectionPoints: [cp])
      collections[worker] = c
      return
    }
    
    collection.count += 1
    collection.collectionPoints.append(CollectionPoint(location: loc, date: Date()))
    
    collections[worker] = collection
  }
  
  mutating func track(location: CLLocation) {
    UserDefaults.standard.track(location: location, index: trackCount)
    trackCount += 1
  }
  
  func storeSession() {
    var track = [(Double, Double)]()
    
    for i in 0..<trackCount {
      let d = i.description
      let lat = UserDefaults.standard.double(forKey: "lat" + d)
      let lng = UserDefaults.standard.double(forKey: "lng" + d)
      track.append((lat, lng))
    }
    
    HarvestDB.collect(from: collections,
                      by: HarvestUser.current.name,
                      on: sessionStart,
                      track: track)
  }
}

extension UserDefaults {
  func track(location: CLLocation, index: Int) {
    let d = String(index)
    set(location.coordinate.latitude, forKey: "lat" + d)
    set(location.coordinate.longitude, forKey: "lng" + d)
  }
}
