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
import SCLAlertView

extension HarvestDB {
  static func requestWorkingFor(
    _ controller: UIViewController?,
    _ completion: @escaping (Bool) -> Void
  ) -> ([(uid: String, wid: String)]?, Bool) -> Void {
    return { ids, succ in
      guard let ids = ids else { // is farmer
        completion(true)
        return
      }
      
      if let (uid, wid) = UserDefaults.standard.getUWID() {
        HarvestUser.current.selectedWorkingForID = (uid, wid)
        completion(true)
      } else if ids.count == 1 {
        HarvestUser.current.selectedWorkingForID = ids.first!
        completion(true)
      } else if ids.count == 0 {
        let alert = SCLAlertView()
        alert.addButton("Done", action: { completion(false) })
        
        alert.showNotice(
          "You're Not Working For Anyone",
          subTitle: "Ensure you've been added to the farm as a worker by your employer.")
        
      } else {
        HarvestDB.getWorkingForFarmNames(uids: ids.map { $0.uid }, result: [], completion: { (names) in
          let alert = SCLAlertView(
            appearance: .optionsAppearance,
            options: zip(names, ids).map { ($0.0, $0.1.uid) }) { option in
              guard let fullOption = ids.first(where: { $0.uid == option }) else {
                completion(false)
                return
              }
          
              HarvestUser.current.selectedWorkingForID = fullOption
              UserDefaults.standard.set(uid: fullOption.uid, wid: fullOption.wid)
              completion(true)
            }
          
          alert.showNotice(
            "Select An Organization",
            subTitle: "Please select the organization that you want to log into")
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
        SCLAlertView().showError("Sign In Failure", subTitle: err.localizedDescription)
        completion(false)
        return
      }
      
      guard let user = user else {
        SCLAlertView().showError("Sign In Failure", subTitle: "An unknow error occured")
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
        SCLAlertView().showError("Sign In Failure", subTitle: error.localizedDescription)
        completion(false)
        return
      }
      
      guard let user = user else {
        SCLAlertView().showError("Sign In Failure", subTitle: "An unknow error occured")
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
    with details: (email: String, password: String),
    name: (first: String, last: String),
    organisationName: String,
    on controller: UIViewController?,
    completion: @escaping (Bool) -> Void = { _ in }
  ) {
    Auth.auth().createUser(
      withEmail: details.email,
      password: details.password
    ) { (user, error) in
      if let error = error {
        SCLAlertView().showError("Sign Up Failure", subTitle: error.localizedDescription)
        completion(false)
        return
      }
      
      guard let user = user else {
        SCLAlertView().showError("Sign Up Failure", subTitle: "An unknown error occured")
        completion(false)
        return
      }
      
      UserDefaults.standard.set(password: details.password)
      UserDefaults.standard.set(username: details.email)
      
      let changeRequest = user.createProfileChangeRequest()
      changeRequest.displayName = name.first + " " + name.last
      changeRequest.commitChanges(completion: nil)
      
      HarvestUser.current.firstname = name.first
      HarvestUser.current.lastname = name.last
      HarvestUser.current.organisationName = organisationName
      HarvestUser.current.setUser(user, details.password, HarvestDB.requestWorkingFor(controller, completion))
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
      SCLAlertView().showError("Sign Out Failure", subTitle: "An unknown error occured")
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
        SCLAlertView().showError("Reset Password Failure", subTitle: error.localizedDescription)
        return
      }
      SCLAlertView().showSuccess(
        "Password Reset Sent",
        subTitle: "An email was sent to \(email) to reset your password")
    }
  }
  
  static func verify(
    phoneNumber: String,
    on controller: UIViewController?,
    completion: ((Bool) -> Void)? = nil
  ) {
    PhoneAuthProvider.provider().verifyPhoneNumber(phoneNumber, uiDelegate: nil) { (verificationID, error) in
      if let error = error {
        SCLAlertView().showError("An Error Occured", subTitle: error.localizedDescription)
        completion?(false)
        return
      }
      UserDefaults.standard.set(verificationID: verificationID)
      
      SCLAlertView().showInfo(
        "Enter Code into Text Field",
        subTitle: "Enter the 6-digit code from the SMS sent to you into the text field")
      
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
    let fnref = ref.child(uid + "/admin/organization")
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
