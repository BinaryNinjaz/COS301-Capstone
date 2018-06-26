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
  
  enum Identifiers {
    static let shallowSessions = "flattendSessions"
    static let sessionsWithDates = "sessionsWithinDates"
    static let expectedYield = "expectedYield"
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
      
      print(json["definition"])
      
      guard let expected = json["expected"] as? Double else {
        completion(.nan)
        return
      }
      
      completion(expected)
    }
  }
}
