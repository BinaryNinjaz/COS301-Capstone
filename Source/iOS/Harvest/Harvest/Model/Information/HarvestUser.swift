//
//  HarvestUser.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/24.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase

final public class HarvestUser {
  var workingForID: (uid: String, wid: String)?
  var email: String
  var firstname: String
  var lastname: String
  var uid: String
  var temporary: HarvestUser? = nil
  
  var displayName: String {
    return firstname + " " + lastname
  }
  
  init() {
    workingForID = nil
    email = ""
    firstname = ""
    lastname = ""
    uid = ""
  }
  
  init(json: [String: Any]) {
    uid = json["uid"] as? String ?? ""
    firstname = json["firstname"] as? String ?? ""
    lastname = json["lastname"] as? String ?? ""
    email = json["email"] as? String ?? ""
    workingForID = nil
  }
  
  func json() -> [String: Any] {
    return [
      "firstname": firstname,
      "lastname": lastname,
      "email": email,
      "workingFor": workingForID ?? "",
      "uid": uid
    ]
  }
  
  func setUser(_ user: User, _ password: String?, _ completion: @escaping (Bool) -> ()) {
    if password != nil { UserDefaults.standard.set(password: password!) }
    UserDefaults.standard.set(username: email)
    
    email = user.email ?? ""
    uid = user.uid
    
    HarvestDB.getWorkingFor(completion: { id in
      HarvestUser.current.workingForID = id
      
      HarvestDB.getHarvestUser { (user) in
        guard let user = user else {
          completion(true)
          return
        }
        
        HarvestUser.current.firstname = user.firstname
        HarvestUser.current.lastname = user.lastname
        completion(true)
      }
    })
  }
  
  func reset() {
    UserDefaults.standard.set(username: nil)
    UserDefaults.standard.set(password: nil)
    
    HarvestUser.current.firstname = ""
    HarvestUser.current.lastname = ""
    HarvestUser.current.email = ""
    HarvestUser.current.uid = ""
    HarvestUser.current.workingForID = nil
  }
  
  
  static var current = HarvestUser()
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
}
