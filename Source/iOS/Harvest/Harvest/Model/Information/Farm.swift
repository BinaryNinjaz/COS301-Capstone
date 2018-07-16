//
//  FarmInfomation.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Swift

final class Farm: Codable {
  var name: String
  var companyName: String
  var email: String
  var contactNumber: String
  var province: String
  var nearestTown: String
  var details: String
  var id: String
  var tempory: Farm?
  
  init(json: [String: Any], id: String) {
    self.id = id
    name = json["name"] as? String ?? ""
    companyName = json["companyName"] as? String ?? ""
    email = json["email"] as? String ?? ""
    contactNumber = json["contactNumber"] as? String ?? ""
    province = json["province"] as? String ?? ""
    nearestTown = json["nearestTown"] as? String ?? ""
    details = json["further"] as? String ?? ""
    tempory = nil
  }
  
  func json() -> [String: [String: Any]] {
    return [id: [
      "name": name,
      "companyName": companyName,
      "email": email,
      "contactNumber": contactNumber,
      "province": province,
      "neartestTown": nearestTown,
      "further": details
    ]]
  }
}

extension Farm: Equatable {
  static func == (lhs: Farm, rhs: Farm) -> Bool {
    return lhs.id == rhs.id
      && lhs.name == rhs.name
      && lhs.companyName == rhs.companyName
      && lhs.email == rhs.email
      && lhs.contactNumber == rhs.contactNumber
      && lhs.province == rhs.province
      && lhs.nearestTown == rhs.nearestTown
      && lhs.details == rhs.details
  }
}

extension Farm: CustomStringConvertible {
  var description: String {
    return name
  }
}
