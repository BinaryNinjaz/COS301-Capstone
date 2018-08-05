//
//  DateHelpers.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Foundation

extension Date {
  func startOfHour(using calendar: Calendar = .current) -> Date {
    let components = calendar.dateComponents([.year, .month, .day, .hour], from: self)
    let s = calendar.date(from: components)!
    
    return s
  }
  
  func startOfDay(using calendar: Calendar = .current) -> Date {
    return calendar.startOfDay(for: self)
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
}

extension Date {
  func asFirebaseSessionKey() -> String {
    let formatter = DateFormatter()
    formatter.dateFormat = "-YYYYMMdd"
    return formatter.string(from: self)
  }
}

extension DateFormatter {
  static func iso8601() -> DateFormatter {
    let result = DateFormatter()
    result.locale = Locale.current
    result.dateFormat = "YYYY-MM-dd'T'HH:mm:ssZZZZZ"
    return result
  }
  
  static func iso8601String(from date: Date) -> String {
    return DateFormatter.iso8601().string(from: date)
  }
  
  static func iso8601Date(from string: String) -> Date {
    return DateFormatter.iso8601().date(from: string)!
  }
}
