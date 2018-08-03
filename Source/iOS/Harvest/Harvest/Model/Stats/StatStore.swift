//
//  StatStore.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/28.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Disk

struct StatStore {
  enum TimeRange: Codable, Equatable, CustomStringConvertible {
    case today, yesterday, thisWeek, lastWeek, thisMonth, lastMonth, thisYear, lastYear
    case between(Date, Date)
    
    static var allCases: [TimeRange] {
      return [
        .today, .yesterday, .thisWeek, .lastWeek, .thisMonth, .lastMonth, .thisYear, .lastYear,
        .between(Date(timeIntervalSince1970: 0), Date(timeIntervalSince1970: 0))
      ]
    }
    
    func wantsStartAndEndDate() -> Bool {
      switch self {
      case .between: return true
      default: return false
      }
    }
    
    func dateRange() -> (Date, Date) {
      switch self {
      case .today: return Date().today()
      case .yesterday: return Date().yesterday()
      case .thisWeek: return Date().thisWeek()
      case .lastWeek: return Date().lastWeek()
      case .thisMonth: return Date().thisMonth()
      case .lastMonth: return Date().lastMonth()
      case .thisYear: return Date().thisYear()
      case .lastYear: return Date().lastYear()
      case let .between(s, e): return (s, e)
      }
    }
    
    enum CodingKeys: String, CodingKey {
      case today, yesterday, thisWeek, lastWeek, thisMonth, lastMonth, thisYear, lastYear
      case betweenStart, betweenEnd
    }
    
    func encode(to encoder: Encoder) throws {
      var container = encoder.container(keyedBy: CodingKeys.self)
      switch self {
      case .today: try container.encode(0, forKey: .today)
      case .yesterday: try container.encode(1, forKey: .yesterday)
      case .thisWeek: try container.encode(2, forKey: .thisWeek)
      case .lastWeek: try container.encode(3, forKey: .lastWeek)
      case .thisMonth: try container.encode(4, forKey: .thisMonth)
      case .lastMonth: try container.encode(5, forKey: .lastMonth)
      case .thisYear: try container.encode(6, forKey: .thisYear)
      case .lastYear: try container.encode(7, forKey: .lastYear)
      case let .between(s, e):
        try container.encode(s, forKey: .betweenStart)
        try container.encode(e, forKey: .betweenEnd)
      }
    }
    
    // swiftlint:disable unused_optional_binding
    init(from decoder: Decoder) throws {
      let container = try decoder.container(keyedBy: CodingKeys.self)
      if let _ = try container.decodeIfPresent(Int.self, forKey: .today) {
        self = .today
      } else if let _ = try container.decodeIfPresent(Int.self, forKey: .yesterday) {
        self = .yesterday
      } else if let _ = try container.decodeIfPresent(Int.self, forKey: .thisWeek) {
        self = .thisWeek
      } else if let _ = try container.decodeIfPresent(Int.self, forKey: .lastWeek) {
        self = .lastWeek
      } else if let _ = try container.decodeIfPresent(Int.self, forKey: .thisMonth) {
        self = .thisMonth
      } else if let _ = try container.decodeIfPresent(Int.self, forKey: .lastMonth) {
        self = .lastMonth
      } else if let _ = try container.decodeIfPresent(Int.self, forKey: .thisYear) {
        self = .thisYear
      } else if let _ = try container.decodeIfPresent(Int.self, forKey: .lastYear) {
        self = .lastYear
      } else {
        if let s = try container.decodeIfPresent(Date.self, forKey: .betweenStart),
          let e = try container.decodeIfPresent(Date.self, forKey: .betweenEnd) {
          self = .between(s, e)
        }
      }
      self = .today
    }
    
    static func == (lhs: TimeRange, rhs: TimeRange) -> Bool {
      if case .today = lhs, case .today = rhs {
        return true
      } else if case .yesterday = lhs, case .yesterday = rhs {
        return true
      } else if case .thisWeek = lhs, case .thisWeek = rhs {
        return true
      } else if case .lastWeek = lhs, case .lastWeek = rhs {
        return true
      } else if case .thisMonth = lhs, case .thisMonth = rhs {
        return true
      } else if case .lastMonth = lhs, case .lastMonth = rhs {
        return true
      } else if case .thisYear = lhs, case .thisYear = rhs {
        return true
      } else if case .lastYear = lhs, case .lastYear = rhs {
        return true
      } else if case let .between(s, e) = lhs, case let .between(t, b) = rhs {
        return s == t && e == b
      }
      return false
    }
    
    var description: String {
      switch self {
      case .today: return "Today"
      case .yesterday: return "Yesterday"
      case .thisWeek: return "This Week"
      case .lastWeek: return "Last Week"
      case .thisMonth: return "This Month"
      case .lastMonth: return "Last Month"
      case .thisYear: return "This Year"
      case .lastYear: return "Last Year"
      case .between: return "Between Dates"
      }
    }
  }
  
  struct Item: Codable {
    var ids: [String]
    var interval: TimeRange
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
