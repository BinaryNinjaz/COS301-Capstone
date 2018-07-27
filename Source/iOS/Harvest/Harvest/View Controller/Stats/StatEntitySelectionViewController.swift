//
//  StatEntitySelectionViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/11.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import Eureka
import SCLAlertView

class StatEntitySelectionViewController: ReloadableFormViewController, TypedRowControllerType {
  var row: RowOf<[EntityItem]>!
  
  typealias RowValue = [EntityItem]
  
  var onDismissCallback: ((UIViewController) -> Void)?
  
  var startDate: Date?
  var endDate: Date?
  var period: HarvestCloud.TimePeriod?
  var grouping: HarvestCloud.GroupBy = .worker
  
  var selected: [EntityItem] = []
  
  // swiftlint:disable cyclomatic_complexity
  // swiftlint:disable function_body_length
  override func viewDidLoad() {
    super.viewDidLoad()
  }
  
  override func setUp() {
    let selectionSection = SelectableSection<ListCheckRow<EntityItem>>(
      "Compare",
      selectionType: .multipleSelection)
    
    for entity in row.value ?? [] {
      selectionSection <<< ListCheckRow<EntityItem>(entity.description) { row in
        row.title = entity.description
        row.selectableValue = entity
        row.value = nil
      }.onChange { row in
        if let sel = row.value {
          guard let idx = self.selected.index(of: entity) else {
            self.selected.append(entity)
            return
          }
          self.selected[idx] = sel
        } else {
          guard let idx = self.selected.index(of: entity) else {
            return
          }
          self.selected.remove(at: idx)
        }
      }
    }
    
    let b = Date(timeIntervalSince1970: 0)
    let n = Date()
    let drange = HarvestCloud.TimePeriod.cases(forRange: (startDate ?? b, endDate ?? n))
    
    let periodSelection = PushRow<HarvestCloud.TimePeriod> { row in
      row.title = "Time Period"
      row.options = drange
      row.value = period
      }.onChange { (row) in
        self.period = row.value
    }
    
    let showStats = ButtonRow { row in
      row.title = "Display Stats"
      }.onCellSelection { _, _ in
        guard !self.selected.isEmpty else {
          SCLAlertView().showNotice(
            "Nothing Selected",
            subTitle: "Please select some things to view by tapping on them")
          return
        }
        
        guard let vc = self.storyboard?.instantiateViewController(withIdentifier: "statsViewController") else {
          return
        }
        
        guard let svc = vc as? StatsViewController else {
          return
        }
        
        svc.startDate = self.startDate
        svc.endDate = self.endDate
        svc.period = self.period
        
        switch self.grouping {
        case .foreman:
          svc.stat = Stat.foremanComparison(self.selected.compactMap {
            if case let .worker(w) = $0 {
              return w
            } else {
              return nil
            }
          })
        case .worker:
          svc.stat = Stat.workerComparison(self.selected.compactMap {
            if case let .worker(w) = $0 {
              return w
            } else {
              return nil
            }
          })
        case .orchard:
          svc.stat = Stat.orchardComparison(self.selected.compactMap {
            if case let .orchard(o) = $0 {
              return o
            } else {
              return nil
            }
          })
        }
        self.navigationController?.pushViewController(svc, animated: true)
    }
    
    if selectionSection.count > 0 {
      form
        +++ selectionSection
      
      if drange.count > 1 {
        form +++ Section("Time Period") <<< periodSelection
      }
      
      form
        +++ Section()
        <<< showStats
    } else {
      form
        +++ Section()
        <<< LabelRow { row in
          row.title = "No \(grouping.title)'s To Compare"
        }
    }
  }
  
  override func tearDown() {
    form.removeAll()
  }
}
