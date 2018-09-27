//
//  StatStore.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/28.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Disk

struct StatStore {
  static var shared = StatStore()
  
  var path: String {
    return HarvestUser.current.uid + "StatStore"
  }
  var store: [Stat] = []
  
  init() {
    store = []
//    try? Disk.remove(path, from: .applicationSupport)
  }
  
  mutating func updateStore() {
    store = (try? Disk.retrieve(path, from: .applicationSupport, as: [Stat].self)) ?? []
  }
  
  mutating func saveItem(item: Stat) {
    try? Disk.append(item, to: path, in: .applicationSupport)
    store.append(item)
  }
  
  mutating func removeItem(withName name: String) {
    if let idx = store.index(where: { $0.name == name }) {
      store.remove(at: idx)
      try? Disk.save(store, to: .applicationSupport, as: path)
    }
  }
  
  mutating func renameItem(withName name: String, toNewName newName: String) {
    if let idx = store.index(where: { $0.name == name }) {
      store[idx].name = newName
      try? Disk.save(store, to: .applicationSupport, as: path)
    }
  }
  
  func getItem(withName name: String) -> Stat? {
    return store.first { $0.name == name }
  }
}

extension StatStore {
  // swiftlint:disable function_body_length
  func setUpPredefinedGraphs() {
    var stats = [Stat]()
    
    stats ++= Stat(
      ids: [],
      timePeriod: .today,
      timeStep: .hourly,
      grouping: .worker,
      mode: .running,
      name: "Today's Worker Performance",
      showExpected: true,
      showAverage: true,
      curveKind: .curve)
    
    stats ++= Stat(
      ids: [],
      timePeriod: .yesterday,
      timeStep: .hourly,
      grouping: .worker,
      mode: .running,
      name: "Yesterday's Worker Performance",
      showExpected: true,
      showAverage: true,
      curveKind: .curve)
    
    stats ++= Stat(
      ids: [],
      timePeriod: .between(Date(), Date()),
      timeStep: .daily,
      grouping: .worker,
      mode: .running,
      name: "Worker Performance",
      showExpected: true,
      showAverage: true,
      curveKind: .curve)
    
    stats ++= Stat(
      ids: [],
      timePeriod: .thisMonth,
      timeStep: .daily,
      grouping: .orchard,
      mode: .running,
      name: "This Month's Orchard Performance",
      showExpected: true,
      showAverage: true,
      curveKind: .curve)
    
    stats ++= Stat(
      ids: [],
      timePeriod: .between(Date(), Date()),
      timeStep: .daily,
      grouping: .orchard,
      mode: .running,
      name: "Orchard Performance",
      showExpected: true,
      showAverage: true,
      curveKind: .curve)
    
    stats ++= Stat(
      ids: [],
      timePeriod: .thisMonth,
      timeStep: .daily,
      grouping: .farm,
      mode: .running,
      name: "This Month's Farm Performance",
      showExpected: true,
      showAverage: true,
      curveKind: .curve)
    
    stats ++= Stat(
      ids: [],
      timePeriod: .between(Date(), Date()),
      timeStep: .daily,
      grouping: .farm,
      mode: .running,
      name: "Farm Performance",
      showExpected: true,
      showAverage: true,
      curveKind: .curve)
    
    for stat in stats {
      StatStore.shared.saveItem(item: stat)
    }
  }
}
