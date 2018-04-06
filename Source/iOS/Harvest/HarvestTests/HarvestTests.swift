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
    let coord = CLLocationCoordinate2DMake(25, -20)
    let date = Date(timeIntervalSince1970: 100000)
    
    HarvestDB.collect(yield: 10.5,
                      from: "dummy@gmail.com",
                      inAmountOfSeconds: 123.45,
                      at: coord,
                      on: date)
    
    
    HarvestDB.yieldCollection(for: "dummy@gmail.com", on: date) { (snapshot) in
      guard let child = snapshot.value as? [String: Any] else {
        XCTAssert(false, "not a valid snapshot")
        return
      }
      
      guard let yield = child["yield"] as? Double else {
        XCTAssert(false, "not a valid snapshot")
        return
      }
      
      guard let email = child["email"] as? String else {
        XCTAssert(false, "not a valid snapshot")
        return
      }
      
      guard let duration = child["duration"] as? Double else {
        XCTAssert(false, "not a valid snapshot")
        return
      }
      
      guard let loc = child["location"] as? [String: Any] else {
        XCTAssert(false, "not a valid snapshot")
        return
      }
      guard let lat = loc["lat"] as? Double else {
        XCTAssert(false, "not a valid snapshot")
        return
      }
      guard let lng = loc["lng"] as? Double else {
        XCTAssert(false, "not a valid snapshot")
        return
      }
      
      guard let cdateinterval = child["date"] as? Double else {
        XCTAssert(false, "not a valid snapshot")
        return
      }
      let cdate = Date(timeIntervalSince1970: cdateinterval)
      print("foooooo")
      XCTAssertEqual(yield, 10.5)
      XCTAssertEqual(email, "dummy@gmail.com")
      XCTAssertEqual(duration, 123.45)
      XCTAssertEqual(lat, 25.0)
      XCTAssertEqual(lng, -20)
      XCTAssertEqual(date, cdate)
    }
    
  }
    
  func testLogin() {
//    HarvestDB.signIn(withEmail: "letanyan.a@gmail.com", andPassword: "letanyan", on: <#T##UIViewController#>)
  }
    
}
