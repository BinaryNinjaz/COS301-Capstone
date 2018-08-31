//
//  SettingsEurekaViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/21.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Eureka
import SCLAlertView
import UIKit
import SafariServices

struct OrganizationInfo: CustomStringConvertible, Equatable {
  var uid: String?
  var name: String
  
  init(_ uid: String?, _ name: String) {
    self.uid = uid
    self.name = name
  }
  
  var description: String {
    return name
  }
  
  static func == (lhs: OrganizationInfo, rhs: OrganizationInfo) -> Bool {
    return lhs.uid == rhs.uid
  }
}

class SettingsEurekaViewController: ReloadableFormViewController, SFSafariViewControllerDelegate {
  override func viewDidLoad() {
    super.viewDidLoad()
  }
  
  override func setUp() {
    websiteSection(form: form)
    userSection(form: form)
    helpSection(form: form)
    
    let resignRow = ButtonRow { row in
      row.title = "Resign"
    }.onCellSelection { (_, _) in
      let confirmationAlert = SCLAlertView(appearance: .warningAppearance)
      confirmationAlert.addButton("Cancel", action: {})
      confirmationAlert.addButton("Resign") {
        HarvestDB.resign { _, _ in
          if let vc = self.storyboard?.instantiateViewController(withIdentifier: "signInOptionViewController") {
            self.present(vc, animated: true, completion: nil)
          }
        }
      }
      
      confirmationAlert.showWarning(
        "Are You Sure?",
        subTitle: """
        Are you sure you want to resign from working for "\(HarvestUser.current.organisationName)"?
        """)
      
    }.cellUpdate { (cell, _) in
      cell.textLabel?.textColor = .white
      cell.backgroundColor = .red
    }
    
    if !HarvestUser.current.workingForID.isEmpty { // is foreman
      form
        +++ Section()
        <<< resignRow
    }
  }
  
  func userSection(form: Form) {
    let userRow = HarvestUser.current.accountIdentifier
    
    let adminRow = AdminRow(tag: nil) { row in
      row.title = userRow
    }
    
    let logoutRow = ButtonRow { row in
      row.title = "Logout"
    }.onCellSelection { (_, _) in
      HarvestDB.signOut { w in
        if w,
          let vc = self
            .storyboard?
            .instantiateViewController(withIdentifier: "signInOptionViewController") {
          self.present(vc, animated: true, completion: nil)
        }
      }
    }
    
    if HarvestUser.current.workingForID.isEmpty { // is farmer
      form
        +++ Section("Admin")
        <<< adminRow
        <<< logoutRow
    } else { // is foreman
      form
        +++ Section(userRow)
        <<< logoutRow
    }
  }
  
  func helpSection(form: Form) {
    let welcomeScreenRow = ButtonRow { row in
      row.title = "Welcome Screen"
    }.onCellSelection { _, _ in
      let avc = self
        .storyboard?
        .instantiateViewController(withIdentifier: "carouselViewController") as! CarouselViewController
      avc.showIntro()
      
      self.present(avc, animated: true, completion: nil)
    }
    
    let tutorialScreenRow = ButtonRow { row in
      row.title = "Tutorial"
    }.onCellSelection { _, _ in
      let avc = self
        .storyboard?
        .instantiateViewController(withIdentifier: "carouselViewController") as! CarouselViewController
      avc.showTutorial()
      
      self.present(avc, animated: true, completion: nil)
    }
    
    let userManualRow = ButtonRow { row in
      row.title = "User Manual"
    }.onCellSelection { _, _ in
      let urlString = """
        https://github.com/BinaryNinjaz/COS301-Capstone/blob/master/Documents/User%20Manual/HarvestUserManual.pdf
        """
      if let url = URL(string: urlString) {
        let controller = SFSafariViewController(url: url)
        self.present(controller, animated: true, completion: nil)
        controller.delegate = self
      } else {
        let alert = SCLAlertView()
        alert.showError("Cannot Open", subTitle: "An error occurred trying to open the User Manual.")
      }
    }
    
    if HarvestUser.current.workingForID.isEmpty { // is farmer
      form
        +++ Section("Help")
        <<< tutorialScreenRow
        <<< welcomeScreenRow
        <<< userManualRow
      
    } else { // is foreman
      form
        +++ Section("Help")
        <<< tutorialScreenRow
        <<< userManualRow
    }
  }
  
  func websiteSection(form: Form) {
    let websiteRow = ButtonRow { row in
      row.title = "harvestapp.co.za"
    }.onCellSelection { _, _ in
      let urlString = """
      https://harvestapp.co.za
      """
      if let url = URL(string: urlString) {
        let controller = SFSafariViewController(url: url)
        self.present(controller, animated: true, completion: nil)
        controller.delegate = self
      } else {
        let alert = SCLAlertView()
        alert.showError("Cannot Open", subTitle: "An error occurred trying to open the User Manual.")
      }
    }
    
    form +++ Section("Companion Website") <<< websiteRow
  }
  
  override func tearDown() {
    form.removeAll()
  }
  
  func safariViewControllerDidFinish(_ controller: SFSafariViewController) {
    controller.dismiss(animated: true, completion: nil)
  }
}
