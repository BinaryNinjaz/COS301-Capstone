//
//  WorkersInfo.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Swift

class Worker {
  enum Kind {
    case worker, foreman
  }
  
  var firstname: String
  var lastname: String
  var assignedOrchard: String
  var kind: Kind
  var details: String
  var email: String
  var id: String
  var tempory: Worker?
  
  init(json: [String: Any], id: String) {
    self.id = id
    firstname = json["name"] as? String ?? ""
    lastname = json["surname"] as? String ?? ""
    assignedOrchard = json["orchard"] as? String ?? ""
    details = json["info"] as? String ?? ""
    email = json["email"] as? String ?? ""
    
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
      "orchard": assignedOrchard,
      "info": details,
      "email": email,
      "type": kind == .foreman ? "Foreman" : "Worker"
    ]]
  }
}

extension Worker : Hashable {
  static func ==(lhs: Worker, rhs: Worker) -> Bool {
    return lhs.id == rhs.id
      && lhs.firstname == rhs.firstname
      && lhs.lastname == rhs.lastname
      && lhs.assignedOrchard == rhs.assignedOrchard
      && lhs.kind == rhs.kind
      && lhs.details == rhs.details
      && lhs.email == rhs.email
  }
  
  var hashValue: Int {
    return "\(firstname)\(lastname)".hashValue
  }
}
