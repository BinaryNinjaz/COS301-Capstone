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
  case foremanComparison([Worker])
  case workerComparison([Worker])
  case orchardComparison([Orchard])
  
  func foremanComparison(
    startDate: Date,
    endDate: Date,
    period: HarvestCloud.TimePeriod,
    completion: @escaping (BarChartData?) -> ()
  ) {
    guard case let .foremanComparison(foremen) = self else {
      completion(nil)
      return
    }
    
    var dataSets = [BarChartDataSet]()
    
    var ids = [String]()
    for foreman in foremen {
      ids.append(foreman.id)
    }
    
    HarvestCloud.timeGraphSessions(
      grouping: .foreman,
      ids: ids,
      period: period,
      startDate: startDate,
      endDate: endDate) { data in
        guard let json = data as? [String: Any] else {
          return
        }
        
        for (key, _dataSetObject) in json {
          guard let dataSetObject = _dataSetObject as? [String: Any] else {
            continue
          }
          
          let foreman = Entities.shared.worker(withId: key)
          
          let dataSet = BarChartDataSet()
          dataSet.label = foreman?.name ?? "Unknown Foreman"
          
          for (offset: i, element: (key: _, value: y)) in dataSetObject.enumerated() {
            _ = dataSet.addEntry(BarChartDataEntry(x: Double(i), y: y as? Double ?? 0))
          }
          
          dataSets.append(dataSet)
        }
        
        completion(BarChartData(dataSets: dataSets))
      }
  }
}
