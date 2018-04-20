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
      "details": details
    ]]
  }
}

extension Farm : Equatable {
  static func ==(lhs: Farm, rhs: Farm) -> Bool {
    return lhs.name == rhs.name
  }
}
