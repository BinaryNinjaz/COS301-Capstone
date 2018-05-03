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

struct HarvestDB {
  static var ref: DatabaseReference! = Database.database().reference()
  
  struct Path {
    static var parent: String {
      return HarvestUser.current.selectedOrganizationUID == nil
        ? HarvestUser.current.uid
        : HarvestUser.current.selectedOrganizationUID!
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
    static var foremen: String {
      return "\(Path.parent)/foremen"
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
    static var admin: String {
      return "\(Path.parent)/admin"
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
      if !".".contains(c) {
        result += "\(c)"
      } else {
        result += ","
      }
    }
    
    return result
  }
}
