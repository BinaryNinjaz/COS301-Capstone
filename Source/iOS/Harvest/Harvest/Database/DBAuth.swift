//
//  DBAuth.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase
import CoreLocation
import GoogleSignIn
import Disk

extension HarvestDB {
  static func signIn(
    withEmail email: String,
    andPassword password: String,
    on controller: UIViewController,
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
      
      HarvestUser.current.setUser(user, password, completion)
      
      if let oldSession = try? Disk
        .retrieve("session", from: .applicationSupport, as: Tracker.self) {
        oldSession.storeSession()
      }
    }
  }
  
  static func signIn(
    with credential: AuthCredential,
    on controller: UIViewController,
    completion: ((Bool) -> Void)?
  ) {
    Auth.auth().signIn(with: credential) { (user, error) in
      if let error = error {
        UIAlertController.present(title: "Sign In Failure",
                                  message: error.localizedDescription,
                                  on: controller)
        completion?(false)
        return
      }
      
      guard let user = user else {
        UIAlertController.present(title: "Sign In Failure",
                                  message: "An unknown error occured",
                                  on: controller)
        completion?(false)
        return
      }
      
      HarvestUser.current.setUser(user, nil) { _ in
        completion?(true)
      }
      if let oldSession = try? Disk.retrieve("session", from: .applicationSupport, as: Tracker.self) {
        oldSession.storeSession()
      }
    }
  }
  
  static func signUp(
    withEmail email: String,
    andPassword password: String,
    name: (first: String, last: String),
    on controller: UIViewController,
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
      HarvestUser.current.setUser(user, password, completion)
      HarvestDB.save(harvestUser: HarvestUser.current)
      
      completion(true)
    }
  }
  
  static func signOut(
    on controller: UIViewController,
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
    on controller: UIViewController
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
  
  static func verify(phoneNumber: String, on controller: UIViewController, completion: ((Bool) -> Void)? = nil) {
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
    completion: @escaping ((uid: String, wid: String)?) -> Void
  ) {
    let wfref = ref.child(
      Path.workingFor
        + "/"
        + HarvestUser.current.accountIdentifier.removedFirebaseInvalids())
    wfref.observeSingleEvent(of: .value) { (snapshot) in
      guard let _uids = snapshot.value as? [String: Any] else {
        completion(nil)
        return
      }
      var result: (String, String)? = nil
      
      for (uid, _wid) in _uids {
        guard let wid = _wid as? String else {
          result = (uid, "")
          continue
        }
        
        result = (uid, wid)
      }
      
      completion(result)
    }
  }
}
