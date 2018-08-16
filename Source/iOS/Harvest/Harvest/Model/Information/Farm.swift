//
//  FarmInfomation.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 University of Pretoria. All rights reserved.
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
    nearestTown = json["town"] as? String ?? ""
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
      "town": nearestTown,
      "further": details
    ]]
  }
  
  func makeChangesPermanent() {
    if let t = tempory {
      name = t.name
      companyName = t.companyName
      email = t.email
      contactNumber = t.contactNumber
      province = t.province
      nearestTown = t.nearestTown
      details = t.details
      tempory = nil
    }
  }
  
  func search(for text: String) -> [(String, String)] {
    var result = [(String, String)]()
    
    let text = text.lowercased()
    
    if name.lowercased().contains(text) {
      result.append(("Name", name))
    }
    
    if companyName.lowercased().contains(text) {
      result.append(("Company Name", companyName))
    }
    
    if email.lowercased().contains(text) {
      result.append(("Email", email))
    }
    
    if contactNumber.lowercased().contains(text) {
      result.append(("Contact Number", contactNumber))
    }
    
    if province.lowercased().contains(text) {
      result.append(("Province", province))
    }
    
    if nearestTown.lowercased().contains(text) {
      result.append(("Nearest Town", nearestTown))
    }
    
    if details.lowercased().contains(text) {
      result.append(("Details", ""))
    }
    
    return result
  }
}

extension Farm: Hashable {
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
  
  var hashValue: Int {
    return id.hashValue
  }
}

extension Farm: CustomStringConvertible {
  var description: String {
    return name
  }
}
