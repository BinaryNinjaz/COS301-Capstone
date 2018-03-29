//
//  HarvestTests.swift
//  HarvestTests
//
//  Created by Letanyan Arumugam on 2018/03/26.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import XCTest
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
  
  func testTemp() {
    XCTAssertEqual([1, 2, 3].reduce(0, +), 6)
  }
    
  func testLogin() {
//    HarvestDB.signIn(withEmail: "letanyan.a@gmail.com", andPassword: "letanyan", on: <#T##UIViewController#>)
  }
    
}
