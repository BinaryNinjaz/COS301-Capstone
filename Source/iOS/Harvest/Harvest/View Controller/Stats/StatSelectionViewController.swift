//
//  StatSelectionViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka

// swiftlint:disable type_body_length
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
  
  // swiftlint:disable function_body_length
  // swiftlint:disable cyclomatic_complexity
  public override func viewDidLoad() {
    super.viewDidLoad()
    
    let lastWeeksOrchardPerformance = StatEntitySelectionRow { row in
      row.title = "Last Weeks Orchard Performance"
      let lastWeek = Date().lastWeek()
      row.startDate = lastWeek.0
      row.endDate = lastWeek.1
      row.period = .daily
      row.grouping = .orchard
      row.value = Entities.shared.orchards.map { EntityItem.orchard($0.value) }
    }
    
    let thisWeeksOrchardPerformance = StatEntitySelectionRow { row in
      row.title = "This Weeks Orchard Performance"
      let thisWeek = Date().thisWeek()
      row.startDate = thisWeek.0
      row.endDate = thisWeek.1
      row.period = .daily
      row.grouping = .orchard
      row.value = Entities.shared.orchards.map { EntityItem.orchard($0.value) }
    }
    
    let lastWeeksWorkerPerformance = StatEntitySelectionRow { row in
      row.title = "Last Weeks Worker Performance"
      let lastWeek = Date().lastWeek()
      row.startDate = lastWeek.0
      row.endDate = lastWeek.1
      row.period = .daily
      row.grouping = .worker
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .worker {
          return EntityItem.worker($0.value)
        }
        return nil
      }
    }
    
    let thisWeeksWorkerPerformance = StatEntitySelectionRow { row in
      row.title = "This Weeks Worker Performance"
      let thisWeek = Date().thisWeek()
      row.startDate = thisWeek.0
      row.endDate = thisWeek.1
      row.period = .daily
      row.grouping = .worker
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .worker {
          return EntityItem.worker($0.value)
        }
        return nil
      }
    }
    
    let lastWeeksForemanPerformance = StatEntitySelectionRow { row in
      row.title = "Last Weeks Foreman Performance"
      let lastWeek = Date().lastWeek()
      row.startDate = lastWeek.0
      row.endDate = lastWeek.1
      row.period = .daily
      row.grouping = .worker
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .foreman {
          return EntityItem.worker($0.value)
        }
        return nil
      }
    }
    
    let thisWeeksForemanPerformance = StatEntitySelectionRow { row in
      row.title = "This Weeks Foreman Performance"
      let thisWeek = Date().thisWeek()
      row.startDate = thisWeek.0
      row.endDate = thisWeek.1
      row.period = .weekly
      row.grouping = .worker
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .foreman {
          return EntityItem.worker($0.value)
        }
        return nil
      }
    }
    
    let lastMonthsOrchardPerformance = StatEntitySelectionRow { row in
      row.title = "Last Months Orchard Performance"
      let lastMonth = Date().lastMonth()
      row.startDate = lastMonth.0
      row.endDate = lastMonth.1
      row.period = .weekly
      row.grouping = .orchard
      row.value = Entities.shared.orchards.map { EntityItem.orchard($0.value) }
    }
    
    let thisMonthsOrchardPerformance = StatEntitySelectionRow { row in
      row.title = "This Months Orchard Performance"
      let thisMonth = Date().thisMonth()
      row.startDate = thisMonth.0
      row.endDate = thisMonth.1
      row.period = .weekly
      row.grouping = .orchard
      row.value = Entities.shared.orchards.map { EntityItem.orchard($0.value) }
    }
    
    let lastMonthsWorkerPerformance = StatEntitySelectionRow { row in
      row.title = "Last Months Worker Performance"
      let lastMonth = Date().lastMonth()
      row.startDate = lastMonth.0
      row.endDate = lastMonth.1
      row.period = .weekly
      row.grouping = .worker
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .worker {
          return EntityItem.worker($0.value)
        }
        return nil
      }
    }
    
    let thisMonthsWorkerPerformance = StatEntitySelectionRow { row in
      row.title = "This Months Worker Performance"
      let thisMonth = Date().thisMonth()
      row.startDate = thisMonth.0
      row.endDate = thisMonth.1
      row.period = .weekly
      row.grouping = .worker
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .worker {
          return EntityItem.worker($0.value)
        }
        return nil
      }
    }
    
    let lastMonthsForemanPerformance = StatEntitySelectionRow { row in
      row.title = "Last Months Foreman Performance"
      let lastMonth = Date().lastMonth()
      row.startDate = lastMonth.0
      row.endDate = lastMonth.1
      row.period = .weekly
      row.grouping = .worker
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .foreman {
          return EntityItem.worker($0.value)
        }
        return nil
      }
    }
    
    let thisMonthsForemanPerformance = StatEntitySelectionRow { row in
      row.title = "This Months Foreman Performance"
      let thisMonth = Date().thisMonth()
      row.startDate = thisMonth.0
      row.endDate = thisMonth.1
      row.period = .weekly
      row.grouping = .worker
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .foreman {
          return EntityItem.worker($0.value)
        }
        return nil
      }
    }
    
    let lastYearsOrchardPerformance = StatEntitySelectionRow { row in
      row.title = "Last Years Orchard Performance"
      let lastYear = Date().lastYear()
      row.startDate = lastYear.0
      row.endDate = lastYear.1
      row.period = .monthly
      row.grouping = .orchard
      row.value = Entities.shared.orchards.map { EntityItem.orchard($0.value) }
    }
    
    let thisYearsOrchardPerformance = StatEntitySelectionRow { row in
      row.title = "This Years Orchard Performance"
      let thisYear = Date().thisYear()
      row.startDate = thisYear.0
      row.endDate = thisYear.1
      row.period = .monthly
      row.grouping = .orchard
      row.value = Entities.shared.orchards.map { EntityItem.orchard($0.value) }
    }
    
    let lastYearsWorkerPerformance = StatEntitySelectionRow { row in
      row.title = "Last Years Worker Performance"
      let lastYear = Date().lastYear()
      row.startDate = lastYear.0
      row.endDate = lastYear.1
      row.period = .monthly
      row.grouping = .worker
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .worker {
          return EntityItem.worker($0.value)
        }
        return nil
      }
    }
    
    let thisYearsWorkerPerformance = StatEntitySelectionRow { row in
      row.title = "This Years Worker Performance"
      let thisYear = Date().thisYear()
      row.startDate = thisYear.0
      row.endDate = thisYear.1
      row.period = .monthly
      row.grouping = .worker
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .worker {
          return EntityItem.worker($0.value)
        }
        return nil
      }
    }
    
    let lastYearsForemanPerformance = StatEntitySelectionRow { row in
      row.title = "Last Years Foreman Performance"
      let lastYear = Date().lastYear()
      row.startDate = lastYear.0
      row.endDate = lastYear.1
      row.period = .monthly
      row.grouping = .worker
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .foreman {
          return EntityItem.worker($0.value)
        }
        return nil
      }
    }
    
    let thisYearsForemanPerformance = StatEntitySelectionRow { row in
      row.title = "This Years Foreman Performance"
      let thisYear = Date().thisYear()
      row.startDate = thisYear.0
      row.endDate = thisYear.1
      row.period = .monthly
      row.grouping = .worker
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .foreman {
          return EntityItem.worker($0.value)
        }
        return nil
      }
    }
    
    form
      +++ Section("Custom")
      <<< customGraph()
    
      +++ Section("Orchard Comparison")
      <<< lastWeeksOrchardPerformance
      <<< thisWeeksOrchardPerformance
      <<< lastMonthsOrchardPerformance
      <<< thisMonthsOrchardPerformance
      <<< lastYearsOrchardPerformance
      <<< thisYearsOrchardPerformance
    
      +++ Section("Worker Comparison")
      <<< lastWeeksWorkerPerformance
      <<< thisWeeksWorkerPerformance
      <<< lastMonthsWorkerPerformance
      <<< thisMonthsWorkerPerformance
      <<< lastYearsWorkerPerformance
      <<< thisYearsWorkerPerformance
    
      +++ Section("Foreman Comparison")
      <<< lastWeeksForemanPerformance
      <<< thisWeeksForemanPerformance
      <<< lastMonthsForemanPerformance
      <<< thisMonthsForemanPerformance
      <<< lastYearsForemanPerformance
      <<< thisYearsForemanPerformance
  }
  
  public override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
  }
  
  public override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    tableView.reloadData()
  }
}
