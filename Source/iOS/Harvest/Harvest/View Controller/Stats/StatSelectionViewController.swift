//
//  StatSelectionViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka

// swiftlint:disable type_body_length
final class StatSelectionViewController: ReloadableFormViewController {
  var refreshControl: UIRefreshControl?
  
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
  func orchardPerformances() -> [StatEntitySelectionRow] {
    let yesterdaysOrchardPerformance = StatEntitySelectionRow { row in
      row.title = "Yesterdays Orchard Performance"
      let yesterday = Date().yesterday()
      row.startDate = yesterday.0
      row.endDate = yesterday.1
      row.period = .hourly
      row.grouping = .orchard
      row.value = Entities.shared.orchards.map { EntityItem.orchard($0.value) }
    }
    
    let todaysOrchardPerformance = StatEntitySelectionRow { row in
      row.title = "Todays Orchard Performance"
      let today = Date().today()
      row.startDate = today.0
      row.endDate = today.1
      row.period = .hourly
      row.grouping = .orchard
      row.value = Entities.shared.orchards.map { EntityItem.orchard($0.value) }
    }
    
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
    
    return [
      todaysOrchardPerformance,
      yesterdaysOrchardPerformance,
      thisWeeksOrchardPerformance,
      lastWeeksOrchardPerformance,
      thisMonthsOrchardPerformance,
      lastMonthsOrchardPerformance,
      thisYearsOrchardPerformance,
      lastYearsOrchardPerformance
    ]
  }
  
  // swiftlint:disable function_body_length
  func workerPerformances() -> [StatEntitySelectionRow] {
    let yesterdaysWorkerPerformance = StatEntitySelectionRow { row in
      row.title = "Yesterdays Worker Performance"
      let yesterday = Date().yesterday()
      row.startDate = yesterday.0
      row.endDate = yesterday.1
      row.period = .hourly
      row.grouping = .worker
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .worker {
          return EntityItem.worker($0.value)
        }
        return nil
      }
    }
    
    let todaysWorkerPerformance = StatEntitySelectionRow { row in
      row.title = "Todays Worker Performance"
      let today = Date().today()
      row.startDate = today.0
      row.endDate = today.1
      row.period = .hourly
      row.grouping = .worker
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .worker {
          return EntityItem.worker($0.value)
        }
        return nil
      }
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
    
    return [
      todaysWorkerPerformance,
      yesterdaysWorkerPerformance,
      thisWeeksWorkerPerformance,
      lastWeeksWorkerPerformance,
      thisMonthsWorkerPerformance,
      lastMonthsWorkerPerformance,
      thisYearsWorkerPerformance,
      lastYearsWorkerPerformance
    ]
  }
  
  // swiftlint:disable function_body_length
  func foremanPerformances() -> [StatEntitySelectionRow] {
    let yesterdaysForemanPerformance = StatEntitySelectionRow { row in
      row.title = "Yesterdays Foreman Performance"
      let yesterday = Date().yesterday()
      row.startDate = yesterday.0
      row.endDate = yesterday.1
      row.period = .hourly
      row.grouping = .foreman
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .foreman {
          return EntityItem.worker($0.value)
        }
        return nil
        } + [EntityItem.worker(Worker(HarvestUser.current))]
    }
    
    let todaysForemanPerformance = StatEntitySelectionRow { row in
      row.title = "Todays Foreman Performance"
      let today = Date().today()
      row.startDate = today.0
      row.endDate = today.1
      row.period = .hourly
      row.grouping = .foreman
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .foreman {
          return EntityItem.worker($0.value)
        }
        return nil
        } + [EntityItem.worker(Worker(HarvestUser.current))]
    }
    
    let lastWeeksForemanPerformance = StatEntitySelectionRow { row in
      row.title = "Last Weeks Foreman Performance"
      let lastWeek = Date().lastWeek()
      row.startDate = lastWeek.0
      row.endDate = lastWeek.1
      row.period = .daily
      row.grouping = .foreman
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .foreman {
          return EntityItem.worker($0.value)
        }
        return nil
      } + [EntityItem.worker(Worker(HarvestUser.current))]
    }
    
    let thisWeeksForemanPerformance = StatEntitySelectionRow { row in
      row.title = "This Weeks Foreman Performance"
      let thisWeek = Date().thisWeek()
      row.startDate = thisWeek.0
      row.endDate = thisWeek.1
      row.period = .daily
      row.grouping = .foreman
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .foreman {
          return EntityItem.worker($0.value)
        }
        return nil
      } + [EntityItem.worker(Worker(HarvestUser.current))]
    }
    
    let lastMonthsForemanPerformance = StatEntitySelectionRow { row in
      row.title = "Last Months Foreman Performance"
      let lastMonth = Date().lastMonth()
      row.startDate = lastMonth.0
      row.endDate = lastMonth.1
      row.period = .weekly
      row.grouping = .foreman
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .foreman {
          return EntityItem.worker($0.value)
        }
        return nil
      } + [EntityItem.worker(Worker(HarvestUser.current))]
    }
    
    let thisMonthsForemanPerformance = StatEntitySelectionRow { row in
      row.title = "This Months Foreman Performance"
      let thisMonth = Date().thisMonth()
      row.startDate = thisMonth.0
      row.endDate = thisMonth.1
      row.period = .weekly
      row.grouping = .foreman
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .foreman {
          return EntityItem.worker($0.value)
        }
        return nil
      } + [EntityItem.worker(Worker(HarvestUser.current))]
    }
    
    let lastYearsForemanPerformance = StatEntitySelectionRow { row in
      row.title = "Last Years Foreman Performance"
      let lastYear = Date().lastYear()
      row.startDate = lastYear.0
      row.endDate = lastYear.1
      row.period = .monthly
      row.grouping = .foreman
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .foreman {
          return EntityItem.worker($0.value)
        }
        return nil
      } + [EntityItem.worker(Worker(HarvestUser.current))]
    }
    
    let thisYearsForemanPerformance = StatEntitySelectionRow { row in
      row.title = "This Years Foreman Performance"
      let thisYear = Date().thisYear()
      row.startDate = thisYear.0
      row.endDate = thisYear.1
      row.period = .monthly
      row.grouping = .foreman
      row.value = Entities.shared.workers.compactMap {
        if $0.value.kind == .foreman {
          return EntityItem.worker($0.value)
        }
        return nil
      } + [EntityItem.worker(Worker(HarvestUser.current))]
    }
    
    return [
      todaysForemanPerformance,
      yesterdaysForemanPerformance,
      thisWeeksForemanPerformance,
      lastWeeksForemanPerformance,
      thisMonthsForemanPerformance,
      lastMonthsForemanPerformance,
      thisYearsForemanPerformance,
      lastYearsForemanPerformance
    ]
  }
  
  override func setUp() {
    let orchardSection = Section("Orchard Comparison")
    for stat in orchardPerformances() {
      orchardSection <<< stat
    }
    
    let workerSection = Section("Worker Comparison")
    for stat in workerPerformances() {
      workerSection <<< stat
    }
    
    let foremanSection = Section("Foreman Comparison")
    for stat in foremanPerformances() {
      foremanSection <<< stat
    }
    
    form
      +++ Section("Custom")
      <<< customGraph()
      
      +++ orchardSection
      
      +++ workerSection
      
      +++ foremanSection
  }
  
  override func tearDown() {
    form.removeAll()
  }
  
  public override func viewDidLoad() {
    super.viewDidLoad()
  }
  
  public override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
  }
  
  public override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    tableView.reloadData()
  }
}
