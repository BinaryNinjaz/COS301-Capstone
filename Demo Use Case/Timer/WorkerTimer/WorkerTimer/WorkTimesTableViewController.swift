//
//  WorkTimesTableViewController.swift
//  WorkerTimer
//
//  Created by Letanyan Arumugam on 2018/03/10.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

class WorkTimesTableViewController: UITableViewController {
  var locationBuffer: [WorkArea] = []
  
  override func viewDidLoad() {
      super.viewDidLoad()
  }

  override func didReceiveMemoryWarning() {
      super.didReceiveMemoryWarning()
  }

  // MARK: - Table view data source

  override func numberOfSections(in tableView: UITableView) -> Int {
      return 1
  }

  override func tableView(
    _ tableView: UITableView,
    numberOfRowsInSection section: Int
  ) -> Int {
      return locationBuffer.count
  }

  
  override func tableView(
    _ tableView: UITableView,
    cellForRowAt indexPath: IndexPath
  ) -> UITableViewCell {
    guard let cell =
      tableView.dequeueReusableCell(withIdentifier: "workTimeCell",
                                    for: indexPath) as? WorkTimeTableViewCell else {
      return UITableViewCell()
    }

    let wa = locationBuffer[indexPath.row]
    
    cell.textLabel?.text = wa.title
    cell.detailTextLabel?.text = formatTimeInterval(wa.workingTime)

    return cell
  }
}
