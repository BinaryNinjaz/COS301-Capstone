//
//  DMWorker.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase

extension HarvestDB {
  static func getWorkers(_ completion: @escaping ([Worker]) -> Void) {
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
  
  static func watchWorkers(_ completion: @escaping ([Worker]) -> Void) {
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
  
  static func save(worker: Worker, oldNumber: String) {
    let workers = ref.child(Path.workers)
    let foremen = ref.child(Path.foremen)
    let workingFor = ref.child(Path.workingFor)
    
    if worker.id == "" {
      worker.id = workers.childByAutoId().key
    }
    let update = worker.json()
    workers.updateChildValues(update)
    
    if worker.kind == .foreman && worker.phoneNumber != "" {
      foremen.updateChildValues([worker.phoneNumber.removedFirebaseInvalids(): true])
      saveWorkerReference(worker, oldNumber)
      if oldNumber != "" && worker.phoneNumber != oldNumber {
        foremen.child(oldNumber.removedFirebaseInvalids()).removeValue()
      }
    } else if worker.phoneNumber != "" {
      foremen.child(worker.phoneNumber.removedFirebaseInvalids()).removeValue()
      workingFor.child(worker.phoneNumber.removedFirebaseInvalids()).removeValue()
    }
  }
  
  static func saveWorkerReference(_ worker: Worker, _ oldNumber: String) {
    let workerRefs = ref.child(Path.workingFor
      + "/"
      + worker.phoneNumber.removedFirebaseInvalids())
    
    let update = [
      HarvestUser.current.uid: worker.id
    ]
    workerRefs.updateChildValues(update)
  }
  
  static func delete(
    worker: Worker,
    completion: @escaping (Error?, DatabaseReference) -> Void
  ) {
    let workers = ref.child(Path.workers)
    let foremen = ref.child(Path.foremen)
    let workingFor = ref.child(Path.workingFor)
    
    guard worker.id != "" else {
      return
    }
    workers.child(worker.id).removeValue(completionBlock: { err, ref in
      guard worker.phoneNumber != "" else {
        completion(err, ref)
        return
      }
      foremen.child(worker
        .phoneNumber
        .removedFirebaseInvalids()).removeValue { err, ref in
        completion(err, ref)
      }
      workingFor.child(worker.phoneNumber.removedFirebaseInvalids()).removeValue()
    })
  }
  
  static func resign(completion: @escaping (Error?, DatabaseReference) -> Void) {
    let workers = Entities.shared.workers
    guard let workerIdx = workers.index(where: { (_, w) -> Bool in
      w.phoneNumber == HarvestUser.current.accountIdentifier
    }) else {
      return
    }
    
    let worker = workers[workerIdx]
    delete(worker: worker.value, completion: completion)
  }
}
