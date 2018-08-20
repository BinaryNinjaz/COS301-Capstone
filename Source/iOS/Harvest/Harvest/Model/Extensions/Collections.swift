//
//  Collections.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/25.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Foundation

extension Array {
  func randomElements(withProbability p: Double) -> [Element] {
    var result = [Element]()
    let amount = Int(1.0 / p)
    
    for e in self {
      if Int(arc4random()) % amount == 0 {
        result.append(e)
      }
    }
    
    return result
  }
}

extension String {
  func captilized() -> String {
    return String((first ?? " ")).uppercased() + String(dropFirst())
  }
}

extension SortedDictionary where Key == Date, Value == SortedSet<Session> {
  mutating func removeSession(withId id: String) {
    for (key, sessions) in self {
      if sessions.index(where: { $0.id == id }) != nil, let temp = self[key] {
        self[key] = SortedSet<Session>(areInIncreasingOrder: self[key]!.areInIncreasingOrder)
        for s in temp where s.id != id {
          self[key]?.insert(unique: s)
        }
      }
    }
  }
}
