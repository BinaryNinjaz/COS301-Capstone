import Foundation

enum HarvestDB {
  enum Path {
    static let parent = "xFBNcNmiuON8ACbAHzH0diWcFQ43"
  }
}

extension TimeZone {
  func offset() -> String {
    let secs = abs(secondsFromGMT())
    let sign = secondsFromGMT() < 0 ? "-" : "+"
    let h = secs / 3600
    let m = secs % 3600
    
    let formatter = NumberFormatter()
    formatter.positiveFormat = "00"
    let sh = formatter.string(from: NSNumber(value: h)) ?? "00"
    let sm = formatter.string(from: NSNumber(value: m)) ?? "00"
    
    return sign + sh + ":" + sm
  }
}

extension Date {
  func thisMonth(using calendar: Calendar = .current) -> (Date, Date) {
    let components = calendar.dateComponents([.year, .month], from: self)
    let s = calendar.date(from: components)!
    
    var nextMonthComps = DateComponents()
    nextMonthComps.month = 1
    nextMonthComps.day = -1
    let e = calendar.date(byAdding: nextMonthComps, to: s)!
    
    return (s, e)
  }
}

enum HarvestCloud {
  static let baseURL = "http://localhost:5000/harvest-ios-1522082524457/us-central1/"
//  static let baseURL = "https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/"
  
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
  
  enum Identifiers {
    static let shallowSessions = "flattendSessions"
    static let sessionsWithDates = "sessionsWithinDates"
    static let expectedYield = "expectedYield"
    static let collections = "collectionsWithinDate"
    static let timeGraphSessions = "timedGraphSessions"
  }
  
  static func runTask(withQuery query: String, completion: @escaping (Any) -> Void) {
    let furl = URL(string: baseURL + query)!
    print(furl)
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
    
    print(furl)
    
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
  
  static func getExpectedYield(orchardId: String, date: Date, completion: @escaping (Double) -> Void) {
    let query = component(onBase: Identifiers.expectedYield, withArgs: [
      ("orchardId", orchardId),
      ("date", date.timeIntervalSince1970.description),
      ("uid", HarvestDB.Path.parent)
    ])
    
    print(date.timeIntervalSince1970.description)
    
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
  
  static func collections(
    ids: [String], 
    startDate: Date, 
    endDate: Date,
    groupBy: HarvestCloud.GroupBy,
    completion: @escaping ([Any]) -> Void
  ) {
    var args = [
      ("startDate", startDate.timeIntervalSince1970.description),
      ("endDate", endDate.timeIntervalSince1970.description),
      ("groupBy", groupBy.description),
      ("uid", HarvestDB.Path.parent)
    ]
    
    for (i, o) in ids.enumerated() {
      args.append(("id\(i)", o))
    }
    
    let body = makeBody(withArgs: args)
    
    runTask(Identifiers.collections, withBody: body) { (serial) in
      guard let json = serial as? [Any] else {
        return
      }
      
      completion(json)
    }
  }
  
  enum TimePeriod : CustomStringConvertible {
    case hourly
    case daily
    case weekly
    case monthly
    case yearly
    
    var description: String {
      switch self {
      case .hourly: return "hourly"
      case .daily: return "daily"
      case .weekly: return "weekly"
      case .monthly: return "monthly"
      case .yearly: return "yearly"
      }
    }
  }
  
  enum GroupBy : CustomStringConvertible {
    case worker
    case orchard
    case foreman
    case farm
    
    var description: String {
      switch self {
      case .worker: return "worker"
      case .orchard: return "orchard"
      case .foreman: return "foreman"
      case .farm: return "farm"
      }
    }
  }
  
  enum Mode : CustomStringConvertible {
    case accumTime
    case accumEntity
    case running
    
    var description: String {
      switch self {
      case .accumTime: return "accumTime"
      case .accumEntity: return "accumEntity"
      case .running: return "running"
      }
    }
  }
  
  static func timeGraphSessions(
    grouping: GroupBy,
    ids: [String],
    period: TimePeriod,
    startDate: Date,
    endDate: Date,
    mode: Mode,
    completion: @escaping (Any) -> Void
  ) {
    let timeZone = Calendar.current.timeZone.offset()
    
    var args = [
      ("groupBy", grouping.description),
      ("period", period.description),
      ("startDate", startDate.timeIntervalSince1970.description),
      ("endDate", endDate.timeIntervalSince1970.description),
      ("offset", timeZone),
      ("mode", mode.description),
      ("uid", HarvestDB.Path.parent)
    ]
    
    for (i, id) in ids.enumerated() {
      args.append(("id\(i)", id))
    }
    
    let body = makeBody(withArgs: args)
    
    runTask(Identifiers.timeGraphSessions, withBody: body) { (serial) in
      completion(serial)
    }
  }
}

func collection() {
  let s = Date(timeIntervalSince1970: 1529853470 * 0)
  let e = Date()

  HarvestCloud.collections(ids: ["-LBl_xZiXFlcTFzkTbGd"], startDate: s, endDate: e, groupBy: .farm) { f in
    print(f)
  }
}

func timeGraphSessionsWorker() {
  let cal = Calendar.current
  
  let wb = cal.date(byAdding: Calendar.Component.weekday, value: -14, to: Date())!.timeIntervalSince1970
  
  let s = Date().thisMonth().0
  let e = Date().thisMonth().1
  let g = HarvestCloud.GroupBy.worker
  let p = HarvestCloud.TimePeriod.daily
  
  let ids = [
    "-LBykXujU0Igjzvq5giB", // Peter Parker 3
    "-LBykjpjTy2RrDApKGLy", // Barry Allen 7
//    "-LBykZoPlQ2xkIMylBr2", // Tony Stark 4
//    "-LBykabv5OJNBsdv0yl7", // Clark Kent 5
//    "-LBykcR9o5_S_ndIYHj9", // Bruce Wayne 6
  ]

  HarvestCloud.timeGraphSessions(
    grouping: g, 
    ids: ids, 
    period: p, 
    startDate: s, 
    endDate: e,
    mode: .running) { o in
    print(o)
  }
}

func timeGraphSessionsOrchard() {
  let cal = Calendar.current
  
  let wb = cal.date(byAdding: Calendar.Component.weekday, value: -14, to: Date())!.timeIntervalSince1970
  
  let s = Date(timeIntervalSince1970: wb)
  let e = Date()
  let g = HarvestCloud.GroupBy.orchard
  let p = HarvestCloud.TimePeriod.daily
  
  let ids = [
//    "-LCEFgdMMO80LR98BzPC", // Block H
    "-LCEFoWPEw7ThnUaz07W", // Block U
//    "-LCnEEUlavG3eFLCC3MI", // Maths Building
    "-LHWqdM5IgQd00XbdZYT", // Block T
  ]

  HarvestCloud.timeGraphSessions(
    grouping: g, 
    ids: ids, 
    period: p, 
    startDate: s, 
    endDate: e,
    mode: .accumEntity) { o in
    print(o)
  }
}

func timeGraphSessionsFarm() {
  let cal = Calendar.current
  
  let wb = cal.date(byAdding: Calendar.Component.weekday, value: -7, to: Date())!.timeIntervalSince1970
  
  let s = Date(timeIntervalSince1970: wb)
  let e = Date()
  let g = HarvestCloud.GroupBy.farm
  let p = HarvestCloud.TimePeriod.daily
  
  let ids = [
    "-LBl_xZiXFlcTFzkTbGd", // Unsworthy
    "-LC3xagNRI3KCPe7RZVL" // Mangogo
  ]

  HarvestCloud.timeGraphSessions(
    grouping: g, 
    ids: ids, 
    period: p, 
    startDate: s, 
    endDate: e,
    mode: .accumTime) { o in
    print(o)
  }
}

func expectedYield() {
  HarvestCloud.getExpectedYield(orchardId: "-LCEFgdMMO80LR98BzPC", date: Date()) {
    print($0)
  }
}

//timeGraphSessionsFarm()

//collection()

timeGraphSessionsWorker()

//timeGraphSessionsOrchard()

RunLoop.main.run()

/*

{
  "-LCEFgdMMO80LR98BzPC" =     {
    "Jul 31" = 19;
  };
  "-LCEFoWPEw7ThnUaz07W" =     {
    "Aug 02" = 418;
    "Jul 27" = 113;
    "Jul 30" = 628;
  };
  "-LCnEEUlavG3eFLCC3MI" =     {
  };
}

*/