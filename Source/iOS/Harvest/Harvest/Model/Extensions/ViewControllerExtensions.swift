//
//  VCExt.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/23.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
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
