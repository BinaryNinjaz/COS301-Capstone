//
//  TrackerModel.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/29.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import UIKit
import CoreLocation
import Disk

struct CollectionPoint {
  var location: CLLocationCoordinate2D
  var date: Date
  var orchard: Orchard?
  var selectedOrchard: String?
  
  init(location: CLLocationCoordinate2D, date: Date, selectedOrchard: String?) {
    self.location = location
    self.date = date
    
    orchard = Entities.shared.orchards.first { $0.value.contains(location) }?.value
    
    self.selectedOrchard = selectedOrchard
  }
}

struct Tracker: Codable {
  private(set) var wid: String
  private(set) var trackCount: Int
  private(set) var sessionStart: Date
  private(set) var lastCollection: Date
  private(set) var collections: [Worker: [CollectionPoint]]
  private(set) var currentOrchard: String?
  
  init(wid: String) {
    self.wid = wid
    trackCount = 0
    sessionStart = Date()
    lastCollection = sessionStart
    collections = [:]
    currentOrchard = nil
  }
  
  func saveState() {
    try? Disk.save(self, to: .applicationSupport, as: "session")
  }
  
  func discardState() {
    try? Disk.remove("session", from: .applicationSupport)
  }
  
  mutating func collect(for worker: Worker, at loc: CLLocation, selectedOrchard: String?) {
    guard var collection = collections[worker] else {
      let cp = CollectionPoint(location: loc.coordinate, date: Date(), selectedOrchard: selectedOrchard)
      collections[worker] = [cp]
      return
    }
    
    collection.append(CollectionPoint(location: loc.coordinate, date: Date(), selectedOrchard: selectedOrchard))
    
    collections[worker] = collection
    
    saveState()
  }
  
  mutating func collect(bags: Int, for worker: Worker, at loc: CLLocation, selectedOrchard: String?) {
    guard var collection = collections[worker] else {
      let cp = CollectionPoint(location: loc.coordinate, date: Date(), selectedOrchard: selectedOrchard)
      collections[worker] = [cp]
      return
    }
    
    let point = CollectionPoint(location: loc.coordinate, date: Date(), selectedOrchard: selectedOrchard)
    let points = repeatElement(point, count: bags)
    
    collection.append(contentsOf: points)
    
    collections[worker] = collection
    
    saveState()
  }
  
  mutating func pop(for worker: Worker) {
    guard var collection = collections[worker], !collection.isEmpty else {
      return
    }
    
    collection.removeLast()
    
    collections[worker] = collection
    
    saveState()
  }
  
  mutating func track(location: CLLocation) {
    currentOrchard = nil
    if let o = Entities.shared.orchards.first(where: { $0.value.contains(location.coordinate) }) {
      currentOrchard = o.value.id
    }
    UserDefaults.standard.track(location: location, index: trackCount)
    trackCount += 1
    saveState()
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
                      byWorkerId: wid,
                      on: sessionStart,
                      track: pathTracked())
    
    try? Disk.remove("session", from: .applicationSupport)
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
  
  enum CodingKeys: String, CodingKey {
    case wid
    case trackCount
    case sessionStart
    case lastCollection
    case collections
    case currentOrchard
  }
  
  public init(from decoder: Decoder) throws {
    let values = try decoder.container(keyedBy: CodingKeys.self)
    
    wid = try values.decode(String.self, forKey: .wid)
    trackCount = try values.decode(Int.self, forKey: .trackCount)
    sessionStart = try values.decode(Date.self, forKey: .sessionStart)
    lastCollection = try values.decode(Date.self, forKey: .lastCollection)
    collections = try values.decode([Worker: [CollectionPoint]].self, forKey: .collections)
    currentOrchard = try values.decode(String.self, forKey: .currentOrchard)
  }
  
  public func encode(to encoder: Encoder) throws {
    var container = encoder.container(keyedBy: CodingKeys.self)
    
    try container.encode(wid, forKey: .wid)
    try container.encode(trackCount, forKey: .trackCount)
    try container.encode(sessionStart, forKey: .sessionStart)
    try container.encode(lastCollection, forKey: .lastCollection)
    try container.encode(collections, forKey: .collections)
    try container.encode(currentOrchard, forKey: .currentOrchard)
  }
}

extension UserDefaults {
  func track(location: CLLocation, index: Int) {
    let d = String(index)
    set(location.coordinate.latitude, forKey: "lat" + d)
    set(location.coordinate.longitude, forKey: "lng" + d)
  }
}
