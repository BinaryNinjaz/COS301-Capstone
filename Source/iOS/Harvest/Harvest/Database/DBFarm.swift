//
//  DBFarm.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase

extension HarvestDB {
  static func getFarms(_ completion: @escaping ([Farm]) -> Void) {
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
  
  static var farms = [Farm]()
  
  static func watchFarms(_ completion: @escaping ([Farm]) -> Void) {
    let fref = ref.child(Path.farms)
    fref.observe(.value) { (snapshot) in
      farms.removeAll()
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
    fref.observe(.childAdded) { (snapshot) in
      guard let farm = snapshot.value as? [String: Any] else {
        return
      }
      let f = Farm(json: farm, id: snapshot.key)
      if farms.index(where: { $0.id == f.id }) == nil {
        farms.append(f)
        completion(farms)
      }
    }
    fref.observe(.childRemoved) { (snapshot) in
      guard let farm = snapshot.value as? [String: Any] else {
        return
      }
      let f = Farm(json: farm, id: snapshot.key)
      if let idx = farms.index(where: { $0.id == f.id }) {
        farms.remove(at: idx)
        completion(farms)
      }
    }
    fref.observe(.childChanged) { (snapshot) in
      guard let farm = snapshot.value as? [String: Any] else {
        return
      }
      let f = Farm(json: farm, id: snapshot.key)
      if let idx = farms.index(where: { $0.id == f.id }) {
        farms.remove(at: idx)
        farms.insert(f, at: idx)
        completion(farms)
      }
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
    completion: @escaping (Error?, DatabaseReference) -> Void
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
