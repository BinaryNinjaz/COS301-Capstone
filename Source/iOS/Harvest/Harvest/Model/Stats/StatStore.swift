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
  }
  
  static var shared = StatStore()
  
  var statDataNames: [String]
  var statData: [Item]
  
  init() {
    statDataNames = (try? Disk.retrieve("StatDataNames", from: .applicationSupport, as: [String].self))
      ?? []
    statData = []
    for name in statDataNames {
      if let data = try? Disk.retrieve(name, from: .applicationSupport, as: Item.self) {
        statData.append(data)
      }
    }
  }
  
  mutating func saveItem(item: Item, withName name: String) {
    try? Disk.append(name, to: "StatDataNames", in: .applicationSupport)
    try? Disk.save(item, to: .applicationSupport, as: name)
    statDataNames.append(name)
    statData.append(item)
  }
  
  mutating func removeItem(withName name: String) {
    if let _ = try? Disk.retrieve(name, from: .applicationSupport, as: Item.self) {
      try? Disk.remove(name, from: .applicationSupport)
      if let idx = statDataNames.index(of: name) {
        statDataNames.remove(at: idx)
        statData.remove(at: idx)
      }
      try? Disk.save(statDataNames, to: .applicationSupport, as: "StatDataNames")
    }
  }
  
  mutating func renameItem(withName name: String, toNewName newName: String) {
    if let item = try? Disk.retrieve(name, from: .applicationSupport, as: Item.self) {
      try? Disk.remove(name, from: .applicationSupport)
      try? Disk.save(item, to: .applicationSupport, as: newName)
      if let idx = statDataNames.index(of: name) {
        statDataNames[idx] = newName
      }
      try? Disk.save(statDataNames, to: .applicationSupport, as: "StatDataNames")
    }
  }
  
  func getItem(withName name: String) -> Item? {
    return try? Disk.retrieve(name, from: .applicationSupport, as: Item.self)
  }
}
