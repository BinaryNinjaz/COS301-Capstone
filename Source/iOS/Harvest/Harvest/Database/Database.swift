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
      return "" //HarvestUser.current.email.removedFirebaseInvalids() // FIXME
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
      return "\(Path.parent)/yields" // FIXME
    }
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
