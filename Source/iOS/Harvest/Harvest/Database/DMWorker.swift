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
  
  static func save(worker: Worker) {
    let workers = ref.child(Path.workers)
    if worker.id == "" {
      worker.id = workers.childByAutoId().key
    }
    let update = worker.json()
    workers.updateChildValues(update)
    if worker.email != "" {
      saveWorkerReference(worker)
    }
  }
  
  static func saveWorkerReference(_ worker: Worker) {
    let workerRefs = ref.child(Path.workingFor + "/" + worker.email.removedFirebaseInvalids())
    
    let update = [
      HarvestUser.current.uid: HarvestUser.current.organizationName
    ]
    workerRefs.updateChildValues(update)
  }
}
