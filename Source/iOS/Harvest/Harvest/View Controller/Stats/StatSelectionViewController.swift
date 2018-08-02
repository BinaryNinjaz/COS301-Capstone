//
//  StatSelectionViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka
import SCLAlertView

final class StatSelectionViewController: ReloadableFormViewController {
  var refreshControl: UIRefreshControl?
  var showingAlert: Bool = false
  
  func customGraph() -> LabelRow {
    return LabelRow { row in
      row.title = "Create Graph"
    }.cellUpdate { cell, _ in
      cell.textLabel?.textAlignment = .center
      cell.textLabel?.textColor = .addOrchard
    }.onCellSelection { _, _ in
      guard let vc = self.storyboard?.instantiateViewController(withIdentifier: "statSetupViewController") else {
        return
      }
      
      self.navigationController?.pushViewController(vc, animated: true)
    }
  }
  
  @objc func longPressCustomGraph(_ recognizer: UIGestureRecognizer) {
    guard !showingAlert else {
      return
    }
    
    let alert = SCLAlertView(appearance: .optionsAppearance)
    
    let name = recognizer.accessibilityLabel ?? ""
    
    alert.addButton("Rename") {
      let infoAlert = SCLAlertView(appearance: .warningAppearance)
      
      let nameTextField = infoAlert.addTextField()
      
      infoAlert.addButton("Rename") {
        StatStore.shared.renameItem(withName: name, toNewName: nameTextField.text ?? "")
      }
      
      infoAlert.addButton("Cancel") {}
      
      infoAlert.showEdit("New Name", subTitle: "Please enter a new name for '\(name)'.")
      
      self.showingAlert = false
    }
    
    alert.addButton("Delete") {
      StatStore.shared.removeItem(withName: name)
      self.showingAlert = false
    }
    
    alert.addButton("Cancel") {
      self.showingAlert = false
    }
    
    showingAlert = true
    alert.showEdit(name, subTitle: "Select an option to perform on the graph '\(name)'.")
  }
  
  func customGraphSection() -> Section {
    let result = Section("Your Graphs")
    for stat in StatStore.shared.store {
      result <<< ButtonRow { row in
        row.title = stat.name
      }.cellUpdate { cell, _ in
        cell.textLabel?.textAlignment = .left
        cell.textLabel?.textColor = .black
        
        let longPress = UILongPressGestureRecognizer(
          target: self,
          action: #selector(self.longPressCustomGraph(_:)))
        longPress.accessibilityLabel = stat.name
        
        cell.addGestureRecognizer(longPress)
      }.onCellSelection { _, _ in
        
        guard let vc = self.storyboard?.instantiateViewController(withIdentifier: "statsViewController") else {
          return
        }
        
        guard let svc = vc as? StatsViewController else {
          return
        }
        
        svc.startDate = stat.startDate
        svc.endDate = stat.endDate
        svc.period = stat.period
        svc.stat = Stat.untyped(stat.ids, stat.grouping)
        svc.mode = stat.mode
        
        self.navigationController?.pushViewController(svc, animated: true)
      }
    }
    
    result <<< customGraph()
    
    return result
  }
  
  // swiftlint:disable function_body_length
  func performanceRows(for entities: [EntityItem]?, grouping: HarvestCloud.GroupBy) -> [StatEntitySelectionRow] {
    let yesterdaysPerformance = StatEntitySelectionRow { row in
      row.title = "Yesterday's \(grouping.title) Performance"
      let yesterday = Date().yesterday()
      row.startDate = yesterday.0
      row.endDate = yesterday.1
      row.period = .hourly
      row.grouping =  grouping
      row.value = entities
    }
    
    let todaysPerformance = StatEntitySelectionRow { row in
      row.title = "Today's \(grouping.title) Performance"
      let today = Date().today()
      row.startDate = today.0
      row.endDate = today.1
      row.period = .hourly
      row.grouping = grouping
      row.value = entities
    }
    
    let lastWeeksPerformance = StatEntitySelectionRow { row in
      row.title = "Last Week's \(grouping.title) Performance"
      let lastWeek = Date().lastWeek()
      row.startDate = lastWeek.0
      row.endDate = lastWeek.1
      row.period = .daily
      row.grouping = grouping
      row.value = entities
    }
    
    let thisWeeksPerformance = StatEntitySelectionRow { row in
      row.title = "This Week's \(grouping.title) Performance"
      let thisWeek = Date().thisWeek()
      row.startDate = thisWeek.0
      row.endDate = thisWeek.1
      row.period = .daily
      row.grouping = grouping
      row.value = entities
    }
    
    let lastMonthsPerformance = StatEntitySelectionRow { row in
      row.title = "Last Month's \(grouping.title) Performance"
      let lastMonth = Date().lastMonth()
      row.startDate = lastMonth.0
      row.endDate = lastMonth.1
      row.period = .weekly
      row.grouping = grouping
      row.value = entities
    }
    
    let thisMonthsPerformance = StatEntitySelectionRow { row in
      row.title = "This Month's \(grouping.title) Performance"
      let thisMonth = Date().thisMonth()
      row.startDate = thisMonth.0
      row.endDate = thisMonth.1
      row.period = .weekly
      row.grouping = grouping
      row.value = entities
    }
    
    let lastYearsPerformance = StatEntitySelectionRow { row in
      row.title = "Last Year's \(grouping.title) Performance"
      let lastYear = Date().lastYear()
      row.startDate = lastYear.0
      row.endDate = lastYear.1
      row.period = .monthly
      row.grouping = grouping
      row.value = entities
    }
    
    let thisYearsPerformance = StatEntitySelectionRow { row in
      row.title = "This Year's \(grouping.title) Performance"
      let thisYear = Date().thisYear()
      row.startDate = thisYear.0
      row.endDate = thisYear.1
      row.period = .monthly
      row.grouping = grouping
      row.value = entities
    }
    
    return [
      todaysPerformance,
      yesterdaysPerformance,
      thisWeeksPerformance,
      lastWeeksPerformance,
      thisMonthsPerformance,
      lastMonthsPerformance,
      thisYearsPerformance,
      lastYearsPerformance
    ]
  }
  
  func orchardPerformances() -> [StatEntitySelectionRow] {
    return performanceRows(
      for: Entities.shared.orchards.map { EntityItem.orchard($0.value) },
      grouping: .orchard)
  }
  
  func workerPerformances() -> [StatEntitySelectionRow] {
    let entities: [EntityItem] = Entities.shared.workers.compactMap {
      if $0.value.kind == .worker {
        return EntityItem.worker($0.value)
      }
      return nil
    }
    return performanceRows(for: entities, grouping: .worker)
  }
  
  func foremanPerformances() -> [StatEntitySelectionRow] {
    let entities: [EntityItem] = Entities.shared.workers.compactMap {
      if $0.value.kind == .foreman {
        return EntityItem.worker($0.value)
      }
      return nil
    } + [EntityItem.worker(Worker(HarvestUser.current))]
    
    return performanceRows(for: entities, grouping: .foreman)
  }
  
  func farmPerformances() -> [StatEntitySelectionRow] {
    return performanceRows(
      for: Entities.shared.farms.map { EntityItem.farm($0.value) },
      grouping: .farm)
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
    
    let farmSection = Section("Farm Comparison")
    for stat in farmPerformances() {
      farmSection <<< stat
    }
    
    form
      +++ customGraphSection()
      
      +++ farmSection
      
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
