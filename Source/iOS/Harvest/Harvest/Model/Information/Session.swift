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
        
        let loc = CLLocation(latitude: lat, longitude: lng)
        let date = Date(timeIntervalSince1970: _date)
        
        colps.append(CollectionPoint(location: loc, date: date))
      }
      
      if let workerEntity = Entities.shared.workers[key],
        case let .worker(worker) = workerEntity {
        result[worker, default: []] = colps
      }
    }
    return result
  }
}


public class Session {
  
  var endDate: Date
  var startDate: Date
  
  var uid: String
  var display: String
  
  var track: [CLLocationCoordinate2D]
  var collections: [Worker: [CollectionPoint]]
  
  var id: String
  var tempory: Session?
  
  init(json: [String: Any], id: String) {
    self.id = id
    
    startDate = Date(timeIntervalSince1970: json["start_date"] as? Double ?? 0.0)
    endDate = Date(timeIntervalSince1970: json["end_date"] as? Double ?? 0.0)
    
    uid = json["uid"] as? String ?? ""
    display = json["display"] as? String ?? ""
    
    track = json.track()
    collections = json.collections()
    
    tempory = nil
  }
  
  func json() -> [String: [String: Any]] {
    return [id: [
      "start_date": startDate.timeIntervalSince1970,
      "end_date": endDate.timeIntervalSince1970,
      "uid": uid,
      "display": display,
      "track": track.firbaseCoordRepresentation(),
      "collections": collections.firebaseCoordRepresentation()
    ]]
  }
}

extension Session : Equatable {
  public static func ==(lhs: Session, rhs: Session) -> Bool {
    return lhs.id == rhs.id
      && lhs.startDate == rhs.startDate
      && lhs.endDate == rhs.endDate
      && lhs.uid == rhs.uid
      && lhs.display == rhs.display
      && lhs.track == rhs.track
//      && lhs.collections == rhs.collections // FIXME MAYBE?
  }
}

extension Session : CustomStringConvertible {
  public var description: String {
    let formatter = DateFormatter()
    formatter.dateStyle = .medium
    
    return formatter.string(from: startDate) + " " + display
  }
  
  var key: String {
    return startDate.timeIntervalSince1970.description + id
  }
}


