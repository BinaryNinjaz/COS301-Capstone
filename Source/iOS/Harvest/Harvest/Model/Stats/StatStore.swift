//
//  StatStore.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/28.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Disk

struct StatStore {
  struct Item: Codable {
    var ids: [String]
    var startDate: Date
    var endDate: Date
    var period: HarvestCloud.TimePeriod
    var grouping: HarvestCloud.GroupBy
    var mode: HarvestCloud.Mode
    var name: String
  }
  
  static var shared = StatStore()
  
  let path = "StatStore"
  var store: [Item]
  
  init() {
    store = (try? Disk.retrieve(path, from: .documents, as: [Item].self)) ?? []
  }
  
  mutating func saveItem(item: Item) {
    try? Disk.append(item, to: path, in: .documents)
    store.append(item)
  }
  
  mutating func removeItem(withName name: String) {
    if let idx = store.index(where: { $0.name == name }) {
      store.remove(at: idx)
      try? Disk.save(store, to: .documents, as: path)
    }
  }
  
  mutating func renameItem(withName name: String, toNewName newName: String) {
    if let idx = store.index(where: { $0.name == name }) {
      store[idx].name = newName
      try? Disk.save(store, to: .documents, as: path)
    }
  }
  
  func getItem(withName name: String) -> Item? {
    return store.first { $0.name == name }
  }
}
