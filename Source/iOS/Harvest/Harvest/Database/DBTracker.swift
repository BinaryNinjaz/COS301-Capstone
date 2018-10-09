//
//  DBTracker.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import FirebaseDatabase
import CoreLocation

extension HarvestDB {
  static func collect(from workers: [Worker: [CollectionPoint]],
                      byWorkerId wid: String,
                      on date: Date,
                      track: [CLLocationCoordinate2D]
  ) {
    let cref = ref.child(Path.sessions)
    let key = cref.childByAutoId().key
    
    let data: [String: Any] = [
      "start_date": DateFormatter.rfc2822String(from: date),
      "end_date": DateFormatter.rfc2822String(from: Date()),
      "wid": wid,
      "collections": workers.firebaseSessionRepresentation(),
      "track": track.firbaseCoordRepresentation()
    ]
    
    let updates = [key: data]
    cref.updateChildValues(updates)
  }
  
  static func update(location: CLLocationCoordinate2D) {
    guard let id = HarvestUser.current.selectedWorkingForID else {
      return
    }
    
    let worker = Entities.shared.worker(withId: id.wid)
    
    let name = (worker?.firstname ?? "") + " " + (worker?.lastname ?? "")
    
    let locations = ref.child(Path.locations)
    let updates = [
        id.wid: [
          "coord": [
            "lat": location.latitude,
            "lng": location.longitude
          ],
          "display": name,
          "date": DateFormatter.rfc2822String(from: Date())
        ]
    ]
    locations.updateChildValues(updates)
  }
  
  static func deleteLocationRequest(forWorkerId wid: String) {
    let request = ref.child(Path.requestedLocations + "/" + wid)
    request.removeValue()
  }
  
  static var locationRequestsListner: UInt?
  static func listenLocationRequested(completion: @escaping (Bool) -> Void) {
    let requests = ref.child(Path.requestedLocations)
    locationRequestsListner = requests.observe(.childAdded) { (snapshot) in
      if snapshot.key == HarvestUser.current.selectedWorkingForID?.wid {
        deleteLocationRequest(forWorkerId: snapshot.key)
        completion(true)
      } else {
        completion(false)
      }
    }
  }
  
  static func checkLocationRequested(completion: @escaping (Bool) -> Void) {
    let requests = ref.child(Path.requestedLocations)
    requests.observeSingleEvent(of: .value) { (snapshot) in
      guard let reqs = snapshot.value as? [String: Any] else {
        return
      }
      for (k, _) in reqs where k == HarvestUser.current.selectedWorkingForID?.wid {
        deleteLocationRequest(forWorkerId: snapshot.key)
        completion(true)
      }
    }
  }
  
  static func removeListnerForLocationRequests() {
    if let handle = locationRequestsListner {
      let requests = ref.child(Path.requestedLocations)
      requests.removeObserver(withHandle: handle)
    }
  }
}
