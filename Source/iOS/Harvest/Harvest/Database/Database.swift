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
  var email: String
  var displayName: String
  var uid: String
  
  static var current = HarvestUser(email: "", displayName: "", uid: "")
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

struct Orchard {
  var bagMass: Double
  var coords: [CLLocationCoordinate2D]
  var crop: String
  var date: Date
  var farm: Int
  var futher: String
  var name: String
  var unit: String
  var xDim: Double
  var yDim: Double
}


struct HarvestDB {
  static var ref: DatabaseReference! = Database.database().reference()
  
  enum Path: String {
    case yields = "yields"
    case locations = "locations"
    case orchards = "orchards"
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
    let wref = ref.child("/workers").queryOrdered(byChild: "surname")
    wref.observe(.value) { (snapshot) in
      var workers = [Worker]()
      for _child in snapshot.children {
        guard let worker = (_child as? DataSnapshot)?.value as? [String: Any] else {
          continue
        }
        let fn = worker["name"] as? String ?? ""
        let ln = worker["surname"] as? String ?? ""
        let w = Worker(firstname: fn, lastname: ln)
        workers.append(w)
      }
      completion(workers)
    }
  }
  
  static func onLastSession(
    _ completion: @escaping ([String: Any]) -> ()
  ) {
    let wref = ref.child("/yields").queryLimited(toLast: 1)
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
  
  static func update(location: CLLocationCoordinate2D) {
    let path = Path.locations.rawValue + "/" + HarvestUser.current.uid
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
    let oref = ref.child(Path.orchards.rawValue)
    oref.observe(.value) { (snapshot) in
      var orchards = [Orchard]()
      for _child in snapshot.children {
        guard let orchard = (_child as? DataSnapshot)?.value as? [String: Any] else {
          continue
        }
        let bagMass = orchard["bagMass"] as? Double ?? 0.0
        let crop = orchard["crop"] as? String ?? ""
        let date = Date(timeIntervalSince1970: orchard["date"] as? Double ?? 0.0)
        let farm = orchard["farm"] as? Int ?? 0
        let further = orchard["further"] as? String ?? ""
        let name = orchard["name"] as? String ?? ""
        let unit = orchard["unit"] as? String ?? ""
        let xDim = orchard["xDim"] as? Double ?? 0.0
        let yDim = orchard["yDim"] as? Double ?? 0.0
        
        let cs = orchard["coords"] as? [Any] ?? []
        var coords = [CLLocationCoordinate2D]()
        
        
        for c in cs {
          guard let c = c as? [String: Any] else {
            continue
          }
          guard let lat = c["lat"] as? Double else {
            continue
          }
          guard let lng = c["lng"] as? Double else {
            continue
          }
          
          coords.append(CLLocationCoordinate2D(latitude: lat, longitude: lng))
        }
        
        let o = Orchard(bagMass: bagMass, coords: coords, crop: crop, date: date, farm: farm, futher: further, name: name, unit: unit, xDim: xDim, yDim: yDim)
        
        orchards.append(o)
      }
      completion(orchards)
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

extension String {
  func removedFirebaseInvalids() -> String {
    var result = ""
    
    for c in self {
      if !"[.*$#]".contains(c) {
        result += "\(c)"
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