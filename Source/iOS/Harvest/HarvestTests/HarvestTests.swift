//
//  HarvestTests.swift
//  HarvestTests
//
//  Created by Letanyan Arumugam on 2018/03/26.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import XCTest
import CoreLocation
@testable import Harvest

class HarvestTests: XCTestCase {
    
  override func setUp() {
    super.setUp()
    continueAfterFailure = true
      // Put setup code here. This method is called before the invocation of each test method in the class.
  }
  
  override func tearDown() {
      // Put teardown code here. This method is called after the invocation of each test method in the class.
      super.tearDown()
  }
  
  func testYieldCollection() {
    var tracker = Tracker()
    let d = Date()
    
    let workerA = Worker(firstname: "Andy", lastname: "Andrews")
    let workerB = Worker(firstname: "Ben", lastname: "Bennet")
    
    let rand = {
      return Double(arc4random()) / Double(UInt32.max) * 60 - 30
    }
    
    let la = rand()
    let lb = rand()
    let lc = rand()
    let ld = rand()
    let le = rand()
    let lf = rand()
    
    let loc0 = CLLocation(latitude: la, longitude: lb)
    let loc1 = CLLocation(latitude: lc, longitude: ld)
    let loc2 = CLLocation(latitude: le, longitude: lf)
    
    tracker.collect(for: workerA, at: loc0)
    tracker.collect(for: workerB, at: loc2)
    tracker.collect(for: workerA, at: loc1)
    tracker.collect(for: workerB, at: loc2)
    tracker.collect(for: workerB, at: loc1)
    
    XCTAssertLessThan(tracker.sessionStart, d)
    
    let collectionPointsA = tracker
      .collections[workerA]?
      .collectionPoints
      .map { $0.location }
    
    let collectionPointsB = tracker
      .collections[workerB]?
      .collectionPoints
      .map { $0.location }
    
    XCTAssertEqual(collectionPointsA, [loc0, loc1])
    XCTAssertEqual(collectionPointsB, [loc2, loc2, loc1])
  }
  
  func testLocationTracking() {
    var tracker = Tracker()
    
    let rand = {
      return Double(arc4random()) / Double(UInt32.max) * 60 - 30
    }
    
    let la = rand()
    let lb = rand()
    let lc = rand()
    let ld = rand()
    let le = rand()
    let lf = rand()
    
    let coords = [la, lb, lc, ld, le, lf]
    var locs = [CLLocation]()
    
    for _ in 0..<10 {
      let i = Int(arc4random()) % 6
      let j = Int(arc4random()) % 6
      locs.append(CLLocation(latitude: coords[i], longitude: coords[j]))
    }
    
    for loc in locs {
      tracker.track(location: loc)
    }
    
    let locsTuple = locs.map { (loc: CLLocation) -> (Double, Double) in
      let lat = loc.coordinate.latitude
      let lng = loc.coordinate.longitude
      return (lat, lng)
    }
    
    for (x, y) in zip(tracker.pathTracked(), locsTuple) {
      XCTAssertEqual(x.0, y.0)
      XCTAssertEqual(x.1, y.1)
    }
  }
    
}
