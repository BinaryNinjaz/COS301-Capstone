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

extension HarvestDB {
  static func signIn(
    withEmail email: String,
    andPassword password: String,
    on controller: UIViewController,
    completion: @escaping (Bool) -> () = { _ in }
    ) {
    var password = password
    if password.count < 6 {
      password += passwordPadding
    }
    
    Auth.auth().signIn(withEmail: email, password: password) { (user, error) in
      if let err = error {
        let alert = UIAlertController.alertController(
          title: "Sign In Failure",
          message: err.localizedDescription)
        controller.present(alert, animated: true, completion: nil)
        completion(false)
        return
      }
      
      guard let user = user else {
        let alert = UIAlertController.alertController(
          title: "Sign In Failure",
          message: "Unknown Error Occured")
        controller.present(alert, animated: true, completion: nil)
        completion(false)
        return
      }
      UserDefaults.standard.set(password: password)
      UserDefaults.standard.set(username: email)
      HarvestUser.current.selectedOrganization = UserDefaults.standard.getOrganization()
      HarvestUser.current.email = user.email!
      HarvestUser.current.displayName = user.displayName ?? ""
      HarvestUser.current.uid = user.uid
      HarvestUser.current.organizationName = UserDefaults.standard.getMyName() ?? ""
      HarvestUser.current.workingForIDs.removeAll(keepingCapacity: true)
      HarvestDB.getWorkingFor(completion: { (uids) in
        HarvestUser.current.workingForIDs.append(contentsOf: uids)
        completion(true)
      })
    }
  }
  
  static func signUp(
    withEmail email: String,
    andPassword password: String,
    name: (first: String, last: String),
    on controller: UIViewController,
    completion: @escaping (Bool) -> () = { _ in }
    ) {
    var password = password
    if password.count < 6 {
      password += passwordPadding
    }
    
    Auth.auth().createUser(withEmail: email, password: password) { (user, error) in
      if let err = error {
        let alert = UIAlertController.alertController(
          title: "Sign Up Failure",
          message: err.localizedDescription)
        controller.present(alert, animated: true, completion: nil)
        completion(false)
        return
      }
      
      guard let user = user else {
        let alert = UIAlertController.alertController(
          title: "Sign Up Failure",
          message: "An unknown error occured")
        controller.present(alert, animated: true, completion: nil)
        completion(false)
        return
      }
      
      UserDefaults.standard.set(password: password)
      UserDefaults.standard.set(username: email)
      
      let changeRequest = user.createProfileChangeRequest()
      changeRequest.displayName = name.first + " " + name.last
      changeRequest.commitChanges(completion: nil)
      
      completion(true)
    }
  }
  
  static func signOut(
    on controller: UIViewController,
    completion: @escaping (Bool) -> () = { _ in }
    ) {
    do {
      try Auth.auth().signOut()
      GIDSignIn.sharedInstance().disconnect()
      GIDSignIn.sharedInstance().signOut()
    } catch {
      //    FIXME  #warning("Complete with proper errors")
      let alert = UIAlertController.alertController(
        title: "Sign Out Failure",
        message: "An unknown error occured")
      controller.present(alert, animated: true, completion: nil)
      completion(false)
      return
    }
    completion(true)
  }
  
  static func getWorkingFor(completion: @escaping ([(uid: String, name: String)]) -> ()){
    let wfref = ref.child(Path.workingFor + "/" + HarvestUser.current.email.removedFirebaseInvalids())
    wfref.observeSingleEvent(of: .value) { (snapshot) in
      guard let _uids = snapshot.value as? [String: Any] else {
        completion([])
        return
      }
      
      var result = [(String, String)]()
      
      for (uid, _name) in _uids {
        guard let name = _name as? String else {
          result.append((uid, ""))
          continue
        }
        
        result.append((uid, name))
      }
      
      completion(result)
    }
  }
}
