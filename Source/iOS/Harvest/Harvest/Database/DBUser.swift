//
//  DBUser.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/24.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase
import CoreLocation
import GoogleSignIn

final class HarvestUser {
  var workingForIDs: [(uid: String, name: String)]
  var email: String
  var displayName: String
  var uid: String
  var myOrganizationName: String {
    didSet {
      UserDefaults.standard.set(myname: myOrganizationName)
    }
  }
  var selectedOrganizationUID: String? = nil {
    didSet {
      UserDefaults.standard.set(organization: selectedOrganizationUID)
    }
  }
  var selectedOrganiztionUIDOrMine: String {
    return selectedOrganizationUID ?? uid
  }
  var selectedOrganiztionNameOrMine: String {
    return workingForName(byUID: selectedOrganiztionUIDOrMine)
      ?? (myOrganizationName == ""
            ? displayName + " Farms"
            : myOrganizationName)
  }
  
  init() {
    workingForIDs = []
    email = ""
    displayName = ""
    uid = ""
    myOrganizationName = ""
  }
  
  func setUser(_ user: User, _ password: String?, _ completion: @escaping (Bool) -> ()) {
    if password != nil { UserDefaults.standard.set(password: password!) }
    UserDefaults.standard.set(username: email)
    
    email = user.email ?? ""
    displayName = user.displayName ?? ""
    uid = user.uid
    HarvestUser.current.selectedOrganizationUID = UserDefaults.standard.getOrganization()
    HarvestUser.current.myOrganizationName = UserDefaults.standard.getMyName() ?? ""
    HarvestUser.current.workingForIDs.removeAll(keepingCapacity: true)
    HarvestDB.getWorkingFor(completion: { (uids) in
      HarvestUser.current.workingForIDs.append(contentsOf: uids)
      completion(true)
    })
  }
  
  func reset() {
    UserDefaults.standard.set(organization: nil)
    UserDefaults.standard.set(myname: nil)
    UserDefaults.standard.set(username: nil)
    UserDefaults.standard.set(password: nil)
    
    HarvestUser.current.displayName = ""
    HarvestUser.current.email = ""
    HarvestUser.current.myOrganizationName = ""
    HarvestUser.current.selectedOrganizationUID = nil
    HarvestUser.current.uid = ""
    HarvestUser.current.workingForIDs.removeAll()
  }
  
  
  static var current = HarvestUser()
  
  func workingForName(byUID uid: String) -> String? {
    for (id, name) in workingForIDs {
      if id == uid {
        return name
      }
    }
    return nil
  }
}

public extension UserDefaults {
  var uid: String {
    return HarvestUser.current.email
  }
  
  public func set(username: String?) {
    guard let u = username else {
      removeObject(forKey: "username")
      return
    }
    
    set(u, forKey: "username")
  }
  
  public func getUsername() -> String? {
    return string(forKey: "username")
  }
  
  public func set(password: String?) {
    guard let p = password else {
      removeObject(forKey: "password")
      return
    }
    
    set(p, forKey: "password")
  }
  
  public func getPassword() -> String? {
    return string(forKey: "password")
  }
  
  public func set(organization: String?) {
    guard let o = organization else {
      removeObject(forKey: uid + "organization")
      return
    }
    
    set(o, forKey: uid + "organization")
  }
  
  public func getOrganization() -> String? {
    return string(forKey: uid + "organization")
  }
  
  public func set(myname: String?) {
    guard let n = myname else {
      removeObject(forKey: uid + "myname")
      return
    }
    
    set(n, forKey: uid + "myname")
  }
  
  public func getMyName() -> String? {
    return string(forKey: uid + "myname")
  }
}
