//
//  WorkersInfo.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Swift

extension Dictionary where Key == String, Value == Any {
  func orchards() -> [String] {
    guard let _orchards = self["orchards"] as? [String] else {
      return []
    }
    
    return _orchards
  }
}

final class Worker {
  enum Kind: Int, Codable {
    case worker, foreman
  }
  
  var firstname: String
  var lastname: String
  var assignedOrchards: [String]
  var kind: Kind
  var details: String
  var phoneNumber: String
  var idNumber: String
  var id: String
  var tempory: Worker?
  
  init(json: [String: Any], id: String) {
    self.id = id
    firstname = json["name"] as? String ?? ""
    lastname = json["surname"] as? String ?? ""
    assignedOrchards = json.orchards()
    details = json["info"] as? String ?? ""
    phoneNumber = (json["phoneNumber"] as? String ?? "").removedFirebaseInvalids()
    idNumber = json["idNumber"] as? String ?? ""
    
    if let kinds = json["type"] as? String {
      kind = kinds == "Foreman"
        ? .foreman
        : .worker
    } else {
      kind = .worker
    }
    tempory = nil
  }
  
  func json() -> [String: [String: Any]] {
    return [id: [
      "name": firstname,
      "surname": lastname,
      "orchards": assignedOrchards,
      "info": details,
      "type": kind == .foreman ? "Foreman" : "Worker",
      "phoneNumber": phoneNumber.removedFirebaseInvalids(),
      "idNumber": idNumber
    ]]
  }
  
  func makeChangesPermanent() {
    if let t = tempory {
      id = t.id
      firstname = t.firstname
      lastname = t.lastname
      assignedOrchards = t.assignedOrchards
      details = t.details
      kind = t.kind
      phoneNumber = t.phoneNumber
      idNumber = t.idNumber
    }
  }
  
  convenience init(_ user: HarvestUser) {
    self.init(json: ["name": "Farm Owner"], id: user.uid)
  }
  
  var name: String {
    return firstname + " " + lastname
  }
  
  static var currentWorker: Worker {
    return Worker(json: [
      "name": HarvestUser.current.displayName,
      "phoneNumber": HarvestUser.current.accountIdentifier
    ], id: HarvestUser.current.uid)
  }
  
  func search(for text: String) -> [(String, String)] {
    var result = [(String, String)]()
    
    let text = text.lowercased()
    
    if name.lowercased().contains(text) {
      result.append(("Name", name))
    }
    
    let orchardNames = Entities.shared.orchards
      .filter { assignedOrchards.contains($0.value.id) }
      .map { $0.value.name }
    let foundOrchards = orchardNames.filter { $0.lowercased().contains(text) }
    if let f = foundOrchards.first {
      result.append(("Assigned Orchard", f + (foundOrchards.count == 1 ? "" : ", ...")))
    }
    
    let farmNames = Entities.shared.farms
      .filter { assignedOrchards.contains($0.value.id) }
      .map { $0.value.name }
    let foundFarms = farmNames.filter { $0.lowercased().contains(text) }
    if let f = foundFarms.first {
      result.append(("Assigned Farm", f + (foundFarms.count == 1 ? "" : ", ...")))
    }
    
    if details.lowercased().contains(text) {
      result.append(("Details", ""))
    }
    
    if (kind == .worker ? "worker" : "foreman").contains(text) {
      result.append(("Kind", kind == .worker ? "Worker" : "Foreman"))
    }
    
    if phoneNumber.lowercased().contains(text) {
      result.append(("Phone Number", phoneNumber))
    }
    
    if idNumber.lowercased().contains(text) {
      result.append(("ID Number", idNumber))
    }
    
    return result
  }
}

extension Worker: Hashable {
  static func == (lhs: Worker, rhs: Worker) -> Bool {
    return lhs.id == rhs.id
      && lhs.firstname == rhs.firstname
      && lhs.lastname == rhs.lastname
      && lhs.assignedOrchards == rhs.assignedOrchards
      && lhs.kind == rhs.kind
      && lhs.details == rhs.details
      && lhs.phoneNumber == rhs.phoneNumber
      && lhs.idNumber == rhs.idNumber
  }
  
  var hashValue: Int {
    return id.hashValue
  }
}

extension Worker: CustomStringConvertible {
  var description: String {
    let samePeople = Entities.shared.workers.filter { $0.value.name == name && $0.value.id != id }
    if samePeople.count > 0 {
      return name + " (" + (idNumber != "" ? idNumber : id) + ")"
    } else {
      return name
    }
  }
}
