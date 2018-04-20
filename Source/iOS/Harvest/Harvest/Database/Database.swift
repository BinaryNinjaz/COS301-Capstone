//
//  Database.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/26.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase
import CoreLocation
import GoogleSignIn

let passwordPadding = "s3cr3ts4uc3"

struct HarvestUser {
  var email: String
  var displayName: String
  var uid: String
  
  static var current = HarvestUser(email: "", displayName: "", uid: "")
}


struct HarvestDB {
  static var ref: DatabaseReference! = Database.database().reference()
  
  struct Path {
    static var parent: String {
      return "" //HarvestUser.current.email.removedFirebaseInvalids()
    }
    static var yields: String {
      return "\(Path.parent)/yields"
    }
    static var locations: String {
      return "\(Path.parent)/locations"
    }
    static var farms: String {
      return "\(Path.parent)/farms"
    }
    static var workers: String {
      return "\(Path.parent)/workers"
    }
    static var orchards: String {
      return "\(Path.parent)/orchards"
    }
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
      HarvestUser.current.email = user.email!
      HarvestUser.current.displayName = user.displayName ?? ""
      HarvestUser.current.uid = user.uid
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
      GIDSignIn.sharedInstance().disconnect()
      GIDSignIn.sharedInstance().signOut()
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
    let wref = ref.child(Path.workers).queryOrdered(byChild: "surname")
    wref.observeSingleEvent(of: .value) { (snapshot) in
      var workers = [Worker]()
      for _child in snapshot.children {
        guard let child = _child as? DataSnapshot else {
          continue
        }
        
        guard let worker = child.value as? [String: Any] else {
          continue
        }
        let w = Worker(json: worker, id: child.key)
        workers.append(w)
      }
      completion(workers)
    }
  }
  
  static func watchWorkers(_ completion: @escaping ([Worker]) -> ()) {
    let wref = ref.child(Path.workers).queryOrdered(byChild: "surname")
    wref.observe(.value) { (snapshot) in
      var workers = [Worker]()
      for _child in snapshot.children {
        guard let child = _child as? DataSnapshot else {
          continue
        }
        
        guard let worker = child.value as? [String: Any] else {
          continue
        }
        let w = Worker(json: worker, id: child.key)
        workers.append(w)
      }
      completion(workers)
    }
  }
  
  static func onLastSession(
    _ completion: @escaping ([String: Any]) -> ()
  ) {
    let wref = ref.child(Path.yields).queryLimited(toLast: 1)
    wref.observe(.value) { (snapshot) in
      guard let session = snapshot.value as? [String: Any] else {
        return
      }
      completion(session)
    }
  }
  
  static func collect(from workers: [Worker: WorkerCollection],
                      by user: (display: String, uid: String),
                      on date: Date,
                      track: [(Double, Double)]) {
    let cref = ref.child(Path.yields)
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
      
      let (f, l) = (w.firstname.removedFirebaseInvalids(),
                    w.lastname.removedFirebaseInvalids())
      cs[f + " " + l] = collections
    }
    
    let data: [String: Any] = [
      "start_date": date.timeIntervalSince1970,
      "end_date": Date().timeIntervalSince1970,
      "display": user.display,
      "uid": user.uid,
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
    let yields = ref.child(Path.yields)
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
  
  static func update(location: CLLocationCoordinate2D) {
    let path = Path.locations + "/" + HarvestUser.current.uid
    let updates =
    [
      path: [
        "coord": [
          "lat": location.latitude,
          "lng": location.longitude
        ],
        "display": HarvestUser.current.displayName
      ]
    ]
    
    ref.updateChildValues(updates)
  }
  
  static func getOrchards(_ completion: @escaping ([Orchard]) -> ()) {
    let oref = ref.child(Path.orchards)
    oref.observeSingleEvent(of: .value) { (snapshot) in
      var orchards = [Orchard]()
      for _child in snapshot.children {
        guard let child = _child as? DataSnapshot else {
          continue
        }
        
        guard let orchard = child.value as? [String: Any] else {
          continue
        }
        
        let o = Orchard(json: orchard, id: child.key)
        orchards.append(o)
      }
      completion(orchards)
    }
  }
  
  static func watchOrchards(_ completion: @escaping ([Orchard]) -> ()) {
    let oref = ref.child(Path.orchards)
    oref.observe(.value) { (snapshot) in
      var orchards = [Orchard]()
      for _child in snapshot.children {
        guard let child = _child as? DataSnapshot else {
          continue
        }
        
        guard let orchard = child.value as? [String: Any] else {
          continue
        }
        
        let o = Orchard(json: orchard, id: child.key)
        orchards.append(o)
      }
      completion(orchards)
    }
  }
  
  static func getFarms(_ completion: @escaping ([Farm]) -> ()) {
    let fref = ref.child(Path.farms)
    fref.observeSingleEvent(of: .value) { (snapshot) in
      var farms = [Farm]()
      for _child in snapshot.children {
        guard let child = _child as? DataSnapshot else {
          continue
        }
        
        guard let farm = child.value as? [String: Any] else {
          continue
        }
        
        let f = Farm(json: farm, id: child.key)
        farms.append(f)
      }
      completion(farms)
    }
  }
  
  static func watchFarms(_ completion: @escaping ([Farm]) -> ()) {
    let fref = ref.child(Path.farms)
    fref.observe(.value) { (snapshot) in
      var farms = [Farm]()
      for _child in snapshot.children {
        guard let child = _child as? DataSnapshot else {
          continue
        }
        
        guard let farm = child.value as? [String: Any] else {
          continue
        }
        
        let f = Farm(json: farm, id: child.key)
        farms.append(f)
      }
      completion(farms)
    }
  }
  
  static func save(farm: Farm) {
    let farms = ref.child(Path.farms)
    let update = farm.json()
    farms.updateChildValues(update)
  }
  
  static func save(orchard: Orchard) {
    let orchards = ref.child(Path.orchards)
    let update = orchard.json()
    orchards.updateChildValues(update)
  }
  
  static func save(worker: Worker) {
    let workers = ref.child(Path.workers)
    let update = worker.json()
    workers.updateChildValues(update)
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

extension String {
  func removedFirebaseInvalids() -> String {
    var result = ""
    
    for c in self {
      if !"[.*$#]".contains(c) {
        result += "\(c)"
      } else {
        result += " "
      }
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
