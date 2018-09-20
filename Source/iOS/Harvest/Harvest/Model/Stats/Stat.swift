//
//  StatStore.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/28.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Disk
import Charts

struct StatStore {
  static var shared = StatStore()
  
  var path: String {
    return HarvestUser.current.uid + "StatStore"
  }
  var store: [Stat] = []
  
  init() {
    store = []
  }
  
  mutating func updateStore() {
    store = (try? Disk.retrieve(path, from: .applicationSupport, as: [Stat].self)) ?? []
  }
  
  mutating func saveItem(item: Stat) {
    try? Disk.append(item, to: path, in: .applicationSupport)
    store.append(item)
  }
  
  mutating func removeItem(withName name: String) {
    if let idx = store.index(where: { $0.name == name }) {
      store.remove(at: idx)
      try? Disk.save(store, to: .applicationSupport, as: path)
    }
  }
  
  mutating func renameItem(withName name: String, toNewName newName: String) {
    if let idx = store.index(where: { $0.name == name }) {
      store[idx].name = newName
      try? Disk.save(store, to: .applicationSupport, as: path)
    }
  }
  
  func getItem(withName name: String) -> Stat? {
    return store.first { $0.name == name }
  }
}

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

extension ChartColorTemplates {
  static func harvestGreen() -> [UIColor] {
    return [
      #colorLiteral(red: 0.6039215686, green: 0.7176470588, blue: 0.4901960784, alpha: 1),
      #colorLiteral(red: 0.4235294118, green: 0.6, blue: 0.3098039216, alpha: 1),
      #colorLiteral(red: 0.2431372549, green: 0.3843137255, blue: 0.1960784314, alpha: 1),
      #colorLiteral(red: 0.1019607843, green: 0.2588235294, blue: 0.1137254902, alpha: 1),
      #colorLiteral(red: 0.09803921569, green: 0.1764705882, blue: 0.09411764706, alpha: 1),
      #colorLiteral(red: 0.007843137255, green: 0.0431372549, blue: 0.01960784314, alpha: 1)
    ]
  }
  
  static func harvestWood() -> [UIColor] {
    return [
      #colorLiteral(red: 0.3058823529, green: 0.3725490196, blue: 0.3843137255, alpha: 1),
      #colorLiteral(red: 0.4235294118, green: 0.431372549, blue: 0.2509803922, alpha: 1),
      #colorLiteral(red: 0.9058823529, green: 0.7019607843, blue: 0.3803921569, alpha: 1),
      #colorLiteral(red: 0.6901960784, green: 0.3725490196, blue: 0.1411764706, alpha: 1),
      #colorLiteral(red: 0.4941176471, green: 0.168627451, blue: 0.1254901961, alpha: 1),
      #colorLiteral(red: 0.2823529412, green: 0.1529411765, blue: 0.03921568627, alpha: 1)
    ]
  }
  
  static func harvestBlue() -> [UIColor] {
    return [
      #colorLiteral(red: 0.4470588235, green: 0.7529411765, blue: 0.9764705882, alpha: 1),
      #colorLiteral(red: 0.2745098039, green: 0.631372549, blue: 0.9725490196, alpha: 1),
      #colorLiteral(red: 0.1921568627, green: 0.462745098, blue: 0.7098039216, alpha: 1),
      #colorLiteral(red: 0.1137254902, green: 0.3019607843, blue: 0.4862745098, alpha: 1),
      #colorLiteral(red: 0.06666666667, green: 0.2078431373, blue: 0.3725490196, alpha: 1),
      #colorLiteral(red: 0.01960784314, green: 0.09411764706, blue: 0.2078431373, alpha: 1)
    ]
  }
  
  static func harvestColorful() -> [UIColor] {
    return [
      #colorLiteral(red: 0.2745098039, green: 0.631372549, blue: 0.9725490196, alpha: 1),
      #colorLiteral(red: 0.5058823529, green: 0.831372549, blue: 0.3254901961, alpha: 1),
      #colorLiteral(red: 0.937254902, green: 0.7333333333, blue: 0.2509803922, alpha: 1),
      #colorLiteral(red: 0.9215686275, green: 0.2509803922, blue: 0.1450980392, alpha: 1),
      #colorLiteral(red: 0.7058823529, green: 0.3176470588, blue: 0.5137254902, alpha: 1),
      #colorLiteral(red: 0.3725490196, green: 0.3725490196, blue: 0.3725490196, alpha: 1)
    ]
  }
  
  static func harvest() -> [UIColor] {
    return harvestColorful()
  }
}

infix operator ++=
extension Array {
  static func ++= (lhs: inout [Element], rhs: Element) {
    lhs.append(rhs)
  }
}

extension StatStore {
  // swiftlint:disable function_body_length
  func setUpPredefinedGraphs() {
    var stats = [Stat]()
    
    stats ++= Stat(
      ids: [],
      timePeriod: .today,
      timeStep: .hourly,
      grouping: .worker,
      mode: .running,
      name: "Today's Worker Performance",
      showExpected: true,
      showAverage: true,
      curveKind: .curve)
    
    stats ++= Stat(
      ids: [],
      timePeriod: .yesterday,
      timeStep: .hourly,
      grouping: .worker,
      mode: .running,
      name: "Yesterday's Worker Performance",
      showExpected: true,
      showAverage: true,
      curveKind: .curve)
    
    stats ++= Stat(
      ids: [],
      timePeriod: .between(Date(), Date()),
      timeStep: .daily,
      grouping: .worker,
      mode: .running,
      name: "Worker Performance",
      showExpected: true,
      showAverage: true,
      curveKind: .curve)
    
    stats ++= Stat(
      ids: [],
      timePeriod: .thisMonth,
      timeStep: .daily,
      grouping: .orchard,
      mode: .running,
      name: "This Month's Orchard Performance",
      showExpected: true,
      showAverage: true,
      curveKind: .curve)
    
    stats ++= Stat(
      ids: [],
      timePeriod: .between(Date(), Date()),
      timeStep: .daily,
      grouping: .orchard,
      mode: .running,
      name: "Orchard Performance",
      showExpected: true,
      showAverage: true,
      curveKind: .curve)
    
    stats ++= Stat(
      ids: [],
      timePeriod: .thisMonth,
      timeStep: .daily,
      grouping: .farm,
      mode: .running,
      name: "This Month's Farm Performance",
      showExpected: true,
      showAverage: true,
      curveKind: .curve)
    
    stats ++= Stat(
      ids: [],
      timePeriod: .between(Date(), Date()),
      timeStep: .daily,
      grouping: .farm,
      mode: .running,
      name: "Farm Performance",
      showExpected: true,
      showAverage: true,
      curveKind: .curve)
    
    for stat in stats {
      StatStore.shared.saveItem(item: stat)
    }
  }
}
