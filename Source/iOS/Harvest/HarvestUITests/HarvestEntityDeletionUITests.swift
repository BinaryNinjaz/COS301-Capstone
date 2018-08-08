//
//  HarvestEntityDeletionUITests.swift
//  HarvestUITests
//
//  Created by Letanyan Arumugam on 2018/07/02.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

// swiftlint:disable line_length
/// swiftlint:disable function_body_length

import XCTest

class HarvestEntityDeletionUITests: XCTestCase {
        
  override func setUp() {
    super.setUp()
    
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
  
  func test1WorkerDeletion() {
    let app = XCUIApplication()
    app.tabBars.buttons["Infomation"].tap()
    app.collectionViews.cells.otherElements.containing(.image, identifier: "Workers").element.tap()
    
    let tablesQuery = app.tables
    tablesQuery/*@START_MENU_TOKEN@*/.staticTexts["Adam Bana"]/*[[".cells.staticTexts[\"Adam Bana\"]",".staticTexts[\"Adam Bana\"]"],[[[-1,1],[-1,0]]],[0]]@END_MENU_TOKEN@*/.tap()
    tablesQuery/*@START_MENU_TOKEN@*/.staticTexts["Delete Worker"]/*[[".cells.staticTexts[\"Delete Worker\"]",".staticTexts[\"Delete Worker\"]"],[[[-1,1],[-1,0]]],[0]]@END_MENU_TOKEN@*/.tap()
  }
  
  func test2OrchardDeletion() {
    let app = XCUIApplication()
    app.tabBars.buttons["Infomation"].tap()
    app.collectionViews.cells.otherElements.containing(.image, identifier: "Orchards").element.tap()
    
    let tablesQuery2 = app.tables
    let tablesQuery = tablesQuery2
    tablesQuery/*@START_MENU_TOKEN@*/.staticTexts["A – B"]/*[[".cells.staticTexts[\"A – B\"]",".staticTexts[\"A – B\"]"],[[[-1,1],[-1,0]]],[0]]@END_MENU_TOKEN@*/.tap()
    
    tablesQuery/*@START_MENU_TOKEN@*/.staticTexts["Delete Orchard"]/*[[".cells.staticTexts[\"Delete Orchard\"]",".staticTexts[\"Delete Orchard\"]"],[[[-1,1],[-1,0]]],[0]]@END_MENU_TOKEN@*/.tap()
  }
  
  func test3FarmDeletion() {
    
    let app = XCUIApplication()
    app.tabBars.buttons["Infomation"].tap()
    app.collectionViews.cells.otherElements.containing(.image, identifier: "Farms").element.tap()
    
    let tablesQuery2 = app.tables
    let tablesQuery = tablesQuery2
    tablesQuery/*@START_MENU_TOKEN@*/.staticTexts["A"]/*[[".cells.staticTexts[\"A\"]",".staticTexts[\"A\"]"],[[[-1,1],[-1,0]]],[0]]@END_MENU_TOKEN@*/.tap()
    
    tablesQuery/*@START_MENU_TOKEN@*/.staticTexts["Delete Farm"]/*[[".cells.staticTexts[\"Delete Farm\"]",".staticTexts[\"Delete Farm\"]"],[[[-1,1],[-1,0]]],[0]]@END_MENU_TOKEN@*/.tap()
  }
    
}
