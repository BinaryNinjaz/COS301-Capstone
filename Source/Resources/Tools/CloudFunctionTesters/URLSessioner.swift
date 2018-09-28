 import Foundation

enum HarvestDB {
  enum Path {
    // static let parent = "xFBNcNmiuON8ACbAHzH0diWcFQ43"
    static let parent = "e0s6n4NZQaVuiCk3U9SC3eFttgr1"
  }
}

let tz = TimeZone.init(secondsFromGMT: 60 * 60 * -0)!

extension DateFormatter {
  static func rfc2822() -> DateFormatter {
    let result = DateFormatter()
    result.locale = Locale.current
    result.timeZone = tz
    // result.dateFormat = "YYYY-MM-dd'T'HH:mm:ssZZZZZ"
    result.dateFormat = "d MMM YYYY HH:mm"
    return result
  }

  static func rfc2822String(from date: Date) -> String {
    return DateFormatter.rfc2822().string(from: date)
  }

  static func rfc2822Date(from string: String) -> Date {
    return DateFormatter.rfc2822().date(from: string)!
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

  func thisYear(using calendar: Calendar = .current) -> (Date, Date) {
    let components = calendar.dateComponents([.year], from: self)
    let s = calendar.date(from: components)!

    var nextYearComps = DateComponents()
    nextYearComps.year = 1
    nextYearComps.month = -1
    let e = calendar.date(byAdding: nextYearComps, to: s)!

    return (s, e)
  }

  func today(using calendar: Calendar = .current) -> (Date, Date) {
    var cal = Calendar.init(identifier: Calendar.Identifier.gregorian)
    cal.timeZone = tz
    let components = cal.dateComponents([.year, .month, .day], from: self)
    let s = cal.date(from: components)!

    var nextMonthComps = DateComponents()
    nextMonthComps.day = 1
    nextMonthComps.minute = -1
    let e = cal.date(byAdding: nextMonthComps, to: s)!

    return (s, e)
  }

  func yesterday(using calendar: Calendar = .current) -> (Date, Date) {
    var cal = Calendar.init(identifier: Calendar.Identifier.gregorian)
    cal.timeZone = tz
    let dayAgo = cal.date(byAdding: .day, value: -1, to: self)!
    return dayAgo.today()
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

  static func getExpectedYield(orchardId: String, date: Date, completion: @escaping ([String: Any]) -> Void) {
    let query = component(onBase: Identifiers.expectedYield, withArgs: [
      ("orchardId", orchardId),
      ("date", date.timeIntervalSince1970.description),
      ("uid", HarvestDB.Path.parent)
    ])

    runTask(withQuery: query) { (serial) in
      guard let json = serial as? [String: Any] else {
        completion([:])
        return
      }

      completion(json)
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
    var args = [
      ("groupBy", grouping.description),
      ("period", period.description),
      ("startDate", DateFormatter.rfc2822String(from: startDate)),
      ("endDate",  DateFormatter.rfc2822String(from: endDate)),
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
  let s = Date(timeIntervalSinceNow: -60 * 60 * 24 * 21)
  let e = Date(timeIntervalSinceNow: -60 * 60 * 24 * 14)
  let g = HarvestCloud.GroupBy.worker
  let p = HarvestCloud.TimePeriod.daily

  print(DateFormatter.rfc2822String(from: s))
  print(DateFormatter.rfc2822String(from: e))

  let ids = [
    // "-LC4tqYXblh6RD6F_LIS", // Tandy Joe
    // "-LHJzXzzw_p4LaK93UYu", // Arthur Melo
    // "-LBykXujU0Igjzvq5giB", // Peter Parker 3
    // "-LBykjpjTy2RrDApKGLy", // Barry Allen 7
//    "-LBykZoPlQ2xkIMylBr2", // Tony Stark 4
//    "-LBykabv5OJNBsdv0yl7", // Clark Kent 5
//    "-LBykcR9o5_S_ndIYHj9", // Bruce Wayne 6
    "-LMOu6sZTSjvZWJBeJxU", // Carl Ciao
    "-LMOu94PBHcy35qEZ_UC", // Doug Dourn
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

  let wb = cal.date(byAdding: Calendar.Component.weekday, value: -2, to: Date())!.timeIntervalSince1970

  let s = Date(timeIntervalSince1970: wb)
  let e = Date(timeIntervalSinceNow: 60 * 60 * 5)
  let g = HarvestCloud.GroupBy.orchard
  let p = HarvestCloud.TimePeriod.hourly

  let ids = [
//    "-LCEFgdMMO80LR98BzPC", // Block H
    // "-LCEFoWPEw7ThnUaz07W", // Block U
//    "-LCnEEUlavG3eFLCC3MI", // Maths Building
    // "-LHWqdM5IgQd00XbdZYT", // Block T
    "-LKi9OIZ28wDqQlE80qX",
    "-LKi9pI9Xrlue8mFXyX_",
    "-LK2z9yQhfZT42ddrKJn",
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
while let x = readLine(), x != "" {
  timeGraphSessionsWorker()
}

// timeGraphSessionsOrchard()

// expectedYield()

RunLoop.main.run()

/*


*/
