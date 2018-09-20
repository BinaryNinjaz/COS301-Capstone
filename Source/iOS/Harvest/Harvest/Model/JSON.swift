//
//  JSON.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/09/20.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Swift

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
  case truth(Bool)
  case number(Double)
  case text(String)
  case list([JSON])
  case json([String: JSON])
  case none
  
  var bool: Bool? {
    if case let .truth(b) = self {
      return b
    }
    return nil
  }
  
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
        case .truth, .number, .text, .none: fatalError()
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
    case .truth, .text, .number:
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
    case let .truth(b): return b
    case let .number(d): return d
    case let .text(s): return s
    case let .list(a): return a.map { $0.value }
    case let .json(j): return j.mapValues { $0.value }
    case .none: return nil
    }
  }
  
  static func == (lhs: JSON, rhs: JSON) -> Bool {
    switch (lhs, rhs) {
    case let (.truth(a), .truth(b)):
      return a == b
      
    case let (.number(a), .number(b)):
      return a == b
      
    case let (.text(a), .text(b)):
      return a == b
        
    case let (.list(a), .list(b)):
      return a == b
        
    case let (.json(a), .json(b)):
      return a == b
        
    case (.none, .none):
      return true
      
    default:
      return false
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
    case let b as Bool: self = .truth(b)
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
      if let v = JSON(any: v) {
        result[k.description] = v
      }
    }
    return result
  }
}

extension JSON: CustomStringConvertible, CustomDebugStringConvertible {
  var debugDescription: String {
    switch self {
    case .truth(let b): return b.description
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
