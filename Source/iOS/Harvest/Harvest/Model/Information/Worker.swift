//
//  WorkersInfo.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
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
  enum Kind : Int, Codable {
    case worker, foreman
  }
  
  var firstname: String
  var lastname: String
  var assignedOrchards: [String]
  var kind: Kind
  var details: String
  var email: String
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
    email = json["email"] as? String ?? ""
    phoneNumber = json["phoneNumber"] as? String ?? ""
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
      "email": email,
      "type": kind == .foreman ? "Foreman" : "Worker",
      "phoneNumber": phoneNumber,
      "idNumber": idNumber
    ]]
  }
  
  static var currentWorker: Worker {
    return Worker(json: [
      "name": HarvestUser.current.displayName,
      "email": HarvestUser.current.email
    ], id: HarvestUser.current.uid)
  }
}

extension Worker : Hashable {
  static func ==(lhs: Worker, rhs: Worker) -> Bool {
    return lhs.id == rhs.id
      && lhs.firstname == rhs.firstname
      && lhs.lastname == rhs.lastname
      && lhs.assignedOrchards == rhs.assignedOrchards
      && lhs.kind == rhs.kind
      && lhs.details == rhs.details
      && lhs.email == rhs.email
      && lhs.phoneNumber == rhs.phoneNumber
      && lhs.idNumber == rhs.idNumber
  }
  
  var hashValue: Int {
    return id.hashValue
  }
}

extension Worker : CustomStringConvertible {
  var description: String {
    return firstname + " " + lastname
  }
}
