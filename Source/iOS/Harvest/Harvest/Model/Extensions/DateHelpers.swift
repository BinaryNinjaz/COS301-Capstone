//
//  DateHelpers.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Foundation

extension Date {
  func today(using calendar: Calendar = .current) -> (Date, Date) {
    let now = Date()
    let dayAfter = calendar.date(byAdding: .day, value: 1, to: now)!
    let s = calendar.startOfDay(for: now)
    let e = calendar.startOfDay(for: dayAfter)
    
    return (s, e)
  }
  
  func yesterday(using calendar: Calendar = .current) -> (Date, Date) {
    let now = Date()
    let dayAgo = calendar.date(byAdding: .day, value: -1, to: now)!
    let s = calendar.startOfDay(for: dayAgo)
    let e = calendar.startOfDay(for: now)
    
    return (s, e)
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
}

extension Date {
  func getWeekDaysInEnglish() -> [String] {
    var calendar = Calendar(identifier: .gregorian)
    calendar.locale = Locale(identifier: "en_US_POSIX")
    return calendar.weekdaySymbols
  }
  
  enum Weekday: String {
    case monday, tuesday, wednesday, thursday, friday, saturday, sunday
  }
  
  enum SearchDirection {
    case next
    case previous
    
    var calendarSearchDirection: Calendar.SearchDirection {
      switch self {
      case .next:
        return .forward
      case .previous:
        return .backward
      }
    }
  }
  
  func next(_ weekday: Weekday, considerToday: Bool = false) -> Date {
    return get(.next,
               weekday,
               considerToday: considerToday)
  }
  
  func previous(_ weekday: Weekday, considerToday: Bool = false) -> Date {
    return get(.previous,
               weekday,
               considerToday: considerToday)
  }
  
  func get(_ direction: SearchDirection,
           _ weekDay: Weekday,
           considerToday consider: Bool = false) -> Date {
    
    let dayName = weekDay.rawValue
    
    let weekdaysName = getWeekDaysInEnglish().map { $0.lowercased() }
    
    assert(weekdaysName.contains(dayName), "weekday symbol should be in form \(weekdaysName)")
    
    let searchWeekdayIndex = weekdaysName.index(of: dayName)! + 1
    
    let calendar = Calendar(identifier: .gregorian)
    
    if consider && calendar.component(.weekday, from: self) == searchWeekdayIndex {
      return self
    }
    
    var nextDateComponent = DateComponents()
    nextDateComponent.weekday = searchWeekdayIndex
    
    let date = calendar.nextDate(after: self,
                                 matching: nextDateComponent,
                                 matchingPolicy: .nextTime,
                                 direction: direction.calendarSearchDirection)
    
    return date!
  }
}
