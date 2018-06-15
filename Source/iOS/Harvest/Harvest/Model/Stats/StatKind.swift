//
//  StatKind.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Swift

enum StatKind: CustomStringConvertible {
  case perSessionWorkers
  case workerHistory
  case orchardHistory
  
  var description: String {
    switch self {
    case .perSessionWorkers: return "Per Session Worker Comparison"
    case .workerHistory: return "Worker Historical Performance"
    case .orchardHistory: return "Orchard Historical Performance"
    }
  }
  
  static var allCases: [StatKind] {
    return [.perSessionWorkers, .workerHistory, .orchardHistory]
  }
}
