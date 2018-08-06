//
//  Collections.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/25.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Darwin

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
