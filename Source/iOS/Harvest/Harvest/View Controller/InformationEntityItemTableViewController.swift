//
//  InformationEntityItemTableViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

class InformationEntityItemTableViewController: UITableViewController {
  var selectedEntity: EntityItem? = nil
  var kind: EntityItem.Kind = .none
  
  var items: SortedEntity? {
    return Entities.shared.items(for: kind)
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    refreshControl = UIRefreshControl()
    refreshControl?.addTarget(self,
                             action: #selector(refreshList(_:)),
                             for: .valueChanged)
    
    if refreshControl != nil {
      tableView.addSubview(refreshControl!)
    }
  }
  
  @objc func refreshList(_ refreshControl: UIRefreshControl) {
    Entities.shared.getOnce(kind) { (es) in
      self.refreshControl?.endRefreshing()
      self.tableView.reloadData();
    }
  }
  
  override func viewWillAppear(_ animated: Bool) {
    if let sel = tableView.indexPathForSelectedRow {
      tableView.deselectRow(at: sel, animated: false)
    }
    tableView.reloadData()
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }
  
  @IBAction func newButtonTouchUp(_ sender: UIBarButtonItem) {
    switch kind {
    case .farm:
      let farm = Farm(json: [:], id: "")
      selectedEntity = .farm(farm)
      performSegue(withIdentifier: "EntitiesToDetail", sender: self)
    case .worker:
      let worker = Worker(json: [:], id: "")
      selectedEntity = .worker(worker)
      performSegue(withIdentifier: "EntitiesToDetail", sender: self)
    case .orchard:
      let orchard = Orchard(json: [:], id: "")
      selectedEntity = .orchard(orchard)
      performSegue(withIdentifier: "EntitiesToDetail", sender: self)
    case .none:
      break
    }
  }
  

  override func numberOfSections(in tableView: UITableView) -> Int {
    return 1
  }

  override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    return items?.count ?? 0
  }

  override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let cell = tableView.dequeueReusableCell(withIdentifier: "informationEntityItemCell", for: indexPath)
    
    guard let item = items?[indexPath.row] else {
      return cell
    }
    
    switch item {
    case let .worker(w):
      cell.textLabel?.text = w.firstname + " " + w.lastname
    case let .orchard(o):
      cell.textLabel?.text = o.name
    case let .farm(f):
      cell.textLabel?.text = f.name
    }
    
    return cell
  }
  
  override func tableView(_ tableView: UITableView, willSelectRowAt indexPath: IndexPath) -> IndexPath? {
    selectedEntity = items?[indexPath.row]
    return indexPath
  }

  // MARK: - Navigation

  // In a storyboard-based application, you will often want to do a little preparation before navigation
  override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
    let vc = segue.destination
    
    if let entityViewController = vc as? EntityViewController {
      entityViewController.entity = selectedEntity
      entityViewController.title = selectedEntity?.name
    }
  }

}
