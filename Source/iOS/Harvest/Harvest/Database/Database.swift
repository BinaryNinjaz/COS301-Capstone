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

class HarvestUser {
  var workingForIDs: [(uid: String, name: String)]
  var email: String
  var displayName: String
  var uid: String
  var organizationName: String {
    didSet {
      UserDefaults.standard.set(myname: organizationName)
    }
  }
  var selectedOrganization: String? = nil
  
  init() {
    workingForIDs = []
    email = ""
    displayName = ""
    uid = ""
    organizationName = ""
  }
  
  static var current = HarvestUser()
}


struct HarvestDB {
  static var ref: DatabaseReference! = Database.database().reference()
  
  struct Path {
    static var parent: String {
      return HarvestUser.current.selectedOrganization == nil
        ? HarvestUser.current.uid
        : HarvestUser.current.selectedOrganization!
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
    static var sessions: String {
      return "\(Path.parent)/sessions"
    }
    static var workingFor: String {
      return "WorkingFor"
    }
  }
  
  static func saveFarmName() {
    
  }
  
  // FIXME: -
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
  var uid: String {
    return HarvestUser.current.uid
  }
  
  public func set(username: String) {
    set(username, forKey: uid + "username")
  }
  
  public func getUsername() -> String? {
    return string(forKey: uid + "username")
  }
  
  public func set(password: String) {
    set(password, forKey: uid + "password")
  }
  
  public func getPassword() -> String? {
    return string(forKey: uid + "password")
  }
  
  public func set(organization: String?) {
    guard let o = organization else {
      removeObject(forKey: uid + "organization")
      return
    }
    
    set(o, forKey: uid + "organization")
  }
  
  public func getOrganization() -> String? {
    return string(forKey: uid + "organization")
  }
  
  public func set(myname: String) {
    set(myname, forKey: uid + "myname")
  }
  
  public func getMyName() -> String? {
    return string(forKey: uid + "myname")
  }
}

extension Array where Element == CLLocationCoordinate2D {
  func firbaseCoordRepresentation() -> [String: Any] {
    var result = [String: Any]()
    var id = 0
    for loc in self {
      let coord = ["lat": loc.latitude, "lng": loc.longitude]
      result[id.description] = coord
      id += 1
    }
    return result
  }
}

extension Dictionary where Key == Worker, Value == [CollectionPoint] {
  func firebaseCoordRepresentation() -> [String: Any] {
    var result = [String: Any]()
    
    for (key, collectionPoints) in self {
      var collections: [String: Any] = [:]
      var i = 0
      
      for collection in collectionPoints {
        collections[i.description] = [
          "coord": [
            "lat": collection.location.coordinate.latitude,
            "lng": collection.location.coordinate.longitude
          ],
          "date": collection.date.timeIntervalSince1970
        ]
        i += 1
      }
      
      result[key.id] = collections
    }
    
    return result
  }
}
