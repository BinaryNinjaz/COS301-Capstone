//
//  StatsViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit
import Charts

class StatsViewController : UIViewController {
  var stat: Stat? = nil
  var barChart: BarChartView? = nil
  var pieChart: PieChartView? = nil
  var lineChart: LineChartView? = nil
  
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
    
    switch stat! { // FIXME
    case .perSessionWorkers: drawPerSessionWorkers()
    case .workerHistory: drawWorkerHistory()
    case .orchardHistory: drawOrchardHistory()
    }
  }
  
  func drawPerSessionWorkers() {
    guard let pieDataSet = stat?.perSessionWorkersData() else {
      return
    }
    pieDataSet.colors = ChartColorTemplates.material()
    
    let pieData = PieChartData(dataSet: pieDataSet)
    pieChart?.data = pieData
    pieChart?.notifyDataSetChanged()
    pieChart?.isHidden = false
    pieChart?.animate(xAxisDuration: 3.0, yAxisDuration: 1.0, easingOption: .easeOutCubic)
  }
  
  func drawWorkerHistory() {
    guard let lineDataSet = stat?.workerHistoryData() else {
      return
    }
    lineDataSet.colors = [[UIColor].green[0]]
    lineDataSet.drawCirclesEnabled = false
    lineDataSet.mode = .linear
    lineDataSet.lineWidth = 4.0
    
    let lineData = LineChartData(dataSet: lineDataSet)
    lineChart?.data = lineData
    lineChart?.notifyDataSetChanged()
    lineChart?.isHidden = false
    lineChart?.animate(xAxisDuration: 1.5, easingOption: .easeOutCubic)
  }
  
  func drawOrchardHistory() {
    guard let (dates, amounts) = stat?.orchardHistoryData() else {
      return
    }
    
    
    let barDataSet = BarChartDataSet()
    for (i, amount) in zip(0..., amounts) {
      barDataSet.values.append(BarChartDataEntry(x: Double(i), y: amount))
    }
    barDataSet.colors = ChartColorTemplates.material()
    
    let barData = BarChartData(dataSet: barDataSet)
    barChart?.xAxis.valueFormatter = OrchardDateFormatter(dates, "dd MMM")
    barChart?.data = barData
    barChart?.notifyDataSetChanged()
    barChart?.isHidden = false
    barChart?.animate(yAxisDuration: 1.5, easingOption: .easeOutCubic)
  }
  
  override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
    switch stat! {
    case .perSessionWorkers: return .all
    case .workerHistory, .orchardHistory: return .allButUpsideDown
    }
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
    barChart?.rightAxis.drawGridLinesEnabled = false
    barChart?.legend.enabled = false
  }
}


extension DateFormatter : IAxisValueFormatter {
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

final class OrchardDateFormatter  : IAxisValueFormatter {
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
