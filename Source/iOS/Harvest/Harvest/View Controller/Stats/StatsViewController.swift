//
//  StatsViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import Charts
import SnapKit
import SCLAlertView

class StatsViewController: UIViewController {
  var stat: Stat?
  var period: HarvestCloud.TimePeriod?
  var startDate: Date?
  var endDate: Date?
  var mode: HarvestCloud.Mode?
  
  var barChart: CombinedChartView?
  var pieChart: PieChartView?
  var lineChart: LineChartView?
  var radarChart: RadarChartView?
  var activityIndicator: UIActivityIndicatorView?
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    let navH = navigationController?.navigationBar.frame.height ?? 0.0
    let tabH = tabBarController?.tabBar.frame.height ?? 0.0
    
    let frame = CGRect(x: 0,
                       y: navH * 2,
                       width: view.frame.width,
                       height: view.frame.height - navH * 2 - tabH)
    
    lineChart = LineChartView(frame: frame)
    barChart = CombinedChartView(frame: frame)
    pieChart = PieChartView(frame: frame)
    radarChart = RadarChartView(frame: frame)
    activityIndicator = UIActivityIndicatorView(activityIndicatorStyle: .whiteLarge)
    activityIndicator?.color = UIColor.harvestGreen
    activityIndicator?.stopAnimating()
    
    drawChart()
    
    view.addSubview(barChart!)
    view.addSubview(pieChart!)
    view.addSubview(lineChart!)
    view.addSubview(radarChart!)
    view.addSubview(activityIndicator!)
    
    barChart?.snp.makeConstraints(Snap.fillParent(on: self))
    lineChart?.snp.makeConstraints(Snap.fillParent(on: self))
    pieChart?.snp.makeConstraints(Snap.fillParent(on: self))
    radarChart?.snp.makeConstraints(Snap.fillParent(on: self))
    setActivityPosition()
    
    setUpLineChart()
    setUpBarChart()
    setUpRadarChart()
    pieChart?.chartDescription = nil
  }
  
  func drawChart() {
    lineChart?.isHidden = true
    barChart?.isHidden = true
    pieChart?.isHidden = true
    radarChart?.isHidden = true
    activityIndicator?.startAnimating()
    
    guard let stat = stat else {
      SCLAlertView().showWarning("No Data", subTitle: "There is no data available to show")
      activityIndicator?.stopAnimating()
      return
    }
    
    switch stat {
    case .workerComparison: drawEntityComparison(entity: .worker)
    case .foremanComparison: drawEntityComparison(entity: .foreman)
    case .orchardComparison: drawEntityComparison(entity: .orchard)
    case .farmComparison: drawEntityComparison(entity: .farm)
    case let .untyped(_, kind):
      switch kind {
      case .worker: drawEntityComparison(entity: .worker)
      case .foreman: drawEntityComparison(entity: .foreman)
      case .orchard: drawEntityComparison(entity: .orchard)
      case .farm: drawEntityComparison(entity: .farm)
      }
    }
  }
  
  func updateBarChart(with data: BarChartData?, and lineData: LineChartData?) {
    DispatchQueue.main.async {
      self.activityIndicator?.stopAnimating()
      guard let barData = data else {
        self.barChart?.data = nil
        return
      }
      
      if self.mode == .running {
        self.lineChart?.notifyDataSetChanged()
        self.lineChart?.data = barData.lineChartData()
        self.lineChart?.data?.setDrawValues(true)
        
        self.lineChart?.isHidden = false
        self.lineChart?.animate(yAxisDuration: 1.5, easingOption: .easeOutCubic)
      } else if self.period == .daily {
        self.radarChart?.notifyDataSetChanged()
        self.radarChart?.data = barData.radarChartData()
        self.radarChart?.data?.setDrawValues(true)
        
        self.radarChart?.isHidden = false
        self.radarChart?.animate(yAxisDuration: 1.5, easingOption: .easeOutCubic)
      } else {
        if barData.dataSetCount > 1 {
          let groupSize = 0.2
          let barSpace = 0.03
          let gg = barData.groupWidth(groupSpace: groupSize, barSpace: barSpace)
          self.barChart?.xAxis.axisMaximum = Double(barData.dataSets[0].entryCount) * gg + 0
          
          barData.groupBars(fromX: 0, groupSpace: groupSize, barSpace: barSpace)
        }
        
        self.barChart?.notifyDataSetChanged()
        
        let combinedData = CombinedChartData()
        combinedData.barData = barData
        if let lineData = lineData, barData.dataSetCount == 1 {
          combinedData.lineData = lineData
        }
        
        self.barChart?.data = combinedData
        self.barChart?.data?.setDrawValues(true)
        
        self.barChart?.isHidden = false
        self.barChart?.animate(yAxisDuration: 1.5, easingOption: .easeOutCubic)
      }
    }
  }
  
  func drawEntityComparison(entity: HarvestCloud.GroupBy) {
    let s = startDate ?? Date(timeIntervalSince1970: 0)
    let e = endDate ?? Date()
    let p = period ?? .daily
    let m = mode ?? .accum
    
    stat?.entityComparison(
      grouping: entity,
      startDate: s,
      endDate: e,
      period: p,
      mode: m,
      completion: updateBarChart)
  }
  
  override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
    return .allButUpsideDown
  }
  
  func setUp(_ legend: Legend?) {
    if let l = legend {
      l.enabled = true
      l.drawInside = true
      l.horizontalAlignment = .right
      l.verticalAlignment = .top
      l.orientation = .vertical
      l.drawInside = true
      l.font = .systemFont(ofSize: 8, weight: .light)
      l.yOffset = 10
      l.xOffset = 10
      l.yEntrySpace = 0
    }
  }
  
  func setUpLineChart() {
    lineChart?.chartDescription?.enabled = false
    lineChart?.dragEnabled = true
    lineChart?.setScaleEnabled(true)
    lineChart?.pinchZoomEnabled = true
    lineChart?.xAxis.drawGridLinesEnabled = false
    lineChart?.xAxis.labelPosition = .bottom
    lineChart?.xAxis.valueFormatter = PeriodValueFormatter(period ?? .daily, startDate, endDate, mode)
    lineChart?.rightAxis.drawGridLinesEnabled = false
    lineChart?.xAxis.axisMinimum = 0
    lineChart?.rightAxis.enabled = false
    lineChart?.noDataFont = UIFont.systemFont(ofSize: 22, weight: .heavy)
    lineChart?.noDataText = "No Data Available to Show"
    setUp(lineChart?.legend)
  }
  
  func setUpBarChart() {
    barChart?.chartDescription?.enabled = false
    barChart?.dragEnabled = true
    barChart?.setScaleEnabled(true)
    barChart?.pinchZoomEnabled = true
    barChart?.xAxis.drawGridLinesEnabled = false
    barChart?.xAxis.labelPosition = .bottom
    barChart?.xAxis.valueFormatter = PeriodValueFormatter(period ?? .daily, startDate, endDate, mode)
    barChart?.rightAxis.drawGridLinesEnabled = false
    barChart?.xAxis.axisMinimum = 0
    barChart?.rightAxis.enabled = false
    barChart?.noDataFont = UIFont.systemFont(ofSize: 22, weight: .heavy)
    barChart?.noDataText = "No Data Available to Show"
    setUp(barChart?.legend)
  }
  
  func setUpRadarChart() {
    radarChart?.chartDescription?.enabled = false
    radarChart?.xAxis.drawGridLinesEnabled = false
    radarChart?.xAxis.labelPosition = .bottom
    radarChart?.xAxis.valueFormatter = PeriodValueFormatter(period ?? .daily, startDate, endDate, mode)
    radarChart?.xAxis.axisMinimum = 0
    radarChart?.noDataFont = UIFont.systemFont(ofSize: 22, weight: .heavy)
    radarChart?.noDataText = "No Data Available to Show"
    setUp(radarChart?.legend)
  }
  
  func setActivityPosition() {
    activityIndicator?.setOriginX(view.frame.width / 2 - (activityIndicator?.frame.width ?? 0) / 2)
    activityIndicator?.setOriginY(view.frame.height / 2 - (activityIndicator?.frame.height ?? 0) / 2)
  }
  
  override func viewWillLayoutSubviews() {
    super.viewWillLayoutSubviews()
    setActivityPosition()
  }
}

extension DateFormatter: IAxisValueFormatter {
  public func stringForValue(_ value: Double, axis: AxisBase?) -> String {
    return string(from: Date(timeIntervalSince1970: value))
  }
  
  static func with(dateStyle: DateFormatter.Style) -> DateFormatter {
    let result = DateFormatter()
    result.dateStyle = dateStyle
    return result
  }
  
  static func with(format: String) -> DateFormatter {
    let result = DateFormatter()
    result.dateFormat = format
    return result
  }
}

final class OrchardDateFormatter: IAxisValueFormatter {
  var formatter: DateFormatter
  var range: [Double]
  
  init(_ range: [Double], _ format: String) {
    formatter = DateFormatter()
    formatter.dateFormat = format
    self.range = range
  }
  
  public func stringForValue(_ value: Double, axis: AxisBase?) -> String {
    let d = range[Int(truncating: NSNumber(value: value))]
    return formatter.string(from: Date(timeIntervalSince1970: d))
  }
}

final class PeriodValueFormatter: IAxisValueFormatter {
  let period: HarvestCloud.TimePeriod
  let startDate: Date?
  let endDate: Date?
  let mode: HarvestCloud.Mode?
  
  init(_ period: HarvestCloud.TimePeriod, _ sd: Date?, _ ed: Date?, _ mode: HarvestCloud.Mode?) {
    self.period = period
    startDate = sd
    endDate = ed
    self.mode = mode ?? .accum
  }
  
  public func stringForValue(_ value: Double, axis: AxisBase?) -> String {
    let possibles: [String]
    if mode == .accum {
      possibles = period.fullPrintableDataSet(between: startDate, and: endDate, limitToDate: period == .weekly)
    } else if let s = startDate, let e = endDate {
      possibles = period.fullRunningDataSet(between: s, and: e)
    } else {
      possibles = []
    }
    
    let pc = Double(possibles.count)
    let max = axis?.axisMaximum == 0 ? pc : (axis?.axisMaximum ?? pc)
    let i = Int(value / max * pc)
    
    if i >= 0 && i < possibles.count {
      return possibles[i]
    } else {
      return ""
    }
  }
}

extension BarChartData {
  func radarChartData() -> RadarChartData {
    let result = RadarChartData()
    
    for dataSet in dataSets {
      let chartData = RadarChartDataSet()
      for i in 0..<dataSet.entryCount {
        guard let bentry = dataSet.entryForIndex(i) as? BarChartDataEntry else {
          continue
        }
        _ = chartData.addEntry(RadarChartDataEntry(value: bentry.y))
      }
      chartData.setColor(dataSet.colors[0])
      chartData.fillColor = dataSet.colors[0]
      chartData.fillAlpha = 0.7
      chartData.lineWidth = 2
      chartData.drawFilledEnabled = true
      chartData.drawValuesEnabled = true
      chartData.label = dataSet.label
      chartData.valueFormatter = DataValueFormatter()
      result.addDataSet(chartData)
    }
    
    return result
  }
  
  func lineChartData() -> LineChartData {
    let result = LineChartData()
    
    for dataSet in dataSets {
      let chartData = LineChartDataSet()
      for i in 0..<dataSet.entryCount {
        guard let bentry = dataSet.entryForIndex(i) as? BarChartDataEntry else {
          continue
        }
        _ = chartData.addEntry(ChartDataEntry(x: bentry.x, y: bentry.y))
      }
      chartData.setColor(dataSet.colors[0])
      chartData.fillColor = dataSet.colors[0]
      chartData.fillAlpha = 0.7
      chartData.lineWidth = 2
      chartData.drawFilledEnabled = false
      chartData.drawValuesEnabled = true
      chartData.label = dataSet.label
      chartData.valueFormatter = DataValueFormatter()
      chartData.drawCirclesEnabled = false
      chartData.mode = .horizontalBezier
      result.addDataSet(chartData)
    }
    
    return result
  }
}
