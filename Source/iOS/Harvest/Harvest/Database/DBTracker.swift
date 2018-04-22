//
//  DBTracker.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase
import CoreLocation
import GoogleSignIn

extension HarvestDB {
  static func collect(from workers: [Worker: [CollectionPoint]],
                      byUserId uid: String,
                      on date: Date,
                      track: [CLLocationCoordinate2D]) {
    let cref = ref.child(Path.sessions)
    let key = cref.childByAutoId().key
    
    let data: [String: Any] = [
      "start_date": date.timeIntervalSince1970,
      "end_date": Date().timeIntervalSince1970,
      "uid": uid,
      "collections": workers.firebaseSessionRepresentation(),
      "track": track.firbaseCoordRepresentation()
    ]
    
    let updates = [key: data]
    
    cref.updateChildValues(updates)
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
}
