//
//  FarmEntity.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Foundation

enum EntityItem {
  enum Kind {
    case farm, orchard, worker, session
    case shallowSession
    case user
    case none
  }
  
  case farm(Farm)
  case orchard(Orchard)
  case worker(Worker)
  case session(Session)
  case shallowSession(ShallowSession)
  case user(HarvestUser)
  
  var name: String {
    switch self {
    case let .farm(f): return f.name
    case let .orchard(o): return o.name
    case let .worker(w): return w.firstname + " " + w.lastname
    case let .session(s): return s.description
    case let .shallowSession(s): return s.description
    case let .user(u): return u.displayName
    }
  }
}

final class Entities {
  private(set) var farms = SortedDictionary<String, Farm>(<)
  private(set) var workers = SortedDictionary<String, Worker>(<)
  private(set) var orchards = SortedDictionary<String, Orchard>(<)
  private(set) var sessions = SortedDictionary<String, Session>(>)
  private(set) var shallowSessions = SortedDictionary<String, ShallowSession>(>)
  
  private(set) var listners: [Int: () -> Void] = [:]
  
  static var shared = Entities()
  
  private init() {
    watch(.farm)
    watch(.orchard)
    watch(.worker)
    watch(.session)
    getOnce(.shallowSession) { _ in }
  }
  
  func reset() {
    farms.removeAll()
    workers.removeAll()
    orchards.removeAll()
    sessions.removeAll()
    shallowSessions.removeAll()
  }
  
  func listen(with f: @escaping () -> Void) -> Int {
    listners[listners.count] = f
    return listners.count - 1
  }
  
  func deregister(listner id: Int) {
    _ = listners.removeValue(forKey: id)
  }
  
  func runListners() {
    listners.forEach { $0.1() }
  }
  
  func listenOnce(with f: @escaping () -> Void) {
    let id = listners.count
    let g: () -> Void = {
      f()
      self.deregister(listner: id)
    }
    
    listners[listners.count] = g
  }
  
  func getOnce(_ kind: EntityItem.Kind, completion: @escaping (Entities) -> Void) {
    switch kind {
    case .worker:
      HarvestDB.getWorkers { (workers) in
        self.workers = SortedDictionary(
          uniqueKeysWithValues: workers.map { worker in
            return (worker.lastname + worker.firstname + worker.id, worker)
        }, <)
        completion(self)
      }
      
    case .orchard:
      HarvestDB.getOrchards { (orchards) in
        self.orchards = SortedDictionary(
          uniqueKeysWithValues: orchards.map { orchard in
            return (orchard.assignedFarm + orchard.name + orchard.id, orchard)
        }, <)
        completion(self)
      }
      
    case .farm:
      HarvestDB.getFarms { (farms) in
        self.farms = SortedDictionary(
          uniqueKeysWithValues: farms.map { farm in
            return (farm.name + farm.id, farm)
        }, <)
        completion(self)
      }
      
    case .session:
      HarvestDB.getSessions { (sessions) in
        self.sessions = SortedDictionary(
          uniqueKeysWithValues: sessions.map { session in
            return (session.key, session)
        }, >)
        completion(self)
      }
      
    case .shallowSession:
      HarvestDB.getShallowSessions(onPage: 1, ofSize: 100) { (sessions) in
        self.shallowSessions = SortedDictionary(
          uniqueKeysWithValues: sessions.map { session in
            return (session.startDate.description, session)
        }, >)
        completion(self)
      }
      
    case .user: break
    case .none: break
    }
  }
  
  func getMultiplesOnce(
    _ kinds: Set<EntityItem.Kind>,
    completion: @escaping (Entities) -> Void
    ) {
    guard let f = kinds.first else {
      completion(self)
      return
    }
    getOnce(f) { entities in
      let s = Set(kinds.dropFirst())
      entities.getMultiplesOnce(s, completion: completion)
    }
  }
  
  func watch(_ kind: EntityItem.Kind) {
    switch kind {
    case .worker:
      HarvestDB.watchWorkers { (workers) in
        self.workers = SortedDictionary(
          uniqueKeysWithValues: workers.map { worker in
            return (worker.lastname + " " + worker.firstname + worker.id, worker)
        }, <)
        self.runListners()
      }
    case .orchard:
      HarvestDB.watchOrchards { (orchards) in
        self.orchards = SortedDictionary(
          uniqueKeysWithValues: orchards.map { orchard in
            return (orchard.name + orchard.id, orchard)
        }, <)
        self.runListners()
      }
    case .farm:
      HarvestDB.watchFarms { (farms) in
        self.farms = SortedDictionary(
          uniqueKeysWithValues: farms.map { farm in
            return (farm.name + farm.id, farm)
        }, <)
        self.runListners()
      }
    case .session:
      HarvestDB.watchSessions { (sessions) in
        self.sessions = SortedDictionary(
          uniqueKeysWithValues: sessions.map { session in
            return (session.key + session.id, session)
        }, >)
        self.runListners()
        self.getOnce(.shallowSession) { _ in }
      }
      
    case .shallowSession: fatalError("Watching shallow session is not supported")
    case .user: fatalError("Watching user is not supported")
    case .none: fatalError("Watching nothing is not supported")
    }
  }
  
  func items(for kind: EntityItem.Kind) -> SortedDictionary<String, EntityItem>? {
    switch kind {
    case .farm: return farms.mapValues { .farm($0) }
    case .orchard: return orchards.mapValues { .orchard($0) }
    case .worker: return workers.mapValues { .worker($0) }
    case .session: return sessions.mapValues { .session($0) }
    case .shallowSession: return shallowSessions.mapValues { .shallowSession($0) }
    case .user: return nil
    case .none: return nil
    }
  }
  
  func sessionDates() -> [Date] {
    var result = [Date]()
    
    for (_, session) in sessions {
      var comps = Calendar.current.dateComponents([.day], from: session.startDate)
      
      if !result.contains(where: {
        Calendar.current.dateComponents([.day], from: $0).day == comps.day
      }) {
        result.append(session.startDate)
      }
    }
    
    return result
  }
  
  func sessionsFor(day: Date) -> [Session] {
    var result = [Session]()
    
    let dayComp = Calendar.current.dateComponents([.day], from: day)
    
    for (_, session) in sessions {
      let sComps = Calendar.current.dateComponents([.day], from: session.startDate)
      
      if sComps.day == dayComp.day {
        result.append(session)
      }
      
    }
    
    return result
  }
  
  func worker(withId id: String) -> Worker? {
    if id == HarvestUser.current.uid {
      return Worker.currentWorker
    }
    
    guard let worker = workers
      .first(where: { $0.1.id == id })?
    .value else {
      return nil
    }
    
    return worker
  }
}
