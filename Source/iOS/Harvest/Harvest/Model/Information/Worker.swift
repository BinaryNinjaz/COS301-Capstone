//
//  WorkersInfo.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Swift

struct Worker {
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
    return lhs.firstname == rhs.firstname && lhs.lastname == rhs.lastname
  }
  
  var hashValue: Int {
    return "\(firstname)\(lastname)".hashValue
  }
}
