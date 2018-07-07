//
//  StatSelectionViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka

public final class StatSelectionViewController: FormViewController {  
  func customGraph() -> LabelRow {
    return LabelRow { row in
      row.title = "Make Your Own Graph"
    }.cellUpdate { cell, _ in
      cell.textLabel?.textAlignment = .left
      cell.textLabel?.textColor = .addOrchard
    }.onCellSelection { _, _ in
      guard let vc = self.storyboard?.instantiateViewController(withIdentifier: "statSetupViewController") else {
        return
      }
      
      self.navigationController?.pushViewController(vc, animated: true)
    }
  }
  
  /// swiftlint:disable function_body_length
  public override func viewDidLoad() {
    super.viewDidLoad()
    
    let lastWeeksOrchardPerformance = prebuiltGraph(
      title: "Last Weeks Orchard Performance",
      startDate: Calendar.current.date(byAdding: .day, value: -7, to: Date())!,
      endDate: Date(),
      period: .daily,
      stat: .orchardComparison(Entities.shared.orchards.map { $0.value }))
    
    form
      +++ Section("Custom")
      <<< customGraph()
    
      +++ Section("Quick")
      <<< lastWeeksOrchardPerformance
  }
  
  public override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
  }
  
  public override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    tableView.reloadData()
  }
}
