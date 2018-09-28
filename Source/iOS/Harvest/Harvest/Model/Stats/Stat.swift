//
//  StatStore.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/28.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Charts

struct Stat: Codable {
  struct SinusoidalFunction {
    var key: String
    var a: Double
    var b: Double
    var c: Double
    var d: Double
    
    func apply(_ x: Double) -> Double {
      return a * sin(b * x + c) + d
    }
    
    func lineChartData(startDate: Date, endDate: Date, step: TimeStep) -> LineChartDataSet {
      let start = Double(startDate.stepsSince1970(step: step))
      let end = Double(endDate.stepsSince1970(step: step))
      
      let interval = Double(step.fullRunningDataSet(between: startDate, and: endDate).count)
      
      let diff = end - start
      let move = diff / interval
      
      var x = start
      let result = LineChartDataSet()
      
      for i in 0..<Int(interval) {
        _ = result.addEntry(ChartDataEntry(x: Double(i), y: apply(x)))
        x += move
      }

      return result
    }
  }
  
  var ids: [String]
  var timePeriod: TimePeriod
  var timeStep: TimeStep
  var grouping: StatKind
  var mode: TimedGraphMode
  var name: String
  var showExpected: Bool
  var showAverage: Bool
  var curveKind: LineGraphCurve
  
  func legendTitle(forKey key: String) -> String {
    let worker = Entities.shared.worker(withId: key)
    let orchard = Entities.shared.orchards.first { $0.value.id == key }.map { $0.value }
    let farm = Entities.shared.farms.first { $0.value.id == key }.map { $0.value }
    
    var message = ""
    
    let missingMessage = self.grouping.description
    message = (self.grouping == .orchard
      ? orchard?.name
      : self.grouping == .worker || self.grouping == .foreman
        ? worker?.name
        : farm?.name) ?? "Unknown \(missingMessage)"
    message = mode == .accumEntity ? "(Sum)" : message
    message = key == "avg" ? "Overall Average" : message
    
    return message
  }
  
  func formatDataSet(_ dataSet: LineChartDataSet) {
    dataSet.drawValuesEnabled = false
    dataSet.valueFormatter = DataValueFormatter()
    dataSet.mode =  curveKind == .curve
      ? .horizontalBezier
      : curveKind == .stepped
        ? .stepped
        : .linear
    dataSet.drawCirclesEnabled = curveKind == .linear
    dataSet.circleColors = [dataSet.colors[0]]
    dataSet.lineWidth = 2
  }
  
  func sortJSONEntities(json: [String: Any]) -> [(String, Any)] {
    var entities = [(String, Any)]()
    for (key, _dataSetObject) in json {
      if entities.isEmpty {
        entities.append((key, _dataSetObject))
      } else {
        let a = UIColor.huePrecedence(key: key)
        var i = 0
        var ins = false
        for (k, _) in entities {
          let b = UIColor.huePrecedence(key: k)
          if a < b {
            entities.insert((key, _dataSetObject), at: i)
            ins = true
            break
          }
          i += 1
        }
        if !ins {
          entities.append((key, _dataSetObject))
        }
      }
    }
    return entities
  }
  
  func graphData(completion: @escaping (LineChartData?) -> Void) {
    let (sd, ed) = timePeriod.dateRange()
    
    var dataSets = [LineChartDataSet]()
    var expectedDataSets: [LineChartDataSet] = []
    
    var expectedDataSetObject: [String: [String: Double?]]?
    
    HarvestCloud.timeGraphSessions(
      grouping: grouping, ids: ids, period: timeStep, startDate: sd, endDate: ed, mode: mode) { data in
        guard let json = data as? [String: Any] else {
          completion(nil)
          return
        }
        
        for (key, _dataSetObject) in self.sortJSONEntities(json: json) {
          if key == "exp" {
            expectedDataSetObject = _dataSetObject as? [String: [String: Double?]]
            continue
          }
          
          guard let dataSetObject = _dataSetObject as? [String: Double] else {
            continue
          }
          
          let dataSet = LineChartDataSet()
          dataSet.label = self.legendTitle(forKey: key)
          
          let fullDataSet = self.mode == .accumTime
            ? self.timeStep.fullDataSet(between: sd, and: ed, limitToDate: self.timeStep == .weekly)
            : self.timeStep.fullRunningDataSet(between: sd, and: ed)
          
          for (e, x) in fullDataSet.enumerated() {
            if let y = dataSetObject[x] {
              _ = dataSet.addEntry(BarChartDataEntry(x: Double(e), y: y))
            } else {
              _ = dataSet.addEntry(BarChartDataEntry(x: Double(e), y: 0.0))
            }
          }
          
          dataSet.setColor(UIColor.hashColor(parent: key, child: key))
          
          self.formatDataSet(dataSet)
          if key == "avg" {
            if self.showAverage {
              dataSet.setColor(.darkGray)
              dataSet.lineDashPhase = 0.5
              dataSet.lineDashLengths = [1, 1]
              dataSets.insert(dataSet, at: 0)
            }
          } else {
            dataSets.append(dataSet)
          }
        }
        
        let data = LineChartData(dataSets: dataSets)
        
        if self.showExpected, let expData = expectedDataSetObject, self.mode != .accumTime {
          expectedDataSets = self.expectedGraphData(json: expData)
          for expDataSet in expectedDataSets {
            data.addDataSet(expDataSet)
          }
        }
        
        completion(data)
    }
  }
  
  func expectedGraphData(
    json: [String: [String: Double?]]
  ) -> [LineChartDataSet] {
    var functions = [SinusoidalFunction]()
    for (key, function) in json {
      let a = function["a"] ?? 0.0
      let b = function["b"] ?? 0.0
      let c = function["c"] ?? 0.0
      let d = function["d"] ?? 0.0
      if let a = a, let b = b, let c = c, let d = d {
        functions.append(SinusoidalFunction(key: key, a: a, b: b, c: c, d: d))
      }
    }
    functions.sort { (a, b) -> Bool in
      return UIColor.huePrecedence(key: a.key) < UIColor.huePrecedence(key: b.key)
    }
    
    let (sd, ed) = timePeriod.dateRange()
    
    return functions.map { fx in
      let dataSet = fx.lineChartData(startDate: sd, endDate: ed, step: timeStep)
      dataSet.setColor(UIColor.hashColor(parent: fx.key, child: fx.key))
      dataSet.lineDashPhase = 0.5
      dataSet.lineDashLengths = [2, 2]
      dataSet.lineWidth = 2
      
      dataSet.label = self.legendTitle(forKey: fx.key) + " (expected)"
      
      formatDataSet(dataSet)
      dataSet.mode = .linear
      
      return dataSet
    }
  }
}

final class DataValueFormatter: IValueFormatter {
  func stringForValue(
    _ value: Double,
    entry: ChartDataEntry,
    dataSetIndex: Int,
    viewPortHandler: ViewPortHandler?
  ) -> String {
    if value.isZero {
      return ""
    } else if trunc(value) == value {
      return Int(value).description
    } else {
      let formatter = NumberFormatter()
      formatter.positiveFormat = ".#"
      return formatter.string(from: NSNumber(value: value)) ?? ""
    }
  }
}

infix operator ++=
extension Array {
  static func ++= (lhs: inout [Element], rhs: Element) {
    lhs.append(rhs)
  }
}
