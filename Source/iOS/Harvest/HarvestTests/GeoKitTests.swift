//
//  GeoKitTests.swift
//  HarvestTests
//
//  Created by Letanyan Arumugam on 2018/04/16.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import XCTest
@testable import Harvest

class GeoKitTests: XCTestCase {
    
  func testEdgeLength() {
    let a = Point(10, 20)
    let b = Point(10, 30)
    let c = Point(20, 30)
    
    XCTAssertEqual(Edge(a, b).length(), 10)
    XCTAssertEqual(Edge(c, b).length(), 10)
    XCTAssertEqual(Edge(a, c).length(), 200.0.squareRoot())
  }
  
  func testEdgeMin() {
    let a = Point(10, 20)
    let b = Point(10, 30)
    let c = Point(20, 30)
    
    XCTAssert(Edge(a, b).min() == (x: 10.0, y: 20.0))
    XCTAssert(Edge(b, c).min() == (x: 10.0, y: 30.0))
    XCTAssert(Edge(a, c).min() == (x: 10.0, y: 20.0))
  }
  
  func testEdgeMax() {
    let a = Point(10, 20)
    let b = Point(10, 30)
    let c = Point(20, 30)
    
    XCTAssert(Edge(a, b).max() == (x: 10.0, y: 30.0))
    XCTAssert(Edge(b, c).max() == (x: 20.0, y: 30.0))
    XCTAssert(Edge(a, c).max() == (x: 20.0, y: 30.0))
  }
  
  func testEdgeGradient() {
    let a = Point(10, 20)
    let b = Point(10, 30)
    let c = Point(20, 30)
    
    XCTAssertEqual(Edge(a, b).gradient(), .infinity)
    XCTAssertEqual(Edge(b, c).gradient(), 0)
    XCTAssertEqual(Edge(a, c).gradient(), 1)
  }
  
  func testEdgeOffset() {
    let a = Point(10, 20)
    let b = Point(10, 30)
    let c = Point(20, 30)
    
    XCTAssertEqual(Edge(a, b).offset(), nil)
    XCTAssertEqual(Edge(b, c).offset(), 30)
    XCTAssertEqual(Edge(a, c).offset(), 10)
  }
  
  func testCreateEdgesFromPoints() {
    let a = Point(10, 20)
    let b = Point(10, 30)
    let c = Point(20, 30)
    
    let edges = createEdges(from: [a, b, c])
    
    let e0 = edges[0]
    let e1 = edges[1]
    let e2 = edges[2]
    
    XCTAssertEqual(Edge(a, b), e0)
    XCTAssertEqual(Edge(b, c), e1)
    XCTAssertEqual(Edge(c, a), e2)
  }
  
  func testEdgeIntersection() {
    let a = Point(10, 20)
    let b = Point(10, 30)
    let c = Point(20, 30)
    
    let p = Edge(a, b).intersection(onLineXEqual: 15)
    let q = Edge(b, c).intersection(onLineXEqual: 15)
    let r = Edge(c, a).intersection(onLineXEqual: 15)
    
    XCTAssertEqual(p, nil)
    XCTAssertEqual(q, Point(15, 30))
    XCTAssertEqual(r, Point(15, 25))
    
    let s = Edge(a, b).intersection(onLineXEqual: 10)
    let t = Edge(b, c).intersection(onLineXEqual: 10)
    let u = Edge(c, a).intersection(onLineXEqual: 10)
    
    XCTAssertEqual(s, nil)
    XCTAssertEqual(t, Point(10, 30))
    XCTAssertEqual(u, Point(10, 20))
  }
  
  func testPolyIntersectionPoints() {
    let a = Point(10, 10)
    let b = Point(40, 10)
    let c = Point(40, 20)
    let d = Point(30, 20)
    let e = Point(30, 30)
    let f = Point(40, 30)
    let g = Point(40, 40)
    let h = Point(10, 40)
    
    
    let s = Poly(a, b, c, d, e, f, g, h)
    
    let ps = s.intersectionPoints(onLineXEqual: 36)
    
    XCTAssertEqual(ps, [Point(36, 10), Point(36, 20), Point(36, 30), Point(36, 40)])
  }
  
  func testPolyIntersectionSegments() {
    let a = Point(10, 10)
    let b = Point(40, 10)
    let c = Point(40, 20)
    let d = Point(30, 20)
    let e = Point(30, 30)
    let f = Point(40, 30)
    let g = Point(40, 40)
    let h = Point(10, 40)
    
    
    let s = Poly(a, b, c, d, e, f, g, h)
    
    let ps = s.intersectionSegments(onLineXEqual: 36)
    
    let e0 = Edge(Point(36, 10), Point(36, 20))
    let e1 = Edge(Point(36, 30), Point(36, 40))
    
    XCTAssertEqual(ps, [e0, e1])
  }
  
  func testPolyContains() {
    let a = Point(10, 10)
    let b = Point(40, 10)
    let c = Point(40, 20)
    let d = Point(30, 20)
    let e = Point(30, 30)
    let f = Point(40, 30)
    let g = Point(40, 40)
    let h = Point(10, 40)
    
    let s = Poly(a, b, c, d, e, f, g, h)
    
    XCTAssertTrue(s.contains(Point(36, 10)))
    XCTAssertTrue(s.contains(Point(36, 16)))
    XCTAssertTrue(s.contains(Point(36, 20)))
    XCTAssertTrue(s.contains(Point(36, 30)))
    XCTAssertTrue(s.contains(Point(36, 40)))
    
    XCTAssertFalse(s.contains(Point(36, 24)))
    
    XCTAssert(s.contains(Point(20, 10)))
    XCTAssert(s.contains(Point(20, 20)))
    XCTAssert(s.contains(Point(20, 30)))
    XCTAssert(s.contains(Point(20, 40)))
    
    XCTAssertFalse(s.contains(Point(50, 50)))
    XCTAssertFalse(s.contains(Point(10, 50)))
  }
  
  
  func testEllipseContains() {
    let major = 10.0
    let minor = 5.0
    let c = Point(1, 3)
    
    let e = Ellip(center: c, majorAxis: major, minorAxis: minor)
    
    XCTAssert(e.contains(Point(2, 4)))
    XCTAssertFalse(e.contains(Point(2, 7)))
    
  }
    
}
