//
//  SettingsEurekaViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/21.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka

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

class SettingsEurekaViewController: FormViewController {
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    let userRow = HarvestUser.current.accountIdentifier
    
    let adminRow = AdminRow(tag: nil, admin: HarvestUser.current) { row in
      row.title = "Admin"
    }
    
    let logoutRow = ButtonRow { row in
      row.title = "Logout"
    }.onCellSelection { (_, _) in
      HarvestDB.signOut(on: self) { w in
        if w,
          let vc = self
            .storyboard?
            .instantiateViewController(withIdentifier: "signInOptionViewController") {
          self.present(vc, animated: true, completion: nil)
        }
      }
    }
    
    let resignRow = ButtonRow { row in
      row.title = "Resign"
    }.onCellSelection { (_, _) in
      
      let confirmation = UIAlertController(title: "Are You Sure?",
                                           message: """
                                           Are you sure you want to remove yourself \
                                           from association with you current farm?
                                           """,
                                           preferredStyle: .alert)
      
      let cancel = UIAlertAction(title: "Cancel", style: .cancel, handler: nil)
      let confirm = UIAlertAction(title: "Resign", style: .destructive, handler: { _ in
        HarvestDB.resign { _, _ in
          if let vc = self.storyboard?.instantiateViewController(withIdentifier: "signInOptionViewController") {
            self.present(vc, animated: true, completion: nil)
          }
        }
      })
      
      confirmation.addAction(cancel)
      confirmation.addAction(confirm)
      
      self.present(confirmation, animated: true, completion: nil)
      
    }.cellUpdate { (cell, _) in
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
