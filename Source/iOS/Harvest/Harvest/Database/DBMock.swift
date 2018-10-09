//
//  DBMock.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/10.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import FirebaseDatabase

// swiftlint:disable type_name
typealias π = DatabaseReferenceMock

final class DatabaseReferenceMock: DatabaseReference {
  var info: JSON
  static var mockDB: JSON = [:]
  var path: String
  var block: (DataSnapshot) -> Void
  private(set) static var keyCount: Int = 0
  private(set) static var listnerCount: UInt = 0
  
  init(path: String, info: JSON) {
    self.info = info
    self.path = path
    self.block = { _ in }
  }
  
  override func observe(_ eventType: DataEventType, with block: @escaping (DataSnapshot) -> Void) -> UInt {
    self.block = block
    defer { π.listnerCount += 1 }
    return π.listnerCount
  }
  
  override func observeSingleEvent(of eventType: DataEventType, with block: @escaping (DataSnapshot) -> Void) {
    let comps = path.split(separator: "/")
    let key = String(comps.last!)
    block(DataSnapshotMock(info: [key: π.mockDB[path]]))
  }
  
  override func updateChildValues(_ values: [AnyHashable: Any]) {
    let json = values.stringKeyedJSON()
    guard let key = json.keys.first else {
      fatalError()
    }
    
    π.mockDB = π.mockDB.setValue(atPath: merge(path: path, withKey: key), with: json[key]!)
  }
  
  override func child(_ pathString: String) -> DatabaseReferenceMock {
    return DatabaseReferenceMock(path: merge(path: path, withKey: pathString), info: info)
  }
  
  override func childByAutoId() -> DatabaseReferenceMock {
    defer { π.keyCount += 1 }
    return DatabaseReferenceMock(path: path + "/" + π.keyCount.description, info: info)
  }
  
  override func queryOrdered(byChild key: String) -> DatabaseQuery {
    return DatabaseQueryMock(reference: self)
  }
  
  override var key: String {
    return path
  }
}

class DataSnapshotMock: DataSnapshot {
  var info: JSON
  
  init(info: JSON) {
    self.info = info
    super.init()
  }
  
  override var children: NSEnumerator {
    let result: NSEnumerator
    switch info {
    case .truth:
      result = NSArray().objectEnumerator()
    case .number:
      result = NSArray().objectEnumerator()
    case .text:
      result = NSArray().objectEnumerator()
    case let .list(a):
      result = NSArray(array: a.map { DataSnapshotMock(info: $0) as Any }).objectEnumerator()
    case let .json(d):
      if case let .json(dd) = d[d.keys.first!]! {
        let arr = NSArray(array: dd.map { (k, v) in
          return DataSnapshotMock(info: [k: v])
        })
        result = arr.objectEnumerator()
      } else if case let .list(aa) = d[d.keys.first!]! {
        result = NSArray(array: aa.map { DataSnapshotMock(info: $0) as Any }).objectEnumerator()
      } else {
        result = NSArray().objectEnumerator()
      }
    case .none:
      return NSArray().objectEnumerator()
    }
    return result
  }
  
  override var key: String {
    switch info {
    case .truth: fatalError()
    case .number: fatalError()
    case .text: fatalError()
    case .list: fatalError()
    case let .json(d): return d.keys.first!
    case .none: fatalError()
    }
  }
  
  override var value: Any? {
    switch info {
    case let .truth(b): return b
    case let .number(d): return d
    case let .text(s): return s
    case .list: return info.value
    case let .json(d): return d[d.keys.first!]?.value
    case .none: return nil
    }
  }
  
  override var description: String {
    return "Snap(\(key)) \(String(describing: value))"
  }
}

class DatabaseQueryMock: DatabaseQuery {
  var reference: DatabaseReferenceMock
  
  init(reference: DatabaseReferenceMock) {
    self.reference = reference
    super.init()
  }
  
  override func observe(_ eventType: DataEventType, with block: @escaping (DataSnapshot) -> Void) -> UInt {
    return reference.observe(eventType, with: block)
  }
  
  override func observeSingleEvent(of eventType: DataEventType, with block: @escaping (DataSnapshot) -> Void) {
    reference.observeSingleEvent(of: eventType, with: block)
  }
}

func updateMockDatabase() {
  
  let orc1: JSON = [
    "name": "orc1",
    "coords": [
      ["lat": 0.1, "lng": 1.0],
      ["lat": 0.1, "lng": 2.0],
      ["lat": 2.0, "lng": 2.0]
    ],
    "farm": "A1"
  ]
  
  let orc2: JSON = [
    "name": "orc2",
    "coords": [
      ["lat": 3.1, "lng": 1.0],
      ["lat": 0.1, "lng": 0.0],
      ["lat": 2.0, "lng": 2.0]
    ],
    "farm": "A2"
  ]
  
  let farm1: JSON = [
    "name": "farm1"
  ]
  
  let farm2: JSON = [
    "name": "farm2"
  ]
  
  π.mockDB = [
    "foo": [
      "workers": [
        "1a": [
          "name": "Andy",
          "surname": "Andrews",
          "type": "Foreman",
          "phoneNumber": "1234567890",
          "orchards": ["1I", "2II"]
        ],
        "2b": ["name": "Beth", "surname": "Bethany", "type": "Foreman", "phoneNumber": "0987654321"],
        "3c": ["name": "Carl", "surname": "Carlos", "type": "Worker"],
        "4d": ["name": "Doug", "surname": "Douglas", "type": "Worker"],
        "5e": ["name": "Ethan", "surname": "Ethanol", "type": "Worker"],
        "6f": ["name": "Frank", "surname": "Frankly", "type": "Worker"]
      ],
      "foremen": ["1234567890": 1, "0987654321": 1],
      "orchards": ["1I": orc1, "2II": orc2],
      "farms": ["A1": farm1, "A2": farm2],
      "sessions": [:]
    ]
  ]
  
  let ref = DatabaseReferenceMock(path: "", info: [:])
  HarvestDB.ref = ref
}
