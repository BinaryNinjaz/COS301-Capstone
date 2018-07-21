//
//  Session.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import CoreLocation

extension Dictionary where Key == String, Value == Any {
  func track() -> [CLLocationCoordinate2D] {
    guard let _track = self["track"] as? [Any] else {
      return []
    }
    
    var result = [CLLocationCoordinate2D]()
    
    for _coord in _track {
      guard let coord = _coord as? [String: Any] else {
        continue
      }
      
      guard let lat = coord["lat"] as? Double else {
        continue
      }
      
      guard let lng = coord["lng"] as? Double else {
        continue
      }
      
      result.append(CLLocationCoordinate2D(latitude: lat, longitude: lng))
    }
    
    return result
  }
  
  func collections() -> [Worker: [CollectionPoint]] {
    guard let _collections = self["collections"] as? [String: Any] else {
      return [:]
    }
    var result: [Worker: [CollectionPoint]] = [:]
    
    for (key, _collectionPoints) in _collections {
      guard let collectionPoints = _collectionPoints as? [Any] else {
        continue
      }
      
      var colps = [CollectionPoint]()
      for _point in collectionPoints {
        guard let point = _point as? [String: Any] else {
          continue
        }
        
        guard let coord = point["coord"] as? [String: Any] else {
          continue
        }
        
        guard let lat = coord["lat"] as? Double else {
          continue
        }
        
        guard let lng = coord["lng"] as? Double else {
          continue
        }
        
        guard let _date = point["date"] as? Double else {
          continue
        }
        
        let loc = CLLocationCoordinate2D(latitude: lat, longitude: lng)
        let date = Date(timeIntervalSince1970: _date)
        
        colps.append(CollectionPoint(location: loc, date: date))
      }
      
      if let worker = Entities.shared.workers.first(where: { (_, value) -> Bool in
        value.id == key
      })?.value {
        result[worker, default: []] = colps
      }
    }
    return result
  }
}

public final class Session {
  
  var endDate: Date
  var startDate: Date
  
  var foreman: Worker
  
  var track: [CLLocationCoordinate2D]
  var collections: [Worker: [CollectionPoint]]
  
  var id: String
  var tempory: Session?
  
  init(json: [String: Any], id: String) {
    self.id = id
    
    startDate = Date(timeIntervalSince1970: json["start_date"] as? Double ?? 0.0)
    endDate = Date(timeIntervalSince1970: json["end_date"] as? Double ?? 0.0)
    
    let wid = json["wid"] as? String ?? ""
    foreman = Entities.shared.worker(withId: wid) ?? Worker(json: ["name": "Farm Owner"], id: HarvestUser.current.uid)
    
    track = json.track()
    collections = json.collections()
    
    tempory = nil
  }
  
  func json() -> [String: [String: Any]] {
    return [id: [
      "start_date": startDate.timeIntervalSince1970,
      "end_date": endDate.timeIntervalSince1970,
      "wid": foreman.id,
      "track": track.firbaseCoordRepresentation(),
      "collections": collections.firebaseSessionRepresentation()
    ]]
  }
}

extension Session: Equatable {
  public static func == (lhs: Session, rhs: Session) -> Bool {
    return lhs.id == rhs.id
      && lhs.startDate == rhs.startDate
      && lhs.endDate == rhs.endDate
      && lhs.foreman == rhs.foreman
      && lhs.track == rhs.track
  }
}

extension Session: CustomStringConvertible {
  public var description: String {
    let formatter = DateFormatter()
    formatter.dateStyle = .medium
    formatter.timeStyle = .medium
    
    return formatter.string(from: startDate) + " ・ " + foreman.description
  }
  
  var key: String {
    return startDate.timeIntervalSince1970.description + id
  }
}

final class ShallowSession {
  var startDate: Date
  var foreman: Worker
  var id: String
  
  init(json: Any) {
    guard let json = json as? [String: Any] else {
      startDate = Date()
      id = startDate.description
      foreman = Worker(HarvestUser.current)
      return
    }
    
    let d = Date(timeIntervalSince1970: json["start_date"] as? Double ?? 0.0)
    startDate = d
    id = json["key"] as? String ?? d.description
    
    let wid = json["wid"] as? String ?? ""
    foreman = Entities.shared.worker(withId: wid) ?? Worker(HarvestUser.current)
  }
  
  func json() -> Any {
    return [
      "key": id,
      "start_date": startDate.timeIntervalSince1970,
      "wid": foreman.id
    ]
  }
}

extension ShallowSession: CustomStringConvertible {
  public var description: String {
    let formatter = DateFormatter()
    formatter.dateStyle = .medium
    formatter.timeStyle = .medium
    
    return foreman.description + " ・ " + formatter.string(from: startDate)
  }
  
  var key: String {
    return startDate.timeIntervalSince1970.description + id
  }
}

extension ShallowSession: Equatable {
  static func == (lhs: ShallowSession, rhs: ShallowSession) -> Bool {
    return lhs.id == rhs.id
  }
}

extension Date {
  func sameDay(as date: Date) -> Bool {
    var aday = Calendar.current.dateComponents([.day], from: self)
    var bday = Calendar.current.dateComponents([.day], from: date)
    
    return aday.day == bday.day
  }
}

extension SortedDictionary where Key == Date, Value == [Session] {
  mutating func accumulateByDay(with sessions: [Session]) {
    for session in sessions {
      var inserted = false
      for (key, _) in self {
        if key.sameDay(as: session.startDate) {
          self[key] = self[key]! + [session]
          inserted = true
          break
        }
      }
      if !inserted {
        self[session.startDate] = [session]
      }
    }
  }
}
