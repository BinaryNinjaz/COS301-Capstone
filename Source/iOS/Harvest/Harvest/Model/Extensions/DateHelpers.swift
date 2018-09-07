//
//  DateHelpers.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/07.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Foundation

extension Date {
  static var key: String {
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyyMMddHHmmss"
    return formatter.string(from: Date())
  }
  
  func startOfHour(using calendar: Calendar = .current) -> Date {
    let components = calendar.dateComponents([.year, .month, .day, .hour], from: self)
    let s = calendar.date(from: components)!
    
    return s
  }
  
  func startOfDay(using calendar: Calendar = .current) -> Date {
    return calendar.startOfDay(for: self)
  }
  
  func endOfDay(using calendar: Calendar = .current) -> Date {
    return today().1
  }
  
  func startOfWeek(using calendar: Calendar = .current) -> Date {
    let components = calendar.dateComponents([.weekOfYear, .yearForWeekOfYear], from: self)
    let s = calendar.date(from: components)!
    
    return s
  }
  
  func startOfMonth(using calendar: Calendar = .current) -> Date {
    let components = calendar.dateComponents([.year, .month], from: self)
    let s = calendar.date(from: components)!
    
    return s
  }
  
  func startOfYear(using calendar: Calendar = .current) -> Date {
    let components = calendar.dateComponents([.year], from: self)
    let s = calendar.date(from: components)!
    
    return s
  }
  
  func date(byAdding comp: Calendar.Component, value: Int, using calendar: Calendar = .current) -> Date {
    return calendar.date(byAdding: comp, value: value, to: self)!
  }
  
  func today(using calendar: Calendar = .current) -> (Date, Date) {
    let components = calendar.dateComponents([.year, .month, .day], from: self)
    let s = calendar.date(from: components)!
    
    var nextDayComps = DateComponents()
    nextDayComps.day = 1
    nextDayComps.minute = -1
    let e = calendar.date(byAdding: nextDayComps, to: s)!
    
    return (s, e)
  }
  
  func yesterday(using calendar: Calendar = .current) -> (Date, Date) {
    let now = Date()
    let dayAgo = calendar.date(byAdding: .day, value: -1, to: now)!
    
    return dayAgo.today(using: calendar)
  }
  
  func thisWeek(using calendar: Calendar = .current) -> (Date, Date) {
    let components = calendar.dateComponents([.weekOfYear, .yearForWeekOfYear], from: self)
    let s = calendar.date(from: components)!
    let e = calendar.date(byAdding: .weekOfYear, value: 1, to: s)!
    
    return (s, e)
  }
  
  func lastWeek(using calendar: Calendar = .current) -> (Date, Date) {
    let aWeekAgo = calendar.date(byAdding: .weekOfYear, value: -1, to: Date())!
    
    return aWeekAgo.thisWeek(using: calendar)
  }
  
  func thisMonth(using calendar: Calendar = .current) -> (Date, Date) {
    let components = calendar.dateComponents([.year, .month], from: self)
    let s = calendar.date(from: components)!
    
    var nextMonthComps = DateComponents()
    nextMonthComps.month = 1
    nextMonthComps.day = -1
    let e = calendar.date(byAdding: nextMonthComps, to: s)!
    
    return (s, e)
  }
  
  func lastMonth(using calendar: Calendar = .current) -> (Date, Date) {
    let aMonthAgo = calendar.date(byAdding: .month, value: -1, to: Date())!
    
    return aMonthAgo.thisMonth(using: calendar)
  }
  
  func thisYear(using calendar: Calendar = .current) -> (Date, Date) {
    let components = calendar.dateComponents([.year], from: self)
    let s = calendar.date(from: components)!
    
    var nextYearComps = DateComponents()
    nextYearComps.year = 1
    nextYearComps.month = -1
    let e = calendar.date(byAdding: nextYearComps, to: s)!
    
    return (s, e)
  }
  
  func lastYear(using calendar: Calendar = .current) -> (Date, Date) {
    let aYearAgo = calendar.date(byAdding: .year, value: -1, to: Date())!
    
    return aYearAgo.thisYear(using: calendar)
  }
  
  func weekNumber(using calendar: Calendar = .current) -> Int {
    let calendar = Calendar.current
    return calendar.component(.weekOfYear, from: self)
  }
  
  static func startOfWeek(from weekNumber: Int, using calendar: Calendar = .current) -> String {
    let currentYear = calendar.component(.yearForWeekOfYear, from: Date())
    var comps = DateComponents()
    comps.calendar = calendar
    comps.weekOfYear = weekNumber
    comps.yearForWeekOfYear = currentYear
    let date = calendar.date(from: comps)!
    
    let formatter = DateFormatter()
    formatter.dateFormat = "dd MMM"
    
    return formatter.string(from: date)
  }
  
  func stepsSince1970(step: TimeStep?) -> Int {
    let startOfTime = Date(timeIntervalSince1970: 0)
    
    guard let step = step else {
      return Calendar.current.dateComponents([.minute], from: startOfTime, to: self).minute ?? 0
    }
    
    switch step {
    case .hourly:
      return Calendar.current.dateComponents([.hour], from: startOfTime, to: self).hour ?? 0
    case .daily:
      return Calendar.current.dateComponents([.day], from: startOfTime, to: self).day ?? 0
    case .weekly:
      return (Calendar.current.dateComponents([.day], from: startOfTime, to: self).day ?? 0) / 7
    case .monthly:
      return Calendar.current.dateComponents([.month], from: startOfTime, to: self).month ?? 0
    case .yearly:
      return Calendar.current.dateComponents([.year], from: startOfTime, to: self).year ?? 0
    }
  }
  
  func daysSince1970() -> Double {
    var interval = Int(timeIntervalSince1970)
    interval -= interval % 86400
    return Double(interval) / 86400.0
  }
  
  func sameDay(as date: Date) -> Bool {
    var aday = Calendar.current.dateComponents([.year, .month, .day], from: self)
    var bday = Calendar.current.dateComponents([.year, .month, .day], from: date)
    
    return aday.year == bday.year &&  aday.month == bday.month && aday.day == bday.day
  }
}

extension DateFormatter {
  static func rfc2822() -> DateFormatter {
    let result = DateFormatter()
    result.locale = Locale.current
    result.dateFormat = "d MMM yyyy HH:mm ZZZ"
    return result
  }
  
  static func rfc2822String(from date: Date) -> String {
    return DateFormatter.rfc2822().string(from: date)
  }
  
  static func rfc2822Date(from string: String) -> Date {
    return DateFormatter.rfc2822().date(from: string) ?? Date(timeIntervalSince1970: 0)
  }
}

enum DateOption: ExpressibleByIntegerLiteral {
  enum Unit {
    case year, month, day, hour, minute, second
    
    func integerRepresentation(withRespectTo date: Date = Date()) -> Int {
      let formatter = DateFormatter()
      switch self {
      case .year: formatter.dateFormat = "y"
      case .month: formatter.dateFormat = "M"
      case .day: formatter.dateFormat = "d"
      case .hour: formatter.dateFormat = "H"
      case .minute: formatter.dateFormat = "m"
      case .second: formatter.dateFormat = "s"
      }
      let str = formatter.string(from: date)
      return Int(string: str) ?? 0
    }
  }
  
  case exact(Int)
  case range(Int, Int)
  case this(Unit)
  
  init(integerLiteral value: Int) {
    self = .exact(value)
  }
  
  func possibleValues(withRespectTo date: Date = Date()) -> Range<Int> {
    switch self {
    case let .exact(d): return d..<(d + 1)
    case let .range(a, b): return a..<b
    case let .this(d):
      let x = d.integerRepresentation(withRespectTo: date)
      return x..<(x + 1)
    }
  }
  
  func random(withRespectTo date: Date = Date()) -> Int {
    let r = possibleValues(withRespectTo: date)
    let d = r.upperBound - r.lowerBound
    return Int(arc4random()) % d + r.lowerBound
  }
}

func ..< (lhs: Int, rhs: Int) -> DateOption {
  return .range(lhs, rhs)
}

extension Date {
  static func random(
    year: DateOption = .this(.year),
    month: DateOption = .this(.month),
    day: DateOption = .this(.day),
    hour: DateOption = .this(.hour),
    minute: DateOption = .this(.minute),
    second: DateOption = .this(.second)
  ) -> Date {
    let str = """
    \(day.random()) \(month.random()) \(year.random()) \(hour.random()):\(minute.random()):\(second.random())
    """
    
    let formatter = DateFormatter()
    formatter.dateFormat = "d M y H:m:s"
    
    return formatter.date(from: str)!
  }
  
  func random(
    year: DateOption = .this(.year),
    month: DateOption = .this(.month),
    day: DateOption = .this(.day),
    hour: DateOption = .this(.hour),
    minute: DateOption = .this(.minute),
    second: DateOption = .this(.second)
  ) -> Date {
    let str = """
    \(day.random(withRespectTo: self)) \
    \(month.random(withRespectTo: self)) \
    \(year.random(withRespectTo: self)) \
    \(hour.random(withRespectTo: self)):\
    \(minute.random(withRespectTo: self)):\
    \(second.random(withRespectTo: self))
    """
    
    let formatter = DateFormatter()
    formatter.dateFormat = "d M y H:m:s"
    
    return formatter.date(from: str)!
  }
  
  static func random(between start: Date, and end: Date) -> Date {
    let s = start.timeIntervalSince1970
    let e = end.timeIntervalSince1970
    
    let dif = e - s
    let rnd = TimeInterval.random() * dif + s
    
    return Date(timeIntervalSince1970: rnd)
  }
}
