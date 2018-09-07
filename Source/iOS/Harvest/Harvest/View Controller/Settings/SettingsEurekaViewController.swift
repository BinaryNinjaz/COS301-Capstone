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
  var generatorViewController: FormViewController?
  
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
    } else {
      let gen = ButtonRow { row in
        row.title = "Generate Sessions"
      }.onCellSelection { _, _ in
        let fvc = FormViewController(style: UITableViewStyle.grouped)
        self.generatorSection(formVC: fvc)
        
        self.present(fvc, animated: true, completion: nil)
      }
      
      form +++ Section("Generate") <<< gen
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
        https://harvestapp.co.za/HarvestUserManual.pdf
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

extension SettingsEurekaViewController {
  func generatorSection(formVC: FormViewController) {
    let form = formVC.form
    let start = DateRow { row in
      row.title = "Start Date"
      row.value = Date().thisYear().0
    }
    
    let end = DateRow { row in
      row.title = "End Date"
      row.value = Date().thisYear().1
    }
    
    let selectionSection = SelectableSection<ListCheckRow<Worker>>(
      "Foremen that'll work",
      selectionType: .multipleSelection)
    
    for entity in Entities.shared.foremen() {
      selectionSection <<< ListCheckRow<Worker>(entity.description) { row in
        row.title = entity.description
        row.selectableValue = entity
        row.value = entity
      }
    }
    
    let generateButton = ButtonRow { row in
      row.title = "Generate"
    }.onCellSelection { _, _ in
      let s = start.value ?? Date()
      let e = end.value ?? Date()
      let fs = selectionSection.selectedRows().compactMap { $0.value }
      
      let generator = SessionsGenerator(period: (s, e), foremen: fs)
      let sessions = generator.generateSessions()
      StoredGeneratedSessions.shared.accumulateByDay(with: sessions)
    }
    
    let dismissButton = ButtonRow { row in
      row.title = "Dismiss"
    }.onCellSelection { _, _ in
      formVC.dismiss(animated: true, completion: nil)
    }
    
    let pushButton = ButtonRow { row in
      row.title = "Push"
    }.onCellSelection { _, _ in
      HarvestDB.save(sessions: StoredGeneratedSessions.shared)
    }
    
    form
      +++ Section("Date")
      <<< start
      <<< end
    
      +++ selectionSection
    
      +++ Section() <<< generateButton
    
      +++ Section() <<< dismissButton
    
      +++ Section() <<< pushButton
  }
}
