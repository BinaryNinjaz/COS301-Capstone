//
//  DonutChartEurekaCell.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/06.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka
import Charts

public class DonutChartCell: Cell<Session>, CellType {
  @IBOutlet weak var chart: PieChartView!
  
  public override func setup() {
    super.setup()
    chart.chartDescription = nil
    chart.legend.enabled = false
    chart.noDataText = "No Data Available to Show"
    chart.noDataFont = UIFont.systemFont(ofSize: 22, weight: .heavy)
    height = {
      self.frame.size.width
    }
  }
  
  public override func update() {
    super.update()
    chart.data = row?.value?.workerPerformanceSummary()
  }
}

// The custom Row also has the cell: CustomCell and its correspond value
public final class DonutChartRow: Row<DonutChartCell>, RowType {
  required public init(tag: String?) {
    super.init(tag: tag)
    // We set the cellProvider to load the .xib corresponding to our cell
    cellProvider = CellProvider<DonutChartCell>(nibName: "DonutChartEurekaCell")
  }
}

extension Session {
  func workerPerformanceSummary() -> PieChartData? {
    let result = PieChartDataSet()
    for (worker, amount) in collections {
      result.values.append(PieChartDataEntry(value: Double(amount.count), label: worker.description))
    }
    result.colors = ChartColorTemplates.harvest()
    return collections.isEmpty ? nil : PieChartData(dataSet: result)
  }
}
