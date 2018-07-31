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
    case .workerComparison: drawWorkerComparison()
    case .foremanComparison: drawForemanComparison()
    case .orchardComparison: drawOrchardComparison()
    case let .untyped(_, kind):
      switch kind {
      case .worker: drawWorkerComparison()
      case .foreman: drawForemanComparison()
      case .orchard: drawOrchardComparison()
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
      if barData.dataSetCount > 1 {
        let groupSize = 0.2
        let barSpace = 0.03
        let gg = barData.groupWidth(groupSpace: groupSize, barSpace: barSpace)
        self.barChart?.xAxis.axisMaximum = Double(barData.dataSets[0].entryCount) * gg + 0
        
        barData.groupBars(fromX: 0, groupSpace: groupSize, barSpace: barSpace)
      }
      
      if self.period == .daily {
        self.radarChart?.notifyDataSetChanged()
        self.radarChart?.data = barData.radarChartData()
        self.radarChart?.data?.setDrawValues(true)
        
        self.radarChart?.isHidden = false
        self.radarChart?.animate(yAxisDuration: 1.5, easingOption: .easeOutCubic)
      } else {
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
  
  func drawForemanComparison() {
    let s = startDate ?? Date(timeIntervalSince1970: 0)
    let e = endDate ?? Date()
    let p = period ?? .daily
    
    stat?.entityComparison(grouping: .foreman, startDate: s, endDate: e, period: p, completion: updateBarChart)
  }
  
  func drawWorkerComparison() {
    let s = startDate ?? Date(timeIntervalSince1970: 0)
    let e = endDate ?? Date()
    let p = period ?? .daily
    
    stat?.entityComparison(grouping: .worker, startDate: s, endDate: e, period: p, completion: updateBarChart)
  }
  
  func drawOrchardComparison() {
    let s = startDate ?? Date(timeIntervalSince1970: 0)
    let e = endDate ?? Date()
    let p = period ?? .daily
    
    stat?.entityComparison(grouping: .orchard, startDate: s, endDate: e, period: p, completion: updateBarChart)
  }
  
  override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
    return .allButUpsideDown
  }
  
  func setUpLineChart() {
    lineChart?.drawGridBackgroundEnabled = false
    lineChart?.gridBackgroundColor = NSUIColor.clear
    lineChart?.xAxis.valueFormatter = DateFormatter.with(format: "dd MMM")
    lineChart?.chartDescription?.enabled = false
    lineChart?.dragEnabled = true
    lineChart?.setScaleEnabled(true)
    lineChart?.pinchZoomEnabled = true
    lineChart?.xAxis.drawGridLinesEnabled = false
    lineChart?.xAxis.labelPosition = .bottom
    lineChart?.rightAxis.drawGridLinesEnabled = false
  }
  
  func setUpBarChart() {
    barChart?.chartDescription?.enabled = false
    barChart?.dragEnabled = true
    barChart?.setScaleEnabled(true)
    barChart?.pinchZoomEnabled = true
    barChart?.xAxis.drawGridLinesEnabled = false
    barChart?.xAxis.labelPosition = .bottom
    barChart?.xAxis.valueFormatter = PeriodValueFormatter(period ?? .daily, startDate, endDate)
    barChart?.rightAxis.drawGridLinesEnabled = false
    barChart?.xAxis.axisMinimum = 0
    barChart?.rightAxis.enabled = false
    barChart?.noDataFont = UIFont.systemFont(ofSize: 22, weight: .heavy)
    barChart?.noDataText = "No Data Available to Show"
    
    if let l = barChart?.legend {
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
  
  func setUpRadarChart() {
    radarChart?.chartDescription?.enabled = false
    radarChart?.xAxis.drawGridLinesEnabled = false
    radarChart?.xAxis.labelPosition = .bottom
    radarChart?.xAxis.valueFormatter = PeriodValueFormatter(period ?? .daily, startDate, endDate)
    radarChart?.xAxis.axisMinimum = 0
    radarChart?.noDataFont = UIFont.systemFont(ofSize: 22, weight: .heavy)
    radarChart?.noDataText = "No Data Available to Show"
    
    if let l = radarChart?.legend {
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
  
  init(_ period: HarvestCloud.TimePeriod, _ sd: Date?, _ ed: Date?) {
    self.period = period
    startDate = sd
    endDate = ed
  }
  
  public func stringForValue(_ value: Double, axis: AxisBase?) -> String {
    let possibles = period.fullPrintableDataSet(between: startDate, and: endDate, limitToDate: period == .weekly)
    let pc = Double(possibles.count)
    let i = Int(value / (axis?.axisMaximum ?? pc) * pc)
    
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
      chartData.drawValuesEnabled = false
      chartData.label = dataSet.label
      result.addDataSet(chartData)
    }
    
    return result
  }
}
