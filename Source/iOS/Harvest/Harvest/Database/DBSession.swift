//
//  DBSession.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase
import CoreLocation
import GoogleSignIn

extension HarvestDB {
  static func getSessions(_ completion: @escaping ([Session]) -> ()) {
    let sref = ref.child(Path.sessions)
    sref.observeSingleEvent(of: .value) { (snapshot) in
      var sessions = [Session]()
      for _child in snapshot.children {
        guard let child = _child as? DataSnapshot else {
          continue
        }
        
        guard let session = child.value as? [String: Any] else {
          continue
        }
        
        let s = Session(json: session, id: child.key)
        sessions.append(s)
      }
      completion(sessions)
    }
  }
  
  static func watchSessions(_ completion: @escaping ([Session]) -> ()) {
    let sref = ref.child(Path.sessions)
    sref.observe(.value) { (snapshot) in
      var sessions = [Session]()
      for _child in snapshot.children {
        guard let child = _child as? DataSnapshot else {
          continue
        }
        
        guard let session = child.value as? [String: Any] else {
          continue
        }
        
        let s = Session(json: session, id: child.key)
        sessions.append(s)
      }
      completion(sessions)
    }
  }
  
  static func save(session: Session) {
    let sessions = ref.child(Path.sessions)
    if session.id == "" {
      session.id = sessions.childByAutoId().key
    }
    let update = session.json()
    sessions.updateChildValues(update)
  }
}
