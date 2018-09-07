//
//  DBUser.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/24.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import FirebaseAuth
import SCLAlertView

extension HarvestDB {
  static func getHarvestUser(_ completion: @escaping (HarvestUser?) -> Void) {
    let aref = ref.child(Path.admin)
    aref.observeSingleEvent(of: .value) { snapshot in
      guard let user = snapshot.value as? [String: Any] else {
        completion(nil)
        return
      }
      let hu = HarvestUser(json: user)
      completion(hu)
    }
  }
  
  static func watchHarvestUser(_ completion: @escaping (HarvestUser) -> Void) {
    let aref = ref.child(Path.admin)
    aref.observe(.value) { (snapshot) in
      guard let user = snapshot.value as? [String: Any] else {
        return
      }
      
      let hu = HarvestUser(json: user)
      completion(hu)
    }
  }
  
  static func save(harvestUser: HarvestUser, oldEmail: String) {
    guard Auth.auth().currentUser?.phoneNumber == nil else {
      return
    }
    
    let admin = ref.child(Path.admin)
    
    let update = harvestUser.json()
    admin.updateChildValues(update)
    
    if let user = Auth.auth().currentUser {
      let profileUpdate = user.createProfileChangeRequest()
      profileUpdate.displayName = (update["firstname"] as? String ?? "") + " " + (update["lastname"] as? String ?? "")
      profileUpdate.commitChanges(completion: nil)
      
      if harvestUser.accountIdentifier != oldEmail {
        user.updateEmail(to: harvestUser.accountIdentifier) { (error) in
          if let error = error {
            SCLAlertView().showError("An Error Occurred", subTitle: error.localizedDescription)
          }
        }
      }
    }
  }
  
  static func delete(harvestUser: HarvestUser, completion: @escaping (Bool) -> Void) {
    
    guard Path.parent != "" else {
      SCLAlertView().showError(
        "User does not exist",
        subTitle: "It appears that the user does not have an an account")
      return
    }
    
    let user = ref.child(Path.parent)
    user.removeValue { (err, _) in
      if let err = err {
        SCLAlertView().showError("An Error Occurred", subTitle: err.localizedDescription)
        completion(false)
      } else {
        completion(true)
      }
    }
  }
}
