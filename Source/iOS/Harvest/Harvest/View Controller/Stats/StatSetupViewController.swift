//
//  StatSetupViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka

public final class StatSetupViewController: FormViewController {
  var workersRow: MultipleSelectorRow<Worker>! = nil
  var sessionsRow: PushRow<ShallowSession>! = nil
  var orchardsRow: MultipleSelectorRow<Orchard>! = nil
  var foremenRow: MultipleSelectorRow<Worker>! = nil
  
  // swiftlint:disable function_body_length
  public override func viewDidLoad() {
    super.viewDidLoad()
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
      row.options = Entities.shared.workers.map { $0.value }
    }
    
    foremenRow = MultipleSelectorRow<Worker> { row in
      row.title = "Foreman Selection"
      row.options = Array(Entities.shared.workers.lazy.map { $0.value }.filter { $0.kind == .foreman })
      row.value = row.options?.first != nil ? [row.options!.first!] : []
      row.hidden = Condition.function(["Stat Kind"]) { form in
        let row = form.rowBy(tag: "Stat Kind") as? PickerRow<StatKind>
        return row?.value != .foremen
      }
      }.cellUpdate { _, row in
        row.options = Entities.shared.workers.map { $0.value }
    }
    
//    sessionsRow = PushRow<ShallowSession> { row in
//      row.title = "Session Selection"
//      row.options = Entities.shared.shallowSessions.map { $0.value }
//      row.value = row.options?.first
//      row.hidden = Condition.function(["Stat Kind"]) { form in
//        let row = form.rowBy(tag: "Stat Kind") as? PickerRow<StatKind>
//        return row?.value != .perSessionWorkers
//      }
//    }.cellUpdate { _, row in
//      row.options = Entities.shared.shallowSessions.map { $0.value }
//    }
    
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
    
    let showStats = ButtonRow { row in
      row.title = "Display Stats"
    }.onCellSelection { _, _ in
      guard let vc = self.storyboard?.instantiateViewController(withIdentifier: "statsViewController") else {
        return
      }
      
      guard let svc = vc as? StatsViewController else {
        return
      }
      
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
        
        +++ Section()
        <<< showStats
    }
  }
  
  public override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
  }
  
  public override func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    tableView.reloadData()
  }
}
