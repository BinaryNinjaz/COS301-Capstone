//
//  OrchardsInfomation.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Foundation
import CoreLocation

class Orchard {
  var bagMass: Double
  var coords: [CLLocationCoordinate2D]
  var crop: String
  var date: Date
  var assignedFarm: String
  var details: String
  var name: String
  var distanceUnit: String
  var xDim: Double
  var yDim: Double
  var id: String
  var tempory: Orchard?
  
  init(json: [String: Any], id: String) {
    self.id = id
    bagMass = json["bagMass"] as? Double ?? 0.0
    crop = json["crop"] as? String ?? ""
    date = Date(timeIntervalSince1970: json["date"] as? Double ?? 0.0)
    assignedFarm = json["farm"] as? String ?? ""
    details = json["further"] as? String ?? ""
    name = json["name"] as? String ?? ""
    distanceUnit = json["unit"] as? String ?? ""
    xDim = json["xDim"] as? Double ?? 0.0
    yDim = json["yDim"] as? Double ?? 0.0
    
    
    coords = [CLLocationCoordinate2D]()
    let cs = json["coords"] as? [Any] ?? []
    for c in cs {
      guard let c = c as? [String: Any] else {
        continue
      }
      guard let lat = c["lat"] as? Double else {
        continue
      }
      guard let lng = c["lng"] as? Double else {
        continue
      }
      
      coords.append(CLLocationCoordinate2D(latitude: lat, longitude: lng))
    }
    
    tempory = nil
  }
  
  func json() -> [String: [String: Any]] {
    return [id: [
      "bagMass": bagMass,
      "crop": crop,
      "date": date.timeIntervalSince1970,
      "farm": assignedFarm,
      "info": details,
      "name": name,
      "unit": distanceUnit,
      "xDim": xDim,
      "yDim": yDim,
      "coords": coords.map { ($0.latitude, $0.longitude) }.firbaseCoordRepresentation()
    ]]
  }
}

extension Orchard : Equatable {
  static func ==(lhs: Orchard, rhs: Orchard) -> Bool {
    return lhs.id == rhs.id
      && lhs.bagMass == rhs.bagMass
      && lhs.crop == rhs.crop
      && lhs.date == rhs.date
      && lhs.assignedFarm == rhs.assignedFarm
      && lhs.details == rhs.details
      && lhs.name == rhs.name
      && lhs.distanceUnit == rhs.distanceUnit
      && lhs.xDim == rhs.xDim
      && lhs.yDim == rhs.yDim
      && lhs.coords == rhs.coords
  }
}

extension Orchard : CustomStringConvertible {
  var description: String {
    return name
  }
}

extension CLLocationCoordinate2D : Equatable {
  public static func ==(lhs: CLLocationCoordinate2D, rhs: CLLocationCoordinate2D) -> Bool {
    return lhs.latitude == rhs.latitude
      && lhs.longitude == rhs.longitude
  }
}
