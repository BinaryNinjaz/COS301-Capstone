//
//  DBCloud.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/06/24.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase

enum HarvestCloud {
  static let baseURL = "https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/"
  
  static func component(onBase base: String, withArgs args: [(String, String)]) -> String {
    guard let first = args.first else {
      return base
    }
    
    let format: ((String, String)) -> String = { kv in
      return kv.0 + "=" + kv.1
    }
    
    var result = base + "?" + format(first)
    
    for kv in args.dropFirst() {
      result += "&" + format(kv)
    }
    
    return result
  }
  
  static func makeBody(withArgs args: [(String, String)]) -> String {
    guard let first = args.first else {
      return ""
    }
    
    let format: ((String, String)) -> String = { kv in
      return kv.0 + "=" + kv.1
    }
    
    var result = format(first)
    
    for kv in args.dropFirst() {
      result += "&" + format(kv)
    }
    
    return result
  }
  
  static func runTask(withQuery query: String, completion: @escaping (Any) -> Void) {
    let furl = URL(string: baseURL + query)!
    
    let task = URLSession.shared.dataTask(with: furl) { data, _, error in
      if let error = error {
        print(error)
        return
      }
      
      guard let data = data else {
        completion(Void())
        return
      }
      
      guard let jsonSerilization = try? JSONSerialization.jsonObject(with: data, options: []) else {
        completion(Void())
        return
      }
      
      completion(jsonSerilization)
    }
    
    task.resume()
  }
  
  static func runTask(_ task: String, withBody body: String, completion: @escaping (Any) -> Void) {
    let furl = URL(string: baseURL + task)!
    var request = URLRequest(url: furl)
    
    //    request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
    request.httpMethod = "POST"
    request.httpBody = body.data(using: .utf8)
    
    let task = URLSession.shared.dataTask(with: request) { data, _, error in
      if let error = error {
        print(error)
        return
      }
      
      guard let data = data else {
        completion(Void())
        return
      }
      
      guard let jsonSerilization = try? JSONSerialization.jsonObject(with: data, options: []) else {
        completion(Void())
        return
      }
      
      completion(jsonSerilization)
    }
    
    task.resume()
  }
  
  static func getShallowSessions(
    onPage page: Int,
    ofSize size: Int,
    _ completion: @escaping ([ShallowSession]) -> Void
  ) {
    let query = component(onBase: Identifiers.shallowSessions, withArgs: [
      ("pageNo", page.description),
      ("pageSize", size.description),
      ("uid", HarvestDB.Path.parent)
    ])
    
    runTask(withQuery: query) { (serial) in
      guard let json = serial as? [Any] else {
        completion([])
        return
      }
      
      var result = [ShallowSession]()
      
      for object in json {
        result.append(ShallowSession(json: object))
      }
      
      completion(result)
    }
  }
  
  static func getExpectedYield(orchardId: String, date: Date, completion: @escaping (Double) -> Void) {
    let query = component(onBase: Identifiers.expectedYield, withArgs: [
      ("orchardId", orchardId),
      ("date", date.timeIntervalSince1970.description),
      ("uid", HarvestDB.Path.parent)
    ])
    
    runTask(withQuery: query) { (serial) in
      guard let json = serial as? [String: Any] else {
        completion(.nan)
        return
      }
      
      guard let expected = json["expected"] as? Double else {
        completion(.nan)
        return
      }
      
      completion(expected)
    }
  }
  
  // swiftlint:disable function_parameter_count
  static func timeGraphSessions(
    grouping: GroupBy,
    ids: [String],
    period: TimePeriod,
    startDate: Date,
    endDate: Date,
    completion: @escaping (Any) -> Void
    ) {
    var args = [
      ("groupBy", grouping.description),
      ("period", period.description),
      ("startDate", startDate.timeIntervalSince1970.description),
      ("endDate", endDate.timeIntervalSince1970.description),
      ("uid", HarvestDB.Path.parent)
    ]
    
    for (i, id) in ids.enumerated() {
      args.append(("id\(i)", id))
    }
    
    let body = makeBody(withArgs: args)
    
    runTask(Identifiers.timeGraphSessions, withBody: body) { (data) in
      completion(data)
    }
  }
}

extension HarvestCloud {
  enum Identifiers {
    static let shallowSessions = "flattendSessions"
    static let sessionsWithDates = "sessionsWithinDates"
    static let expectedYield = "expectedYield"
    static let orchardCollections = "orchardCollectionsWithinDate"
    static let timeGraphSessions = "timedGraphSessions"
  }
  
  enum TimePeriod: String, CustomStringConvertible, Codable {
    case hourly
    case daily
    case weekly
    case monthly
    case yearly
    
    static var allCases: [TimePeriod] {
      return [.hourly, .daily, .weekly, .monthly, .yearly]
    }
    
    static func cases(forRange r: (Date, Date)) -> [TimePeriod] {
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
    
    var description: String {
      switch self {
      case .hourly: return "hourly"
      case .daily: return "daily"
      case .weekly: return "weekly"
      case .monthly: return "monthly"
      case .yearly: return "yearly"
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
  }
  
  enum GroupBy: String, CustomStringConvertible, Codable {
    case worker
    case orchard
    case foreman
    
    init(_ statKind: StatKind) {
      switch statKind {
      case .workers: self = .worker
      case .orchards: self = .orchard
      case .foremen: self = .foreman
      }
    }
    
    var description: String {
      switch self {
      case .worker: return "worker"
      case .orchard: return "orchard"
      case .foreman: return "foreman"
      }
    }
    
    var title: String {
      switch self {
      case .worker: return "Worker"
      case .orchard: return "Orchard"
      case .foreman: return "Foreman"
      }
    }
  }
  
  enum Mode: String, CustomStringConvertible {
    case accum, running
    
    var description: String {
      switch self {
      case .accum: return "accum"
      case .running: return "running"
      }
    }
    
    var title: String {
      switch self {
      case .accum: return "Accumulating"
      case .running: return "Running"
      }
    }
  }
}
