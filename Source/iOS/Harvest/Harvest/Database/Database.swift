//
//  Database.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/26.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase
import CoreLocation

let passwordPadding = "s3cr3ts4uc3"

struct HarvestUser {
  var name: String
  
  static var current = HarvestUser(name: "")
}


struct HarvestDB {
  static var ref: DatabaseReference! = Database.database().reference()
  
  enum Path: String {
    case yields = "mockfarm/yields"
  }
  
  //MARK: - Authentication
  
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
      HarvestUser.current.name = user.email!
      completion(true)
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
    } catch {
//      #warning("Complete with proper errors")
      let alert = UIAlertController.alertController(
        title: "Sign Out Failure",
        message: "An unknown error occured")
      controller.present(alert, animated: true, completion: nil)
      completion(false)
      return
    }
    completion(true)
  }
  
  // MARK: - Yield
  
  static func collect(yield: Double,
                      from email: String,
                      inAmountOfSeconds amount: TimeInterval,
                      at loc: CLLocationCoordinate2D,
                      on date: Date) {
    let cref = ref.child(Path.yields.rawValue)
    let key = cref.childByAutoId().key
    let data: [String: Any] = [
      "date": date.timeIntervalSince1970,
      "yield": yield,
      "email": email,
      "duration": amount,
      "location": ["lat": loc.latitude, "lng": loc.longitude]
    ]
    let updates = ["yields/\(key)": data]
    
    ref.updateChildValues(updates)
  }
  
}


extension UserDefaults {
  func set(username: String) {
    set(username, forKey: "username")
  }
  
  func getUsername() -> String? {
    return string(forKey: "username")
  }
  
  func set(password: String) {
    set(password, forKey: "password")
  }
  
  func getPassword() -> String? {
    return string(forKey: "password")
  }
}
