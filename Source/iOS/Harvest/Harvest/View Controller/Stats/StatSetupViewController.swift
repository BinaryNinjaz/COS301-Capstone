//
//  StatSetupViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/07.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Eureka
import SCLAlertView

// swiftlint:disable type_body_length
final class StatSetupViewController: ReloadableFormViewController {
  var statKindRow: PickerRow<StatKind>?
  
  var workersRow: MultipleSelectorRow<Worker>?
  var orchardsRow: MultipleSelectorRow<Orchard>?
  var foremenRow: MultipleSelectorRow<Worker>?
  var farmsRow: MultipleSelectorRow<Farm>?
  
  var timePeriodRow: PushRow<TimePeriod>?
  var startDateRow: DateRow?
  var endDateRow: DateRow?
  var timeStepRow: PushRow<TimeStep>?
  var modeRow: SegmentedRow<TimedGraphMode>?
  
  public override func viewDidLoad() {
    super.viewDidLoad()
    navigationItem.rightBarButtonItem = UIBarButtonItem(
      title: "Save",
      style: .plain,
      target: self,
      action: #selector(saveStat))
  }
  
  @objc func saveStat() {
    var ids = [String]()
    
    if let kind = statKindRow?.value {
      switch kind {
      case .worker:
        for worker in workersRow?.value ?? [] {
          ids.append(worker.id)
        }
      case .foreman:
        for foreman in foremenRow?.value ?? [] {
          ids.append(foreman.id)
        }
      case .orchard:
        for orchard in orchardsRow?.value ?? [] {
          ids.append(orchard.id)
        }
      case .farm:
        for farm in farmsRow?.value ?? [] {
          ids.append(farm.id)
        }
      }
    }
    
    let sk = statKindRow?.value ?? .worker
    let sd = startDateRow?.value ?? Date()
    let ed = endDateRow?.value ?? Date()
    let step = timeStepRow?.value ?? .daily
    let period = timePeriodRow?.value ?? .today
    let mode = modeRow?.value ?? TimedGraphMode.accumEntity
    
    let filledPeriod: TimePeriod
    if period.wantsStartAndEndDate() {
      filledPeriod = .between(sd, ed)
    } else {
      filledPeriod = period
    }
    
    let alert = SCLAlertView(appearance: .warningAppearance)
    let statNameTextView = alert.addTextField()
    statNameTextView.placeholder = "Graph Name"
    
    alert.addButton("Save") {
      let item = Stat(
        ids: ids,
        timePeriod: filledPeriod,
        timeStep: step,
        grouping: sk,
        mode: mode,
        name: statNameTextView.text ?? Date().description)
      
      StatStore.shared.saveItem(item: item)
      
      SCLAlertView.showSuccessToast(message: "Graph Saved")
    }
    
    alert.addButton("Cancel") {}
    
    alert.showEdit("Graph Name", subTitle: "Please enter a name to save your custom graph as.")
  }
  
  // swiftlint:disable function_body_length
  override func setUp() {
    let modeSection = Section(
      header: "Accumulation",
      footer: (self.modeRow?.value ?? TimedGraphMode.running)
        .explanation(for: self.statKindRow?.value ?? .farm, by: self.timeStepRow?.value ?? .hourly))
    modeSection.footer?.height = { 80 }
    
    statKindRow = PickerRow<StatKind>("Stat Kind") { row in
      row.options = StatKind.allCases
      row.value = .farm
    }.onChange { (row) in
      UIView.performWithoutAnimation {
        let ent = row.value?.description ?? "Farm"
        self.modeRow?.cell.segmentedControl.setTitle("By \(ent)", forSegmentAt: 1)
        modeSection.footer?.title = (self.modeRow?.value ?? TimedGraphMode.running)
          .explanation(for: row.value ?? .farm, by: self.timeStepRow?.value ?? .hourly)
        modeSection.reload()
      }
    }
    
    workersRow = MultipleSelectorRow<Worker> { row in
      row.title = "Worker Selection"
      row.options = Array(Entities.shared.workers.lazy.map { $0.value }.filter { $0.kind == .worker })
      row.value = row.options?.first != nil ? [row.options!.first!] : []
      row.hidden = Condition.function(["Stat Kind"]) { form in
        let row = form.rowBy(tag: "Stat Kind") as? PickerRow<StatKind>
        return row?.value != .worker
      }
    }.cellUpdate { _, row in
      row.onPresentCallback = { _, tovc in
        tovc.allSelector()
      }
      row.options = Array(Entities.shared.workers.lazy.map { $0.value }.filter { $0.kind == .worker })
    }
    
    foremenRow = MultipleSelectorRow<Worker> { row in
      row.title = "Foreman Selection"
      row.options = Array(Entities.shared.workers.lazy.map { $0.value }.filter { $0.kind == .foreman })
      row.options?.append(Worker(HarvestUser.current))
      row.value = row.options?.first != nil ? [row.options!.first!] : []
      row.hidden = Condition.function(["Stat Kind"]) { form in
        let row = form.rowBy(tag: "Stat Kind") as? PickerRow<StatKind>
        return row?.value != .foreman
      }
    }.cellUpdate { _, row in
      row.onPresentCallback = { _, tovc in
        tovc.allSelector()
      }
      row.options = Array(Entities.shared.workers.lazy.map { $0.value }.filter { $0.kind == .foreman })
      row.options?.append(Worker(HarvestUser.current))
    }
    
    orchardsRow = MultipleSelectorRow<Orchard> { row in
      row.title = "Orchard Selection"
      row.options = Entities.shared.orchards.map { $0.value }
      row.value = row.options?.first != nil ? [row.options!.first!] : []
      row.hidden = Condition.function(["Stat Kind"]) { form in
        let row = form.rowBy(tag: "Stat Kind") as? PickerRow<StatKind>
        return row?.value != .orchard
      }
    }.cellUpdate { _, row in
      row.onPresentCallback = { _, tovc in
        tovc.allSelector()
      }
      row.options = Entities.shared.orchards.map { $0.value }
    }
    
    farmsRow = MultipleSelectorRow<Farm> { row in
      row.title = "Farm Selection"
      row.options = Entities.shared.farms.map { $0.value }
      row.value = row.options?.first != nil ? [row.options!.first!] : []
      row.hidden = Condition.function(["Stat Kind"]) { form in
        let row = form.rowBy(tag: "Stat Kind") as? PickerRow<StatKind>
        return row?.value != .farm
      }
    }.cellUpdate { _, row in
      row.onPresentCallback = { _, tovc in
        tovc.allSelector()
      }
      row.options = Entities.shared.farms.map { $0.value }
    }
    
    timePeriodRow = PushRow<TimePeriod>("Time Interval") { row in
      row.title = "Time Interval"
      row.options = TimePeriod.allCases
      row.value = .today
    }
    
    startDateRow = DateRow { row in
      row.title = "From Date"
      
      let wb = Date().thisWeek().0
      row.value = wb
      
      row.hidden = Condition.function(["Time Interval"]) { form in
        let row = form.rowBy(tag: "Time Interval") as? PushRow<TimePeriod>
        return !(row?.value?.wantsStartAndEndDate() ?? true)
      }
    }
    
    endDateRow = DateRow { row in
      row.title = "Up to Date"
      let wb = Date().thisWeek().1
      row.value = wb
      
      row.hidden = Condition.function(["Time Interval"]) { form in
        let row = form.rowBy(tag: "Time Interval") as? PushRow<TimePeriod>
        return !(row?.value?.wantsStartAndEndDate() ?? true)
      }
    }
    
    timeStepRow = PushRow<TimeStep> { row in
      row.title = "Time Period"
      row.options = TimeStep.allCases
      row.value = .hourly
    }.onChange { row in
      UIView.performWithoutAnimation {
        let mode = self.modeRow?.value ?? TimedGraphMode.running
        modeSection.footer?.title = mode
          .explanation(for: self.statKindRow?.value ?? .farm, by: row.value ?? .hourly)
        
        let timeStep = (row.value ?? .hourly).itemizedDescription.localizedCapitalized
        self.modeRow?.cell.segmentedControl.setTitle("By \(timeStep)", forSegmentAt: 2)
        modeSection.reload()
      }
    }
    
    modeRow = SegmentedRow<TimedGraphMode> { row in
      row.options = [.running, .accumEntity, .accumTime]
      row.value = .running
    }.cellUpdate { (cell, _) in
      let timeStep = (self.timeStepRow?.value ?? .hourly).itemizedDescription.localizedCapitalized
      cell.segmentedControl.setTitle("None", forSegmentAt: 0)
      cell.segmentedControl.setTitle("By \(timeStep)", forSegmentAt: 2)
      let ent = self.statKindRow?.value?.description ?? "Farm"
      self.modeRow?.cell.segmentedControl.setTitle("By \(ent)", forSegmentAt: 1)
    }.onChange { (row) in
      UIView.performWithoutAnimation {
        modeSection.footer?.title = (row.value ?? TimedGraphMode.running)
          .explanation(for: self.statKindRow?.value ?? .farm, by: self.timeStepRow?.value ?? .hourly)
        modeSection.reload()
      }
    }
    
    let showStats = ButtonRow { row in
      row.title = "Display Stats"
    }.onCellSelection { _, _ in
      guard let vc = self.storyboard?.instantiateViewController(withIdentifier: "statsViewController") else {
        return
      }
      
      guard let svc = vc as? StatsViewController else {
        return
      }
      
      let timePeriod: TimePeriod
      if let period = self.timePeriodRow?.value {
        if case .between = period {
          let sd = self.startDateRow?.value ?? Date(timeIntervalSince1970: 0)
          let ed = self.endDateRow?.value ?? Date()
          timePeriod = .between(sd, ed)
        } else {
          timePeriod = period
        }
      } else {
        timePeriod = .today
      }
      let timeStep = self.timeStepRow?.value ?? .daily
      let mode = self.modeRow?.value ?? .accumEntity
      
      let kind = self.statKindRow?.value ?? .worker
      
      let ids: [String]
      if kind == .foreman, let fs = self.foremenRow?.value {
        ids = fs.map { $0.id }
      } else if kind == .worker, let ws = self.workersRow?.value {
        ids = ws.map { $0.id }
      } else if kind == .orchard, let os = self.orchardsRow?.value {
        ids = os.map { $0.id }
      } else if kind == .farm, let fs = self.farmsRow?.value {
        ids = fs.map { $0.id }
      } else {
        ids = []
      }
      
      svc.stat = Stat(ids: ids, timePeriod: timePeriod, timeStep: timeStep, grouping: kind, mode: mode, name: "")
        
      self.navigationController?.pushViewController(svc, animated: true)
    }
    
    Entities.shared.getMultiplesOnce([.orchard, .worker]) { (_) in
      guard let statKindRow = self.statKindRow,
            let workersRow = self.workersRow,
            let orchardsRow = self.orchardsRow,
            let foremenRow = self.foremenRow,
            let farmRow = self.farmsRow,
            let timePeriodRow = self.timePeriodRow,
            let startDateRow = self.startDateRow,
            let endDateRow = self.endDateRow,
            let timeStepRow = self.timeStepRow,
            let modeRow = self.modeRow
      else {
          return
      }
        
      self.form
        +++ Section("Comparison")
        <<< statKindRow
        
        +++ Section()
        <<< workersRow
        <<< orchardsRow
        <<< foremenRow
        <<< farmRow
        
        +++ Section("Details")
        <<< timeStepRow
        <<< timePeriodRow
        <<< startDateRow
        <<< endDateRow
        
        +++ modeSection
        <<< modeRow
        
        +++ Section()
        <<< showStats
    }
  }
  
  override func tearDown() {
    form.removeAll()
  }
  
  public override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
  }
  
  public override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    tableView.reloadData()
  }
}
