//
//  FarmInfomation.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Swift

class Farm {
  var name: String
  var details: String
  var id: String
  var tempory: Farm?
  
  init(json: [String: Any], id: String) {
    self.id = id
    name = json["name"] as? String ?? ""
    details = json["further"] as? String ?? ""
    tempory = nil
  }
  
  func json() -> [String: [String: Any]] {
    return [id: [
      "name": name,
      "info": details
    ]]
  }
}

extension Farm : Equatable {
  static func ==(lhs: Farm, rhs: Farm) -> Bool {
    return lhs.id == rhs.id
      && lhs.name == rhs.name
      && lhs.details == rhs.details
  }
}
