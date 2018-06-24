//
//  DBCloud.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/06/24.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Firebase

extension HarvestDB {
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
  
  enum CloudFunctions {
    static let shallowSessions = "flattendSessions"
    static let sessionsWithDates = "sessionsWithinDates"
  }
  
  static func getShallowSessions(
    onPage page: Int,
    ofSize size: Int,
    _ completion: @escaping ([ShallowSession]) -> Void
  ) {
    let query = component(onBase: CloudFunctions.shallowSessions, withArgs: [
      ("pageNo", page.description),
      ("pageSize", size.description),
      ("uid", Path.parent)
    ])
    
    let furl = URL(string: baseURL + query)!
    
    let task = URLSession.shared.dataTask(with: furl) { data, _, error in
      if let error = error {
        print(error)
        return
      }
      
      guard let data = data else {
        completion([])
        return
      }
      
      guard let jsonSerilization = try? JSONSerialization.jsonObject(with: data, options: []) else {
        completion([])
        return
      }
      
      guard let json = jsonSerilization as? [Any] else {
        completion([])
        return
      }
      
      var result = [ShallowSession]()
      
      for object in json {
        result.append(ShallowSession(json: object))
      }
      
      completion(result)
    }
    
    task.resume()
  }
}
