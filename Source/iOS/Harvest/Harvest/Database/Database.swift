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

struct Worker :  Hashable {
  var firstname: String
  var lastname: String
  
  static func ==(lhs: Worker, rhs: Worker) -> Bool {
    return lhs.firstname == rhs.firstname && lhs.lastname == rhs.lastname
  }
  
  var hashValue: Int {
    return "\(firstname)\(lastname)".hashValue
  }
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
  
  static func getWorkers(_ completion: @escaping ([Worker]) -> ()) {
    let wref = ref.child("/workers")
    wref.observeSingleEvent(of: .value) { (snapshot) in
      var workers = [Worker]()
      for _child in snapshot.children {
        guard let worker = (_child as? DataSnapshot)?.value as? [String: Any] else {
          continue
        }
        let fn = worker["name"] as? String ?? ""
        let ln = worker["surname"] as? String ?? ""
        print(worker)
        let w = Worker(firstname: fn, lastname: ln)
        workers.append(w)
      }
      completion(workers)
    }
  }
  
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
  
  static func collect(from workers: [Worker: WorkerCollection],
                      by email: String,
                      on date: Date,
                      track: [(Double, Double)]) {
    let cref = ref.child(Path.yields.rawValue)
    let key = cref.childByAutoId().key
    
    var cs = [String: Any]()
    
    for (w, c) in workers {
      var collections = [String: Any]()
      var i = 0
      
      for cp in c.collectionPoints {
        let d = cp.date.timeIntervalSince1970
        let lat = cp.location.coordinate.latitude
        let lng = cp.location.coordinate.longitude
        
        collections[i.description] = [
          "date": d,
          "coord": [
            "lat": lat,
            "lng": lng
          ]
        ]
        
        i += 1
      }
      
      cs[w.firstname + " " + w.lastname] = collections
    }
    
    let data: [String: Any] = [
      "start_date": date.timeIntervalSince1970,
      "end_date": Date().timeIntervalSince1970,
      "email": email,
      "collections": cs,
      "track": track.firbaseCoordRepresentation()
    ]
    
    let updates = ["yields/\(key)": data]
    
    ref.updateChildValues(updates)
  }
  
  static func yieldCollection(
    for user: String,
    on date: Date,
    completion: @escaping (DataSnapshot) -> ()
  ) {
    let yields = ref.child(Path.yields.rawValue)
    yields.observeSingleEvent(of: .value) { (snapshot) in
      for _child in snapshot.children {
        guard let child = (_child as? DataSnapshot)?.value as? [String: Any] else {
          continue
        }
        guard let email = child["email"] as? String else {
          continue
        }
        guard let cdate = child["date"] as? Date else {
          continue
        }
        
        if email == user && cdate == date {
          completion(_child as! DataSnapshot)
          return
        }
      }
    }
  }
}

extension Array where Element == (Double, Double) {
  func firbaseCoordRepresentation() -> [String: Any] {
    var result = [String: Any]()
    var id = 0
    for (lat, lng) in self {
      let coord = ["lat": lat, "lng": lng]
      result[id.description] = coord
      id += 1
    }
    return result
  }
}

public extension UserDefaults {
  public func set(username: String) {
    set(username, forKey: "username")
  }
  
  public func getUsername() -> String? {
    return string(forKey: "username")
  }
  
  public func set(password: String) {
    set(password, forKey: "password")
  }
  
  public func getPassword() -> String? {
    return string(forKey: "password")
  }
}
