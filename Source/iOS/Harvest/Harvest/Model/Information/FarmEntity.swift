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
    case farm, orchard, worker, session, none
  }
  
  case farm(Farm)
  case orchard(Orchard)
  case worker(Worker)
  case session(Session)
  
  var name: String {
    switch self {
    case let .farm(f): return f.name
    case let .orchard(o): return o.name
    case let .worker(w): return w.firstname + " " + w.lastname
    case let .session(s): return s.description // FIXME
    }
  }
  
  var id: String {
    switch self {
    case let .farm(f): return f.id
    case let .orchard(o): return o.id
    case let .worker(w): return w.id
    case let .session(s): return s.id
    }
  }
  
  var farm: Farm? {
    switch self {
    case let .farm(f): return f
    default: return nil
    }
  }
  
  var orchard: Orchard? {
    switch self {
    case let .orchard(o): return o
    default: return nil
    }
  }
  
  var worker: Worker? {
    switch self {
    case let .worker(w): return w
    default: return nil
    }
  }
  
  var session: Session? {
    switch self {
    case let .session(s): return s
    default: return nil
    }
  }
}

extension Array where Element == EntityItem {
  func orchards() -> [Orchard] {
    return compactMap {
      if case let .orchard(o) = $0 {
        return o
      } else {
        return nil
      }
    }
  }
  
  func workers() -> [Worker] {
    return compactMap {
      if case let .worker(w) = $0 {
        return w
      } else {
        return nil
      }
    }
  }
  
  func farms() -> [Farm] {
    return compactMap {
      if case let .farm(f) = $0 {
        return f
      } else {
        return nil
      }
    }
  }
  
  func sessions() -> [Session] {
    return compactMap {
      if case let .session(s) = $0 {
        return s
      } else {
        return nil
      }
    }
  }
}

typealias SortedEntity = SortedDictionary<String, EntityItem>

extension SortedDictionary where Key == String, Value == EntityItem {
  func orchards() -> [Orchard] {
    return compactMap {
      if case let .orchard(o) = $0.value {
        return o
      } else {
        return nil
      }
    }
  }
  
  func workers() -> [Worker] {
    return compactMap {
      if case let .worker(w) = $0.value {
        return w
      } else {
        return nil
      }
    }
  }
  
  func farms() -> [Farm] {
    return compactMap {
      if case let .farm(f) = $0.value {
        return f
      } else {
        return nil
      }
    }
  }
  
  func sessions() -> [Session] {
    return compactMap {
      if case let .session(s) = $0.value {
        return s
      } else {
        return nil
      }
    }
  }
}


class Entities {
  private(set) var farms = SortedEntity(<)
  private(set) var workers = SortedEntity(<)
  private(set) var orchards = SortedEntity(<)
  private(set) var sessions = SortedEntity(<)
  
  static var shared = Entities()
  
  private init() {
    watch(.farm)
    watch(.orchard)
    watch(.worker)
    watch(.session)
  }
  
  func getOnce(_ kind: EntityItem.Kind, completion: @escaping (Entities) -> ()) {
    switch kind {
    case .worker:
      HarvestDB.getWorkers { (workers) in
        self.workers = SortedDictionary(
          uniqueKeysWithValues: workers.map { worker in
            return (worker.firstname + " " + worker.lastname, .worker(worker))
        }, <)
        completion(self)
      }
    case .orchard:
      HarvestDB.getOrchards { (orchards) in
        self.orchards = SortedDictionary(
          uniqueKeysWithValues: orchards.map { orchard in
            return (orchard.name, .orchard(orchard))
        }, <)
        completion(self)
      }
    case .farm:
      HarvestDB.getFarms { (farms) in
        self.farms = SortedDictionary(
          uniqueKeysWithValues: farms.map { farm in
            return (farm.name, .farm(farm))
        }, <)
        completion(self)
      }
    case .session:
      HarvestDB.getSessions { (sessions) in
        self.sessions = SortedDictionary(
          uniqueKeysWithValues: sessions.map { session in
            return (session.key, .session(session))
        }, <)
        completion(self)
      }
      
    case .none: break
    }
  }
  
  func getMultiplesOnce(
    _ kinds: Set<EntityItem.Kind>,
    completion: @escaping (Entities) -> ()
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
            return (worker.firstname + " " + worker.lastname, .worker(worker))
        }, <)
      }
    case .orchard:
      HarvestDB.watchOrchards { (orchards) in
        self.orchards = SortedDictionary(
          uniqueKeysWithValues: orchards.map { orchard in
            return (orchard.name, .orchard(orchard))
        }, <)
      }
    case .farm:
      HarvestDB.watchFarms { (farms) in
        self.farms = SortedDictionary(
          uniqueKeysWithValues: farms.map { farm in
            return (farm.name, .farm(farm))
        }, <)
      }
    case .session:
      HarvestDB.watchSessions { (sessions) in
        self.sessions = SortedDictionary(
          uniqueKeysWithValues: sessions.map { session in
            return (session.key, .session(session))
        }, <)
      }
      
    case .none: break
    }
  }
  
  func items(for kind: EntityItem.Kind) -> SortedEntity? {
    switch kind {
    case .farm: return farms
    case .orchard: return orchards
    case .worker: return workers
    case .session: return sessions
    case .none: return nil
    }
  }
  
  func workersList() -> [Worker] {
    return workers.workers()
  }
  
  func farmsList() -> [Farm] {
    return farms.farms()
  }
  
  func orchardsList() -> [Orchard] {
    return orchards.orchards()
  }
  
  func sessionsList() -> [Session] {
    return sessions.sessions()
  }
  
  func sessionDates() -> [Date] {
    var result = [Date]()
    
    for session in sessionsList() {
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
    
    for session in sessionsList() {
      let sComps = Calendar.current.dateComponents([.day], from: session.startDate)
      
      if sComps.day == dayComp.day {
        result.append(session)
      }
      
    }
    
    return result
  }
  
  func worker(withId id: String) -> Worker? {
    guard let entity = workers
      .first(where: { $0.1.id == id })?
    .value else {
      return nil
    }
    
    if case let .worker(w) = entity {
      return w
    }
    return nil
  }
}
