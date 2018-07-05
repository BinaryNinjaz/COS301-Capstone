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
  
  // swiftlint:disable function_parameter_count
  func comparison(
    ids: [String],
    grouping: HarvestCloud.GroupBy,
    startDate: Date,
    endDate: Date,
    period: HarvestCloud.TimePeriod,
    completion: @escaping ([String: Any]?) -> Void
  ) {
    HarvestCloud.timeGraphSessions(
      grouping: grouping,
      ids: ids,
      period: period,
      startDate: startDate,
      endDate: endDate) { data in
        guard let json = data as? [String: Any] else {
          completion(nil)
          return
        }
        
        completion(json)
      }
  }
  
  func foremanComparison(
    startDate: Date,
    endDate: Date,
    period: HarvestCloud.TimePeriod,
    completion: @escaping (BarChartData?) -> Void
  ) {
    guard case let .foremanComparison(foremen) = self else {
      completion(nil)
      return
    }
    
    var ids = [String]()
    for foreman in foremen {
      ids.append(foreman.id)
    }
    
    var dataSets = [BarChartDataSet]()
    
    comparison(
      ids: ids,
      grouping: .foreman,
      startDate: startDate,
      endDate: endDate,
      period: period) { json in
        guard let json = json else {
          completion(nil)
          return
        }
        var i = 0
        let colors = ChartColorTemplates.harvest()
        for (key, _dataSetObject) in json {
          guard let dataSetObject = _dataSetObject as? [String: Double] else {
            continue
          }
          
          let worker = Entities.shared.worker(withId: key)
          
          let dataSet = BarChartDataSet()
          dataSet.label = worker?.name ?? "Unknown Foreman"
          
          for (e, x) in period.fullDataSet().enumerated() {
            if let y = dataSetObject[x] {
              _ = dataSet.addEntry(BarChartDataEntry(x: Double(e), y: y))
            } else {
              _ = dataSet.addEntry(BarChartDataEntry(x: Double(e), y: 0.0))
            }
          }
          
          dataSet.setColor(colors[i % colors.count])
          dataSet.drawValuesEnabled = false
          i += 1
          dataSets.append(dataSet)
        }
        
        let data = BarChartData(dataSets: dataSets)
        
        completion(data)
    }
  }
  
  func workerComparison(
    startDate: Date,
    endDate: Date,
    period: HarvestCloud.TimePeriod,
    completion: @escaping (BarChartData?) -> Void
  ) {
    guard case let .workerComparison(foremen) = self else {
      completion(nil)
      return
    }
    
    var ids = [String]()
    for foreman in foremen {
      ids.append(foreman.id)
    }
    
    var dataSets = [BarChartDataSet]()
    
    comparison(
      ids: ids,
      grouping: .worker,
      startDate: startDate,
      endDate: endDate,
      period: period) { json in
        guard let json = json else {
          completion(nil)
          return
        }
        var i = 0
        let colors = ChartColorTemplates.harvest()
        for (key, _dataSetObject) in json {
          guard let dataSetObject = _dataSetObject as? [String: Double] else {
            continue
          }
          
          let worker = Entities.shared.worker(withId: key)
          
          let dataSet = BarChartDataSet()
          dataSet.label = worker?.name ?? "Unknown Worker"
          
          for (e, x) in period.fullDataSet().enumerated() {
            if let y = dataSetObject[x] {
              _ = dataSet.addEntry(BarChartDataEntry(x: Double(e), y: y))
            } else {
              _ = dataSet.addEntry(BarChartDataEntry(x: Double(e), y: 0.0))
            }
          }
          
          dataSet.setColor(colors[i % colors.count])
          dataSet.drawValuesEnabled = false
          i += 1
          dataSets.append(dataSet)
        }
        
        let data = BarChartData(dataSets: dataSets)
        
        completion(data)
      }
  }
  
  func orchardComparison(
    startDate: Date,
    endDate: Date,
    period: HarvestCloud.TimePeriod,
    completion: @escaping (BarChartData?) -> Void
  ) {
    guard case let .orchardComparison(orchards) = self else {
      completion(nil)
      return
    }
    
    var ids = [String]()
    for orchard in orchards {
      ids.append(orchard.id)
    }
    
    var dataSets = [BarChartDataSet]()
    
    comparison(
      ids: ids,
      grouping: .orchard,
      startDate: startDate,
      endDate: endDate,
      period: period) { json in
        guard let json = json else {
          completion(nil)
          return
        }
        var i = 0
        let colors = ChartColorTemplates.harvest()
        for (key, _dataSetObject) in json {
          guard let dataSetObject = _dataSetObject as? [String: Double] else {
            continue
          }
          
          let orchard = Entities.shared.orchards.first { $0.value.id == key }.map { $0.value }
          
          let dataSet = BarChartDataSet()
          dataSet.label = orchard?.name ?? "Unknown Orchard"
          
          for (e, x) in period.fullDataSet().enumerated() {
            if let y = dataSetObject[x] {
              _ = dataSet.addEntry(BarChartDataEntry(x: Double(e), y: y))
            } else {
              _ = dataSet.addEntry(BarChartDataEntry(x: Double(e), y: 0.0))
            }
          }
          
          dataSet.setColor(colors[i % colors.count])
          dataSet.drawValuesEnabled = false
          i += 1
          dataSets.append(dataSet)
        }
        
        let data = BarChartData(dataSets: dataSets)
        
        completion(data)
      }
  }
  
}

extension ChartColorTemplates {
  static func harvest() -> [UIColor] {
    return [
      #colorLiteral(red: 1, green: 0.1491314173, blue: 0, alpha: 1),
      #colorLiteral(red: 1, green: 0.5781051517, blue: 0, alpha: 1),
      #colorLiteral(red: 0.9994240403, green: 0.9855536819, blue: 0, alpha: 1),
      #colorLiteral(red: 0.5563425422, green: 0.9793455005, blue: 0, alpha: 1),
      #colorLiteral(red: 0, green: 0.9768045545, blue: 0, alpha: 1),
      #colorLiteral(red: 0, green: 0.9810667634, blue: 0.5736914277, alpha: 1),
      #colorLiteral(red: 0, green: 0.9914394021, blue: 1, alpha: 1),
      #colorLiteral(red: 0, green: 0.5898008943, blue: 1, alpha: 1),
      #colorLiteral(red: 0.01680417731, green: 0.1983509958, blue: 1, alpha: 1),
      #colorLiteral(red: 0.5818830132, green: 0.2156915367, blue: 1, alpha: 1),
      #colorLiteral(red: 1, green: 0.2527923882, blue: 1, alpha: 1),
      #colorLiteral(red: 1, green: 0.1857388616, blue: 0.5733950138, alpha: 1),
    ]
  }
}
