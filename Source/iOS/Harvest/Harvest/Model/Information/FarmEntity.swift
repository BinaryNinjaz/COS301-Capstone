//
//  FarmEntity.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Foundation

struct SearchPair<Item: Equatable>: Equatable {
  var item: Item
  var reason: String
  
  init(_ item: Item, _ reason: String) {
    (self.item, self.reason) = (item, reason)
  }
}

enum EntityItem: Equatable, CustomStringConvertible {
  enum Kind: CustomStringConvertible {
    case farm, orchard, worker, session
    case user
    case none
    
    var description: String {
      switch self {
      case .farm: return "Farm"
      case .orchard: return "Orchard"
      case .worker: return "Worker"
      case .session: return "Session"
      case .user: return "User"
      case .none: return "None"
      }
    }
  }
  
  case farm(Farm)
  case orchard(Orchard)
  case worker(Worker)
  case session(Session)
  case user(HarvestUser)
  
  var name: String {
    switch self {
    case let .farm(f): return f.name
    case let .orchard(o): return o.name
    case let .worker(w): return w.firstname + " " + w.lastname
    case let .session(s): return s.description
    case let .user(u): return u.displayName
    }
  }
  
  var id: String {
    switch self {
    case let .farm(f): return f.id
    case let .orchard(o): return o.id
    case let .worker(w): return w.id
    case let .session(s): return s.id
    case let .user(u): return u.uid
    }
  }
  
  var description: String {
    switch self {
    case let .farm(f): return f.description
    case let .orchard(o): return o.description
    case let .worker(w): return w.description
    case let .session(s): return s.description
    case let .user(u): return u.displayName
    }
  }
  
  func search(for text: String) -> [(String, String)] {
    switch self {
    case let .worker(w): return w.search(for: text)
    case let .farm(f): return f.search(for: text)
    case let .orchard(o): return o.search(for: text)
    default: return []
    }
  }
  
  var kind: Kind {
    switch self {
    case .farm: return .farm
    case .worker: return .worker
    case .orchard: return .orchard
    case .session: return .session
    case .user: return .user
    }
  }
  
  static func == (lhs: EntityItem, rhs: EntityItem) -> Bool {
    return lhs.id == rhs.id
  }
}

final class Entities {
  private(set) var farms = SortedDictionary<String, Farm>(<)
  private(set) var workers = SortedDictionary<String, Worker>(<)
  private(set) var orchards = SortedDictionary<String, Orchard>(<)
  
  private(set) var listners: [Int: () -> Void] = [:]
  
  static var shared = Entities()
  
  private init() {
    
  }
  
  func start() { // MUST be called at the start of main program after login
    watch(.farm)
    watch(.orchard)
    watch(.worker)
  }
  
  func reset() {
    farms.removeAll()
    workers.removeAll()
    orchards.removeAll()
  }
  
  func listen(with f: @escaping () -> Void) -> Int {
    f()
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
        self.runListners()
      }
      
    case .orchard:
      HarvestDB.getFarms { (farms) in
        self.farms = SortedDictionary(
          uniqueKeysWithValues: farms.map { farm in
            return (farm.name + farm.id, farm)
        }, <)
        HarvestDB.getOrchards({ (orchards) in
          self.orchards = SortedDictionary(
            uniqueKeysWithValues: orchards.map { v in
              (v.description + v.id, v)
          }, <)
          completion(self)
          self.runListners()
        })
      }
      
    case .farm:
      HarvestDB.getFarms { (farms) in
        self.farms = SortedDictionary(
          uniqueKeysWithValues: farms.map { farm in
            return (farm.name + farm.id, farm)
        }, <)
        self.orchards = SortedDictionary(
          uniqueKeysWithValues: self.orchards.map { _, v in
            (v.description + v.id, v)
        }, <)
        completion(self)
        self.runListners()
      }
      
    case .session: break
    case .user: break
    case .none: break
    }
  }
  
  func getMultiplesOnce(
    _ kinds: [EntityItem.Kind],
    completion: @escaping (Entities) -> Void
  ) {
    guard let f = kinds.first else {
      completion(self)
      return
    }
    getOnce(f) { entities in
      let s = Array(kinds.dropFirst())
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
      break
      
    case .farm:
      HarvestDB.watchFarms { (farms) in
        self.farms = SortedDictionary(
          uniqueKeysWithValues: farms.map { farm in
            return (farm.name + farm.id, farm)
        }, <)
        HarvestDB.watchOrchards { (orchards) in
          self.orchards = SortedDictionary(
            uniqueKeysWithValues: orchards.map { v in
              return (v.description + v.id, v)
          }, <)
          self.runListners()
        }
      }
      
    case .session: fatalError("Watching sessions is too expensive and should not be done")
    case .user: fatalError("Watching user is not supported")
    case .none: fatalError("Watching nothing is not supported")
    }
  }
  
  func addItem(_ item: EntityItem) {
    switch item {
    case let .farm(f): farms.insert(unique: (f.id, f))
    case let .orchard(o): orchards.insert(unique: (o.id, o))
    case let .worker(w): workers.insert(unique: (w.id, w))
    default: break
    }
  }
  
  func items(for kind: EntityItem.Kind) -> SortedDictionary<String, EntityItem>? {
    switch kind {
    case .farm: return farms.mapValues { .farm($0) }
    case .orchard: return orchards.mapValues { .orchard($0) }
    case .worker: return workers.mapValues { .worker($0) }
    case .session: return nil
    case .user: return nil
    case .none: return nil
    }
  }
  
  func worker(withId id: String) -> Worker? {
    if id == HarvestUser.current.uid {
      return Worker.currentWorker
    }
    
    guard let worker = workers.first(where: { $0.1.id == id })?.value else {
      return nil
    }
    
    return worker
  }
  
  func foremen() -> [Worker] {
    return Array(workers.lazy.map { $0.value }.filter { $0.kind == .foreman })
  }
  
  func workersList() -> [Worker] {
    return Array(workers.lazy.map { $0.value }.filter { $0.kind == .worker })
  }
  
  func entities(for kind: StatKind) -> [EntityItem] {
    switch kind {
    case .farm: return farms.map { .farm($0.value) }
    case .worker: return workers.compactMap { $0.value.kind == .worker ? .worker($0.value) : nil }
    case .foreman:
      return workers
        .compactMap { $0.value.kind == .foreman ? .worker($0.value) : nil }
        + [.worker(Worker(HarvestUser.current))]
    case .orchard: return orchards.map { .orchard($0.value) }
    }
  }
}

extension SortedDictionary where Value == EntityItem {
  func search(for text: String) -> SortedDictionary<String, SortedArray<SearchPair<EntityItem>>> {
    var result = SortedDictionary<String, SortedArray<SearchPair<EntityItem>>>()
    
    for (_, entity) in self {
      let props = entity.search(for: text)
      
      for (prop, reason) in props {
        if result[prop] == nil {
          result[prop] = SortedArray<SearchPair<EntityItem>>([]) { $0.item.name < $1.item.name }
        }
        let pair = SearchPair(entity, reason)
        if !(result[prop]?.contains(pair) ?? true) {
          result[prop]?.insert(pair)
        }
        
      }
    }
    
    return result
  }
}
