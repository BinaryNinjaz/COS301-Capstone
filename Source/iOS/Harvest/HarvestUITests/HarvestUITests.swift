//
//  HarvestUITests.swift
//  HarvestUITests
//
//  Created by Letanyan Arumugam on 2018/03/26.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import XCTest

class HarvestUITests: XCTestCase {
        
  override func setUp() {
    super.setUp()
    UserDefaults.standard.removeObject(forKey: "password")
    UserDefaults.standard.removeObject(forKey: "username")
  
    // Put setup code here. This method is called before the invocation of each test method in the class.
  
    // In UI tests it is usually best to stop immediately when a failure occurs.
    continueAfterFailure = false
    // UI tests must launch the application that they test. Doing this in setup will make sure it happens for each test method.
    XCUIApplication().launch()

    // In UI tests it’s important to set the initial state - such as interface orientation - required for your tests before they run. The setUp method is a good place to do this.
  }
  
  override func tearDown() {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    super.tearDown()
  }
  
  func testAutoSignIn() {
    
  }

  func testYieldCollection() {
    UserDefaults.standard.set("letanyan.a@gmail.com", forKey: "username")
    UserDefaults.standard.set("letanyan", forKey: "password")
    
    let app = XCUIApplication()
    let textField = app.children(matching: .window).element(boundBy: 0).children(matching: .other).element.children(matching: .other).element.children(matching: .other).element.children(matching: .other).element.children(matching: .other).element.children(matching: .textField).element
    textField.tap()
    app.buttons["Start Session"].tap()
    textField.tap()
    app.buttons["Collect"].tap()
    app.buttons["Stop"].tap()
    
  }
}
