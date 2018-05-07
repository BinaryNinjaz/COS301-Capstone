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
    pieDataSet.colors = ChartColorTemplates.joyful()
    
    let pieData = PieChartData(dataSet: pieDataSet)
    pieChart?.data = pieData
    pieChart?.notifyDataSetChanged()
    pieChart?.isHidden = false
  }
  
  func drawWorkerHistory() {
    guard let lineDataSet = stat?.workerHistoryData() else {
      return
    }
    lineDataSet.colors = [NSUIColor.Bootstrap.green[0]]
    lineDataSet.circleColors = [NSUIColor.Bootstrap.green[1]]
    
    let lineData = LineChartData(dataSet: lineDataSet)
    lineChart?.data = lineData
    lineChart?.drawGridBackgroundEnabled = false
    lineChart?.gridBackgroundColor = NSUIColor.clear
    lineChart?.notifyDataSetChanged()
    lineChart?.xAxis.valueFormatter = DateFormatter.with(format: "MM/dd")
    lineChart?.isHidden = false
  }
  
  func drawOrchardHistory() {
    guard let lineDataSet = stat?.orchardHistoryData() else {
      return
    }
    lineDataSet.colors = [NSUIColor.Bootstrap.green[0]]
    lineDataSet.circleColors = [NSUIColor.Bootstrap.green[1]]
    
    let lineData = LineChartData(dataSet: lineDataSet)
    lineChart?.data = lineData
    lineChart?.drawGridBackgroundEnabled = false
    lineChart?.gridBackgroundColor = NSUIColor.clear
    lineChart?.notifyDataSetChanged()
    lineChart?.xAxis.valueFormatter = DateFormatter.with(format: "MM/dd")
    lineChart?.isHidden = false
  }
  
  override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
    switch stat! {
    case .perSessionWorkers: return .all
    case .workerHistory, .orchardHistory: return .allButUpsideDown
    }
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
