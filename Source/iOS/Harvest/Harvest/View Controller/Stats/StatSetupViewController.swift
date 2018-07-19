//
//  StatSetupViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka

final class StatSetupViewController: ReloadableFormViewController {
  var workersRow: MultipleSelectorRow<Worker>! = nil
  var orchardsRow: MultipleSelectorRow<Orchard>! = nil
  var foremenRow: MultipleSelectorRow<Worker>! = nil
  
  var startDateRow: DateRow! = nil
  var endDateRow: DateRow! = nil
  var periodRow: PushRow<HarvestCloud.TimePeriod>! = nil
  
  // swiftlint:disable function_body_length
  public override func viewDidLoad() {
    super.viewDidLoad()
    
  }
  
  override func setUp() {
    let statKind = PickerRow<StatKind>("Stat Kind") { row in
      row.options = StatKind.allCases
      row.value = .workers
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
    
    let showStats = ButtonRow { row in
      row.title = "Display Stats"
      }.onCellSelection { _, _ in
        guard let vc = self.storyboard?.instantiateViewController(withIdentifier: "statsViewController") else {
          return
        }
        
        guard let svc = vc as? StatsViewController else {
          return
        }
        
        svc.startDate = self.startDateRow.value
        svc.endDate = self.endDateRow.value
        svc.period = self.periodRow.value
        
        let kind = statKind.value ?? .workers
        switch kind {
        case .foremen:
          if let fs = self.foremenRow.value {
            svc.stat = .foremanComparison(Array(fs))
          }
        case .workers:
          if let ws = self.workersRow.value {
            svc.stat = .workerComparison(Array(ws))
          }
        case .orchards:
          if let os = self.orchardsRow.value {
            svc.stat = .orchardComparison(Array(os))
          }
        }
        
        self.navigationController?.pushViewController(svc, animated: true)
    }
    
    Entities.shared.getMultiplesOnce([.orchard, .session, .worker]) { (_) in
      self.form
        +++ Section()
        <<< statKind
        
        +++ Section()
        <<< self.workersRow
        <<< self.orchardsRow
        <<< self.foremenRow
        
        +++ Section("Details")
        <<< self.periodRow
        <<< self.startDateRow
        <<< self.endDateRow
        
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
