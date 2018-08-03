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
  case untyped([String], HarvestCloud.GroupBy)
  case foremanComparison([Worker])
  case workerComparison([Worker])
  case orchardComparison([Orchard])
  case farmComparison([Farm])
  
  // swiftlint:disable function_parameter_count
  func comparison(
    ids: [String],
    grouping: HarvestCloud.GroupBy,
    startDate: Date,
    endDate: Date,
    period: HarvestCloud.TimePeriod,
    mode: HarvestCloud.Mode,
    completion: @escaping ([String: Any]?) -> Void
  ) {
    HarvestCloud.timeGraphSessions(
      grouping: grouping,
      ids: ids,
      period: period,
      startDate: startDate,
      endDate: endDate,
      mode: mode) { data in
        guard let json = data as? [String: Any] else {
          completion(nil)
          return
        }
        
        completion(json)
      }
  }
  
  func identifiers(for grouping: HarvestCloud.GroupBy) -> [String]? {
    if case let .untyped(ids, _) = self {
      return ids
    }
    
    var entities: [EntityItem]
    
    if grouping == .foreman, case let .foremanComparison(foremen) = self {
      entities = foremen.map { .worker($0) }
    } else if grouping == .worker, case let .workerComparison(workers) = self {
      entities = workers.map { .worker($0) }
    } else if grouping == .orchard, case let .orchardComparison(orchards) = self {
      entities = orchards.map { .orchard($0) }
    } else if grouping == .farm, case let .farmComparison(farms) = self {
      entities = farms.map {.farm($0) }
    } else {
      return nil
    }
    
    var ids = [String]()
    for entity in entities {
      switch entity {
      case let .worker(w): ids.append(w.id)
      case let .orchard(o): ids.append(o.id)
      case let .farm(f): ids.append(f.id)
      default: continue
      }
    }
    
    return ids
  }
  
  func entityComparison(
    grouping: HarvestCloud.GroupBy,
    startDate: Date,
    endDate: Date,
    period: HarvestCloud.TimePeriod,
    mode: HarvestCloud.Mode,
    completion: @escaping (LineChartData?) -> Void
  ) {
    guard let ids = identifiers(for: grouping) else {
      completion(nil)
      return
    }
    
    var dataSets = [LineChartDataSet]()
    
    comparison(
      ids: ids,
      grouping: grouping,
      startDate: startDate,
      endDate: endDate,
      period: period,
      mode: mode) { json in
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
          let orchard = Entities.shared.orchards.first { $0.value.id == key }.map { $0.value }
          let farm = Entities.shared.farms.first { $0.value.id == key }.map { $0.value }
          
          let dataSet = LineChartDataSet()
          
          let missingMessage = grouping.description
          dataSet.label = (grouping == .orchard
            ? orchard?.name
            : grouping == .worker
              ? worker?.name
              : farm?.name) ?? "Unknown \(missingMessage)"
          dataSet.label = mode == .accumEntity ? "(Sum)" : dataSet.label
          dataSet.label = key == "avg" ? "Overall Average" : dataSet.label
          
          let fullDataSet = mode == .accumTime
            ? period.fullDataSet(
                between: startDate,
                and: endDate,
                limitToDate: period == .weekly)
            : period.fullRunningDataSet(between: startDate, and: endDate)
          
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
