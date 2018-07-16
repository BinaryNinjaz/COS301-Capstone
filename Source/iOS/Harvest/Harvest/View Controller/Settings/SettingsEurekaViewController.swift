//
//  SettingsEurekaViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/21.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka

struct OrganizationInfo : CustomStringConvertible, Equatable {
  var uid: String?
  var name: String
  
  init(_ uid: String?, _ name: String) {
    self.uid = uid
    self.name = name
  }
  
  var description: String {
    return name
  }
  
  static func ==(lhs: OrganizationInfo, rhs: OrganizationInfo) -> Bool {
    return lhs.uid == rhs.uid
  }
}

class SettingsEurekaViewController : FormViewController {
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    let userRow = HarvestUser.current.email
    
    let adminRow = AdminRow(tag: nil, admin: HarvestUser.current) { row in
      row.title = "Admin"
    }
    
    let logoutRow = ButtonRow() { row in
      row.title = "Logout"
    }.onCellSelection { (cell, row) in
      HarvestDB.signOut(on: self) { w in
        if w,
          let vc = self
            .storyboard?
            .instantiateViewController(withIdentifier: "signInViewController") {
          self.present(vc, animated: true, completion: nil)
        }
      }
    }
    
    let resignRow = ButtonRow() { row in
      row.title = "Resign"
    }.onCellSelection { (cell, row) in
      HarvestDB.resign { _, _ in
        if let vc = self.storyboard?.instantiateViewController(withIdentifier: "signInViewController") {
          self.present(vc, animated: true, completion: nil)
        }
      }
    }.cellUpdate { (cell, row) in
      cell.textLabel?.textColor = .white
      cell.backgroundColor = .red
    }
    
    if HarvestUser.current.workingForID == nil {
      form
        +++ Section(userRow)
        <<< adminRow
        <<< logoutRow
    } else {
      form
        +++ Section(userRow)
        <<< logoutRow
        +++ Section()
        <<< resignRow
    }
    
    
    
    
  }
  
}
