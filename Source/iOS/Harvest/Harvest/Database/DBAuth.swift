//
//  DBAuth.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import FirebaseAuth
import FirebaseDatabase
import GoogleSignIn
import Disk
import SCLAlertView
import CryptoSwift

extension HarvestDB {
  static func requestWorkingFor(
    _ completion: @escaping (Bool) -> Void
  ) -> ([(uid: String, wid: String)]?, Bool) -> Void {
    return { ids, succ in
      guard let user = Auth.auth().currentUser else {
        SCLAlertView().showError(
          "Not Signed In",
          subTitle: "You seem to not be signed in. Please try signing in again")
        return
      }
      
      // ensure is foreman else call completion(true)
      guard let pn = user.phoneNumber else {
        completion(true)
        return
      }
      
      if let uid = UserDefaults.standard.getUID(), let wid = UserDefaults.standard.getWID() {
        HarvestUser.current.selectedWorkingForID = (uid, wid)
        HarvestDB.getWorkingForFarmName(uid: uid) { (name) in
          HarvestUser.current.organisationName = name ?? "your farm"
        }
        completion(true)
      } /*else if ids.count == 1 {
        HarvestUser.current.selectedWorkingForID = ids.first!
        HarvestDB.getWorkingForFarmName(uid: ids.first!.uid) { (name) in
          HarvestUser.current.organisationName = name ?? "your farm"
        }
        completion(true)
      }*/ else if ids == nil || ids?.count == 0 {
        HarvestUser.current.reset()
        
        let alert = SCLAlertView(appearance: .optionsAppearance)
        alert.addButton("Okay", action: { completion(false) })
        
        alert.showNotice(
          "You're Not Working For Anyone",
          subTitle: """
          Ensure you've been added to the farm as a worker by your employer. Your number as stored \
          in the system is \(pn)
          """)
        
      } else {
        let ids = ids!
        HarvestDB.getWorkingForFarmNames(uids: ids.map { $0.uid }, result: [], completion: { (names) in
          let alert = SCLAlertView(
            appearance: .optionsAppearance,
            options: zip(names, ids).map { ($0.0, $0.1.uid) }) { option, name in
              guard let fullOption = ids.first(where: { $0.uid == option }) else {
                completion(false)
                return
              }
              HarvestUser.current.organisationName = name
              HarvestUser.current.selectedWorkingForID = fullOption
              UserDefaults.standard.set(uid: fullOption.uid, wid: fullOption.wid)
              completion(true)
            }
          
          alert.addButton("Cancel", action: { completion(false) })
          
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
    Auth.auth().signIn(withEmail: email, password: password) { (authResult, error) in
      if email == "iostester@harvestapp.co.za" {
        print(UserDefaults.standard.bool(forKey: "tester"))
        UserDefaults.standard.set(true, forKey: "tester")
        updateMockDatabase()
        completion(true)
        return
      }
      
      if let error = error {
        let nserr = error as NSError
        if [AuthErrorCode.emailAlreadyInUse, .wrongPassword, .userNotFound]
          .contains(AuthErrorCode(rawValue: nserr.code)) {
          SCLAlertView().showError("Sign In Failure", subTitle: "Your email or password is incorrect.")
        } else {
          SCLAlertView().showError("Sign In Failure", subTitle: error.localizedDescription)
          print(error)
        }
        completion(false)
        return
      }
      
      guard let user = authResult?.user else {
        SCLAlertView().showError("Sign In Failure", subTitle: "An unknow error occured")
        completion(false)
        return
      }
      
      guard user.isEmailVerified else {
        let alert = SCLAlertView(appearance: SCLAlertView.SCLAppearance.warningAppearance)
        
        alert.addButton("Resend Email") {
          user.sendEmailVerification { (err) in
            if let err = err {
              SCLAlertView().showError("Verification Email Failed", subTitle: err.localizedDescription)
            }
          }
        }
        
        alert.addButton("Okay") {}
        
        alert.showError(
          "Sign In Failure",
          subTitle: "Please verify your email address by clicking on the link in the verification email.")
        completion(false)
        return
      }
      
      HarvestUser.current.setUser(user, password, HarvestDB.requestWorkingFor(completion))
      
      if let oldSession = try? Disk
        .retrieve("session", from: .applicationSupport, as: Tracker.self) {
        oldSession.storeSession()
      }
    }
  }
  
  static func signIn(
    with credential: AuthCredential,
    completion: @escaping (Bool) -> Void = { _ in }
  ) {
    Auth.auth().signInAndRetrieveData(with: credential) { (authResult, error) in
      if let error = error {
        let nserr = error as NSError
        if [AuthErrorCode.emailAlreadyInUse, .wrongPassword, .userNotFound]
          .contains(AuthErrorCode(rawValue: nserr.code)) {
          SCLAlertView().showError("Sign In Failure", subTitle: "Your Email or password is incorrect.")
        } else {
          SCLAlertView().showError("Sign In Failure", subTitle: error.localizedDescription)
          print(error)
        }
        completion(false)
        return
      }
      
      guard let user = authResult?.user else {
        SCLAlertView().showError("Sign In Failure", subTitle: "An unknow error occured")
        completion(false)
        return
      }
      
      guard user.phoneNumber != nil || user.isEmailVerified else {
        let alert = SCLAlertView(appearance: SCLAlertView.SCLAppearance.warningAppearance)
        
        alert.addButton("Resend Email") {
          user.sendEmailVerification { (err) in
            if let err = err {
              SCLAlertView().showError("Verification Email Failed", subTitle: err.localizedDescription)
            }
          }
        }
        
        alert.addButton("Okay") {}
        
        alert.showError(
          "Sign In Failure",
          subTitle: "Please verify your email address by clicking on the link in the verification email.")
        completion(false)
        return
      }
      
      HarvestUser.current.uid = user.uid
      HarvestUser.current.accountIdentifier = user.email ?? ""
      HarvestUser.current.organisationName = user.email ?? ""
      
      HarvestDB.save(harvestUser: HarvestUser.current, oldEmail: "")
      HarvestUser.current.setUser(user, nil, requestWorkingFor(completion))
      
      if let oldSession = try? Disk.retrieve("session", from: .applicationSupport, as: Tracker.self) {
        oldSession.storeSession()
      }
    }
  }
  
  static func signUp(
    with details: (email: String, password: String),
    name: (first: String, last: String),
    organisationName: String,
    completion: @escaping (Bool) -> Void = { _ in }
  ) {
    Auth.auth().createUser(
      withEmail: details.email,
      password: details.password
    ) { (authResult, error) in
      if let error = error {
        SCLAlertView().showError("Sign Up Failure", subTitle: error.localizedDescription)
        completion(false)
        return
      }
      
      guard let user = authResult?.user else {
        SCLAlertView().showError("Sign Up Failure", subTitle: "An unknown error occured")
        completion(false)
        return
      }
      
      user.sendEmailVerification { (err) in
        if let err = err {
          SCLAlertView().showError("Verification Email Failed", subTitle: err.localizedDescription)
          completion(false)
          return
        } else {
          let alert = SCLAlertView(appearance: .warningAppearance)
          
          alert.addButton("Okay") {
            let changeRequest = user.createProfileChangeRequest()
            changeRequest.displayName = name.first + " " + name.last
            changeRequest.commitChanges(completion: nil)
            
            HarvestUser.current.firstname = name.first
            HarvestUser.current.lastname = name.last
            HarvestUser.current.organisationName = organisationName
            HarvestUser.current.setUser(user, details.password, { _, _ in })
            HarvestDB.save(harvestUser: HarvestUser.current, oldEmail: "")
            
            completion(true)
          }
          
          alert.showNotice(
            "Verification Email Sent",
            subTitle: """
            To complete your registration please follow the instructions emailed to you.
            """)
        }
      }
    }
  }
  
  static func signOut(
    completion: @escaping (Bool) -> Void = { _ in }
  ) {
    do {
      UserDefaults.standard.removeObject(forKey: "tester")
      
      TrackerViewController.tracker?.storeSession()
      TrackerViewController.tracker = nil
      try Auth.auth().signOut()
      GIDSignIn.sharedInstance().disconnect()
      GIDSignIn.sharedInstance().signOut()
      
      HarvestUser.current.reset()
      Entities.shared.reset()
      
    } catch let e {
      SCLAlertView().showError("Sign Out Failure", subTitle: e.localizedDescription)
      completion(false)
      return
    }
    completion(true)
  }
  
  static func resetPassword(
    forEmail email: String
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
    let wfref = ref.child(Path.workingFor
        + "/"
        + hashed(phoneNumber: HarvestUser.current.accountIdentifier.removedFirebaseInvalids()))
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
    
    let success: (DataSnapshot) -> Void = { snapshot in
      guard let name = snapshot.value as? String else {
        completion(uid)
        return
      }
      completion(name)
    }
    
    let failure: (Error) -> Void = { error in
      completion(nil)
    }
    
    fnref.observeSingleEvent(of: .value, with: success, withCancel: failure)
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
        HarvestDB.getWorkingForFarmNames(uids: rest, result: result, completion: completion)
        return
      }
      HarvestDB.getWorkingForFarmNames(uids: rest, result: result + [name], completion: completion)
    }
  }
}

func hashed(phoneNumber: String) -> String {
  let bytes = phoneNumber.bytes
  var hasher = SHA2(variant: .sha256)
  _ = try? hasher.update(withBytes: bytes)
  let result = try? hasher.finish()
  
  let text = result?.hexEncodedString() ?? ""
  return text
}

extension Array where Element == UInt8 {
  func hexEncodedString() -> String {
    return map { String(format: "%02hhx", $0) }.joined()
  }
}
