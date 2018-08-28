//
//  DBCloud.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/06/24.
//  Copyright © 2018 University of Pretoria. All rights reserved.
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
  
  // swiftlint:disable function_parameter_count
  static func timeGraphSessions(
    grouping: StatKind,
    ids: [String],
    period: TimeStep,
    startDate: Date,
    endDate: Date,
    mode: TimedGraphMode,
    completion: @escaping (Any) -> Void
  ) {
    var args = [
      ("groupBy", grouping.identifier),
      ("period", period.identifier),
      ("startDate", DateFormatter.rfc2822String(from: startDate)),
      ("endDate", DateFormatter.rfc2822String(from: endDate)),
      ("mode", mode.identifier),
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
    static let timeGraphSessions = "timedGraphSessions"
  }
}
