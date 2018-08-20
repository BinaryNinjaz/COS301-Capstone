//
//  OrchardsInfomation.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 University of Pretoria. All rights reserved.
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
  
  var bagMass: Double?
  var coords: [CLLocationCoordinate2D]
  var crop: String
  var cultivars: [String]
  var date: Date
  var assignedFarm: String
  var details: String
  var name: String
  var treeSpacing: Double?
  var rowSpacing: Double?
  var irrigationKind: IrrigationKind
  var assignedWorkers = [(String, WorkerAssignmentOperation)]() // only for infoRepresentation purposes
  
  var id: String
  var tempory: Orchard?
  
  init(json: [String: Any], id: String) {
    self.id = id
    bagMass = json["bagMass"] as? Double
    crop = json["crop"] as? String ?? ""
    cultivars = json["cultivars"] as? [String] ?? []
    date = Date(timeIntervalSince1970: json["date"] as? Double ?? Date().timeIntervalSince1970)
    assignedFarm = json["farm"] as? String ?? ""
    details = json["further"] as? String ?? ""
    name = json["name"] as? String ?? ""
    treeSpacing = json["treeSpacing"] as? Double
    rowSpacing = json["rowSpacing"] as? Double
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
      "bagMass": bagMass == nil ? "" : bagMass!,
      "crop": crop,
      "date": date.timeIntervalSince1970,
      "farm": assignedFarm,
      "further": details,
      "name": name,
      "treeSpacing": treeSpacing == nil ? "" : treeSpacing!,
      "rowSpacing": rowSpacing == nil ? "" : rowSpacing!,
      "coords": coords.firbaseCoordRepresentation(),
      "cultivars": cultivars,
      "irrigation": irrigationKind.rawValue
    ]]
  }
  
  func makeChangesPermanent() {
    if let t = tempory {
      id = t.id
      bagMass = t.bagMass
      crop = t.crop
      date = t.date
      assignedFarm = t.assignedFarm
      details = t.details
      name = t.name
      treeSpacing = t.treeSpacing
      rowSpacing = t.rowSpacing
      coords = t.coords
      cultivars = t.cultivars
      irrigationKind = t.irrigationKind
    }
  }
  
  // swiftlint:disable cyclomatic_complexity
  func search(for text: String) -> [(String, String)] {
    var result = [(String, String)]()
    
    let text = text.lowercased()
    
    if name.lowercased().contains(text) {
      result.append(("Name", name))
    }
    
    let farm = Entities.shared.farms.first { $0.value.id == assignedFarm }
    for (prop, reason) in farm?.value.search(for: text) ?? [] {
      result.append(("Farm " + prop, reason))
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
    
    if treeSpacing?.description.contains(text) ?? false {
      result.append(("Tree Spacing", treeSpacing!.description))
    }
    
    if rowSpacing?.description.contains(text) ?? false {
      result.append(("Row Spacing", rowSpacing!.description))
    }
    
    if details.lowercased().contains(text) {
      result.append(("Details", ""))
    }
    
    let formatter = DateFormatter()
    formatter.dateStyle = .full
    formatter.timeStyle = .none
    let d = formatter.string(from: date)
    if d.lowercased().contains(text) {
      result.append(("Date", d))
    }
    
    if bagMass?.description.contains(text) ?? false {
      result.append(("Bag Mass", bagMass!.description))
    }
    
    return result
  }
}

extension Orchard: Equatable {
  static public func == (lhs: Orchard, rhs: Orchard) -> Bool {
    let _id = lhs.id == rhs.id
    let _bm = lhs.bagMass == rhs.bagMass || (lhs.bagMass?.isNaN ?? true) && (rhs.bagMass?.isNaN ?? true)
    let _cr = lhs.crop == rhs.crop
    let _dt = lhs.date == rhs.date
    let _af = lhs.assignedFarm == rhs.assignedFarm
    let _de = lhs.details == rhs.details
    let _nm = lhs.name == rhs.name
    let _ts = lhs.treeSpacing == rhs.treeSpacing
      || (lhs.treeSpacing?.isNaN ?? true) && (rhs.treeSpacing?.isNaN ?? true)
    let _rs = lhs.rowSpacing == rhs.rowSpacing
      || (lhs.rowSpacing?.isNaN ?? true) && (rhs.rowSpacing?.isNaN ?? true)
    let _cs = lhs.coords == rhs.coords
    let _cu = lhs.cultivars == rhs.cultivars
    let _ir = lhs.irrigationKind == rhs.irrigationKind
    
    return _id && _bm && _cr && _dt && _af && _de && _nm && _ts && _rs && _cs && _cu && _ir
  }
}

extension Orchard: CustomStringConvertible {
  public var description: String {
    guard let farm = Entities
      .shared
      .farms
      .first(where: { $0.value.id == assignedFarm }) else {
      return id + " - " + name
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
