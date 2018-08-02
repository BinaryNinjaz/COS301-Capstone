//
//  DBMock.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/10.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase

func merge(path: String, withKey k: String) -> String {
  var k = k
  if k.hasPrefix(path) {
    return k
  }
  let p: String
  if path.hasSuffix("/") {
    p = String(path.dropLast())
  } else {
    p = path
  }
  if k.hasPrefix("/") {
    k.remove(at: k.startIndex)
  }
  return p + "/" + k
}

indirect enum JSON: Equatable {
  case number(Double)
  case text(String)
  case list([JSON])
  case json([String: JSON])
  case none
  
  var double: Double? {
    if case let .number(d) = self {
      return d
    }
    return nil
  }
  
  var string: String? {
    if case let .text(s) = self {
      return s
    }
    return nil
  }
  
  var array: [JSON]? {
    if case let .list(a) = self {
      return a
    }
    return nil
  }
  
  var dictionary: [String: JSON]? {
    if case let .json(d) = self {
      return d
    }
    return nil
  }
  
  subscript(path: String) -> JSON {
    get {
      let comps = path.split(separator: "/").map(String.init)
      var result = self
      for comp in comps {
        switch result {
        case .number, .text, .none: fatalError()
        case let .list(items):
          let idx = Int(comp)!
          result = items[idx]
        case let .json(items):
          result = items[comp] ?? .none
        }
      }
      return result
    }
  }
  
  mutating func updateValue(atPath p: String, with v: JSON) {
    self = setValue(atPath: p, with: v)
  }
  
  func setValue(atPath p: String, with v: JSON) -> JSON {
    guard let comp = p.split(separator: "/").first else {
      return v
    }
    
    let rest = p.split(separator: "/").dropFirst().map(String.init).joined(separator: "/")
    
    switch self {
    case .text:
      return v
    case .number:
      return v
    case let .list(items):
      if let idx = Int(String(comp)) {
        var result = [JSON]()
        for (i, item) in items.enumerated() {
          result.append(i == idx
            ? item.setValue(atPath: rest, with: v)
            : item)
        }
        return .list(result)
      }
      return .list([])
    case let .json(items):
      var result = [String: JSON]()
      var matched = false
      for (s, item) in items {
        let compMatch = String(comp) == s
        matched = matched || compMatch
        result[s] = compMatch
          ? item.setValue(atPath: rest, with: v)
          : item
      }
      if !matched {
        result[String(comp)] = v
      }
      return .json(result)
    case .none:
      return v
    }
  
  }
  
  var value: Any? {
    switch self {
    case let .number(d): return d
    case let .text(s): return s
    case let .list(a): return a.map { $0.value }
    case let .json(j): return j.mapValues { $0.value }
    case .none: return nil
    }
  }
  
  static func == (lhs: JSON, rhs: JSON) -> Bool {
    switch lhs {
    case let .number(a):
      if case let .number(b) = rhs {
        return a == b
      } else {
        return false
      }
    case let .text(a):
      if case let .text(b) = rhs {
        return a == b
      } else {
        return false
      }
    case let .list(a):
      if case let .list(b) = rhs {
        return a == b
      } else {
        return false
      }
    case let .json(a):
      if case let .json(b) = rhs {
        return a == b
      } else {
        return false
      }
    case .none:
      return rhs == .none
    }
  }
}

extension JSON: ExpressibleByIntegerLiteral, ExpressibleByFloatLiteral {
  typealias IntegerLiteralType = Int
  init(integerLiteral: Int) {
    self = .number(Double(integerLiteral))
  }
  
  typealias FloatLiteralType = Double
  init(floatLiteral: Double) {
    self = .number(floatLiteral)
  }
}

extension JSON: ExpressibleByStringLiteral {
  typealias ExtendedGraphemeClusterLiteralType = String
  typealias StringLiteralType = String
  typealias UnicodeScalarLiteralType = String
  
  init(extendedGraphemeClusterLiteral: String) {
    self = .text(extendedGraphemeClusterLiteral)
  }
  
  init(stringLiteral: String) {
    self = .text(stringLiteral)
  }
  
  init(unicodeScalarLiteral: String) {
    self = .text(unicodeScalarLiteral)
  }
}

extension JSON: ExpressibleByArrayLiteral {
  typealias ArrayLiteralElement = JSON
  
  init(arrayLiteral: JSON...) {
    self = .list(arrayLiteral)
  }
  
  init?(any a: Any) {
    switch a {
    case let d as Double: self = .number(d)
    case let s as String: self = .text(s)
    case let a as [Any]:
      var result = [JSON]()
      for x in a {
        if let y = JSON(any: x) {
          result.append(y)
        }
      }
      self = .list(result)
    case let d as [String: Any]:
      var result = [String: JSON]()
      for (k, v) in d {
        if let y = JSON(any: v) {
          result[k] = y
        }
      }
      self = .json(result)
    default:
      return nil
    }
  }
}

extension JSON: ExpressibleByDictionaryLiteral {
  typealias Key = String
  typealias Value = JSON
  
  init(dictionaryLiteral: (Key, Value)...) {
    self = .json(Dictionary.init(uniqueKeysWithValues: dictionaryLiteral))
  }
  
  init(_ dictionary: [AnyHashable: Any]) {
    self = .json(dictionary.stringKeyedJSON())
  }
}

extension Dictionary where Key == AnyHashable, Value == Any {
  func stringKeyedJSON() -> [String: JSON] {
    var result = [String: JSON]()
    for (k, v) in self {
      if let k = k.base as? String, let v = JSON(any: v) {
        result[k] = v
      }
    }
    return result
  }
}

extension JSON: CustomStringConvertible, CustomDebugStringConvertible {
  var debugDescription: String {
    switch self {
    case .number(let n): return n.debugDescription
    case .text(let s): return s.debugDescription
    case .list(let a): return a.debugDescription
    case .json(let d): return d.debugDescription
    case .none: return "nil"
    }
  }
  
  private func paddedChildren(with pad: Int, for json: JSON, _ initialPad: Bool) -> String {
    let repeatChar: (Character, Int) -> String = { (c, n) in
      var result = ""
      for _ in 0..<n {
        result.append(c)
      }
      return result
    }
    
    let distance = repeatChar(" ", pad)
    let d1 = repeatChar(" ", pad - 1)
    
    var result = "\(initialPad ? d1 : ""){\n"
    
    if case let .json(d) = json {
      for (k, v) in d {
        switch v {
        case .json(let j):
          let p = paddedChildren(with: pad + 1, for: .json(j), false)
          result += distance + "\(k): " + p
          
        case .list(let a):
          let p = paddedChildren(with: pad + 1, for: .list(a), false)
          result += distance + "\(k): " + p
          
        default:
          result += distance + "\(k): " + v.description + "\n"
        }
      }
    } else if case let .list(a) = json {
      let r = a.map { j -> String in
        var res = paddedChildren(with: pad, for: j, false)
        if res.last == "\n" { res.removeLast() }
        return res
      }
      
      let rf = r.dropFirst()
      let f = rf.reduce(r.first ?? "") { (r: String, i: String) -> String in
        return r + ", " + i
      }
      
      return "[" + f + "]\n"
    } else if case let .number(d) = json {
      return d.description
    } else if case let .text(s) = json {
      return s
    }
    
    return result + "\(d1)}\n"
  }
  
  var description: String {
    return paddedChildren(with: 1, for: self, true)
  }
}

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
    case .number: fatalError()
    case .text: fatalError()
    case .list: fatalError()
    case let .json(d): return d.keys.first!
    case .none: fatalError()
    }
  }
  
  override var value: Any? {
    switch info {
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
