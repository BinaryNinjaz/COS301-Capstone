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

  func testYieldCollection() {
    
    let app = XCUIApplication()
    let cellsQuery = app.collectionViews.cells
    let trissMerigoldElementsQuery = cellsQuery.otherElements.containing(.staticText, identifier:"Triss Merigold")
    let staticText = trissMerigoldElementsQuery.staticTexts["0"]
    staticText.tap()
    app.alerts["Session Not Started"].buttons["Okay"].tap()
    app.buttons["Start"].tap()
    cellsQuery.otherElements.containing(.staticText, identifier:"Zoltan Chivay").staticTexts["0"].tap()
    cellsQuery.otherElements.containing(.staticText, identifier:"Keira Metz").staticTexts["0"].tap()
    staticText.tap()
    trissMerigoldElementsQuery.staticTexts["1"].tap()
    app.buttons["Stop"].tap()
    
  }
}
