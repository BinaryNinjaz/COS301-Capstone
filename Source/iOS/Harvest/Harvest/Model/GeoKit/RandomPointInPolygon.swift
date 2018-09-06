//
//  RandomPointInPolygon.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/09/06.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Darwin

extension BinaryFloatingPoint {
  static func random() -> Self {
    let r = Double(arc4random()) / Double(UInt32.max)
    
    return r as! Self
  }
}

struct Codomain<T: BinaryFloatingPoint>: CustomStringConvertible {
  var low, high: Point<T>
  
  init(_ p1: Point<T>, _ p2: Point<T>) {
    (low, high) = p1.y > p2.y ? (p2, p1) : (p1, p2)
  }
  
  var description: String {
    return "\(low.y)..\(high.y)"
  }
  
  var range: ClosedRange<T> {
    return low.y...high.y
  }
}

extension Poly {
  func intersections(of line: LinearEquation<Number>) -> [Point<Number>] {
    var result = [Point<Number>]()
    
    for edge in edges {
      guard let p = edge.intersects(line: line), !result.contains(p) else {
        continue
      }
      result.append(p)
    }
    
    return result
  }
  
  func codomains(of line: LinearEquation<Number>) -> [Codomain<Number>] {
    let segments = intersections(of: line)
    let ps = segments.sorted { $0.y < $1.y }
    var result = [Codomain<Number>]()
    
    for pi in stride(from: 0, to: ps.count, by: 2) {
      let a = ps[pi]
      let b = ps[pi + 1]
      result.append(Codomain(a, b))
    }
    
    return result
  }
  
  var domain: ClosedRange<Number> {
    let ps = points.sorted { $0.x < $1.x }
    guard let f = ps.first, let l = ps.last else {
      return 0...0
    }
    return f.x...l.x
  }
  
  func randomDomain() -> Number {
    let r = Number.random()
    let dom = domain
    
    let lb = dom.lowerBound
    let ub = dom.upperBound
    let df = ub - lb
    
    return r * df + lb
  }
  
  func randomCodomain(at x: Number) -> Number {
    // straight vertical line is represeted with points (x, a) (x, b) where a != b. ex. a = 0, b = 1
    let codoms = codomains(of: LinearEquation(p1: Point(x, 0), p2: Point(x, 1)))
    
    let i = Int(arc4random()) % codoms.count
    let codom = codoms[i]
    
    let r = Number.random()
    
    let lb = codom.range.lowerBound
    let ub = codom.range.upperBound
    let df = ub - lb
    
    return abs(r * df) + lb
  }
  
  func randomPoint() -> Point<Number> {
    let x = randomDomain()
    let y = randomCodomain(at: x)
    return Point(x, y)
  }
}
