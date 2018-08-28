//
//  StatEntitySelectionViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/11.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Eureka
import SCLAlertView

class StatEntitySelectionViewController: ReloadableFormViewController, TypedRowControllerType {
  var row: RowOf<[String]>!
  
  typealias RowValue = [String]
  
  var onDismissCallback: ((UIViewController) -> Void)?
  
  var timePeriod: TimePeriod?
  var timeStep: TimeStep?
  var grouping: StatKind = .worker
  var mode: TimedGraphMode?
  
  var selected: [String] = []
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    let allOptions = Set(Entities.shared.entities(for: grouping).map { $0.id })
    let isAllSelected = allOptions.subtracting(selected).isEmpty
    
    navigationItem.rightBarButtonItem = UIBarButtonItem(
      title: isAllSelected ? "Deselect All" : "Select All",
      style: .plain,
      target: self,
      action: #selector(changeSelection))
  }
  
  @objc func changeSelection() {
    if navigationItem.rightBarButtonItem?.title == "Select All" {
      navigationItem.rightBarButtonItem?.title = "Deselect All"
      selected = Entities.shared.entities(for: grouping).map { $0.id }
      row.value = selected
    } else {
      navigationItem.rightBarButtonItem?.title = "Select All"
      selected.removeAll()
      row.value = selected
    }
    reloadFormVC()
  }
  
  func selectableEntitiesSection() -> SelectableSection<ListCheckRow<EntityItem>> {
    let selectionSection = SelectableSection<ListCheckRow<EntityItem>>(
      "Compare",
      selectionType: .multipleSelection)
    
    for entity in Entities.shared.entities(for: grouping) {
      selectionSection <<< ListCheckRow<EntityItem>(entity.description) { row in
        row.title = entity.description
        row.selectableValue = entity
        row.value = self.row.value?.contains(entity.id) ?? false ? entity : nil
      }.onChange { row in
        if let sel = row.value {
          guard let idx = self.selected.index(of: entity.id) else {
            self.selected.append(entity.id)
            return
          }
          self.selected[idx] = sel.id
        } else {
          guard let idx = self.selected.index(of: entity.id) else {
            return
          }
          self.selected.remove(at: idx)
        }
      }
    }
    
    return selectionSection
  }
  
  // swiftlint:disable function_body_length
  override func setUp() {
    let selectionSection = selectableEntitiesSection()
    for id in self.row?.value ?? [] {
      selected.append(id)
    }
    
    let b = Date(timeIntervalSince1970: 0)
    let n = Date()
    let drange = TimeStep.cases(forRange: timePeriod?.dateRange() ?? (b, n))
    
    let timeStepSelection = PushRow<TimeStep> { row in
      row.title = "Time Step"
      row.options = drange
      row.value = timeStep
    }.onChange { (row) in
      self.timeStep = row.value
    }
    
    let startDateRow = DateRow { row in
      row.title = "From Date"
      
      let wb = Date().thisWeek().0
      row.value = wb
      if case let .between(_, b)? = self.timePeriod {
        self.timePeriod = .between(wb, b)
      }
    }.onChange { row in
      let date = row.value ?? Date()
      if case let .between(_, b)? = self.timePeriod {
        self.timePeriod = .between(date, b)
      }
    }
    
    let endDateRow = DateRow { row in
      row.title = "Up to Date"
      row.value = Date().thisWeek().1.endOfDay()
      if case let .between(a, _)? = self.timePeriod {
        self.timePeriod = .between(a, Date())
      }
    }.onChange { row in
      let date = row.value?.endOfDay() ?? Date()
      if case let .between(a, _)? = self.timePeriod {
        self.timePeriod = .between(a, date)
      }
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
      
      guard
        let svc = self.storyboard?.instantiateViewController(withIdentifier: "statsViewController")
        as? StatsViewController
      else {
        return
      }
      
      let stat = Stat(
        ids: self.selected,
        timePeriod: self.timePeriod ?? .today,
        timeStep: self.timeStep ?? .daily,
        grouping: self.grouping,
        mode: self.mode ?? .accumTime,
        name: "")
      
      svc.stat = stat
      
      self.navigationController?.pushViewController(svc, animated: true)
    }
    
    guard selectionSection.count > 0 else {
      form
        +++ Section()
        <<< LabelRow { row in
          row.title = "No \(grouping)'s To Compare"
      }
      return
    }
    
    form
      +++ selectionSection
    
    if drange.count > 1 {
      form +++ Section("Time Period") <<< timeStepSelection
    }
    if case .between? = timePeriod {
      form
        +++ Section("Between Date")
        <<< startDateRow
        <<< endDateRow
    }
    
    form
      +++ Section()
      <<< showStats
  }
  
  override func tearDown() {
    form.removeAll()
  }
}
