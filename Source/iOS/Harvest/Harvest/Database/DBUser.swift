//
//  DBUser.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/24.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase

extension HarvestDB {
  static func getHarvestUser(_ completion: @escaping (HarvestUser?) -> Void) {
    let aref = ref.child(Path.admin)
    aref.observeSingleEvent(of: .value) { snapshot in
      guard let user = snapshot.value as? [String: Any] else {
        completion(nil)
        return
      }
      
      let hu = HarvestUser(json: user)
      completion(hu)
    }
  }
  
  static func watchHarvestUser(_ completion: @escaping (HarvestUser) -> Void) {
    let aref = ref.child(Path.admin)
    aref.observe(.value) { (snapshot) in
      guard let user = snapshot.value as? [String: Any] else {
        return
      }
      
      let hu = HarvestUser(json: user)
      completion(hu)
    }
  }
  
  static func save(harvestUser: HarvestUser) {
    let admin = ref.child(Path.admin)
    
    let update = harvestUser.json()
    admin.updateChildValues(update)
  }
}
