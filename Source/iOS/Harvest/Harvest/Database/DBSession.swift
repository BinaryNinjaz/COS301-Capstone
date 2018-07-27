//
//  DBSession.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase

extension HarvestDB {
  private static var sessionListners = [UInt]()
  private static var sessionEndKey: String?
  
  static func getSessions(
    limitedToLast n: UInt,
    _ completion: @escaping ([Session]) -> Void
  ) {
    let sref: DatabaseQuery
    if let end = sessionEndKey {
      sref = ref.child(Path.sessions)
        .queryOrderedByKey()
        .queryEnding(atValue: end)
        .queryLimited(toLast: n)
    } else {
      sref = ref.child(Path.sessions)
        .queryOrderedByKey()
        .queryLimited(toLast: n)
    }
    
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
      sessionEndKey = sessions.last?.id
      if !sessions.isEmpty {
        sessions.removeLast()
      }
      completion(sessions)
    }
    
    sessionListners += [sref.observe(.value, with: reader)]
  }
  
  static func getRefreshedSessions(
    limitedToLast n: UInt,
    _ completion: @escaping ([Session]) -> Void
  ) {
    sessionListners.forEach { ref.removeObserver(withHandle: $0) }
    sessionEndKey = nil
    getSessions(limitedToLast: n, completion)
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
