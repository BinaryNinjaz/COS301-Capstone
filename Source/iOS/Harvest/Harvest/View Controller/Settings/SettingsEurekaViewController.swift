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
    
    
    let nameRow = NameRow() { row in
      row.title = "Organization Name"
      row.value = HarvestUser.current.myOrganizationName
      row.placeholder = HarvestUser.current.myOrganizationName
    }.onChange { row in
      HarvestUser.current.myOrganizationName = row.value ?? ""
    }.cellUpdate { (cell, row) in
      cell.textField.textAlignment = .left
      cell.titleLabel?.textColor = .titleLabel
      cell.textField.clearButtonMode = .whileEditing
    }
    
    let organizationRow = PickerRow<OrganizationInfo>() { row in
      row.options = []
      var aOrgan = OrganizationInfo(nil, "My Organization")
      
      row.options.append(aOrgan)
      for organ in HarvestUser.current.workingForIDs {
        let o = OrganizationInfo(organ.uid, organ.name)
        row.options.append(o)
        if o.uid == HarvestUser.current.selectedOrganizationUID {
          aOrgan = o
        }
      }
      row.value = aOrgan
    }.onChange { row in
      HarvestUser.current.selectedOrganizationUID = row.value?.uid == "My Organization"
        ? nil
        : row.value?.uid
    }
    
    
    let userRow = """
      \(HarvestUser.current.email) (\(HarvestUser.current.displayName))
      """
    
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
    
    form
      +++ Section("Organization Name")
      <<< nameRow
      
      +++ Section("Current Organization")
      <<< organizationRow
      
      +++ Section(userRow)
      <<< adminRow
      <<< logoutRow
  }
  
}
