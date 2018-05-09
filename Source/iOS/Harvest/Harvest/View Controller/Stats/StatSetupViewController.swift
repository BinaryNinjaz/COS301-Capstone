//
//  StatSetupViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/07.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka

public final class StatSetupViewController: FormViewController {
  var workersRow: PushRow<Worker>! = nil
  var sessionsRow: PushRow<Session>! = nil
  var orchardsRow: PushRow<Orchard>! = nil
  
  public override func viewDidLoad() {
    super.viewDidLoad()
    let statKind = PickerRow<StatKind>("Stat Kind") { row in
      row.options = StatKind.allCases
      row.value = .perSessionWorkers
    }
    
    workersRow = PushRow<Worker>() { row in
      row.title = "Worker Selection"
      row.options = Entities.shared.workersList()
      row.value = row.options?.first
      row.hidden = Condition.function(["Stat Kind"]) { form in
        let row = form.rowBy(tag: "Stat Kind") as? PickerRow<StatKind>
        return row?.value != .workerHistory
      }
    }.cellUpdate{ cell, row in
      row.options = Entities.shared.workersList()
    }
    
    sessionsRow = PushRow<Session>() { row in
      row.title = "Session Selection"
      row.options = Entities.shared.sessionsList()
      row.value = row.options?.last
      row.hidden = Condition.function(["Stat Kind"]) { form in
        let row = form.rowBy(tag: "Stat Kind") as? PickerRow<StatKind>
        return row?.value != .perSessionWorkers
      }
    }.cellUpdate{ cell, row in
      row.options = Entities.shared.sessionsList()
    }
    
    orchardsRow = PushRow<Orchard>() { row in
      row.title = "Orchard Selection"
      row.options = Entities.shared.orchardsList()
      row.value = row.options?.first
      row.hidden = Condition.function(["Stat Kind"]) { form in
        let row = form.rowBy(tag: "Stat Kind") as? PickerRow<StatKind>
        return row?.value != .orchardHistory
      }
    }.cellUpdate{ cell, row in
      row.options = Entities.shared.orchardsList()
    }
    
    let showStats = ButtonRow() { row in
      row.title = "Display Stats"
    }.onCellSelection { cell, row in
      guard let vc = self.storyboard?.instantiateViewController(withIdentifier: "statsViewController") else {
        return
      }
      
      guard let svc = vc as? StatsViewController else {
        return
      }
      
      let kind = statKind.value ?? .perSessionWorkers
      switch kind {
      case .perSessionWorkers:
        if let s = self.sessionsRow.value {
          svc.stat = Stat.perSessionWorkers(s)
        }
      case .workerHistory:
        if let w = self.workersRow.value {
          svc.stat = Stat.workerHistory(w)
        }
      case .orchardHistory:
        if let o = self.orchardsRow.value {
          svc.stat = Stat.orchardHistory(o)
        }
      }
      
      self.navigationController?.pushViewController(svc, animated: true)
    }
    
    Entities.shared.getMultiplesOnce([.orchard, .session, .worker]) { (entities) in
      self.form
        +++ Section()
        <<< statKind
        
        +++ Section()
        <<< self.workersRow
        <<< self.sessionsRow
        <<< self.orchardsRow
        
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
