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
  var collectionPoints: [CollectionPoint]
}

struct Tracker {
  private(set) var trackCount: Int
  private(set) var sessionStart: Date
  private(set) var lastCollection: Date
  private(set) var collections: [Worker: [CollectionPoint]]
  
  init() {
    trackCount = 0
    sessionStart = Date()
    lastCollection = sessionStart
    collections = [:]
  }
  
  mutating func collect(for worker: Worker, at loc: CLLocation) {
    guard var collection = collections[worker] else {
      let cp = CollectionPoint(location: loc, date: Date())
      collections[worker] = [cp]
      return
    }
    
    collection.append(CollectionPoint(location: loc, date: Date()))
    
    collections[worker] = collection
  }
  
  mutating func pop(for worker: Worker) {
    guard var collection = collections[worker],
      !collection.isEmpty else {
      return
    }
    
    collection.removeLast()
    
    collections[worker] = collection
  }
  
  mutating func track(location: CLLocation) {
    UserDefaults.standard.track(location: location, index: trackCount)
    trackCount += 1
  }
  
  func pathTracked() -> [CLLocationCoordinate2D] {
    var result = [CLLocationCoordinate2D]()
    
    for i in 0..<trackCount {
      let d = i.description
      let lat = UserDefaults.standard.double(forKey: "lat" + d)
      let lng = UserDefaults.standard.double(forKey: "lng" + d)
      result.append(CLLocationCoordinate2D(latitude: lat, longitude: lng))
    }
    
    return result
  }
  
  func storeSession() {
    HarvestDB.collect(from: collections,
                      byUserId: HarvestUser.current.uid,
                      on: sessionStart,
                      track: pathTracked())
  }
  
  func totalCollected() -> Int {
    var result = 0
    for (_, wc) in collections {
      result += wc.count
    }
    return result
  }
  
  func durationFormatted() -> String {
    let end = Date()
    
    let formatter = DateComponentsFormatter()
    formatter.unitsStyle = .short
    formatter.allowedUnits = [.minute, .second, .hour]
    
    return formatter.string(from: end.timeIntervalSince(sessionStart)) ?? ""
  }
}

extension UserDefaults {
  func track(location: CLLocation, index: Int) {
    let d = String(index)
    set(location.coordinate.latitude, forKey: "lat" + d)
    set(location.coordinate.longitude, forKey: "lng" + d)
  }
}
