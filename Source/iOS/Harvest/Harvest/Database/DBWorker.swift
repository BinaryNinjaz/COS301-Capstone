//
//  DMWorker.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 University of Pretoria. All rights reserved.
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
  
  static var workers = [Worker]()
  
  static func watchWorkers(_ completion: @escaping ([Worker]) -> Void) {
    let wref = ref.child(Path.workers).queryOrdered(byChild: "surname")
    wref.observeSingleEvent(of: .value) { (snapshot) in
      workers.removeAll()
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
    wref.observe(.childAdded) { (snapshot) in
      guard let worker = snapshot.value as? [String: Any] else {
        return
      }
      let w = Worker(json: worker, id: snapshot.key)
      if workers.index(where: { $0.id == w.id }) == nil {
        workers.append(w)
        completion(workers)
      }
    }
    wref.observe(.childRemoved) { (snapshot) in
      guard let worker = snapshot.value as? [String: Any] else {
        return
      }
      let w = Worker(json: worker, id: snapshot.key)
      if let idx = workers.index(where: { $0.id == w.id }) {
        workers.remove(at: idx)
        completion(workers)
      }
    }
    wref.observe(.childChanged) { (snapshot) in
      guard let worker = snapshot.value as? [String: Any] else {
        return
      }
      let w = Worker(json: worker, id: snapshot.key)
      if let idx = workers.index(where: { $0.id == w.id }) {
        workers.remove(at: idx)
        workers.insert(w, at: idx)
        completion(workers)
      }
    }
  }
  
  static func save(worker: Worker, oldWorker: Worker) {
    let workers = ref.child(Path.workers)
    let foremen = ref.child(Path.foremen)
    let workingFor = ref.child(Path.workingFor)
    let locations = ref.child(Path.locations)
    let requestedLocations = ref.child(Path.requestedLocations)
    
    if worker.id == "" {
      worker.id = workers.childByAutoId().key
    }
    let update = worker.json()
    workers.updateChildValues(update)
    
    if worker.kind == .worker && oldWorker.kind == .foreman && oldWorker.phoneNumber != "" {
      let workerPath = worker.phoneNumber.removedFirebaseInvalids()
      foremen.child(workerPath).removeValue()
      workingFor.child(workerPath).removeValue()
      locations.child(workerPath).removeValue()
      requestedLocations.child(workerPath).removeValue()
    }
    
    if worker.kind == .foreman && worker.phoneNumber != "" {
      foremen.updateChildValues([worker.phoneNumber.removedFirebaseInvalids(): true])
      saveWorkerReference(worker, oldWorker.phoneNumber)
      if oldWorker.phoneNumber != "" && worker.phoneNumber != oldWorker.phoneNumber {
        foremen.child(oldWorker.phoneNumber.removedFirebaseInvalids()).removeValue()
      }
    } else if worker.phoneNumber != "" {
      let workerPath = worker.phoneNumber.removedFirebaseInvalids()
      foremen.child(workerPath).removeValue()
      workingFor.child(workerPath).removeValue()
      locations.child(workerPath).removeValue()
      requestedLocations.child(workerPath).removeValue()
    }
  }
  
  static func saveWorkerReference(_ worker: Worker, _ oldNumber: String) {
    if worker.phoneNumber != "" {
      let workerRefs = ref.child(Path.workingFor
        + "/"
        + worker.phoneNumber.removedFirebaseInvalids())
      let update = [
        HarvestUser.current.uid: worker.id
      ]
      workerRefs.updateChildValues(update)
    }
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
      if let uid = HarvestUser.current.selectedWorkingForID?.uid {
        workingFor
          .child(worker.phoneNumber.removedFirebaseInvalids())
          .child(uid)
          .removeValue()
      }
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
    delete(worker: worker.value) { err, ref in
      HarvestDB.signOut { _ in
        completion(err, ref)
      }
    }
  }
}
