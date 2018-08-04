//
//  StatStore.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/28.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Disk
import Charts

struct StatStore {
  static var shared = StatStore()
  
  let path = "StatStore"
  var store: [Stat]
  
  init() {
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
  var ids: [String]
  var timePeriod: TimePeriod
  var timeStep: TimeStep
  var grouping: StatKind
  var mode: TimedGraphMode
  var name: String
  
  func graphData(completion: @escaping (LineChartData?) -> Void) {
    let (sd, ed) = timePeriod.dateRange()
    
    var dataSets = [LineChartDataSet]()
    
    HarvestCloud.timeGraphSessions(
      grouping: grouping, ids: ids, period: timeStep, startDate: sd, endDate: ed, mode: mode) { data in
        guard let json = data as? [String: Any] else {
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
          let orchard = Entities.shared.orchards.first { $0.value.id == key }.map { $0.value }
          let farm = Entities.shared.farms.first { $0.value.id == key }.map { $0.value }
          
          let dataSet = LineChartDataSet()
          
          let missingMessage = self.grouping.description
          dataSet.label = (self.grouping == .orchard
            ? orchard?.name
            : self.grouping == .worker
            ? worker?.name
            : farm?.name) ?? "Unknown \(missingMessage)"
          dataSet.label = self.mode == .accumEntity ? "(Sum)" : dataSet.label
          dataSet.label = key == "avg" ? "Overall Average" : dataSet.label
          
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
          
          dataSet.setColor(colors[i % colors.count])
          dataSet.drawValuesEnabled = false
          dataSet.valueFormatter = DataValueFormatter()
          dataSet.mode = .horizontalBezier
          dataSet.drawCirclesEnabled = false
          dataSet.lineWidth = 2
          if key == "avg" {
            dataSet.setColor(.darkGray)
            dataSet.lineDashPhase = 0.5
            dataSet.lineDashLengths = [1, 1]
          }
          i += 1
          dataSets.append(dataSet)
        }
        
        let data = LineChartData(dataSets: dataSets)
        
        completion(data)
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
      timeStep: .weekly,
      grouping: .orchard,
      mode: .running,
      name: "This Month's Orchard Performance")
    
    stats ++= Stat(
      ids: [],
      timePeriod: .between(Date(), Date()),
      timeStep: .weekly,
      grouping: .orchard,
      mode: .running,
      name: "Orchard Performance")
    
    stats ++= Stat(
      ids: [],
      timePeriod: .thisMonth,
      timeStep: .weekly,
      grouping: .farm,
      mode: .running,
      name: "This Month's Farm Performance")
    
    stats ++= Stat(
      ids: [],
      timePeriod: .between(Date(), Date()),
      timeStep: .weekly,
      grouping: .farm,
      mode: .running,
      name: "Farm Performance")
    
    for stat in stats {
      StatStore.shared.saveItem(item: stat)
    }
  }
}
