//
//  HarvestTests.swift
//  HarvestTests
//
//  Created by Letanyan Arumugam on 2018/03/26.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import XCTest
import CoreLocation
@testable import Harvest

class HarvestTests: XCTestCase {
    
  override func setUp() {
    super.setUp()
    continueAfterFailure = true
    
    updateMockDatabase()
    let ref = DatabaseReferenceMock(path: "", info: [:])
    HarvestDB.ref = ref
  }
  
  override func tearDown() {
      // Put teardown code here. This method is called after the invocation of each test method in the class.
      super.tearDown()
  }
  
  func setUpYieldTracker() -> (Tracker, [CLLocationCoordinate2D], [String: Worker]) {
    var tracker = Tracker(wid: "1a")
    
    let workerA = Worker(json: ["name": "Carl", "surname": "Carlos"], id: "3c")
    let workerB = Worker(json: ["name": "Doug", "surname": "Douglas"], id: "4d")
    
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
    
    let selOrc = "1I"
    
    tracker.collect(for: workerA, at: loc0, selectedOrchard: selOrc)
    tracker.collect(for: workerB, at: loc2, selectedOrchard: selOrc)
    tracker.collect(for: workerA, at: loc1, selectedOrchard: selOrc)
    tracker.collect(for: workerB, at: loc2, selectedOrchard: selOrc)
    tracker.collect(for: workerB, at: loc1, selectedOrchard: selOrc)
    
    return (tracker, [loc0.coordinate, loc1.coordinate, loc2.coordinate], ["3c": workerA, "4d": workerB])
  }
  
  func testYieldCollectionAmount() {
    let (tracker, _, workers) = setUpYieldTracker()
    
    let collectionPointsA = tracker
      .collections[workers["3c"]!]?
      .map { $0.location }
    
    let collectionPointsB = tracker
      .collections[workers["4d"]!]?
      .map { $0.location }
    
    XCTAssertEqual(collectionPointsA?.count, 2)
    XCTAssertEqual(collectionPointsB?.count, 3)
    
    tracker.storeSession()
    
    let w3c = π.mockDB["foo/sessions/\(π.keyCount - 1)/collections/3c"]
    let w4d = π.mockDB["foo/sessions/\(π.keyCount - 1)/collections/4d"]
    
    XCTAssertEqual(w3c.dictionary!.count, 2)
    XCTAssertEqual(w4d.dictionary!.count, 3)
    
    tracker.discardState()
  }
  
  func testYieldCollectionCoords() {
    let (tracker, locs, workers) = setUpYieldTracker()
    
    let collectionPointsA = tracker
      .collections[workers["3c"]!]?
      .map { $0.location }
    
    let collectionPointsB = tracker
      .collections[workers["4d"]!]?
      .map { $0.location }
    
    XCTAssertEqual(collectionPointsA, [locs[0], locs[1]])
    XCTAssertEqual(collectionPointsB, [locs[2], locs[2], locs[1]])
    
    tracker.discardState()
  }
  
  func testYieldCollectionClocking() {
    let (tracker, _, _) = setUpYieldTracker()
    let d = Date()
    
    XCTAssertLessThan(tracker.sessionStart, d)
    
    tracker.discardState()
  }
  
  func testLocationTracking() {
    var tracker = Tracker(wid: "1a")
    
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
      _ = tracker.track(location: loc)
    }
    
    let locsTuple = locs.map { (loc: CLLocation) -> (Double, Double) in
      let lat = loc.coordinate.latitude
      let lng = loc.coordinate.longitude
      return (lat, lng)
    }
    
    for (x, y) in zip(tracker.pathTracked(), locsTuple) {
      XCTAssertEqual(x.latitude, y.0)
      XCTAssertEqual(x.longitude, y.1)
      
    }
    
    tracker.storeSession()
    
    let trackmock = π.mockDB["foo/sessions/\(π.keyCount - 1)/track"].array!
    
    for (i, loc) in trackmock.enumerated() {
      let lat = loc["lat"].double!
      let lng = loc["lng"].double!
      XCTAssertEqual(lat, locs[i].coordinate.latitude)
      XCTAssertEqual(lng, locs[i].coordinate.longitude)
    }
    
    tracker.discardState()
  }
  
  func testAddingDeletingOrchard() {
    HarvestDB.getOrchards { (orchards) in
      for orchard in orchards {
        if orchard.id == "1I" {
          XCTAssertEqual(orchard.name, "orc1")
          
          XCTAssertEqual(orchard.coords[0].latitude, 0.1)
          XCTAssertEqual(orchard.coords[0].longitude, 1.0)
          
          XCTAssertEqual(orchard.coords[1].latitude, 0.1)
          XCTAssertEqual(orchard.coords[1].longitude, 2.0)
          
          XCTAssertEqual(orchard.coords[2].latitude, 2.0)
          XCTAssertEqual(orchard.coords[2].longitude, 2.0)
        } else if orchard.id == "2II" {
          XCTAssertEqual(orchard.name, "orc2")
          
          XCTAssertEqual(orchard.coords[0].latitude, 3.1)
          XCTAssertEqual(orchard.coords[0].longitude, 1.0)
          
          XCTAssertEqual(orchard.coords[1].latitude, 0.1)
          XCTAssertEqual(orchard.coords[1].longitude, 0.0)
          
          XCTAssertEqual(orchard.coords[2].latitude, 2.0)
          XCTAssertEqual(orchard.coords[2].longitude, 2.0)
        }
      }
      
      XCTAssertEqual(π.mockDB["foo/orchards/1I"], [
        "name": "orc1",
        "coords": [
          ["lat": 0.1, "lng": 1.0],
          ["lat": 0.1, "lng": 2.0],
          ["lat": 2.0, "lng": 2.0]
        ]
        ])
      HarvestDB.delete(orchard: orchards.first(where: {$0.id == "1I"})!) { (error, ref) in
        XCTAssertEqual(π.mockDB["foo/orchards/1I"], JSON.none)
      }
    }
  }
  
  func testModifyOrchard() {
    HarvestDB.getOrchards { (orchards) in
      let o = orchards.first(where: { $0.id == "2II" })!
      o.crop = "42"
      HarvestDB.save(orchard: o)
      HarvestDB.getOrchards({ (orchards) in
        let oo = orchards.first(where: { $0.id == "2II" })!
        XCTAssertEqual(oo.crop, "42")
      })
    }
  }
  
  func testAddingDeletingWorker() {
    HarvestDB.getWorkers { (workers) in
      for worker in workers {
        if worker.id == "1a" {
          XCTAssertEqual(worker.name, "Andy")
          
          XCTAssertEqual(worker.assignedOrchards, ["1I", "2II"])
          XCTAssertEqual(worker.kind, .foreman)
        } else if worker.id == "2b" {
          XCTAssertEqual(worker.name, "Beth")
        }
      }
      
      XCTAssertEqual(π.mockDB["foo/workers/1a"], [
        "name": "Andy",
        "surname": "Andrews",
        "type": "Foreman",
        "phoneNumber": "1234567890",
        "assignedOrchards": ["1I", "2II"]
      ])
      HarvestDB.delete(worker: workers.first(where: {$0.id == "1a"})!) { (error, ref) in
        XCTAssertEqual(π.mockDB["foo/workers/1a"], JSON.none)
      }
    }
  }
  
  func testModifyWorker() {
    HarvestDB.getWorkers { (workers) in
      let o = workers.first(where: { $0.id == "2b" })!
      o.tempory = Worker.init(json: o.json()["2b"]!, id: "2b")
      o.tempory?.idNumber = "42"
      HarvestDB.save(worker: o.tempory!, oldWorker: o)
      HarvestDB.getWorkers({ (workers) in
        let oo = workers.first(where: { $0.id == "2b" })!
        XCTAssertEqual(oo.idNumber, "42")
      })
    }
  }
  
  func testAddingDeletingFarm() {
    HarvestDB.getFarms { (farms) in
      for farm in farms {
        if farm.id == "A1" {
          XCTAssertEqual(farm.name, "farm1")
        } else if farm.id == "2b" {
          XCTAssertEqual(farm.name, "farm2")
        }
      }
      
      XCTAssertEqual(π.mockDB["foo/farms/A1"], [
        "name": "farm1",
      ])
      HarvestDB.delete(farm: farms.first(where: {$0.id == "A1"})!) { (error, ref) in
        XCTAssertEqual(π.mockDB["foo/farm/A1"], JSON.none)
      }
    }
  }
  
  func testModifyFarm() {
    HarvestDB.getFarms { (farms) in
      let o = farms.first(where: { $0.id == "A2" })!
      o.tempory = Farm.init(json: o.json()["A2"]!, id: "A2")
      o.tempory?.name = "bar"
      HarvestDB.save(farm: o.tempory!)
      HarvestDB.getFarms({ (farms) in
        let oo = farms.first(where: { $0.id == "A2" })!
        XCTAssertEqual(oo.name, "bar")
      })
    }
  }
    
}
