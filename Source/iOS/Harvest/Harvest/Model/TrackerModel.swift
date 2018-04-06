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
  var sessionStart: Date
  var lastCollection: Date
  var collections: [Worker: WorkerCollection]
  
  init() {
    sessionStart = Date()
    lastCollection = sessionStart
    collections = [:]
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
}
