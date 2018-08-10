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
      let start = startDate.daysSince1970()
      let end = endDate.daysSince1970()
      
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
  
  func legendTitle(forKey key: String) -> String {
    let worker = Entities.shared.worker(withId: key)
    let orchard = Entities.shared.orchards.first { $0.value.id == key }.map { $0.value }
    let farm = Entities.shared.farms.first { $0.value.id == key }.map { $0.value }
    
    var message = ""
    
    let missingMessage = self.grouping.description
    message = (self.grouping == .orchard
      ? orchard?.name
      : self.grouping == .worker
        ? worker?.name
        : farm?.name) ?? "Unknown \(missingMessage)"
    message = mode == .accumEntity ? "(Sum)" : message
    message = key == "avg" ? "Overall Average" : message
    
    return message
  }
  
  func dataSetColor(key: String, allUsedColors: inout [String: UIColor], position i: Int) -> UIColor {
    let colors = ChartColorTemplates.harvest()
    if let color = allUsedColors[key] {
      return color
    } else {
      if i < colors.count {
        allUsedColors[key] = colors[i]
      } else {
        allUsedColors[key] = UIColor.randomColor()
      }
      return allUsedColors[key]!
    }
  }
  
  func formatDataSet(_ dataSet: LineChartDataSet) {
    dataSet.drawValuesEnabled = false
    dataSet.valueFormatter = DataValueFormatter()
    dataSet.mode = .horizontalBezier
    dataSet.drawCirclesEnabled = false
    dataSet.lineWidth = 2
  }
  
  func graphData(completion: @escaping (LineChartData?) -> Void) {
    let (sd, ed) = timePeriod.dateRange()
    
    var dataSets = [LineChartDataSet]()
    var expectedDataSets: [LineChartDataSet] = []
    
    HarvestCloud.timeGraphSessions(
      grouping: grouping, ids: ids, period: timeStep, startDate: sd, endDate: ed, mode: mode) { data in
        guard let json = data as? [String: Any] else {
          completion(nil)
          return
        }
        
        var i = 0
        
        var allUsedColors = [String: UIColor]()
        for (key, _dataSetObject) in json {
          if key == "exp" {
            if let data = _dataSetObject as? [String: [String: Double]] {
              expectedDataSets = self.expectedGraphData(
                json: data,
                allUsedColors: &allUsedColors,
                position: i)
            }
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
          
          dataSet.setColor(self.dataSetColor(key: key, allUsedColors: &allUsedColors, position: i))
          
          self.formatDataSet(dataSet)
          if key == "avg" {
            dataSet.setColor(.darkGray)
            dataSet.lineDashPhase = 0.5
            dataSet.lineDashLengths = [1, 1]
            dataSets.insert(dataSet, at: 0)
          } else {
            dataSets.append(dataSet)
          }
          i += 1
        }
        
        let data = LineChartData(dataSets: dataSets)
        for expDataSet in expectedDataSets {
          data.addDataSet(expDataSet)
        }
        
        completion(data)
    }
  }
  
  func expectedGraphData(
    json: [String: [String: Double]],
    allUsedColors: inout [String: UIColor],
    position: Int
  ) -> [LineChartDataSet] {
    var functions = [SinusoidalFunction]()
    for (key, function) in json {
      let a = function["a"] ?? 0.0
      let b = function["b"] ?? 0.0
      let c = function["c"] ?? 0.0
      let d = function["d"] ?? 0.0
      functions.append(SinusoidalFunction(key: key, a: a, b: b, c: c, d: d))
    }
    
    let (sd, ed) = timePeriod.dateRange()
    
    return functions.map { fx in
      let dataSet = fx.lineChartData(startDate: sd, endDate: ed, step: timeStep)
      let color = dataSetColor(key: fx.key, allUsedColors: &allUsedColors, position: position)
      dataSet.setColor(color.withAlphaComponent(0.25))
      
      dataSet.label = self.legendTitle(forKey: fx.key)
      
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
      name: "Today's Worker Performance")
    
    stats ++= Stat(
      ids: [],
      timePeriod: .yesterday,
      timeStep: .hourly,
      grouping: .worker,
      mode: .running,
      name: "Yesterday's Worker Performance")
    
    stats ++= Stat(
      ids: [],
      timePeriod: .between(Date(), Date()),
      timeStep: .daily,
      grouping: .worker,
      mode: .running,
      name: "Worker Performance")
    
    stats ++= Stat(
      ids: [],
      timePeriod: .thisMonth,
      timeStep: .daily,
      grouping: .orchard,
      mode: .running,
      name: "This Month's Orchard Performance")
    
    stats ++= Stat(
      ids: [],
      timePeriod: .between(Date(), Date()),
      timeStep: .daily,
      grouping: .orchard,
      mode: .running,
      name: "Orchard Performance")
    
    stats ++= Stat(
      ids: [],
      timePeriod: .thisMonth,
      timeStep: .daily,
      grouping: .farm,
      mode: .running,
      name: "This Month's Farm Performance")
    
    stats ++= Stat(
      ids: [],
      timePeriod: .between(Date(), Date()),
      timeStep: .daily,
      grouping: .farm,
      mode: .running,
      name: "Farm Performance")
    
    for stat in stats {
      StatStore.shared.saveItem(item: stat)
    }
  }
}
