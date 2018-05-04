//
//  SessionInformationViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/05/04.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

class SessionInformationViewController: InformationEntityItemTableViewController {
  override func viewDidLoad() {
    super.viewDidLoad()
    kind = .session
    
    refreshControl?.beginRefreshing()
    
    Entities.shared.getMultiplesOnce([.worker, .session, .orchard]) { _ in
      self.tableView.reloadData()
      self.refreshControl?.endRefreshing()
    }
  }
}
