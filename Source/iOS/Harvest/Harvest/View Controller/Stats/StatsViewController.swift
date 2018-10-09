//
//  StatsViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/07.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import UIKit
import Charts
import SnapKit
import SCLAlertView

class StatsViewController: UIViewController {
  var stat: Stat?
  
  var lineChart: LineChartView?
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
    activityIndicator = UIActivityIndicatorView(style: .whiteLarge)
    activityIndicator?.color = UIColor.harvestGreen
    activityIndicator?.stopAnimating()
    
    drawChart()
    
    view.addSubview(lineChart!)
    view.addSubview(activityIndicator!)
    
    lineChart?.snp.makeConstraints(Snap.fillParent(on: self))
    setActivityPosition()
    
    setUpLineChart()
  }
  
  func drawChart() {
    lineChart?.isHidden = true
    activityIndicator?.startAnimating()
    
    guard let stat = stat else {
      SCLAlertView().showWarning("No Data", subTitle: "There is no data available to show")
      activityIndicator?.stopAnimating()
      return
    }
    
    stat.graphData(completion: updateChart)
  }
  
  func updateChart(with data: LineChartData?) {
    DispatchQueue.main.async {
      self.activityIndicator?.stopAnimating()
      guard let lineData = data else {
        self.lineChart?.data = nil
        return
      }
      self.lineChart?.notifyDataSetChanged()
      
      self.lineChart?.data = lineData
      self.lineChart?.data?.setDrawValues(true)
      
      self.lineChart?.isHidden = false
      self.lineChart?.animate(yAxisDuration: 1.5, easingOption: .easeOutCubic)
    }
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
    lineChart?.xAxis.valueFormatter = PeriodValueFormatter(
      stat?.timeStep ?? .daily,
      stat?.timePeriod ?? .today,
      stat?.mode ?? .accumTime)
    lineChart?.rightAxis.drawGridLinesEnabled = false
    lineChart?.leftAxis.axisMinimum = 0
    lineChart?.rightAxis.enabled = false
    lineChart?.noDataFont = UIFont.systemFont(ofSize: 22, weight: .heavy)
    lineChart?.noDataText = "No Data Available to Show"
    setUp(lineChart?.legend)
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
  let step: TimeStep
  let period: TimePeriod
  let mode: TimedGraphMode?
  
  init(_ step: TimeStep, _ period: TimePeriod, _ mode: TimedGraphMode?) {
    self.step = step
    self.period = period
    self.mode = mode ?? .accumTime
  }
  
  public func stringForValue(_ value: Double, axis: AxisBase?) -> String {
    let possibles: [String]
    let drange = period.dateRange()
    if mode == .accumTime {
      possibles = step.fullPrintableDataSet(between: drange.0, and: drange.1, limitToDate: step == .weekly)
    } else {
      possibles = step.fullRunningDataSet(between: drange.0, and: drange.1)
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
