//
//  DBSession.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase

extension HarvestDB {
  static func getSessions(
    limitedToLast n: UInt,
    fromKey end: String?,
    _ completion: @escaping ([Session], String) -> Void
  ) {
    let reader: (DataSnapshot) -> Void = { snapshot in
      var sessions = [Session]()
      for _child in snapshot.children {
        guard let child = _child as? DataSnapshot else {
          continue
        }
        
        guard let session = child.value as? [String: Any] else {
          continue
        }
        let w = Session(json: session, id: child.key)
        sessions.insert(w, at: 0)
      }
      let end = sessions.last?.id ?? ""
      sessions.removeLast()
      completion(sessions, end)
    }
    
    let sref: DatabaseQuery
    if let end = end {
      sref = ref.child(Path.sessions)
        .queryOrderedByKey()
        .queryEnding(atValue: end)
        .queryLimited(toLast: n)
    } else {
      sref = ref.child(Path.sessions)
        .queryOrderedByKey()
        .queryLimited(toLast: n)
    }
    
    sref.observeSingleEvent(of: .value, with: reader)
  }
  
  static func getSession(id: String, _ completion: @escaping (Session) -> Void) {
    let sref = ref.child(Path.sessions + "/" + id)
    sref.observeSingleEvent(of: .value) { (snapshot) in
      guard let session = snapshot.value as? [String: Any] else {
        return
      }
      
      let s = Session(json: session, id: snapshot.key)
      completion(s)
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
  
  static func delete(
    session: Session,
    completion: @escaping (Error?, DatabaseReference) -> Void
  ) {
    let sessions = ref.child(Path.sessions)
    guard session.id != "" else {
      return
    }
    sessions.child(session.id).removeValue(completionBlock: { (err, ref) in
      completion(err, ref)
    })
  }
}
