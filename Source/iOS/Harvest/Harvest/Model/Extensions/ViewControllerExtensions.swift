//
//  VCExt.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/23.
//  Copyright © 2018 University of Pretoria. All rights reserved.
//

import Eureka

extension UIViewController {
  func hideKeyboardWhenTappedAround() {
    let tap = UITapGestureRecognizer(target: self,
                                     action: #selector(
                                      UIViewController.dismissKeyboard))
    tap.cancelsTouchesInView = false
    view.addGestureRecognizer(tap)
  }
  
  @objc func dismissKeyboard() {
    view.endEditing(true)
  }
}

public protocol ReloadableViewController {
  func setUp()
  func tearDown()
}

public class ReloadableFormViewController: FormViewController, ReloadableViewController {
  public func setUp() {
    fatalError()
  }
  
  public func tearDown() {
    fatalError()
  }
  
  func addRefreshControl() {
    let refreshControl = UIRefreshControl()
    refreshControl.addTarget(self, action: #selector(refreshList(_:)), for: .valueChanged)
    tableView.addSubview(refreshControl)
  }
  
  @objc func refreshList(_ refreshControl: UIRefreshControl) {
    refreshControl.endRefreshing()
    reloadFormVC()
  }
  
  func reloadFormVC() {
    UIView.performWithoutAnimation {
      tearDown()
      setUp()
    }
  }
  
  public override func viewDidLoad() {
    super.viewDidLoad()
    addRefreshControl()
    UIView.performWithoutAnimation {
      setUp()
    }
  }
}

class AllSelector {
  var onSelectionChange: (Bool) -> Void
  
  init(onSelectionChange: @escaping (Bool) -> Void) {
    self.onSelectionChange = onSelectionChange
  }
  
  @objc func changeSelection(sender: UIBarButtonItem) {
    if sender.title == "Select All" {
      sender.title = "Deselect All"
      onSelectionChange(true)
    } else {
      sender.title = "Select All"
      onSelectionChange(false)
    }
  }
  
  static var shared = AllSelector { _ in }
}

extension MultipleSelectorViewController {
  func allSelector() {
    AllSelector.shared.onSelectionChange = { isSelectAll in
      if isSelectAll {
        if let row = self.row as? MultipleSelectorRow, let options = row.options {
          self.row.value = Set(options)
        }
        self.form.rows.forEach { $0.baseCell.accessoryType = .checkmark }
      } else {
        self.row.value = []
        self.form.rows.forEach { $0.baseCell.accessoryType = .none }
      }
    }
    let isAllSelected: Bool
    if let row = self.row as? MultipleSelectorRow, let options = row.options, let value = row.value {
      isAllSelected = Set(options) == value
    } else {
      isAllSelected = false
    }
    
    let button = UIBarButtonItem(
      title: isAllSelected ? "Deselect All" : "Select All",
      style: .plain,
      target: AllSelector.shared,
      action: #selector(AllSelector.changeSelection(sender:)))
    navigationItem.rightBarButtonItem = button
  }
}
