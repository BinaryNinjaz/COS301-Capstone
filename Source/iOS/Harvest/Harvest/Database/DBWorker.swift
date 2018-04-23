//
//  DMWorker.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase
import CoreLocation
import GoogleSignIn

extension HarvestDB {
  static func getWorkers(_ completion: @escaping ([Worker]) -> ()) {
    let wref = ref.child(Path.workers).queryOrdered(byChild: "surname")
    wref.observeSingleEvent(of: .value) { (snapshot) in
      var workers = [Worker]()
      for _child in snapshot.children {
        guard let child = _child as? DataSnapshot else {
          continue
        }
        
        guard let worker = child.value as? [String: Any] else {
          continue
        }
        let w = Worker(json: worker, id: child.key)
        workers.append(w)
      }
      completion(workers)
    }
  }
  
  static func watchWorkers(_ completion: @escaping ([Worker]) -> ()) {
    let wref = ref.child(Path.workers).queryOrdered(byChild: "surname")
    wref.observe(.value) { (snapshot) in
      var workers = [Worker]()
      for _child in snapshot.children {
        guard let child = _child as? DataSnapshot else {
          continue
        }
        
        guard let worker = child.value as? [String: Any] else {
          continue
        }
        let w = Worker(json: worker, id: child.key)
        workers.append(w)
      }
      completion(workers)
    }
  }
  
  static func save(worker: Worker, oldEmail: String) {
    let workers = ref.child(Path.workers)
    let foremen = ref.child(Path.foremen)
    
    if worker.id == "" {
      worker.id = workers.childByAutoId().key
    }
    let update = worker.json()
    workers.updateChildValues(update)
    if worker.email != "" {
      foremen.updateChildValues([worker.email.removedFirebaseInvalids(): true])
      saveWorkerReference(worker, oldEmail)
      if oldEmail != "" && worker.email != oldEmail {
        foremen.child(oldEmail.removedFirebaseInvalids()).removeValue()
      }
    }
  }
  
  static func saveWorkerReference(_ worker: Worker, _ oldEmail: String) {
    let workerRefs = ref.child(Path.workingFor + "/" + worker.email.removedFirebaseInvalids())
    
    let update = [
      HarvestUser.current.selectedOrganiztionUIDOrMine: HarvestUser.current.selectedOrganiztionNameOrMine
    ]
    workerRefs.updateChildValues(update)
  }
  
  static func delete(worker: Worker, completion: @escaping (Error?, DatabaseReference) -> ()) {
    let workers = ref.child(Path.workers)
    let foremen = ref.child(Path.foremen)
    let workingFor = ref.child(Path.workingFor)
    
    guard worker.id != "" else {
      return
    }
    workers.child(worker.id).removeValue(completionBlock: { err, ref in
      foremen.child(worker.email.removedFirebaseInvalids()).removeValue() { err, ref in
        completion(err, ref)
      }
      if let orgUID = HarvestUser.current.selectedOrganizationUID {
        workingFor.child(worker.email.removedFirebaseInvalids() + "/" + orgUID).removeValue()
      }
    })
  }
}
