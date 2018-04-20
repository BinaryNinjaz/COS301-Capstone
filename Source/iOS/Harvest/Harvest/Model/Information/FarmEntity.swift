//
//  FarmEntity.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/20.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Swift

enum EntityItem {
  enum Kind {
    case farm, orchard, worker, none
  }
  
  case farm(Farm)
  case orchard(Orchard)
  case worker(Worker)
  
  var name: String {
    switch self {
    case let .farm(f): return f.name
    case let .orchard(o): return o.name
    case let .worker(w): return w.firstname + " " + w.lastname
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
}

typealias SortedEntity = SortedDictionary<String, EntityItem>

class Entities {
  private var farms = SortedDictionary<String, EntityItem>(<)
  private var workers = SortedDictionary<String, EntityItem>(<)
  private var orchards = SortedDictionary<String, EntityItem>(<)
  
  static var shared = Entities()
  
  private init() {
    watch(.farm)
    watch(.orchard)
    watch(.worker)
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
    case .none: break
    }
  }
  
  func items(for kind: EntityItem.Kind) -> SortedEntity? {
    switch kind {
    case .farm: return farms
    case .orchard: return orchards
    case .worker: return workers
    case .none: return nil
    }
  }
}
