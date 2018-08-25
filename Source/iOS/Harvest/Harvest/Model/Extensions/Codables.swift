//
//  Codables.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/21.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Foundation
import CoreLocation

extension CLLocationCoordinate2D: Codable {
  enum CodingKeys: String, CodingKey {
    case latitude
    case longitude
  }
  
  public init(from decoder: Decoder) throws {
    let values = try decoder.container(keyedBy: CodingKeys.self)
    self.init()
    latitude = try values.decode(Double.self, forKey: .latitude)
    longitude = try values.decode(Double.self, forKey: .longitude)
  }
  
  public func encode(to encoder: Encoder) throws {
    var container = encoder.container(keyedBy: CodingKeys.self)
    try container.encode(latitude, forKey: .latitude)
    try container.encode(longitude, forKey: .longitude)
  }
}

extension CollectionPoint: Codable {
  enum CodingKeys: String, CodingKey {
    case location
    case date
    case selectedOrchard
  }
  
  public init(from decoder: Decoder) throws {
    let values = try decoder.container(keyedBy: CodingKeys.self)
    let loc = try values.decode(CLLocationCoordinate2D.self, forKey: .location)
    location = loc
    date = try values.decode(Date.self, forKey: .date)
    orchard = Entities.shared.orchards.first { $0.value.contains(loc) }?.value
    selectedOrchard = try values.decode(String.self, forKey: .selectedOrchard)
  }
  
  public func encode(to encoder: Encoder) throws {
    var container = encoder.container(keyedBy: CodingKeys.self)
    try container.encode(location, forKey: .location)
    try container.encode(date, forKey: .date)
  }
}

extension Worker: Codable {
  enum CodingKeys: String, CodingKey {
    case firstname
    case lastname
    case assignedOrchards
    case kind
    case details
    case phoneNumber
    case id
  }
  
  public convenience init(from decoder: Decoder) throws {
    let values = try decoder.container(keyedBy: CodingKeys.self)
    self.init(json: [:], id: "")
    
    firstname = try values.decode(String.self, forKey: .firstname)
    lastname = try values.decode(String.self, forKey: .lastname)
    assignedOrchards = try values.decode(Array<String>.self,
                                         forKey: .assignedOrchards)
    kind = try values.decode(Worker.Kind.self, forKey: .kind)
    details = try values.decode(String.self, forKey: .details)
    phoneNumber = try values.decode(String.self, forKey: .phoneNumber)
    id = try values.decode(String.self, forKey: .id)
  }
  
  public func encode(to encoder: Encoder) throws {
    var container = encoder.container(keyedBy: CodingKeys.self)
    
    try container.encode(firstname, forKey: .firstname)
    try container.encode(lastname, forKey: .lastname)
    try container.encode(assignedOrchards, forKey: .assignedOrchards)
    try container.encode(kind, forKey: .kind)
    try container.encode(details, forKey: .details)
    try container.encode(phoneNumber, forKey: .phoneNumber)
    try container.encode(id, forKey: .id)
  }
}
