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
  
  func setUpYieldTracker() -> (Tracker, [CLLocation], [String: Worker]) {
    var tracker = Tracker()
    
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
    
    return (tracker, [loc0, loc1, loc2], ["A": workerA, "B": workerB])
  }
  
  func testYieldCollectionAmount() {
    let (tracker, _, workers) = setUpYieldTracker()
    
    let collectionPointsA = tracker
      .collections[workers["A"]!]?
      .collectionPoints
      .map { $0.location }
    
    let collectionPointsB = tracker
      .collections[workers["B"]!]?
      .collectionPoints
      .map { $0.location }
    
    XCTAssertEqual(collectionPointsA?.count, 2)
    XCTAssertEqual(collectionPointsB?.count, 3)
  }
  
  func testYieldCollectionCoords() {
    let (tracker, locs, workers) = setUpYieldTracker()
    
    let collectionPointsA = tracker
      .collections[workers["A"]!]?
      .collectionPoints
      .map { $0.location }
    
    let collectionPointsB = tracker
      .collections[workers["B"]!]?
      .collectionPoints
      .map { $0.location }
    
    XCTAssertEqual(collectionPointsA, [locs[0], locs[1]])
    XCTAssertEqual(collectionPointsB, [locs[2], locs[2], locs[1]])
  }
  
  func testYieldCollectionClocking() {
    let (tracker, _, _) = setUpYieldTracker()
    let d = Date()
    
    XCTAssertLessThan(tracker.sessionStart, d)
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
