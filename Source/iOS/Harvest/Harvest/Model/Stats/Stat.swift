//
//  Stat.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Charts
import CoreLocation

enum Stat {
  case perSessionWorkers(Session)
  case workerHistory(Worker)
  case orchardHistory(Orchard)
  
  func perSessionWorkersData() -> PieChartDataSet? {
    guard case .perSessionWorkers(let session) = self else {
      return nil
    }
    
    let result = PieChartDataSet()
    for (worker, amount) in session.collections {
      result.values.append(PieChartDataEntry(value: Double(amount.count), label: worker.description))
    }
    return result
  }
  
  func workerHistoryData() -> LineChartDataSet? {
    guard case .workerHistory(let worker) = self else {
      return nil
    }
    
    let sessions = Entities.shared.sessionsList()
    
    let result = LineChartDataSet()
    result.label = worker.description
    var started = false
    var i = 0.0
    
    for session in sessions {
      let collections = session.collections[worker]
      if !started && collections == nil {
        continue
      }
      started = true
      let amount = collections?.count ?? 0
      let date = session.startDate
      let entry = ChartDataEntry(x: date.timeIntervalSince1970, y: Double(amount))
      i += 1
      result.values.append(entry)
    }
    
    return result
  }
  
  func orchardHistoryData() -> LineChartDataSet? {
    guard case .orchardHistory(let orchard) = self else {
      return nil
    }
    var interResult = [Date: Double]()
    let sessions = Entities.shared.sessionsList()
    
    let poly = Poly(orchard.coords.map { Point($0.longitude, $0.latitude) })
    let calendar = Calendar.current
    
    for session in sessions {
      session.collections.forEach { w, points in
        points.forEach { point in
          let p = Point<CLLocationDegrees>.init(point.location.longitude, point.location.latitude)
          if poly.contains(p) {
            var comps = calendar.dateComponents([.era, .calendar, .month, .day], from: point.date)
            comps.hour = 0
            interResult[calendar.date(from: comps)!, default: 0] += 1
          }
        }
      }
    }
    
    let result = LineChartDataSet()
    result.label = orchard.description
    for (d, amount) in interResult {
      result.values.append(ChartDataEntry(x: d.timeIntervalSince1970, y: amount))
    }
    print(interResult)
    
    return result
    
  }
}
