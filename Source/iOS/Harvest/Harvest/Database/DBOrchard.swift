//
//  DBOrchard.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase
import CoreLocation
import GoogleSignIn

extension HarvestDB {
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
  
  static func save(orchard: Orchard) {
    let orchards = ref.child(Path.orchards)
    if orchard.id == "" {
      orchard.id = orchards.childByAutoId().key
    }
    let update = orchard.json()
    orchards.updateChildValues(update)
  }
}
