//
//  LinearEquation.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/09/06.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Darwin

public struct LinearEquation<Number: BinaryFloatingPoint>: CustomStringConvertible {
  let y, x, c: Number
  
  init(p1: Point<Number>, p2: Point<Number>) {
    if p1.x == p2.x {
      x = 1
      y = 0
      c = p1.x
      // (y - y1) = m(x - x1)
    } else {
      let m = (p2.y - p1.y) / (p2.x - p1.x)
      x = -m
      y = 1
      c = m * -p1.x + p1.y
    }
  }
  
  public var description: String {
    return "y = \(x)x + \(c)"
  }
  
  func intersects(line: LinearEquation<Number>) -> Point<Number> {
    // 1  0.8 1
    // 1 -0.8 5
    // --------
    // 1  0.8 1
    // 0 -1.6 4
    
    // x = 4/-1.6
    // y = 1 - 0.8x
    
    let mr = (line.y / y)
    let (_, rx, rc) = (line.y - mr * y, line.x - mr * x, line.c - mr * c)
    
    let xr = rc / rx
    let yr = c - x * xr
    
    return Point(xr, yr)
  }
}
