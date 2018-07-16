//
//  StatKind.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Swift

enum StatKind: CustomStringConvertible {
//  case sessions
  case workers
  case orchards
  case foremen
  
  var description: String {
    switch self {
    case .workers: return "Worker Comparisons"
    case .orchards: return "Orchard Comparisons"
    case .foremen: return "Foremen Comparisons"
    }
  }
  
  static var allCases: [StatKind] {
    return [.workers, orchards, .foremen]
  }
}
