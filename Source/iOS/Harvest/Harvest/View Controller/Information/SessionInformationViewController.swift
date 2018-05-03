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
    kind = .session
    
    refreshControl?.beginRefreshing()
    
    Entities.shared.getMultiplesOnce([.session, .orchard, .worker]) { _ in
      self.tableView.reloadData()
      self.refreshControl?.endRefreshing()
    }
  }
}
