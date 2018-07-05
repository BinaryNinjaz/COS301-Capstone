//
//  StatsViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import Charts

class StatsViewController: UIViewController {
  var stat: Stat?
  var period: HarvestCloud.TimePeriod?
  var startDate: Date?
  var endDate: Date?
  
  var barChart: BarChartView?
  var pieChart: PieChartView?
  var lineChart: LineChartView?
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    let navH = navigationController?.navigationBar.frame.height ?? 0.0
    let tabH = tabBarController?.tabBar.frame.height ?? 0.0
    
    let frame = CGRect(x: 0,
                       y: navH * 2,
                       width: view.frame.width,
                       height: view.frame.height - navH * 2 - tabH)
    
    lineChart = LineChartView(frame: frame)
    barChart = BarChartView(frame: frame)
    pieChart = PieChartView(frame: frame)
    
    drawChart()
    
    view.addSubview(barChart!)
    view.addSubview(pieChart!)
    view.addSubview(lineChart!)
    
    setUpLineChart()
    setUpBarChart()
    pieChart?.chartDescription = nil
  }
  
  func drawChart() {
    lineChart?.isHidden = true
    barChart?.isHidden = true
    pieChart?.isHidden = true
    
    guard let stat = stat else {
      UIAlertController.present(title: "No Data", message: "There is no data available to show", on: self)
      return
    }
    
    switch stat {
    case .workerComparison: drawWorkerComparison()
    case .foremanComparison: drawForemanComparison()
    case .orchardComparison: drawOrchardComparison()
    }
  }
  
//  func drawPerSessionWorkers() {
//    stat?.perSessionWorkersData { pieDataSet in
//      guard let pieDataSet = pieDataSet else {
//        return
//      }
//      pieDataSet.colors = ChartColorTemplates.material()
//
//      let pieData = PieChartData(dataSet: pieDataSet)
//      self.pieChart?.data = pieData
//      self.pieChart?.notifyDataSetChanged()
//      self.pieChart?.isHidden = false
//      self.pieChart?.animate(xAxisDuration: 3.0, yAxisDuration: 1.0, easingOption: .easeOutCubic)
//    }
//  }
  
  func updateBarChart(with data: BarChartData?) {
    guard let barData = data else {
      // FIXME: Show no data
      return
    }
    if barData.dataSetCount > 1 {
      let groupSize = 0.2
      let barSpace = 0.03
      let gg = barData.groupWidth(groupSpace: groupSize, barSpace: barSpace)
      barChart?.xAxis.axisMaximum = Double(barData.dataSets[0].entryCount) * gg + 0
      
      barData.groupBars(fromX: 0, groupSpace: groupSize, barSpace: barSpace)
    }
    DispatchQueue.main.async {
      self.barChart?.notifyDataSetChanged()
      self.barChart?.data = barData
      
      self.barChart?.isHidden = false
      self.barChart?.animate(yAxisDuration: 1.5, easingOption: .easeOutCubic)
    }
  }
  
  func drawForemanComparison() {
    let s = startDate ?? Date(timeIntervalSince1970: 0)
    let e = endDate ?? Date()
    let p = period ?? .daily
    
    stat?.foremanComparison(startDate: s, endDate: e, period: p, completion: updateBarChart)
  }
  
  func drawWorkerComparison() {
    let s = startDate ?? Date(timeIntervalSince1970: 0)
    let e = endDate ?? Date()
    let p = period ?? .daily
    
    stat?.workerComparison(startDate: s, endDate: e, period: p, completion: updateBarChart)
  }
  
  func drawOrchardComparison() {
    let s = startDate ?? Date(timeIntervalSince1970: 0)
    let e = endDate ?? Date()
    let p = period ?? .daily
    
    stat?.orchardComparison(startDate: s, endDate: e, period: p, completion: updateBarChart)
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
    barChart?.xAxis.valueFormatter = PeriodValueFormatter(period ?? .daily)
    barChart?.rightAxis.drawGridLinesEnabled = false
    barChart?.xAxis.axisMinimum = 0
    barChart?.rightAxis.enabled = false
    
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
  
  init(_ period: HarvestCloud.TimePeriod) {
    self.period = period
  }
  
  public func stringForValue(_ value: Double, axis: AxisBase?) -> String {
    let possibles = period.fullPrintableDataSet()
    let pc = Double(possibles.count)
    let i = Int(value / (axis?.axisMaximum ?? pc) * pc)
    
    if i >= 0 && i < possibles.count {
      return possibles[i]
    } else {
      return ""
    }
  }
}
