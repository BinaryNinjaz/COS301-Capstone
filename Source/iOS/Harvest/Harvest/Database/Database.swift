//
//  Database.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/03/26.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase

enum HarvestDB {
  static var ref: DatabaseReference! = Database.database().reference()
  
  enum Path {
    static var parent: String {
      if ref is DatabaseReferenceMock {
        return "foo"
      }
      return HarvestUser.current.selectedWorkingForID?.uid ?? HarvestUser.current.uid
    }
    static var yields: String {
      return "\(Path.parent)/yields"
    }
    static var locations: String {
      return "\(Path.parent)/locations"
    }
    static var requestedLocations: String {
      return "\(Path.parent)/requestedLocations"
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
}

extension String {
  func removedFirebaseInvalids() -> String {
    var result = String()
    result.reserveCapacity(count)
    
    for c in self {
      switch c {
      case ".": result += ","
      case " ": continue
      default: result += "\(c)"
      }
    }
    
    return result
  }
}
