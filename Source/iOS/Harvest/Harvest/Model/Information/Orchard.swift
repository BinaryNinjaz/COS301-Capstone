//
//  OrchardsInfomation.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Foundation
import CoreLocation

enum IrrigationKind: String, CustomStringConvertible {
  case micro = "Micro"
  case drip = "Drip"
  case floppy = "Floppy"
  case dragLines = "Drag Lines"
  case other = "Other"
  case none = "None (dry land)"
  
  static var allCases = [
    IrrigationKind.micro,
    .drip,
    .floppy,
    .dragLines,
    .other,
    .none
  ]
  
  var description: String {
    return self.rawValue
  }
}

public final class Orchard {
  enum WorkerAssignmentOperation {
    case remove, add, assigned, unassigned
  }
  
  var bagMass: Double
  var coords: [CLLocationCoordinate2D]
  var crop: String
  var cultivars: [String]
  var date: Date
  var assignedFarm: String
  var details: String
  var name: String
  var treeSpacing: Double
  var rowSpacing: Double
  var irrigationKind: IrrigationKind
  var assignedWorkers = [(String, WorkerAssignmentOperation)]() // only for infoRepresentation purposes
  
  var id: String
  var tempory: Orchard?
  
  init(json: [String: Any], id: String) {
    self.id = id
    bagMass = json["bagMass"] as? Double ?? .nan
    crop = json["crop"] as? String ?? ""
    cultivars = json["cultivars"] as? [String] ?? []
    date = Date(timeIntervalSince1970: json["date"] as? Double ?? 0.0)
    assignedFarm = json["farm"] as? String ?? ""
    details = json["further"] as? String ?? ""
    name = json["name"] as? String ?? ""
    treeSpacing = json["treeSpacing"] as? Double ?? .nan
    rowSpacing = json["rowSpacing"] as? Double ?? .nan
    irrigationKind = IrrigationKind(rawValue:
      json["irrigation"] as? String ?? ""
    ) ?? .none
    
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
      "bagMass": bagMass.isNaN ? "" : bagMass,
      "crop": crop,
      "date": date.timeIntervalSince1970,
      "farm": assignedFarm,
      "further": details,
      "name": name,
      "treeSpacing": treeSpacing.isNaN ? "" : treeSpacing,
      "rowSpacing": rowSpacing.isNaN ? "" : rowSpacing,
      "coords": coords.firbaseCoordRepresentation(),
      "cultivars": cultivars,
      "irrigation": irrigationKind.rawValue
    ]]
  }
  
  // swiftlint:disable cyclomatic_complexity
  func search(for text: String) -> [(String, String)] {
    var result = [(String, String)]()
    
    let text = text.lowercased()
    
    if name.lowercased().contains(text) {
      result.append(("Name", name))
    }
    
    let farmName = Entities.shared.farms.first { $0.value.id == assignedFarm }.map { $0.value.name } ?? ""
    if farmName.lowercased().contains(text) {
      result.append(("Assigned Farm", farmName))
    }
    
    let workerNames = Entities.shared.workers
      .filter { $0.value.assignedOrchards.contains(id) }
      .map { $0.value.name }
    let foundWorkers = workerNames.filter { $0.lowercased().contains(text) }
    if let f = foundWorkers.first {
      result.append(("Assigned Workers", f + (foundWorkers.count == 1 ? "" : ", ...")))
    }
    
    if crop.lowercased().contains(text) {
      result.append(("Crop", crop))
    }
    
    let foundCultivars = cultivars.filter { $0.lowercased().contains(text) }
    if let c = foundCultivars.first {
      result.append(("Cultivar", c + (foundCultivars.count == 1 ? "" : ", ...")))
    }
    
    if irrigationKind.rawValue.lowercased().contains(text) {
      result.append(("Irrigation Kind", irrigationKind.rawValue))
    }
    
    if treeSpacing.description.contains(text) {
      result.append(("Tree Spacing", treeSpacing.description))
    }
    
    if rowSpacing.description.contains(text) {
      result.append(("Row Spacing", rowSpacing.description))
    }
    
    if details.lowercased().contains(text) {
      result.append(("Details", ""))
    }
    
    if date.description.lowercased().contains(text) {
      result.append(("Date", date.description))
    }
    
    if bagMass.description.contains(text) {
      result.append(("Bag Mass", bagMass.description))
    }
    
    return result
  }
}

extension Orchard: Equatable {
  static public func == (lhs: Orchard, rhs: Orchard) -> Bool {
    return lhs.id == rhs.id
      && lhs.bagMass == rhs.bagMass
      && lhs.crop == rhs.crop
      && lhs.date == rhs.date
      && lhs.assignedFarm == rhs.assignedFarm
      && lhs.details == rhs.details
      && lhs.name == rhs.name
      && lhs.treeSpacing == rhs.treeSpacing
      && lhs.rowSpacing == rhs.rowSpacing
      && lhs.coords == rhs.coords
      && lhs.cultivars == rhs.cultivars
      && lhs.irrigationKind == rhs.irrigationKind
  }
}

extension Orchard: CustomStringConvertible {
  public var description: String {
    guard let farm = Entities
      .shared
      .farms
      .first(where: { $0.value.id == assignedFarm }) else {
      return name + " – Unassigned (" + Date().timeIntervalSince1970.description + ")"
    }
    return farm.value.name + " – " + name
  }
}

extension Orchard: Hashable {
  public var hashValue: Int {
    return id.hashValue
  }
}

extension CLLocationCoordinate2D: Equatable {
  public static func == (
    lhs: CLLocationCoordinate2D,
    rhs: CLLocationCoordinate2D
  ) -> Bool {
    return lhs.latitude == rhs.latitude
      && lhs.longitude == rhs.longitude
  }
}
