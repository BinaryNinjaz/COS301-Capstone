//
//  InformationEntityItemTableViewController.swift
//  Harvest
//
//  Created by Letanyan Arumugam on 2018/04/19.
//  Copyright © 2018 Letanyan Arumugam. All rights reserved.
//

import UIKit

class InformationEntityItemTableViewController: UITableViewController {
  var listnerId: Int?
  var selectedEntity: EntityItem?
  var kind: EntityItem.Kind = .none {
    didSet {
      navigationItem.rightBarButtonItem?.isEnabled = kind != .session
    }
  }
  
  @IBOutlet weak var searchBar: UISearchBar?
  var filteredItems: SortedDictionary<String, SortedArray<SearchPair<EntityItem>>>?
  
  var items: SortedDictionary<String, EntityItem>? {
    return Entities.shared.items(for: kind)
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    refreshControl = UIRefreshControl()
    refreshControl?.addTarget(self,
                             action: #selector(refreshList(_:)),
                             for: .valueChanged)
    
    tableView.addSubview(refreshControl!)
    
    if listnerId == nil {
      listnerId = Entities.shared.listen { self.tableView.reloadData() }
    }
    
    searchBar?.delegate = self
  }
  
  @objc func refreshList(_ refreshControl: UIRefreshControl) {
    Entities.shared.getOnce(kind) { (_) in
      self.refreshControl?.endRefreshing()
      self.tableView.reloadData()
    }
  }
  
  override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(true)
    if let id = listnerId {
      listnerId = nil
      Entities.shared.deregister(listner: id)
    }
  }
  
  override func viewWillAppear(_ animated: Bool) {
    if let sel = tableView.indexPathForSelectedRow {
      tableView.deselectRow(at: sel, animated: false)
    }
    if listnerId == nil {
      listnerId = Entities.shared.listen { self.tableView.reloadData() }
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
      
    case .session:
      break
      
    case .user:
      break
      
    case .none:
      break
    }
  }

  override func numberOfSections(in tableView: UITableView) -> Int {
    return filteredItems?.count ?? 1
  }

  override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    if let filtered = filteredItems {
      let s = filtered.startIndex
      let i = filtered.index(s, offsetBy: section)
      return filtered[i].value.count
    } else {
      return items?.count ?? 0
    }
  }

  override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let cell = tableView.dequeueReusableCell(withIdentifier: "informationEntityItemCell", for: indexPath)
    
    let item: EntityItem
    let reason: String?
    if let filtered = filteredItems {
      let s = filtered.startIndex
      let i = filtered.index(s, offsetBy: indexPath.section)
      let pair = filtered[i].value[indexPath.row]
      (item, reason) = (pair.item, pair.reason)
    } else if let i = items?[indexPath.row] {
      item = i
      reason = ""
    } else {
      return cell
    }
    
    switch item {
    case let .worker(w):
      cell.textLabel?.text = w.firstname + " " + w.lastname
    case let .orchard(o):
      cell.textLabel?.text = o.description
    case let .farm(f):
      cell.textLabel?.text = f.name
    case let .session(s):
      cell.textLabel?.text = s.foreman.description
    case .user:
      cell.textLabel?.text = ""
    }
    
    cell.detailTextLabel?.text = reason
    
    return cell
  }
  
  override func tableView(_ tableView: UITableView, willSelectRowAt indexPath: IndexPath) -> IndexPath? {
    if let filtered = filteredItems {
      let s = filtered.startIndex
      let i = filtered.index(s, offsetBy: indexPath.section)
      selectedEntity = filtered[i].value[indexPath.row].item
    } else {
      selectedEntity = items?[indexPath.row]
    }
    return indexPath
  }
  
  override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
    if let filtered = filteredItems {
      let s = filtered.startIndex
      let i = filtered.index(s, offsetBy: section)
      return filtered[i].key
    } else {
      return ""
    }
  }
  
  override func tableView(
    _ tableView: UITableView,
    editingStyleForRowAt indexPath: IndexPath
  ) -> UITableViewCellEditingStyle {
    return UITableViewCellEditingStyle.none
  }

  // MARK: - Navigation
  override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
    let vc = segue.destination
    
    if let entityViewController = vc as? EntityViewController {
      entityViewController.entity = selectedEntity
      entityViewController.title = selectedEntity?.name
    }
  }
  
}

extension InformationEntityItemTableViewController: UISearchBarDelegate {
  func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
    if searchText.isEmpty {
      filteredItems = nil
    } else {
      filteredItems = items?.search(for: searchText)
    }
    tableView.reloadData()
  }
}
