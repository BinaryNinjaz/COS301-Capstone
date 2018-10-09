//
//  StatSelectionViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/07/07.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Eureka
import SCLAlertView

final class StatSelectionViewController: ReloadableFormViewController {
  var refreshControl: UIRefreshControl?
  var showingAlert: Bool = false
  
  func customGraph() -> LabelRow {
    return LabelRow { row in
      row.title = "Create New Graph"
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
      
      self.reloadFormVC()
      
      self.showingAlert = false
    }
    
    alert.addButton("Delete") {
      StatStore.shared.removeItem(withName: name)
      self.reloadFormVC()
      self.showingAlert = false
    }
    
    alert.addButton("Cancel") {
      self.showingAlert = false
    }
    
    showingAlert = true
    alert.showEdit(name, subTitle: "Select an option to perform on the graph '\(name)'.")
  }
  
  func buttonRow(for stat: Stat) -> StatEntitySelectionRow {
    return StatEntitySelectionRow { row in
      row.title = stat.name
      row.timePeriod = stat.timePeriod
      row.timeStep = stat.timeStep
      row.grouping = stat.grouping
      row.mode = stat.mode
      row.value = stat.ids
    }.cellUpdate { cell, _ in
      cell.textLabel?.textAlignment = .left
      cell.textLabel?.textColor = .black
      cell.detailTextLabel?.textColor = .clear
      
      let longPress = UILongPressGestureRecognizer(
        target: self,
        action: #selector(self.longPressCustomGraph(_:)))
      longPress.accessibilityLabel = stat.name
      
      cell.addGestureRecognizer(longPress)
    }
  }
  
  func customGraphSection() -> Section {
    let result = Section()
    
    result <<< customGraph()
    
    return result
  }
  
  override func setUp() {
    let orchardSection = Section("Orchard Comparison")
    let workerSection = Section("Worker Comparison")
    let foremanSection = Section("Foreman Comparison")
    let farmSection = Section("Farm Comparison")
    
    for stat in StatStore.shared.store {
      switch stat.grouping {
      case .farm: farmSection <<< buttonRow(for: stat)
      case .orchard: orchardSection <<< buttonRow(for: stat)
      case .worker: workerSection <<< buttonRow(for: stat)
      case .foreman: foremanSection <<< buttonRow(for: stat)
      }
    }
    
    form
      +++ customGraphSection()
    
    if !farmSection.isEmpty {
      form +++ farmSection
    }
    if !orchardSection.isEmpty {
      form +++ orchardSection
    }
    if !workerSection.isEmpty {
      form +++ workerSection
    }
    if !foremanSection.isEmpty {
      form +++ foremanSection
    }
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
    reloadFormVC()
  }
}
