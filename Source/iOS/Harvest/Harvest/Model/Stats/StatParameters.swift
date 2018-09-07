//
//  StatTimePeriod.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/08/04.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Foundation

enum TimePeriod: Codable, Equatable, CustomStringConvertible {
  case today, yesterday, thisWeek, lastWeek, thisMonth, lastMonth, thisYear, lastYear
  case between(Date, Date)
  
  static var allCases: [TimePeriod] {
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
      } else {
        self = .today
      }
    }
  }
  
  static func == (lhs: TimePeriod, rhs: TimePeriod) -> Bool {
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
    case .between: return "Between Exact Dates"
    }
  }
}

enum TimedGraphMode: String, CustomStringConvertible, Codable {
  case accumTime, accumEntity, running
  
  var identifier: String {
    switch self {
    case .accumTime: return "accumTime"
    case .accumEntity: return "accumEntity"
    case .running: return "running"
    }
  }
  
  var description: String {
    switch self {
    case .accumTime: return "Interval"
    case .accumEntity: return "Entity"
    case .running: return "Running"
    }
  }
  
  func explanation(for kind: StatKind, by step: TimeStep) -> String {
    switch self {
    case .running: return """
      No data accumulation will take place, a regular graph will be displayed whereby \
      each \(kind.identifier) will have its own line.
      """
      
    case .accumEntity: return """
      The selected \(kind.identifier)s will have their values for each \(step.itemizedDescription) \
      summed up, so that the sum of each individual \(kind.identifier)'s collections accross the selected \
      time period. for that \(step.itemizedDescription) will be shown, for each \(step.itemizedDescription).
      """
      
    case .accumTime: return """
      The sum accross each \(step.itemizedDescription) will be shown, so that the total bags collected \
      each week by each of the selected \(kind.identifier)s will be shown.
      """
    }
  }
}

enum StatKind: String, CustomStringConvertible, Codable {
  case worker
  case orchard
  case foreman
  case farm
  
  static var allCases: [StatKind] {
    return [.farm, .orchard, .worker, .foreman]
  }
  
  var identifier: String {
    switch self {
    case .worker: return "worker"
    case .orchard: return "orchard"
    case .foreman: return "foreman"
    case .farm: return "farm"
    }
  }
  
  var description: String {
    switch self {
    case .worker: return "Worker"
    case .orchard: return "Orchard"
    case .foreman: return "Foreman"
    case .farm: return "Farm"
    }
  }
}

enum TimeStep: String, CustomStringConvertible, Codable {
  case hourly
  case daily
  case weekly
  case monthly
  case yearly
  
  static var allCases: [TimeStep] {
    return [.hourly, .daily, .weekly, .monthly, .yearly]
  }
  
  static func cases(forRange r: (Date, Date)) -> [TimeStep] {
    let days = Calendar.current.dateComponents([.day], from: r.0, to: r.1).day!
    let weeks = Calendar.current.dateComponents([.weekOfYear], from: r.0, to: r.1).weekOfYear!
    
    if days <= 1 {
      return [.hourly]
    } else if weeks <= 1 {
      return [.daily]
    } else if weeks <= 4 {
      return [.weekly]
    } else {
      return [.weekly, .monthly]
    }
  }
  
  var identifier: String {
    switch self {
    case .hourly: return "hourly"
    case .daily: return "daily"
    case .weekly: return "weekly"
    case .monthly: return "monthly"
    case .yearly: return "yearly"
    }
  }
  
  var description: String {
    switch self {
    case .hourly: return "Hourly"
    case .daily: return "Daily"
    case .weekly: return "Weekly"
    case .monthly: return "Monthly"
    case .yearly: return "Yearly"
    }
  }
  
  var itemizedDescription: String {
    switch self {
    case .hourly: return "hour"
    case .daily: return "day"
    case .weekly: return "week"
    case .monthly: return "month"
    case .yearly: return "year"
    }
  }
  
  func fullDataSet(between start: Date? = nil, and end: Date? = nil, limitToDate: Bool = false) -> [String] {
    switch self {
    case .hourly:
      return (0...23).map(String.init)
    case .daily:
      return ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]
    case .weekly:
      let base = (1...54).map(String.init)
      if limitToDate {
        guard let s = start?.weekNumber(), let e = end?.weekNumber() else {
          return base
        }
        return (s...e).map(String.init)
      }
      return base
    case .monthly:
      return [
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
      ]
    case .yearly:
      return ["2018", "2019", "2020", "2021", "2022"]
    }
  }
  
  func fullPrintableDataSet(
    between start: Date? = nil,
    and end: Date? = nil,
    limitToDate: Bool = false
    ) -> [String] {
    switch self {
    case .hourly:
      return (0...23).map { ($0 < 10 ? "0\($0):00" : "\($0):00") }
    case .daily:
      return ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]
    case .weekly:
      let base = (1...54).map { Date.startOfWeek(from: $0) }
      if limitToDate {
        guard let s = start?.weekNumber(), let e = end?.weekNumber() else {
          return base
        }
        return (s...e).map { Date.startOfWeek(from: $0) }
      }
      return base
    case .monthly:
      return [
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec"
      ]
    case .yearly:
      return ["2018", "2019", "2020", "2021", "2022"]
    }
  }
  
  func formatStrings(between startDate: Date, and endDate: Date) -> (String, String, String) {
    let isSameYear = startDate.startOfYear() == endDate.startOfYear()
    let isSameMonth = startDate.startOfMonth() == endDate.startOfMonth()
    let isSameDay = startDate.startOfDay() == endDate.startOfDay()
    
    let fmtYear = isSameYear ? "" : "yyyy "
    let fmtMonth = isSameMonth ? "" : "MMM "
    let fmtDay = isSameDay ? "" : "dd"
    
    return (fmtYear, fmtMonth, fmtDay)
  }
  
  func fullRunningDataSet(between startDate: Date, and endDate: Date) -> [String] {
    let (fmtYear, fmtMonth, fmtDay) = formatStrings(between: startDate, and: endDate)
    let fmt = fmtYear + fmtMonth + fmtDay
    
    var result = [String]()
    let formatter = DateFormatter()
    let comp: Calendar.Component
    let format: String
    var start: Date
    let end: Date
    
    switch self {
    case .hourly:
      comp = .hour
      format = fmt + (fmt == "" ? "" : " ") + "HH:mm"
      start = startDate.startOfDay()
      end = endDate.startOfDay().date(byAdding: .day, value: 1)
      
    case .daily:
      comp = .day
      format = fmt == "" ? "EEE" : fmt
      start = startDate.startOfDay()
      end = endDate.startOfDay().date(byAdding: .day, value: 1)
      
    case .weekly:
      comp = .weekOfYear
      format = fmt == "" ? "EEE" : fmt
      start = startDate.startOfWeek()
      end = endDate.startOfWeek().date(byAdding: .weekOfYear, value: 1)
      
    case .monthly:
      comp = .month
      format = fmtYear + "MMM"
      start = startDate.startOfMonth()
      end = endDate.startOfMonth().date(byAdding: .month, value: 1)
      
    case .yearly:
      comp = .year
      format = "yyyy"
      start = startDate.startOfYear()
      end = endDate.startOfYear().date(byAdding: .year, value: 1)
      
    }
    
    formatter.dateFormat = format
    
    while start < end {
      result.append(formatter.string(from: start))
      start = start.date(byAdding: comp, value: 1)
    }
    
    return result
  }
}
