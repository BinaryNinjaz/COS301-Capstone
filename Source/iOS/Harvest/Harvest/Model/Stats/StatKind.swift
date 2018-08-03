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
  case farms
  
  var description: String {
    switch self {
    case .workers: return "Worker Comparisons"
    case .orchards: return "Orchard Comparisons"
    case .foremen: return "Foremen Comparisons"
    case .farms: return "Farm Comparisons"
    }
  }
  
  var title: String {
    switch self {
    case .workers: return "Worker"
    case .orchards: return "Orchard"
    case .foremen: return "Foremen"
    case .farms: return "Farm"
    }
  }
  
  static var allCases: [StatKind] {
    return [.farms, .orchards, .workers, .foremen]
  }
}
