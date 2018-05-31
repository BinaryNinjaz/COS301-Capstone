//
//  DBFarm.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase
import CoreLocation
import GoogleSignIn

extension HarvestDB {
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
    if farm.id == "" {
      farm.id = farms.childByAutoId().key
    }
    let update = farm.json()
    farms.updateChildValues(update)
  }
  
  static func delete(
    farm: Farm,
    completion: @escaping (Error?, DatabaseReference) -> ()
  ) {
    let farms = ref.child(Path.farms)
    guard farm.id != "" else {
      return
    }
    
    farms.child(farm.id).removeValue(completionBlock: { (err, ref) in
      completion(err, ref)
    })
  }
}
