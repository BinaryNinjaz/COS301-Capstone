//
//  DBAuth.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase
import GoogleSignIn
import Disk

extension HarvestDB {
  static func requestWorkingFor(
    _ controller: UIViewController?,
    _ completion: @escaping (Bool) -> Void
  ) -> ([(uid: String, wid: String)], Bool) -> Void {
    return { ids, succ in
      if let (uid, wid) = UserDefaults.standard.getUWID() {
        HarvestUser.current.selectedWorkingForID = (uid, wid)
        completion(true)
      } else if ids.count == 1 {
        HarvestUser.current.selectedWorkingForID = ids.first!
        completion(true)
      } else if ids.count == 0 {
        // Farmer shouldn't be working for anyone
        completion(true)
      } else {
        HarvestDB.getWorkingForFarmNames(uids: ids.map { $0.uid }, result: [], completion: { (names) in
          UIAlertController.present(
            title: "Select A Farm",
            message: "Please select the farm that you want to log into",
            options: zip(names, ids).map { ($0.0, $0.1.uid) },
            on: controller) { option in
              guard let fullOption = ids.first(where: { $0.uid == option }) else {
                completion(false)
                return
              }
              
              HarvestUser.current.selectedWorkingForID = fullOption
              UserDefaults.standard.set(uid: fullOption.uid, wid: fullOption.wid)
              completion(true)
            }
        })
        
      }
    }
  }
  
  static func signIn(
    withEmail email: String,
    andPassword password: String,
    on controller: UIViewController?,
    completion: @escaping (Bool) -> Void = { _ in }
  ) {
    Auth.auth().signIn(withEmail: email, password: password) { (user, error) in
      if let err = error {
        UIAlertController.present(title: "Sign In Failure",
                                  message: err.localizedDescription,
                                  on: controller)
        
        completion(false)
        return
      }
      
      guard let user = user else {
        UIAlertController.present(title: "Sign In Failure",
                                  message: "An unknown error occured",
                                  on: controller)
        completion(false)
        return
      }
      
      HarvestUser.current.setUser(user, password, HarvestDB.requestWorkingFor(controller, completion))
      
      if let oldSession = try? Disk
        .retrieve("session", from: .applicationSupport, as: Tracker.self) {
        oldSession.storeSession()
      }
    }
  }
  
  static func signIn(
    with credential: AuthCredential,
    on controller: UIViewController?,
    completion: @escaping (Bool) -> Void = { _ in }
  ) {
    Auth.auth().signIn(with: credential) { (user, error) in
      if let error = error {
        UIAlertController.present(title: "Sign In Failure",
                                  message: error.localizedDescription,
                                  on: controller)
        completion(false)
        return
      }
      
      guard let user = user else {
        UIAlertController.present(title: "Sign In Failure",
                                  message: "An unknown error occured",
                                  on: controller)
        completion(false)
        return
      }
      
      HarvestUser.current.setUser(user, nil, HarvestDB.requestWorkingFor(controller, completion))
      
      if let oldSession = try? Disk.retrieve("session", from: .applicationSupport, as: Tracker.self) {
        oldSession.storeSession()
      }
    }
  }
  
  static func signUp(
    withEmail email: String,
    andPassword password: String,
    name: (first: String, last: String),
    on controller: UIViewController?,
    completion: @escaping (Bool) -> Void = { _ in }
  ) {
    Auth.auth().createUser(
      withEmail: email,
      password: password
    ) { (user, error) in
      if let error = error {
        UIAlertController.present(title: "Sign Up Failure",
                                  message: error.localizedDescription,
                                  on: controller)
        completion(false)
        return
      }
      
      guard let user = user else {
        UIAlertController.present(title: "Sign Up Failure",
                                  message: "An unknown error occured",
                                  on: controller)
        completion(false)
        return
      }
      
      UserDefaults.standard.set(password: password)
      UserDefaults.standard.set(username: email)
      
      let changeRequest = user.createProfileChangeRequest()
      changeRequest.displayName = name.first + " " + name.last
      changeRequest.commitChanges(completion: nil)
      
      HarvestUser.current.firstname = name.first
      HarvestUser.current.lastname = name.last
      HarvestUser.current.setUser(user, password, HarvestDB.requestWorkingFor(controller, completion))
      HarvestDB.save(harvestUser: HarvestUser.current)
      
      completion(true)
    }
  }
  
  static func signOut(
    on controller: UIViewController?,
    completion: @escaping (Bool) -> Void = { _ in }
  ) {
    do {
      TrackerViewController.tracker?.storeSession()
      TrackerViewController.tracker = nil
      try Auth.auth().signOut()
      GIDSignIn.sharedInstance().disconnect()
      GIDSignIn.sharedInstance().signOut()
      
      HarvestUser.current.reset()
      Entities.shared.reset()
      
    } catch {
      //    FIXME  #warning("Complete with proper errors")
      UIAlertController.present(title: "Sign Out Failure",
                                message: "An unknown error occured",
                                on: controller)
      completion(false)
      return
    }
    completion(true)
  }
  
  static func resetPassword(
    forEmail email: String,
    on controller: UIViewController?
  ) {
    Auth.auth().sendPasswordReset(withEmail: email) { (error) in
      if let error = error {
        UIAlertController.present(title: "Reset Password Failure",
                                  message: error.localizedDescription,
                                  on: controller)
        return
      }
      
      UIAlertController.present(title: "Password Reset Sent",
                                message: "An email was sent to \(email) to reset your password",
                                on: controller)
    }
  }
  
  static func verify(
    phoneNumber: String,
    on controller: UIViewController?,
    completion: ((Bool) -> Void)? = nil
  ) {
    PhoneAuthProvider.provider().verifyPhoneNumber(phoneNumber, uiDelegate: nil) { (verificationID, error) in
      if let error = error {
        UIAlertController.present(title: "An Error Occured",
                                  message: error.localizedDescription,
                                  on: controller)
        completion?(false)
        return
      }
      UserDefaults.standard.set(verificationID: verificationID)
      
      UIAlertController.present(title: "Enter Code in Password Field",
                                message: "Enter the code from the SMS sent to you into the password",
                                on: controller)
      
      completion?(true)
    }
  }
  
  static func getWorkingFor(
    completion: @escaping ([(uid: String, wid: String)]) -> Void
  ) {
    let wfref = ref.child(
      Path.workingFor
        + "/"
        + HarvestUser.current.accountIdentifier.removedFirebaseInvalids())
    wfref.observeSingleEvent(of: .value) { (snapshot) in
      guard let _uids = snapshot.value as? [String: Any] else {
        completion([])
        return
      }
      var result: [(String, String)] = []
      
      for (uid, _wid) in _uids {
        guard let wid = _wid as? String else {
          result += [(uid, "")]
          continue
        }
        
        result += [(uid, wid)]
      }
      
      completion(result)
    }
  }
  
  static func getWorkingForFarmName(uid: String, completion: @escaping (String?) -> Void) {
    let fnref = ref.child(uid + "/admin/farmName")
    fnref.observeSingleEvent(of: .value) { (snapshot) in
      guard let name = snapshot.value as? String else {
        completion(nil)
        return
      }
      completion(name)
    }
  }
  
  static func getWorkingForFarmNames(
    uids: [String],
    result: [String],
    completion: @escaping ([String]) -> Void
  ) {
    guard let uid = uids.first else {
      completion(result)
      return
    }
    HarvestDB.getWorkingForFarmName(uid: uid) { (name) in
      let rest = Array(uids.dropFirst())
      
      guard let name = name else {
        HarvestDB.getWorkingForFarmNames(uids: rest, result: result + [uid], completion: completion)
        return
      }
      
      HarvestDB.getWorkingForFarmNames(uids: rest, result: result + [name], completion: completion)
    }
    
  }
}
