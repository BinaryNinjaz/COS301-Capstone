//
//  StatSetupViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka
import SCLAlertView

final class StatSetupViewController: ReloadableFormViewController {
  var statKindRow: PickerRow<StatKind>?
  
  var workersRow: MultipleSelectorRow<Worker>?
  var orchardsRow: MultipleSelectorRow<Orchard>?
  var foremenRow: MultipleSelectorRow<Worker>?
  var farmsRow: MultipleSelectorRow<Farm>?
  
  var startDateRow: DateRow?
  var endDateRow: DateRow?
  var periodRow: PushRow<HarvestCloud.TimePeriod>?
  var modeRow: SwitchRow?
  
  // swiftlint:disable function_body_length
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
      case .workers:
        for worker in workersRow?.value ?? [] {
          ids.append(worker.id)
        }
      case .foremen:
        for foreman in foremenRow?.value ?? [] {
          ids.append(foreman.id)
        }
      case .orchards:
        for orchard in orchardsRow?.value ?? [] {
          ids.append(orchard.id)
        }
      case .farms:
        for farm in farmsRow?.value ?? [] {
          ids.append(farm.id)
        }
      }
    }
    
    let sk = statKindRow?.value ?? .workers
    let sd = startDateRow?.value ?? Date()
    let ed = endDateRow?.value ?? Date()
    let period = periodRow?.value ?? .daily
    let mode = modeRow?.value == true ? HarvestCloud.Mode.accum : .running
    
    let alert = SCLAlertView(appearance: .warningAppearance)
    let statNameTextView = alert.addTextField()
    statNameTextView.placeholder = "Graph Name"
    
    alert.addButton("Save") {
      let item = StatStore.Item(
        ids: ids,
        startDate: sd,
        endDate: ed,
        period: period,
        grouping: HarvestCloud.GroupBy(sk),
        mode: mode,
        name: statNameTextView.text ?? Date().description)
      
      StatStore.shared.saveItem(item: item)
    }
    
    alert.addButton("Cancel") {}
    
    alert.showEdit("Graph Name", subTitle: "Please enter a name to save your custom graph as.")
  }
  
  override func setUp() {
    statKindRow = PickerRow<StatKind>("Stat Kind") { row in
      row.options = StatKind.allCases
      row.value = .farms
    }
    
    workersRow = MultipleSelectorRow<Worker> { row in
      row.title = "Worker Selection"
      row.options = Array(Entities.shared.workers.lazy.map { $0.value }.filter { $0.kind == .worker })
      row.value = row.options?.first != nil ? [row.options!.first!] : []
      row.hidden = Condition.function(["Stat Kind"]) { form in
        let row = form.rowBy(tag: "Stat Kind") as? PickerRow<StatKind>
        return row?.value != .workers
      }
    }.cellUpdate { _, row in
      row.options = Array(Entities.shared.workers.lazy.map { $0.value }.filter { $0.kind == .worker })
    }
    
    foremenRow = MultipleSelectorRow<Worker> { row in
      row.title = "Foreman Selection"
      row.options = Array(Entities.shared.workers.lazy.map { $0.value }.filter { $0.kind == .foreman })
      row.options?.append(Worker(HarvestUser.current))
      row.value = row.options?.first != nil ? [row.options!.first!] : []
      row.hidden = Condition.function(["Stat Kind"]) { form in
        let row = form.rowBy(tag: "Stat Kind") as? PickerRow<StatKind>
        return row?.value != .foremen
      }
    }.cellUpdate { _, row in
      row.options = Array(Entities.shared.workers.lazy.map { $0.value }.filter { $0.kind == .foreman })
      row.options?.append(Worker(HarvestUser.current))
    }
    
    orchardsRow = MultipleSelectorRow<Orchard> { row in
      row.title = "Orchard Selection"
      row.options = Entities.shared.orchards.map { $0.value }
      row.value = row.options?.first != nil ? [row.options!.first!] : []
      row.hidden = Condition.function(["Stat Kind"]) { form in
        let row = form.rowBy(tag: "Stat Kind") as? PickerRow<StatKind>
        return row?.value != .orchards
      }
    }.cellUpdate { _, row in
      row.options = Entities.shared.orchards.map { $0.value }
    }
    
    farmsRow = MultipleSelectorRow<Farm> { row in
      row.title = "Farm Selection"
      row.options = Entities.shared.farms.map { $0.value }
      row.value = row.options?.first != nil ? [row.options!.first!] : []
      row.hidden = Condition.function(["Stat Kind"]) { form in
        let row = form.rowBy(tag: "Stat Kind") as? PickerRow<StatKind>
        return row?.value != .farms
      }
    }.cellUpdate { _, row in
      row.options = Entities.shared.farms.map { $0.value }
    }
    
    startDateRow = DateRow { row in
      row.title = "From Date"
      
      let cal = Calendar.current
      let wb = cal.date(byAdding: Calendar.Component.weekday, value: -7, to: Date())
      row.value = wb
    }
    
    endDateRow = DateRow { row in
      row.title = "Upto Date"
      row.value = Date()
    }
    
    periodRow = PushRow<HarvestCloud.TimePeriod> { row in
      row.title = "Time Period"
      row.options = HarvestCloud.TimePeriod.allCases
      row.value = .daily
    }
    
    modeRow = SwitchRow("ModeRow") { row in
      row.value = false
      row.title = "Accumulate Data"
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
      
      svc.startDate = self.startDateRow?.value
      svc.endDate = self.endDateRow?.value
      svc.period = self.periodRow?.value
      svc.mode = self.modeRow?.value == true ? .accum : .running
      
      let kind = self.statKindRow?.value ?? .workers
      
      if kind == .foremen, let fs = self.foremenRow?.value {
        svc.stat = .foremanComparison(Array(fs))
      } else if kind == .workers, let ws = self.workersRow?.value {
        svc.stat = .workerComparison(Array(ws))
      } else if kind == .orchards, let os = self.orchardsRow?.value {
        svc.stat = .orchardComparison(Array(os))
      } else if kind == .farms, let fs = self.farmsRow?.value {
        svc.stat = .farmComparison(Array(fs))
      }
        
      self.navigationController?.pushViewController(svc, animated: true)
    }
    
    Entities.shared.getMultiplesOnce([.orchard, .worker]) { (_) in
      guard let statKindRow = self.statKindRow,
            let workersRow = self.workersRow,
            let orchardsRow = self.orchardsRow,
            let foremenRow = self.foremenRow,
            let farmRow = self.farmsRow,
            let periodRow = self.periodRow,
            let startDateRow = self.startDateRow,
            let endDateRow = self.endDateRow,
            let modeRow = self.modeRow
      else {
          return
      }
        
      self.form
        +++ Section()
        <<< statKindRow
        
        +++ Section()
        <<< workersRow
        <<< orchardsRow
        <<< foremenRow
        <<< farmRow
        
        +++ Section("Details")
        <<< periodRow
        <<< startDateRow
        <<< endDateRow
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
